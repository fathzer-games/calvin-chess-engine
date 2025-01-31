package com.kelseyde.calvin.search;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.board.Piece;
import com.kelseyde.calvin.engine.EngineConfig;
import com.kelseyde.calvin.evaluation.NNUE;
import com.kelseyde.calvin.movegen.MoveGenerator;
import com.kelseyde.calvin.movegen.MoveGenerator.MoveFilter;
import com.kelseyde.calvin.search.SearchHistory.PlayedMove;
import com.kelseyde.calvin.search.SearchStack.SearchStackEntry;
import com.kelseyde.calvin.search.picker.MovePicker;
import com.kelseyde.calvin.search.picker.QuiescentMovePicker;
import com.kelseyde.calvin.search.picker.ScoredMove;
import com.kelseyde.calvin.tables.tt.HashEntry;
import com.kelseyde.calvin.tables.tt.HashFlag;
import com.kelseyde.calvin.tables.tt.TranspositionTable;
import com.kelseyde.calvin.uci.UCI;

import java.util.ArrayList;
import java.util.List;

/**
 * Classical alpha-beta search with iterative deepening. This is the main search algorithm used by the engine.
 * </p>
 * Alpha-beta search seeks to reduce the number of nodes that need to be evaluated in the search tree. It does this by
 * pruning branches that are guaranteed to be worse than the best move found so far, or that are guaranteed to be 'too
 * good' and could only be reached by sup-optimal play by the opponent.
 * @see <a href="https://www.chessprogramming.org/Alpha-Beta">Chess Programming Wiki</a>
 * </p>
 * Iterative deepening is a search strategy that does a full search at a depth of 1 ply, then a full search at 2 ply,
 * then 3 ply and so on, until the time limit is exhausted. In case the timeout is reached in the middle of an iteration,
 * the search can still fall back on the best move found in the previous iteration. By prioritising searching the best
 * move found in the previous iteration -- and by using a {@link TranspositionTable} -- the iterative approach is much
 * more efficient than it might sound.
 * @see <a href="https://www.chessprogramming.org/Iterative_Deepening">Chess Programming Wiki</a>
 */
public class Searcher implements Search {

    final EngineConfig config;
    final TranspositionTable tt;
    final MoveGenerator movegen;
    final SearchHistory history;
    final SearchStack ss;
    final ThreadData td;
    final NNUE eval;

    Move bestMoveCurrent;
    int bestScoreCurrent;

    TimeControl tc;
    Board board;

    public Searcher(EngineConfig config, TranspositionTable tt, ThreadData td) {
        this.config = config;
        this.tt = tt;
        this.td = td;
        this.history = new SearchHistory(config);
        this.movegen = new MoveGenerator();
        this.ss = new SearchStack();
        this.eval = new NNUE();
    }

    /**
     * Search the current position, increasing the depth each iteration, to find the best move within the given time limit.
     * @param timeControl the maximum duration to search
     * @return a {@link SearchResult} containing the best move, the eval, and other search info.
     */
    @Override
    public SearchResult search(TimeControl timeControl) {

        final List<Move> rootMoves = movegen.generateMoves(board);
        if (rootMoves.size() == 1) {
            return handleOnlyOneLegalMove(rootMoves);
        }

        tc = timeControl;
        ss.clear();
        td.reset();
        history.reset();

        Move bestMoveRoot = null;
        int bestScoreRoot = 0;

        int alpha = Score.MIN;
        int beta = Score.MAX;

        int reduction = 0;
        final int maxReduction = config.aspMaxReduction.value;
        int window = config.aspMargin.value;

        while (!shouldStopSoft() && td.depth < Search.MAX_DEPTH) {
            // Reset variables for the current depth iteration
            bestMoveCurrent = null;
            bestScoreCurrent = 0;
            td.seldepth = 0;

            final int searchDepth = td.depth - reduction;

            // Perform alpha-beta search for the current depth
            final int score = search(searchDepth, 0, alpha, beta, false);

            // Update the best move and evaluation if a better move is found
            if (bestMoveCurrent != null) {
                history.updateBestMoveStability(bestMoveRoot, bestMoveCurrent);
                history.updateBestScoreStability(bestScoreRoot, bestScoreCurrent);
                bestMoveRoot = bestMoveCurrent;
                bestScoreRoot = bestScoreCurrent;
                if (td.isMainThread()) {
                    SearchResult result = SearchResult.of(bestMoveRoot, bestScoreRoot, td, tc);
                    UCI.writeSearchInfo(result);
                }
            }

            // Check if search is cancelled or a checkmate is found
            if (shouldStop() || Score.isMateScore(score)) {
                break;
            }

            // Aspiration windows - https://www.chessprogramming.org/Aspiration_Windows
            // Use the search score from the previous iteration to guess the score from the current iteration.
            // Based on this guess, we can narrow the alpha-beta window around the previous score, causing more cut-offs
            // and thus speeding up the search. If the true score is outside the window, a costly re-search is required.

            // Adjust the aspiration window in case the score fell outside the current window
            if (score <= alpha) {
                // If score <= alpha, re-search with an expanded aspiration window
                beta = (alpha + beta) / 2;
                alpha -= window;
                window *= 2;
                reduction = 0;
                continue;
            }
            if (score >= beta) {
                // If score >= beta, re-search with an expanded aspiration window
                beta += window;
                window *= 2;
                reduction = Math.min(maxReduction, reduction + 1);
                continue;
            }

            // Center the aspiration window around the score from the current iteration, to be used next time.
            window = config.aspMargin.value;
            alpha = score - window;
            beta = score + window;

            // Increment depth and reset retry counter for next iteration
            td.depth++;
        }

        // Clear move ordering cache and return the search result
        history.getKillerTable().clear();

        if (bestMoveRoot == null) {
            // If time expired before a best move was found in search, pick the first legal move.
            bestMoveRoot = rootMoves.get(0);
        }

        return SearchResult.of(bestMoveRoot, bestScoreRoot, td, tc);

    }

    /**
     * Run a single iteration of the iterative deepening search for a specific depth.
     *
     * @param depth               The number of ply deeper left to go in the current search ('ply remaining').
     * @param ply                 The number of ply already examined in the current search ('ply from root').
     * @param alpha               The lower bound for search scores ('we can do at least this well').
     * @param beta                The upper bound for search scores ('our opponent can do at most this well').
     * @param cutNode             Whether this node is an expected cut-node (i.e. a fail-high node).
     */
    public int search(int depth, int ply, int alpha, int beta, boolean cutNode) {

        // If timeout is reached, exit immediately
        if (shouldStop()) return alpha;

        final boolean inCheck = movegen.isCheck(board);
        final boolean rootNode = ply == 0;
        final boolean pvNode = beta - alpha > 1;

        // If depth is reached, drop into quiescence search
        if (depth <= 0 && !inCheck) return quiescenceSearch(alpha, beta, ply);
        if (depth < 0) depth = 0;

        // If the game is drawn by repetition, insufficient material or fifty move rule, return zero
        if (ply > 0 && isDraw()) return Score.DRAW;

        // Update the selective search depth
        if (ply + 1 > td.seldepth) td.seldepth = ply + 1;

        // If the maximum depth is reached, return the static evaluation of the position
        if (ply >= MAX_DEPTH) return inCheck ? 0 : eval.evaluate();

        // Mate Distance Pruning - https://www.chessprogramming.org/Mate_Distance_Pruning
        // Exit early if we have already found a forced mate at an earlier ply
        alpha = Math.max(alpha, -Score.MATE + ply);
        beta = Math.min(beta, Score.MATE - ply);
        if (alpha >= beta) return alpha;

        history.getKillerTable().clear(ply + 1);

        // Probe the transposition table in case this node has been searched before. If so, we can potentially re-use the
        // result of the previous search and save some time, only if the following conditions are met:
        //  a) we are not in a PV node,
        //  b) it was searched to a sufficient depth, and
        //  c) the score is either exact, or outside the bounds of the current alpha-beta window.
        final HashEntry ttEntry = tt.get(board.key(), ply);
        final boolean ttHit = ttEntry != null;

        if (!pvNode
                && ttHit
                && isSufficientDepth(ttEntry, depth)
                && (ttEntry.score() <= alpha || cutNode)) {
            if (isWithinBounds(ttEntry, alpha, beta)) {
                return ttEntry.score();
            }
            else if (depth <= config.ttExtensionDepth.value) {
                depth++;
            }
        }

        Move ttMove = null;
        if (ttHit && ttEntry.move() != null) {
            // Even if we can't re-use the entire tt entry, we can still use the stored move to improve move ordering.
            ttMove = ttEntry.move();
        }

        // Internal Iterative Deepening - https://www.chessprogramming.org/Internal_Iterative_Deepening
        // If the position has not been searched yet, the search will be potentially expensive. So let's search with a
        // reduced depth expecting to record a move that we can use later for a full-depth search.
        if (!rootNode
                && (pvNode || cutNode)
                && (!ttHit || ttEntry.move() == null)
                && depth >= config.iirDepth.value) {
            --depth;
        }

        int rawStaticEval = Integer.MIN_VALUE;
        int uncorrectedStaticEval = Integer.MIN_VALUE;
        int staticEval = Integer.MIN_VALUE;
        if (!inCheck) {
            // Re-use cached static eval if available. Don't compute static eval while in check.
            rawStaticEval = ttHit ? ttEntry.staticEval() : eval.evaluate();
            uncorrectedStaticEval = rawStaticEval;

            if (!ttHit) {
                tt.put(board.key(), HashFlag.NONE, 0, 0, null, rawStaticEval, 0);
            }

            staticEval = ttMove != null ?
                    rawStaticEval :
                    history.correctEvaluation(board, ss, ply, rawStaticEval);
            if (ttHit &&
                    (ttEntry.flag() == HashFlag.EXACT ||
                    (ttEntry.flag() == HashFlag.LOWER && ttEntry.score() >= rawStaticEval) ||
                    (ttEntry.flag() == HashFlag.UPPER && ttEntry.score() <= rawStaticEval))) {
                staticEval = ttEntry.score();
                uncorrectedStaticEval = staticEval;
            }
        }

        SearchStackEntry sse = ss.get(ply);
        sse.staticEval = staticEval;

        int futilityReduction = 0;

        // We are 'improving' if the static eval of the current position is greater than it was on our previous turn.
        // If our position is improving we can be more aggressive in our beta pruning - where the eval is too high - but
        // should be more cautious in our alpha pruning - where the eval is too low.
        final boolean improving = isImproving(ply, staticEval);

        // Pre-move-loop pruning: If the static eval indicates a fail-high or fail-low, there are several heuristic we
        // can employ to prune the node and its entire subtree, without searching any moves.
        if (!pvNode && !inCheck) {

            // Reverse Futility Pruning - https://www.chessprogramming.org/Reverse_Futility_Pruning
            // If the static evaluation + some significant margin is still above beta, then let's assume this position
            // is a cut-node and will fail-high, and not search any further.
            if (depth <= config.rfpDepth.value && !Score.isMateScore(alpha)) {

                int baseMargin = depth * (improving ? config.rfpImpMargin.value : config.rfpMargin.value);
                int blend = depth * config.rfpBlend.value;

                int pruneMargin = baseMargin - blend;
                int reduceMargin = baseMargin + blend;

                // If the evaluation is significantly higher than beta, prune the node entirely
                if (staticEval - pruneMargin >= beta) {
                    return (staticEval + beta) / 2;
                }

                // Else, apply reduction to quiet moves, using a dynamic scaling based on how far the eval is from beta
                else if (staticEval - reduceMargin >= beta) {
                    // Calculate distance from beta in units of 'blend' to scale reduction dynamically
                    int delta = (staticEval - beta) - reduceMargin;
                    futilityReduction = 1 + Math.min(2, delta / blend);
                }
            }

            // Razoring - https://www.chessprogramming.org/Razoring
            // At low depths, if the static evaluation + some significant margin is still below alpha, then let's perform
            // a quick quiescence search to see if the position is really that bad. If it is, we can prune the node.
            if (depth <= config.razorDepth.value
                && staticEval + config.razorMargin.value * depth < alpha) {

                final int score = quiescenceSearch(alpha, alpha + 1, ply);
                if (score < alpha) {
                    return score;
                }
            }

            // Null Move Pruning - https://www.chessprogramming.org/Null_Move_Pruning
            // If the static evaluation + some significant margin is still above beta after giving the opponent two moves
            // in a row (making a 'null' move), then let's assume this position is a cut-node and will fail-high, and
            // not search any further.
            if (sse.nullMoveAllowed
                && depth >= config.nmpDepth.value
                && staticEval >= beta
                && (!ttHit || cutNode || ttEntry.score() >= beta)
                && board.hasPiecesRemaining(board.isWhite())) {

                ss.get(ply + 1).nullMoveAllowed = false;
                board.makeNullMove();
                td.nodes++;

                final int base = config.nmpBase.value;
                final int divisor = config.nmpDivisor.value;
                final int evalScale = config.nmpEvalScale.value;
                final int evalMaxReduction = config.nmpEvalMaxReduction.value;
                final int evalReduction = Math.min((staticEval - beta) / evalScale, evalMaxReduction);
                final int r = base
                        + depth / divisor
                        + evalReduction;

                final int score = -search(depth - r, ply + 1, -beta, -beta + 1, !cutNode);

                board.unmakeNullMove();
                ss.get(ply + 1).nullMoveAllowed = true;

                if (score >= beta) {
                    return Score.isMateScore(score) ? beta : score;
                }
            }

        }

        Move bestMove = null;
        int bestScore = Score.MIN;
        int flag = HashFlag.UPPER;

        sse.searchedMoves = new ArrayList<>();
        final MovePicker movePicker = new MovePicker(config, movegen, ss, history, board, ply, ttMove, inCheck);
        int movesSearched = 0;

        while (true) {

            final ScoredMove scoredMove = movePicker.next();
            if (scoredMove == null) {
                break;
            }
            Move move = scoredMove.move();
            movesSearched++;

            final Piece piece = scoredMove.piece();
            final Piece captured = scoredMove.captured();
            final boolean isCapture = captured != null;

            int extension = 0;
            int reduction = 0;

            if (inCheck) {
                extension = 1;
            }

            // Futility Pruning - https://www.chessprogramming.org/Futility_Pruning
            // If the static evaluation + some margin is still < alpha, and the current move is not interesting (checks,
            // captures, promotions), then let's assume it will fail low and prune this node.
            if (!pvNode
                    && !rootNode
                    && !inCheck
                    && depth <= config.fpDepth.value
                    && scoredMove.isQuiet()) {

                // Two margins - a strict margin where we fully prune the move, and a softer margin where we reduce depth.
                int pruneMargin = config.fpMargin.value + depth * config.fpScale.value;
                int reduceMargin = pruneMargin + depth * config.fpBlend.value;

                if (staticEval + pruneMargin <= alpha) {
                    sse.currentMove = null;
                    movePicker.setSkipQuiets(true);
                    continue;
                }
                else if (staticEval + reduceMargin <= alpha) {
                    // Calculate distance from alpha to scale reduction dynamically
                    int delta = (alpha - staticEval) - pruneMargin;

                    int maxReduction = config.fpDepth.value;
                    futilityReduction = 1 + Math.min(delta / (reduceMargin - pruneMargin), maxReduction - 1);
                }
            }

            final int historyScore = scoredMove.historyScore();

            // Late Move Reductions - https://www.chessprogramming.org/Late_Move_Reductions
            // If the move is ordered late in the list, and isn't a 'noisy' move like a check, capture or promotion,
            // let's save time by assuming it's less likely to be good, and reduce the search depth.
            if (depth >= config.lmrDepth.value
                    && movesSearched >= (pvNode ? config.lmrMinPvMoves.value : config.lmrMinMoves.value) + (rootNode ? 1 : 0)) {

                // Reductions are based on the depth and the number of moves searched so far.
                reduction = config.lmrReductions[isCapture ? 1 : 0][depth][movesSearched];

                // Reduce less in PV nodes.
                reduction -= pvNode ? 1 : 0;

                // Reduce moves with a bad history score more aggressively, and reduce less if the history score is good.
                reduction -= 2 * historyScore / config.quietHistMaxScore.value;

                reduction = Math.max(0, reduction);
            }

            // History pruning - https://www.chessprogramming.org/History_Leaf_Pruning
            // Quiet moves which have a bad history score are pruned at the leaf nodes. This is a simple heuristic
            // that assumes that moves which have historically been bad are likely to be bad in the current position.
            if (!pvNode
                    && !rootNode
                    && scoredMove.isQuiet()
                    && depth - reduction <= config.hpMaxDepth.value
                    && historyScore < config.hpMargin.value * depth + config.hpOffset.value) {
                sse.currentMove = null;
                movePicker.setSkipQuiets(true);
                continue;
            }

            // Late Move Pruning - https://www.chessprogramming.org/Futility_Pruning#Move_Count_Based_Pruning
            // If the move is ordered very late in the list, and isn't a 'noisy' move like a check, capture or
            // promotion, let's assume it's less likely to be good, and fully skip searching that move.
            final int lmpCutoff = (depth * config.lmpMultiplier.value) / (1 + (improving ? 0 : 1));
            if (!pvNode
                    && !rootNode
                    && !inCheck
                    && scoredMove.isQuiet()
                    && depth <= config.lmpDepth.value
                    && movesSearched >= lmpCutoff) {
                sse.currentMove = null;
                movePicker.setSkipQuiets(true);
                continue;
            }

            // PVS SEE Pruning - https://www.chessprogramming.org/Static_Exchange_Evaluation
            // Prune moves that lose material beyond a certain threshold, once all the pieces have been exchanged.
            if (!pvNode
                    && !rootNode
                    && depth <= config.seeMaxDepth.value
                    && movesSearched > 1
                    && (scoredMove.isQuiet() || (scoredMove.isBadNoisy() && isCapture))
                    && !Score.isMateScore(bestScore)) {

                int threshold = scoredMove.isQuiet() ?
                        config.seeQuietMargin.value * depth :
                        config.seeNoisyMargin.value * depth * depth;
                if (!SEE.see(board, move, threshold)) {
                    continue;
                }

            }


            eval.makeMove(board, move);
            if (!board.makeMove(move)) {
                eval.unmakeMove();
                continue;
            }
            final int nodesBefore = td.nodes;
            td.nodes++;

            if (scoredMove.isQuiet() || scoredMove.isBadNoisy()) {
                reduction += futilityReduction;
            }

            PlayedMove playedMove = new PlayedMove(move, piece, captured);
            sse.currentMove = playedMove;
            sse.searchedMoves.add(playedMove);

            int score;

            if (pvNode && movesSearched == 1) {
                // Principal Variation Search - https://www.chessprogramming.org/Principal_Variation_Search
                // The first move must be searched with the full alpha-beta window. If our move ordering is any good
                // then we expect this to be the best move, and so we need to retrieve the exact score.
                score = -search(depth - 1 + extension, ply + 1, -beta, -alpha, false);
            }
            else {
                // For all other moves apart from the principal variation, search with a null window (-alpha - 1, -alpha),
                // to try and prove the move will fail low while saving the time spent on a full search.
                score = -search(depth - 1 - reduction + extension, ply + 1, -alpha - 1, -alpha, true);

                if (score > alpha && (score < beta || reduction > 0)) {
                    // If we reduced the depth and/or used a null window, and the score beat alpha, we need to do a
                    // re-search with the full window and depth. This is costly, but hopefully doesn't happen too often.
                    score = -search(depth - 1 + extension, ply + 1, -beta, -alpha, false);
                }
            }

            eval.unmakeMove();
            board.unmakeMove();
            sse.currentMove = null;

            if (rootNode) {
                td.addNodes(move, td.nodes - nodesBefore);
            }

            if (shouldStop()) {
                return alpha;
            }

            if (score > bestScore) {
                bestScore = score;
            }

            if (score > alpha) {
                // If the score is better than alpha, we have a new best move.
                bestMove = move;
                alpha = score;
                flag = HashFlag.EXACT;

                sse.bestMove = playedMove;
                if (rootNode) {
                    bestMoveCurrent = move;
                    bestScoreCurrent = score;
                }

                if (score >= beta) {
                    // If the score is greater than beta, the position is outside the bounds of the current alpha-beta
                    // window. Our opponent won't allow us to reach this position, so we can cut off the search here.
                    flag = HashFlag.LOWER;
                    break;
                }
            }
        }

        if (movesSearched == 0) {
            // If there are no legal moves, and it's check, then it's checkmate. Otherwise, it's stalemate.
            return inCheck ? -Score.MATE + ply : Score.DRAW;
        }

        if (bestScore >= beta) {
            final PlayedMove best = sse.bestMove;
            final int historyDepth = depth
                    + (staticEval <= alpha ? 1 : 0)
                    + (bestScore > beta + 50 ? 1 : 0);
            history.updateHistory(best, board.isWhite(), historyDepth, ply, ss);
        }

        if (!inCheck
            && Score.isDefinedScore(bestScore)
            && (bestMove == null || board.isQuiet(bestMove))
            && !(flag == HashFlag.LOWER && uncorrectedStaticEval >= bestScore)
            && !(flag == HashFlag.UPPER && uncorrectedStaticEval <= bestScore)) {
            history.updateCorrectionHistory(board, ss, ply, depth, bestScore, uncorrectedStaticEval);
        }

        // Store the best move and score in the transposition table for future reference.
        if (!shouldStop()) {
            tt.put(board.key(), flag, depth, ply, bestMove, rawStaticEval, bestScore);
        }

        return bestScore;

    }

    /**
     * Extend the search by searching captures until a 'quiet' position is reached, where there are no further captures
     * and therefore limited potential for winning tactics that drastically alter the evaluation. Used to mitigate the
     * worst of the 'horizon effect'.
     *
     * @see <a href="https://www.chessprogramming.org/Quiescence_Search">Chess Programming Wiki</a>
     */
    int quiescenceSearch(int alpha, int beta, int ply) {

        if (shouldStop()) {
            return alpha;
        }

        // If the game is drawn by repetition, insufficient material or fifty move rule, return zero.
        if (ply > 0 && isDraw()) return Score.DRAW;

        // If the maximum depth is reached, return the static evaluation of the position.
        if (ply >= MAX_DEPTH) return movegen.isCheck(board) ? 0 : eval.evaluate();

        // Update the selective search depth
        if (ply + 1 > td.seldepth) td.seldepth = ply + 1;

        final boolean pvNode = beta - alpha > 1;

        // Exit the quiescence search early if we already have an accurate score stored in the hash table.
        final HashEntry ttEntry = tt.get(board.key(), ply);
        final boolean ttHit = ttEntry != null;
        if (!pvNode
                && ttHit
                && isWithinBounds(ttEntry, alpha, beta)) {
            return ttEntry.score();
        }
        Move ttMove = null;
        if (ttHit && ttEntry.move() != null) {
            ttMove = ttEntry.move();
        }

        final boolean inCheck = movegen.isCheck(board);

        MoveFilter filter;

        // Re-use cached static eval if available. Don't compute static eval while in check.
        int rawStaticEval = Integer.MIN_VALUE;
        int staticEval = Integer.MIN_VALUE;

        if (inCheck) {
            // If we are in check, we need to generate 'all' legal moves that evade check, not just captures. Otherwise,
            // we risk missing simple mate threats.
            filter = MoveFilter.ALL;
        } else {
            // If we are not in check, then we have the option to 'stand pat', i.e. decline to continue the capture chain,
            // if the static evaluation of the position is good enough.

            rawStaticEval = ttHit ? ttEntry.staticEval() : eval.evaluate();

            if (!ttHit) {
                tt.put(board.key(), HashFlag.NONE, 0, 0, null, rawStaticEval, 0);
            }

            staticEval = ttMove != null ?
                    rawStaticEval :
                    history.correctEvaluation(board, ss, ply, rawStaticEval);
            if (ttHit &&
                    (ttEntry.flag() == HashFlag.EXACT ||
                    (ttEntry.flag() == HashFlag.LOWER && ttEntry.score() >= rawStaticEval) ||
                    (ttEntry.flag() == HashFlag.UPPER && ttEntry.score() <= rawStaticEval))) {
                staticEval = ttEntry.score();
            }

            if (staticEval >= beta) {
                return staticEval;
            }
            if (staticEval > alpha) {
                alpha = staticEval;
            }
            filter = MoveFilter.CAPTURES_ONLY;
        }

        final QuiescentMovePicker movePicker = new QuiescentMovePicker(config, movegen, ss, history, board, ply, ttMove, inCheck);
        movePicker.setFilter(filter);

        int movesSearched = 0;

        Move bestMove = null;
        int bestScore = alpha;
        final int futilityScore = bestScore + config.qsFpMargin.value;
        int flag = HashFlag.UPPER;

        while (true) {

            final ScoredMove scoredMove = movePicker.next();
            if (scoredMove == null) break;
            final Move move = scoredMove.move();
            movesSearched++;

            // Delta Pruning - https://www.chessprogramming.org/Delta_Pruning
            // If the captured piece + a margin still has no potential of raising alpha, let's assume this position
            // is bad for us no matter what we do, and not bother searching any further
            final Piece captured = scoredMove.captured();
            if (!inCheck
                    && captured != null
                    && !move.isPromotion()
                    && (staticEval + SEE.value(captured) + config.dpMargin.value < alpha)) {
                continue;
            }

            // Futility Pruning
            // The same heuristic as used in the main search, but applied to the quiescence. Skip captures that don't
            // win material when the static eval plus some margin is sufficiently below alpha.
            if (captured != null
                && futilityScore <= alpha
                && !SEE.see(board, move, 1)) {
                continue;
            }

            // SEE Pruning - https://www.chessprogramming.org/Static_Exchange_Evaluation
            // Evaluate the possible captures + recaptures on the target square, in order to filter out losing capture
            // chains, such as capturing with the queen a pawn defended by another pawn.
            if (!inCheck && !SEE.see(board, move, config.qsSeeThreshold.value)) {
                continue;
            }

            eval.makeMove(board, move);
            if (!board.makeMove(move)) continue;
            td.nodes++;
            final int score = -quiescenceSearch(-beta, -alpha, ply + 1);
            eval.unmakeMove();
            board.unmakeMove();

            if (score > bestScore) {
                bestScore = score;
            }
            if (score > alpha) {
                flag = HashFlag.EXACT;
                bestMove = move;
                alpha = score;
                if (score >= beta) {
                    flag = HashFlag.LOWER;
                    break;
                }
            }
        }

        if (movesSearched == 0 && inCheck) {
            return -Score.MATE + ply;
        }

        if (!shouldStop()) {
            tt.put(board.key(), flag, 0, ply, bestMove, rawStaticEval, bestScore);
        }

        return bestScore;

    }

    @Override
    public void setPosition(Board board) {
        this.board = board;
        this.eval.setPosition(board);
    }

    @Override
    public void setHashSize(int hashSizeMb) {
        this.tt.resize(hashSizeMb);
    }

    @Override
    public void setThreadCount(int threadCount) {
        // do nothing as this implementation is single-threaded
    }

    private boolean shouldStop() {
        // Exit if global search is cancelled
        if (config.searchCancelled) return true;
        return !config.pondering && tc != null && tc.isHardLimitReached(td.depth, td.nodes);
    }

    private boolean shouldStopSoft() {
        if (config.pondering || tc == null)
            return false;
        final int bestMoveStability = history.getBestMoveStability();
        final int scoreStability = history.getBestScoreStability();
        final int bestMoveNodes = td.getNodes(bestMoveCurrent);
        return tc.isSoftLimitReached(td.depth, td.nodes, bestMoveNodes, bestMoveStability, scoreStability);
    }

    private boolean isDraw() {
        return Score.isEffectiveDraw(board);
    }

    /**
     * Compute whether our position is improving relative to previous static evaluations. If we are in check, we're not
     * improving. If we were in check 2 plies ago, check 4 plies ago. If we were in check 4 plies ago, return true.
     */
    private boolean isImproving(int ply, int staticEval) {
        if (staticEval == Integer.MIN_VALUE) return false;
        if (ply < 2) return false;
        int lastEval = ss.get(ply - 2).staticEval;
        if (lastEval == Integer.MIN_VALUE) {
            if (ply < 4) return false;
            lastEval = ss.get(ply - 4).staticEval;
            if (lastEval == Integer.MIN_VALUE) {
                return true;
            }
        }
        return lastEval < staticEval;
    }

    private SearchResult handleOnlyOneLegalMove(List<Move> rootMoves) {
        // If there is only one legal move, play it immediately
        final Move move = rootMoves.get(0);
        final int eval = this.eval.evaluate();
        SearchResult result = SearchResult.of(move, eval, td, tc);
        if (td.isMainThread())
            UCI.writeSearchInfo(result);
        return result;
    }

    public boolean isWithinBounds(HashEntry entry, int alpha, int beta) {
        return entry.flag() == HashFlag.EXACT ||
                (Score.isDefinedScore(entry.score()) &&
                        (entry.flag() == HashFlag.UPPER && entry.score() <= alpha ||
                                entry.flag() == HashFlag.LOWER && entry.score() >= beta));
    }

    public boolean isSufficientDepth(HashEntry entry, int depth) {
        return entry.depth() >= depth;
    }

    @Override
    public TranspositionTable getTranspositionTable() {
        return tt;
    }

    @Override
    public void clearHistory() {
        tt.clear();
        eval.clearHistory();
        history.clear();
    }

}
