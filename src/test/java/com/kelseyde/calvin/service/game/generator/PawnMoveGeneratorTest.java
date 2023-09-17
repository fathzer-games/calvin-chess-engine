package com.kelseyde.calvin.service.game.generator;

import com.kelseyde.calvin.model.*;
import com.kelseyde.calvin.model.move.Move;
import com.kelseyde.calvin.model.move.MoveType;
import com.kelseyde.calvin.service.game.generator.bitboard.PawnMoveGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public class PawnMoveGeneratorTest {

    private final PawnMoveGenerator generator = new PawnMoveGenerator();

    private final Piece whitePawn = new Piece(Colour.WHITE, PieceType.PAWN);
    private final Piece blackPawn = new Piece(Colour.BLACK, PieceType.PAWN);

    private Board board;

    @BeforeEach
    public void beforeEach() {
        board = Board.emptyBoard();
    }

    @Test
    public void testStartingPositions() {

        board = new Board();
        Set<Move> expected = Set.of(
                moveBuilder(8, 16).build(), moveBuilder(8, 24).build(),
                moveBuilder(9, 17).build(), moveBuilder(9, 25).build(),
                moveBuilder(10, 18).build(), moveBuilder(10, 26).build(),
                moveBuilder(11, 19).build(), moveBuilder(11, 27).build(),
                moveBuilder(12, 20).build(), moveBuilder(12, 28).build(),
                moveBuilder(13, 21).build(), moveBuilder(13, 29).build(),
                moveBuilder(14, 22).build(), moveBuilder(14, 30).build(),
                moveBuilder(15, 23).build(), moveBuilder(15, 31).build()
        );

        assertMoves(board, expected);

    }

    @Test
    public void testPawnCannotMoveThroughPiece() {

        board = Board.emptyBoard();
        board.setPiece(16, new Piece(Colour.WHITE, PieceType.ROOK));
        assertMovesFromSquare(board, 8, Set.of());
        board.unsetPiece(16);

        board.setPiece(17, new Piece(Colour.BLACK, PieceType.PAWN));
        assertMovesFromSquare(board, 9, Set.of());
        board.unsetPiece(17);

        board.setPiece(18, new Piece(Colour.WHITE, PieceType.KING));
        assertMovesFromSquare(board, 10, Set.of());
        board.unsetPiece(18);

        board.setPiece(19, new Piece(Colour.BLACK, PieceType.QUEEN));
        assertMovesFromSquare(board, 11, Set.of());
        board.unsetPiece(19);

        board.setPiece(44, new Piece(Colour.BLACK, PieceType.BISHOP));
        assertMovesFromSquare(board, 52, Set.of());
        board.unsetPiece(44);

        board.setPiece(45, new Piece(Colour.BLACK, PieceType.PAWN));
        assertMovesFromSquare(board, 53, Set.of());
        board.unsetPiece(45);

        board.setPiece(46, new Piece(Colour.BLACK, PieceType.KNIGHT));
        assertMovesFromSquare(board, 54, Set.of());
        board.unsetPiece(46);

        board.setPiece(47, new Piece(Colour.BLACK, PieceType.ROOK));
        assertMovesFromSquare(board, 55, Set.of());
        board.unsetPiece(47);

    }

    @Test
    public void testWhitePawnsNotOnStartingSquares() {

        board = Board.emptyBoard();
        board.setPiece(16, whitePawn);
        assertMovesFromSquare(board, 16, Set.of(moveBuilder(16, 24).build()));
        board.unsetPiece(16);

        board.setPiece(34, whitePawn);
        assertMovesFromSquare(board, 34, Set.of(moveBuilder(34, 42).build()));
        board.unsetPiece(34);

        board.setPiece(45, whitePawn);
        assertMovesFromSquare(board, 45, Set.of(moveBuilder(45, 53).build()));
        board.unsetPiece(45);

    }

    @Test
    public void testBlackPawnsNotOnStartingSquares() {

        board = Board.emptyBoard();
        board.setTurn(Colour.BLACK);

        board.setPiece(45, blackPawn);
        assertMovesFromSquare(board, 45, Set.of(moveBuilder(45, 37).build()));
        board.unsetPiece(45);

        board.setPiece(35, blackPawn);
        assertMovesFromSquare(board, 35, Set.of(moveBuilder(35, 27).build()));
        board.unsetPiece(35);

        board.setPiece(31, blackPawn);
        assertMovesFromSquare(board, 31, Set.of(moveBuilder(31, 23).build()));
        board.unsetPiece(31);

    }

    @Test
    public void testWhitePawnCaptures() {

        board = Board.emptyBoard();
        board.setPiece(9, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(16, new Piece(Colour.BLACK, PieceType.PAWN));
        board.setPiece(18, new Piece(Colour.BLACK, PieceType.PAWN));
        assertMovesFromSquare(board, 9,
                Set.of(moveBuilder(9, 16).build(), moveBuilder(9, 17).build(), moveBuilder(9, 18).build(), moveBuilder(9, 25).build()));

        board.setPiece(31, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(38, new Piece(Colour.BLACK, PieceType.PAWN));
        // should not capture the wrapped piece
        board.setPiece(40, new Piece(Colour.BLACK, PieceType.PAWN));
        assertMovesFromSquare(board, 31,
                Set.of(moveBuilder(31, 38).build(), moveBuilder(31, 39).build()));

        board.setPiece(41, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(48, new Piece(Colour.BLACK, PieceType.PAWN));
        // should not capture white pieces
        board.setPiece(50, new Piece(Colour.WHITE, PieceType.ROOK));
        assertMovesFromSquare(board, 41,
                Set.of(moveBuilder(41, 48).isCapture(true).build(),
                        moveBuilder(41, 49).isCapture(false).build()));

    }

    @Test
    public void testBlackPawnCaptures() {

        board = Board.emptyBoard();
        board.setTurn(Colour.BLACK);

        board.setPiece(49, new Piece(Colour.BLACK, PieceType.PAWN));
        board.setPiece(40, new Piece(Colour.WHITE, PieceType.PAWN));
        board.setPiece(42, new Piece(Colour.WHITE, PieceType.PAWN));
        assertMovesFromSquare(board, 49,
                Set.of(moveBuilder(49, 40).build(), moveBuilder(49, 41).build(), moveBuilder(49, 42).build(), moveBuilder(49, 33).build()));

        board.setPiece(32, new Piece(Colour.BLACK, PieceType.PAWN));
        board.setPiece(25, new Piece(Colour.WHITE, PieceType.PAWN));
        // should not capture the wrapped piece
        board.setPiece(23, new Piece(Colour.WHITE, PieceType.PAWN));
        assertMovesFromSquare(board, 32,
                Set.of(moveBuilder(32, 24).build(), moveBuilder(32, 25).build()));

    }

    @Test
    public void testWhiteEnPassant() {

        board = Board.emptyBoard();
        board.setPiece(35, whitePawn);
        board.setPiece(34, blackPawn);
        board.setEnPassantTarget(new BitBoard(1L << 42));

        Set<Move> legalWhiteMoves = generator.generateMoves(board);

        Move standardMove = moveBuilder(35, 43).build();
        Move enPassantCapture = moveBuilder(35, 42).moveType(MoveType.EN_PASSANT)
                .enPassantCapture(new BitBoard(1L << 34))
                .isCapture(true)
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalWhiteMoves);

    }

    @Test
    public void testWhiteEnPassantWithOtherCapture() {

        board = Board.emptyBoard();
        board.setPiece(35, whitePawn);
        board.setPiece(52, blackPawn);
        board.setPiece(42, new Piece(Colour.BLACK, PieceType.QUEEN));
        board.setEnPassantTarget(new BitBoard(1L << 44));

        Set<Move> legalWhiteMoves = generator.generateMoves(board);

        Move standardMove = moveBuilder(35, 43).build();
        Move standardCapture = moveBuilder(35, 42).isCapture(true).build();
        Move enPassantCapture = moveBuilder(35, 44)
                .moveType(MoveType.EN_PASSANT)
                .enPassantCapture(new BitBoard(1L << 36))
                .isCapture(true)
                .build();

        Assertions.assertEquals(Set.of(standardMove, standardCapture, enPassantCapture), legalWhiteMoves);

    }

//    @Test TODO retest once Game is working with bitboards
//    public void testWhiteDoubleEnPassantIsImpossible() {
//
//        board.setPiece(35, whitePawn);
//        // we need another white piece to spend a move in between black's pawn moves
//        board.setPiece(0, new Piece(Colour.WHITE, PieceType.ROOK));
//        // two black pawns on starting positions
//        board.setPiece(50, new Piece(Colour.BLACK, PieceType.PAWN));
//        board.setPiece(52, new Piece(Colour.BLACK, PieceType.PAWN));
//
//        game.setTurn(Colour.BLACK);
//
//        // first double pawn move from black
//        Move blackDoubleMove = generator.generatePseudoLegalMoves(board, 50).stream()
//                .filter(move -> move.getEndSquare() == 34)
//                .findFirst().orElseThrow();
//        game.makeMove(blackDoubleMove);
//
//        Move whiteRookMove = moveBuilder().startSquare(0).endSquare(8).build();
//        game.makeMove(whiteRookMove);
//
//        // second double pawn move from black, should make the first en-passant capture impossible
//        blackDoubleMove = generator.generatePseudoLegalMoves(board, 52).stream()
//                .filter(move -> move.getEndSquare() == 36)
//                .findFirst().orElseThrow();
//        game.makeMove(blackDoubleMove); // first double pawn move from black
//
//        Set<Move> legalWhiteMoves = generator.generatePseudoLegalMoves(board, 35);
//
//        Move standardMove = moveBuilder().startSquare(35).endSquare(43).build();
//        Move enPassantCapture = moveBuilder().startSquare(35).endSquare(44)
//                .enPassantCapturedSquare(36)
//                .moveType(MoveType.EN_PASSANT)
//                .isCapture(true)
//                .build();
//
//        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalWhiteMoves);
//
//    }

    @Test
    public void testBlackEnPassant() {

        board.setPiece(29, blackPawn);
        board.setPiece(30, whitePawn);
        board.setEnPassantTarget(new BitBoard(1L << 22));
        board.setTurn(Colour.BLACK);

        Set<Move> legalBlackMoves = generator.generateMoves(board);

        Move standardMove = moveBuilder(29, 21).build();
        Move enPassantCapture = moveBuilder(29, 22).moveType(MoveType.EN_PASSANT)
                .enPassantCapture(new BitBoard(1L << 30))
                .isCapture(true)
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalBlackMoves);

    }

    @Test
    public void testBlackEnPassantWithOtherCapture() {

        board.setPiece(29, blackPawn);
        board.setPiece(28, whitePawn);
        board.setPiece(22, new Piece(Colour.WHITE, PieceType.ROOK));
        board.setEnPassantTarget(new BitBoard(1L << 20));
        board.setTurn(Colour.BLACK);

        Set<Move> legalBlackMoves = generator.generateMoves(board);

        Move standardMove = moveBuilder(29, 21).build();
        Move standardCapture = moveBuilder(29, 22).isCapture(true).build();
        Move enPassantCapture = moveBuilder(29, 20).startSquare(29).endSquare(20)
                .enPassantCapture(new BitBoard(1L << 28))
                .moveType(MoveType.EN_PASSANT)
                .isCapture(true)
                .build();

        Assertions.assertEquals(Set.of(standardMove, standardCapture, enPassantCapture), legalBlackMoves);

    }
//
//    @Test TODO retest when Game works with Bitboards
//    public void testBlackDoubleEnPassantIsImpossible() {
//
//        board.setPiece(25, blackPawn);
//        // we need another black piece to spend a move in between black's pawn moves
//        board.setPiece(63, new Piece(Colour.BLACK, PieceType.ROOK));
//        // two black pawns on starting positions
//        board.setPiece(8, new Piece(Colour.WHITE, PieceType.PAWN));
//        board.setPiece(10, new Piece(Colour.WHITE, PieceType.PAWN));
//        game.setTurn(Colour.WHITE);
//
//        // first double pawn move from white
//        Move whiteDoubleMove = generator.generatePseudoLegalMoves(board, 10).stream()
//                .filter(move -> move.getEndSquare() == 26)
//                .findFirst().orElseThrow();
//        game.makeMove(whiteDoubleMove);
//
//        Move blackRookMove = moveBuilder().startSquare(63).endSquare(62).build();
//        game.makeMove(blackRookMove);
//
//        // second double pawn move from black, should make the first en-passant capture impossible
//        whiteDoubleMove = generator.generatePseudoLegalMoves(board, 8).stream()
//                .filter(move -> move.getEndSquare() == 24)
//                .findFirst().orElseThrow();
//        game.makeMove(whiteDoubleMove); // first double pawn move from black
//
//        Set<Move> legalBlackMoves = generator.generatePseudoLegalMoves(board, 25);
//
//        Move standardMove = moveBuilder().startSquare(25).endSquare(17).build();
//        Move enPassantCapture = moveBuilder().startSquare(25).endSquare(16)
//                .enPassantCapturedSquare(24)
//                .moveType(MoveType.EN_PASSANT)
//                .isCapture(true)
//                .build();
//
//        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalBlackMoves);
//
//    }
//
//    @Test
//    public void testEnPassantRemovesCapturedPawn() {
//
//        Game game = new Game();
//        game.makeMove(MoveUtils.fromNotation("e2", "e4"));
//        game.makeMove(MoveUtils.fromNotation("g8", "f6"));
//        game.makeMove(MoveUtils.fromNotation("e4", "e5"));
//        game.makeMove(MoveUtils.fromNotation("d7", "d5"));
//        //en passant
//        game.makeMove(MoveUtils.fromNotation("e5", "d6"));
//
//        Assertions.assertTrue(board.getPieceAt(MoveUtils.fromNotation("d5")).isEmpty());
//
//    }
//
    @Test
    public void testWhiteStandardPromotion() {
        board = Board.emptyBoard();
        board.setPiece(51, whitePawn);
        Set<Move> legalMoves = generator.generateMoves(board);
        Assertions.assertEquals(
                Set.of(moveBuilder(51, 59)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.QUEEN).isCapture(false).build(),
                        moveBuilder(51, 59)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.ROOK).isCapture(false).build(),
                        moveBuilder(51, 59)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.BISHOP).isCapture(false).build(),
                        moveBuilder(51, 59)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.KNIGHT).isCapture(false).build()),
                legalMoves
        );
    }

    @Test
    public void testBlackStandardPromotion() {
        board.setPiece(8, blackPawn);
        board.setTurn(Colour.BLACK);
        Set<Move> legalMoves = generator.generateMoves(board);
        Assertions.assertEquals(
                Set.of(moveBuilder(8, 0)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.QUEEN)
                                .isCapture(false).build(),
                        moveBuilder(8, 0)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.ROOK)
                                .isCapture(false).build(),
                        moveBuilder(8, 0)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.BISHOP)
                                .isCapture(false).build(),
                        moveBuilder(8, 0)
                                .moveType(MoveType.PROMOTION)
                                .promotionPieceType(PieceType.KNIGHT)
                                .isCapture(false).build()),
                legalMoves
        );
    }

    @Test
    public void testWhiteCapturePromotion() {
        board.setPiece(51, whitePawn);
        board.setPiece(58, new Piece(Colour.BLACK, PieceType.QUEEN));
        Set<Move> legalMoves = generator.generateMoves(board);
        Assertions.assertEquals(Set.of(
                moveBuilder(51, 59)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.QUEEN)
                        .isCapture(false).build(),
                moveBuilder(51, 59)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.ROOK)
                        .isCapture(false).build(),
                moveBuilder(51, 59)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.BISHOP)
                        .isCapture(false).build(),
                moveBuilder(51, 59)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.KNIGHT)
                        .isCapture(false).build(),
                moveBuilder(51, 58)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.QUEEN)
                        .isCapture(true).build(),
                moveBuilder(51, 58)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.ROOK)
                        .isCapture(true).build(),
                moveBuilder(51, 58)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.BISHOP)
                        .isCapture(true).build(),
                moveBuilder(51, 58)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.KNIGHT)
                        .isCapture(true).build()),
                legalMoves
        );
    }

    @Test
    public void testBlackCapturePromotion() {
        board.setPiece(15, blackPawn);
        board.setPiece(6, new Piece(Colour.WHITE, PieceType.BISHOP));
        board.setTurn(Colour.BLACK);
        Set<Move> legalMoves = generator.generateMoves(board);
        Set<Move> expectedLegalMoves = Set.of(
                moveBuilder(15, 7)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.QUEEN)
                        .isCapture(false).build(),
                moveBuilder(15, 7)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.ROOK)
                        .isCapture(false).build(),
                moveBuilder(15, 7)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.BISHOP)
                        .isCapture(false).build(),
                moveBuilder(15, 7)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.KNIGHT)
                        .isCapture(false).build(),
                moveBuilder(15, 6)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.QUEEN)
                        .isCapture(true).build(),
                moveBuilder(15, 6)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.ROOK)
                        .isCapture(true).build(),
                moveBuilder(15, 6)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.BISHOP)
                        .isCapture(true).build(),
                moveBuilder(15, 6)
                        .moveType(MoveType.PROMOTION)
                        .promotionPieceType(PieceType.KNIGHT)
                        .isCapture(true).build());
        Assertions.assertEquals(expectedLegalMoves, legalMoves);
    }

    private void assertMoves(Board board, Set<Move> expected) {
        Set<Move> actual = generator.generateMoves(board);

        Assertions.assertEquals(
                expected.stream().map(Move::getKey).collect(Collectors.toSet()),
                actual.stream().map(Move::getKey).collect(Collectors.toSet()));
    }

    private void assertMovesFromSquare(Board board, int square, Set<Move> expected) {
        Set<Move> actual = generator.generateMoves(board).stream()
                .filter(move -> move.getStartSquare() == square)
                .collect(Collectors.toSet());
        Assertions.assertEquals(
                expected.stream().map(Move::getKey).collect(Collectors.toSet()),
                actual.stream().map(Move::getKey).collect(Collectors.toSet()));
    }
    
    private Move.MoveBuilder moveBuilder(int startSquare, int endSquare) {
        return Move.builder()
                .startSquare(startSquare)
                .endSquare(endSquare)
                .pieceType(PieceType.PAWN);
    }

}