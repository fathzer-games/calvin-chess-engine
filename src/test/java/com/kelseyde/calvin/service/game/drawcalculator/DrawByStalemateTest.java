package com.kelseyde.calvin.service.game.drawcalculator;

import com.kelseyde.calvin.model.Board;
import com.kelseyde.calvin.model.Colour;
import com.kelseyde.calvin.model.Piece;
import com.kelseyde.calvin.model.PieceType;
import com.kelseyde.calvin.model.game.DrawType;
import com.kelseyde.calvin.model.game.Game;
import com.kelseyde.calvin.model.game.result.DrawResult;
import com.kelseyde.calvin.model.game.result.GameResult;
import com.kelseyde.calvin.model.move.Move;
import com.kelseyde.calvin.utils.BoardUtils;
import com.kelseyde.calvin.utils.MoveUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DrawByStalemateTest {

    @Test
    public void testSimpleQueenStalemate() {

        Board board = BoardUtils.emptyBoard();
        board.setPiece(56, new Piece(Colour.BLACK, PieceType.KING));
        board.setPiece(42, new Piece(Colour.WHITE, PieceType.KING));
        board.setPiece(1, new Piece(Colour.WHITE, PieceType.QUEEN));

        Game game = new Game(board);
        GameResult result = game.playMove(move("b1", "b6"));

        // king stalemated in the corner
        Assertions.assertEquals(GameResult.ResultType.DRAW, result.getResultType());
        Assertions.assertEquals(DrawType.STALEMATE, ((DrawResult) result).getDrawType());

    }

    @Test
    public void testSimpleKingAndPawnStalemate() {

        Board board = BoardUtils.emptyBoard();
        board.setPiece(60, new Piece(Colour.BLACK, PieceType.KING));
        board.setPiece(43, new Piece(Colour.WHITE, PieceType.KING));
        board.setPiece(52, new Piece(Colour.WHITE, PieceType.PAWN));

        Game game = new Game(board);
        GameResult result = game.playMove(move("d6", "e6"));

        // king stalemated by king and pawn
        Assertions.assertEquals(GameResult.ResultType.DRAW, result.getResultType());
        Assertions.assertEquals(DrawType.STALEMATE, ((DrawResult) result).getDrawType());

    }

    @Test
    public void testSimpleKingAndBishopStalemate() {

        Board board = BoardUtils.emptyBoard();
        board.setPiece(63, new Piece(Colour.BLACK, PieceType.KING));
        board.setPiece(46, new Piece(Colour.WHITE, PieceType.KING));
        board.setPiece(47, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(37, new Piece(Colour.WHITE, PieceType.BISHOP));

        Game game = new Game(board);
        GameResult result = game.playMove(move("f5", "e6"));

        // king stalemated in the corner
        Assertions.assertEquals(GameResult.ResultType.DRAW, result.getResultType());
        Assertions.assertEquals(DrawType.STALEMATE, ((DrawResult) result).getDrawType());

    }

    @Test
    public void testStalemateWithPinnedPawn() {

        Board board = BoardUtils.emptyBoard();
        board.setPiece(63, new Piece(Colour.BLACK, PieceType.KING));
        board.setPiece(54, new Piece(Colour.BLACK, PieceType.PAWN));
        board.setPiece(38, new Piece(Colour.WHITE, PieceType.KING));
        board.setPiece(47, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(36, new Piece(Colour.WHITE, PieceType.BISHOP));
        board.setPiece(37, new Piece(Colour.WHITE, PieceType.BISHOP));
        board.setPiece(9, new Piece(Colour.WHITE, PieceType.QUEEN));

        Game game = new Game(board);
        GameResult result = game.playMove(move("b2", "a2"));

        // even though pawn could pseudo-legally capture on h6 with check, it is pinned, therefore stalemate
        Assertions.assertEquals(GameResult.ResultType.DRAW, result.getResultType());
        Assertions.assertEquals(DrawType.STALEMATE, ((DrawResult) result).getDrawType());

    }

    private Move move(String startSquare, String endSquare) {
        return MoveUtils.fromNotation(startSquare, endSquare);
    }

}
