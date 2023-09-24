package com.kelseyde.calvin.movegeneration.generator;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.piece.PieceType;

public class RookMoveGenerator extends SlidingMoveGenerator {

    @Override
    public PieceType getPieceType() {
        return PieceType.ROOK;
    }

    @Override
    protected long getSliders(Board board, boolean isWhite) {
        return isWhite ? board.getWhiteRooks() : board.getBlackRooks();
    }

    @Override
    protected boolean isOrthogonal() {
        return true;
    }

    @Override
    protected boolean isDiagonal() {
        return false;
    }

}
