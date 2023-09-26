package com.kelseyde.calvin.evaluation.kingsafety;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.evaluation.BoardEvaluator;

//@Service
public class CastlingPotentialEvaluator implements BoardEvaluator {

    @Override
    public int evaluate(Board board) {
        int whiteScore = calculateCastlingScore(board, true);
        int blackScore = calculateCastlingScore(board, false);
        return whiteScore - blackScore;
    }

    private int calculateCastlingScore(Board board, boolean b) {
            return 0;
    }

}
