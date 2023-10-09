package com.kelseyde.calvin.movegeneration.generator;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.PieceType;
import com.kelseyde.calvin.board.move.Move;

import java.util.List;

public interface PseudoLegalMoveGenerator {

    PieceType getPieceType();

    List<Move> generatePseudoLegalMoves(Board board);

    long generateAttackMask(Board board, boolean isWhite);

    long generateAttackMaskFromSquare(Board board, int square, boolean isWhite);

}
