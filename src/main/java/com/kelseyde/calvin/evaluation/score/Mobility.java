package com.kelseyde.calvin.evaluation.score;

import com.kelseyde.calvin.board.Bitwise;
import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Piece;
import com.kelseyde.calvin.engine.EngineConfig;
import com.kelseyde.calvin.generation.Attacks;

/**
 * Mobility evaluation gives bonuses for the number of possible moves for each piece. Possible moves are defined as moves
 * to squares that are not occupied by a friendly piece, or attacked by an enemy pawn.
 * <p>
 * The bonus is a non-linear value that is zero-centered, meaning a piece gets zero for having its 'average' number of possible moves,
 * a scaled penalty for fewer moves and a scaled bonus for more moves. The values are also weighted based on game phase.
 *
 * @see <a href="https://www.chessprogramming.org/Mobility">Chess Programming Wiki</a>
 */
public class Mobility {

    public static int score(EngineConfig config, Board board, boolean isWhite, float phase) {

        long friendlyBlockers = board.getKing(isWhite) | board.getPawns(isWhite);
        long opponentBlockers = board.getPieces(!isWhite);
        long blockers = friendlyBlockers | opponentBlockers;
        long opponentPawnAttacks = Attacks.pawnAttacks(board.getPawns(!isWhite), !isWhite);

        int[] knightMgBonus = config.getMiddlegameMobilityBonus()[Piece.KNIGHT.getIndex()];
        int[] knightEgBonus = config.getEndgameMobilityBonus()[Piece.KNIGHT.getIndex()];
        int[] bishopMgBonus = config.getMiddlegameMobilityBonus()[Piece.BISHOP.getIndex()];
        int[] bishopEgBonus = config.getEndgameMobilityBonus()[Piece.BISHOP.getIndex()];
        int[] rookMgBonus = config.getMiddlegameMobilityBonus()[Piece.ROOK.getIndex()];
        int[] rookEgBonus = config.getEndgameMobilityBonus()[Piece.ROOK.getIndex()];
        int[] queenMgBonus = config.getMiddlegameMobilityBonus()[Piece.QUEEN.getIndex()];
        int[] queenEgBonus = config.getEndgameMobilityBonus()[Piece.QUEEN.getIndex()];

        int middlegameScore = 0;
        int endgameScore = 0;

        long knights = board.getKnights(isWhite);
        while (knights != 0) {
            int square = Bitwise.getNextBit(knights);
            long attacks = Attacks.knightAttacks(square);
            long moves = attacks &~ friendlyBlockers &~ opponentPawnAttacks;
            int moveCount = Bitwise.countBits(moves);
            middlegameScore += knightMgBonus[moveCount];
            endgameScore += knightEgBonus[moveCount];
            knights = Bitwise.popBit(knights);
        }

        long bishops = board.getBishops(isWhite);
        while (bishops != 0) {
            int square = Bitwise.getNextBit(bishops);
            long attacks = Attacks.bishopAttacks(square, blockers);
            long moves = attacks &~ friendlyBlockers &~ opponentPawnAttacks;
            int moveCount = Bitwise.countBits(moves);
            middlegameScore += bishopMgBonus[moveCount];
            endgameScore += bishopEgBonus[moveCount];
            bishops = Bitwise.popBit(bishops);
        }

        long rooks = board.getRooks(isWhite);
        while (rooks != 0) {
            int square = Bitwise.getNextBit(rooks);
            long attacks = Attacks.rookAttacks(square, blockers);
            long moves = attacks &~ friendlyBlockers &~ opponentPawnAttacks;
            int moveCount = Bitwise.countBits(moves);
            middlegameScore += rookMgBonus[moveCount];
            endgameScore += rookEgBonus[moveCount];
            rooks = Bitwise.popBit(rooks);
        }

        long queens = board.getQueens(isWhite);
        while (queens != 0) {
            int square = Bitwise.getNextBit(queens);
            long attacks = Attacks.rookAttacks(square, blockers) | Attacks.bishopAttacks(square, blockers);
            long moves = attacks &~ friendlyBlockers &~ opponentPawnAttacks;
            int moveCount = Bitwise.countBits(moves);
            middlegameScore += queenMgBonus[moveCount];
            endgameScore += queenEgBonus[moveCount];
            queens = Bitwise.popBit(queens);
        }

        return Phase.taperedEval(middlegameScore, endgameScore, phase);

    }

}
