package com.kelseyde.calvin.service.generator;

import com.kelseyde.calvin.model.board.Board;
import com.kelseyde.calvin.model.move.Move;
import com.kelseyde.calvin.model.piece.PieceType;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QueenMoveGenerator extends SlidingMoveGenerator {

    @Getter
    private final PieceType pieceType = PieceType.QUEEN;

    @Override
    public Set<Move> generateLegalMoves(Board board, int startSquare) {
        return null;
    }

}
