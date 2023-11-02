package com.kelseyde.calvin.evaluation;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Move;

/**
 *
 */
public interface Evaluator {

    void init(Board board);

    void makeMove(Move move);

    void unmakeMove();

    int get();

}
