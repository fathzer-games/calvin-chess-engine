package com.kelseyde.calvin.service.generator;

import com.kelseyde.calvin.model.Board;
import com.kelseyde.calvin.model.Colour;
import com.kelseyde.calvin.model.Piece;
import com.kelseyde.calvin.model.PieceType;
import com.kelseyde.calvin.model.game.Game;
import com.kelseyde.calvin.model.move.Move;
import com.kelseyde.calvin.model.move.MoveType;
import com.kelseyde.calvin.model.move.config.EnPassantConfig;
import com.kelseyde.calvin.model.move.config.PromotionConfig;
import com.kelseyde.calvin.utils.MoveUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class PawnMoveGeneratorTest {

    private final PawnMoveGenerator generator = new PawnMoveGenerator();

    private final Piece whitePawn = new Piece(Colour.WHITE, PieceType.PAWN);
    private final Piece blackPawn = new Piece(Colour.BLACK, PieceType.PAWN);

    private Game game;

    @BeforeEach
    public void beforeEach() {
        Board board = Board.emptyBoard();
        game = Game.fromPosition(board);
    }

    @Test
    public void testStartingPositions() {

        assertLegalSquares(8, whitePawn, Set.of(16, 24));
        assertLegalSquares(9, whitePawn, Set.of(17, 25));
        assertLegalSquares(10, whitePawn, Set.of(18, 26));
        assertLegalSquares(11, whitePawn, Set.of(19, 27));
        assertLegalSquares(12, whitePawn, Set.of(20, 28));
        assertLegalSquares(13, whitePawn, Set.of(21, 29));
        assertLegalSquares(14, whitePawn, Set.of(22, 30));
        assertLegalSquares(15, whitePawn, Set.of(23, 31));

        assertLegalSquares(48, blackPawn, Set.of(40, 32));
        assertLegalSquares(49, blackPawn, Set.of(41, 33));
        assertLegalSquares(50, blackPawn, Set.of(42, 34));
        assertLegalSquares(51, blackPawn, Set.of(43, 35));
        assertLegalSquares(52, blackPawn, Set.of(44, 36));
        assertLegalSquares(53, blackPawn, Set.of(45, 37));
        assertLegalSquares(54, blackPawn, Set.of(46, 38));
        assertLegalSquares(55, blackPawn, Set.of(47, 39));

    }

    @Test
    public void testPawnCannotMoveThroughPiece() {

        game.getBoard().setPiece(16, new Piece(Colour.WHITE, PieceType.ROOK));
        assertLegalSquares(8, whitePawn, Set.of());
        game.getBoard().unsetPiece(16);

        game.getBoard().setPiece(17, new Piece(Colour.BLACK, PieceType.PAWN));
        assertLegalSquares(9, whitePawn, Set.of());
        game.getBoard().unsetPiece(17);

        game.getBoard().setPiece(18, new Piece(Colour.WHITE, PieceType.KING));
        assertLegalSquares(10, whitePawn, Set.of());
        game.getBoard().unsetPiece(18);

        game.getBoard().setPiece(19, new Piece(Colour.BLACK, PieceType.QUEEN));
        assertLegalSquares(11, whitePawn, Set.of());
        game.getBoard().unsetPiece(19);

        game.getBoard().setPiece(44, new Piece(Colour.BLACK, PieceType.BISHOP));
        assertLegalSquares(52, blackPawn, Set.of());
        game.getBoard().unsetPiece(44);

        game.getBoard().setPiece(45, new Piece(Colour.BLACK, PieceType.PAWN));
        assertLegalSquares(53, blackPawn, Set.of());
        game.getBoard().unsetPiece(45);

        game.getBoard().setPiece(46, new Piece(Colour.BLACK, PieceType.KNIGHT));
        assertLegalSquares(54, blackPawn, Set.of());
        game.getBoard().unsetPiece(46);

        game.getBoard().setPiece(47, new Piece(Colour.BLACK, PieceType.ROOK));
        assertLegalSquares(55, blackPawn, Set.of());
        game.getBoard().unsetPiece(47);

    }

    @Test
    public void testWhitePawnsNotOnStartingSquares() {

        assertLegalSquares(16, whitePawn, Set.of(24));
        assertLegalSquares(34, whitePawn, Set.of(42));
        assertLegalSquares(45, whitePawn, Set.of(53));
        assertLegalSquares(55, whitePawn, Set.of(63));

    }

    @Test
    public void testBlackPawnsNotOnStartingSquares() {

        assertLegalSquares(45, blackPawn, Set.of(37));
        assertLegalSquares(35, blackPawn, Set.of(27));
        assertLegalSquares(31, blackPawn, Set.of(23));
        assertLegalSquares(8, blackPawn, Set.of(0));

    }

    @Test
    public void testWhitePawnCaptures() {

        game.getBoard().setPiece(16, new Piece(Colour.BLACK, PieceType.PAWN));
        game.getBoard().setPiece(18, new Piece(Colour.BLACK, PieceType.PAWN));
        assertLegalSquares(9, whitePawn, Set.of(16, 17, 18, 25));

        game.getBoard().setPiece(38, new Piece(Colour.BLACK, PieceType.PAWN));
        // should not capture the wrapped piece
        game.getBoard().setPiece(40, new Piece(Colour.BLACK, PieceType.PAWN));
        assertLegalSquares(31, whitePawn, Set.of(38, 39));

        game.getBoard().setPiece(58, new Piece(Colour.BLACK, PieceType.PAWN));
        // should not capture white pieces
        game.getBoard().setPiece(60, new Piece(Colour.WHITE, PieceType.PAWN));
        assertLegalSquares(51, whitePawn, Set.of(58, 59));

    }

    @Test
    public void testBlackPawnCaptures() {

        game.getBoard().setPiece(40, new Piece(Colour.WHITE, PieceType.PAWN));
        game.getBoard().setPiece(42, new Piece(Colour.WHITE, PieceType.PAWN));
        assertLegalSquares(49, blackPawn, Set.of(40, 41, 42, 33));

        game.getBoard().setPiece(25, new Piece(Colour.WHITE, PieceType.PAWN));
        // should not capture the wrapped piece
        game.getBoard().setPiece(23, new Piece(Colour.WHITE, PieceType.PAWN));
        assertLegalSquares(32, blackPawn, Set.of(24, 25));

        game.getBoard().setPiece(3, new Piece(Colour.WHITE, PieceType.PAWN));
        // should not capture black pieces
        game.getBoard().setPiece(5, new Piece(Colour.BLACK, PieceType.PAWN));
        assertLegalSquares(12, blackPawn, Set.of(3, 4));

    }

    @Test
    public void testWhiteEnPassant() {

        game.getBoard().setPiece(35, whitePawn);
        game.getBoard().setPiece(50, blackPawn);
        game.setTurn(Colour.BLACK);

        Move blackDoubleMove = generator.generatePseudoLegalMoves(game, 50).stream()
                .filter(move -> move.getEndSquare() == 34)
                .findFirst().orElseThrow();
        game.handleMove(blackDoubleMove);

        Set<Move> legalWhiteMoves = generator.generatePseudoLegalMoves(game, 35);

        Move standardMove = Move.builder().startSquare(35).endSquare(43).build();
        Move enPassantCapture = Move.builder().startSquare(35).endSquare(42).type(MoveType.EN_PASSANT)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(34)
                        .build())
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalWhiteMoves);

    }

    @Test
    public void testWhiteEnPassantWithOtherCapture() {

        game.getBoard().setPiece(35, whitePawn);
        game.getBoard().setPiece(52, blackPawn);
        game.getBoard().setPiece(42, new Piece(Colour.BLACK, PieceType.QUEEN));
        game.setTurn(Colour.BLACK);

        Move blackDoubleMove = generator.generatePseudoLegalMoves(game, 52).stream()
                .filter(move -> move.getEndSquare() == 36)
                .findFirst().orElseThrow();
        game.handleMove(blackDoubleMove);

        Set<Move> legalWhiteMoves = generator.generatePseudoLegalMoves(game, 35);

        Move standardMove = Move.builder().startSquare(35).endSquare(43).build();
        Move standardCapture = Move.builder().startSquare(35).endSquare(42).build();
        Move enPassantCapture = Move.builder().startSquare(35).endSquare(44)
                .type(MoveType.EN_PASSANT)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(36)
                        .build())
                .build();

        Assertions.assertEquals(Set.of(standardMove, standardCapture, enPassantCapture), legalWhiteMoves);

    }

    @Test
    public void testWhiteDoubleEnPassantIsImpossible() {

        game.getBoard().setPiece(35, whitePawn);
        // we need another white piece to spend a move in between black's pawn moves
        game.getBoard().setPiece(0, new Piece(Colour.WHITE, PieceType.ROOK));
        // two black pawns on starting positions
        game.getBoard().setPiece(50, new Piece(Colour.BLACK, PieceType.PAWN));
        game.getBoard().setPiece(52, new Piece(Colour.BLACK, PieceType.PAWN));
        game.setTurn(Colour.BLACK);

        // first double pawn move from black
        Move blackDoubleMove = generator.generatePseudoLegalMoves(game, 50).stream()
                .filter(move -> move.getEndSquare() == 34)
                .findFirst().orElseThrow();
        game.handleMove(blackDoubleMove);

        Move whiteRookMove = Move.builder().startSquare(0).endSquare(8).build();
        game.handleMove(whiteRookMove);

        // second double pawn move from black, should make the first en-passant capture impossible
        blackDoubleMove = generator.generatePseudoLegalMoves(game, 52).stream()
                .filter(move -> move.getEndSquare() == 36)
                .findFirst().orElseThrow();
        game.handleMove(blackDoubleMove); // first double pawn move from black

        Set<Move> legalWhiteMoves = generator.generatePseudoLegalMoves(game, 35);

        Move standardMove = Move.builder().startSquare(35).endSquare(43).build();
        Move enPassantCapture = Move.builder().startSquare(35).endSquare(44)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(36)
                        .build())
                .type(MoveType.EN_PASSANT)
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalWhiteMoves);

    }

    @Test
    public void testBlackEnPassant() {

        game.getBoard().setPiece(29, blackPawn);
        game.getBoard().setPiece(14, whitePawn);
        game.setTurn(Colour.WHITE);

        Move whiteDoubleMove = generator.generatePseudoLegalMoves(game, 14).stream()
                .filter(move -> move.getEndSquare() == 30)
                .findFirst().orElseThrow();
        game.handleMove(whiteDoubleMove);

        Set<Move> legalBlackMoves = generator.generatePseudoLegalMoves(game, 29);

        Move standardMove = Move.builder().startSquare(29).endSquare(21).build();
        Move enPassantCapture = Move.builder().startSquare(29).endSquare(22).type(MoveType.EN_PASSANT)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(30)
                        .build())
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalBlackMoves);

    }

    @Test
    public void testBlackEnPassantWithOtherCapture() {

        game.getBoard().setPiece(29, blackPawn);
        game.getBoard().setPiece(12, whitePawn);
        game.getBoard().setPiece(22, new Piece(Colour.WHITE, PieceType.ROOK));
        game.setTurn(Colour.WHITE);

        Move whiteDoubleMove = generator.generatePseudoLegalMoves(game, 12).stream()
                .filter(move -> move.getEndSquare() == 28)
                .findFirst().orElseThrow();
        game.handleMove(whiteDoubleMove);

        Set<Move> legalBlackMoves = generator.generatePseudoLegalMoves(game, 29);

        Move standardMove = Move.builder().startSquare(29).endSquare(21).build();
        Move standardCapture = Move.builder().startSquare(29).endSquare(22).build();
        Move enPassantCapture = Move.builder().startSquare(29).endSquare(20)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(28)
                        .build())
                .type(MoveType.EN_PASSANT)
                .build();

        Assertions.assertEquals(Set.of(standardMove, standardCapture, enPassantCapture), legalBlackMoves);

    }

    @Test
    public void testBlackDoubleEnPassantIsImpossible() {

        game.getBoard().setPiece(25, blackPawn);
        // we need another black piece to spend a move in between black's pawn moves
        game.getBoard().setPiece(63, new Piece(Colour.BLACK, PieceType.ROOK));
        // two black pawns on starting positions
        game.getBoard().setPiece(8, new Piece(Colour.WHITE, PieceType.PAWN));
        game.getBoard().setPiece(10, new Piece(Colour.WHITE, PieceType.PAWN));
        game.setTurn(Colour.WHITE);

        // first double pawn move from white
        Move whiteDoubleMove = generator.generatePseudoLegalMoves(game, 10).stream()
                .filter(move -> move.getEndSquare() == 26)
                .findFirst().orElseThrow();
        game.handleMove(whiteDoubleMove);

        Move blackRookMove = Move.builder().startSquare(63).endSquare(62).build();
        game.handleMove(blackRookMove);

        // second double pawn move from black, should make the first en-passant capture impossible
        whiteDoubleMove = generator.generatePseudoLegalMoves(game, 8).stream()
                .filter(move -> move.getEndSquare() == 24)
                .findFirst().orElseThrow();
        game.handleMove(whiteDoubleMove); // first double pawn move from black

        Set<Move> legalBlackMoves = generator.generatePseudoLegalMoves(game, 25);

        Move standardMove = Move.builder().startSquare(25).endSquare(17).build();
        Move enPassantCapture = Move.builder().startSquare(25).endSquare(16)
                .enPassantConfig(EnPassantConfig.builder()
                        .enPassantCapturedSquare(24)
                        .build())
                .type(MoveType.EN_PASSANT)
                .build();

        Assertions.assertEquals(Set.of(standardMove, enPassantCapture), legalBlackMoves);

    }

    @Test
    public void testEnPassantRemovesCapturedPawn() {

        Game game = new Game();
        game.handleMove(MoveUtils.fromNotation("e2", "e4"));
        game.handleMove(MoveUtils.fromNotation("g8", "f6"));
        game.handleMove(MoveUtils.fromNotation("e4", "e5"));
        game.handleMove(MoveUtils.fromNotation("d7", "d5"));
        //en passant
        game.handleMove(MoveUtils.fromNotation("e5", "d6"));

        Assertions.assertTrue(game.getBoard().pieceAt(MoveUtils.fromNotation("d5")).isEmpty());

    }

    @Test
    public void testWhiteStandardPromotion() {
        game.getBoard().setPiece(51, whitePawn);
        Set<Move> legalMoves = generator.generatePseudoLegalMoves(game, 51);
        Assertions.assertEquals(
                Set.of(Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build()),
                legalMoves
        );
    }

    @Test
    public void testBlackStandardPromotion() {
        game.getBoard().setPiece(8, blackPawn);
        Set<Move> legalMoves = generator.generatePseudoLegalMoves(game, 8);
        Assertions.assertEquals(
                Set.of(Move.builder().startSquare(8).endSquare(0)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(8).endSquare(0)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(8).endSquare(0)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(8).endSquare(0)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build()),
                legalMoves
        );
    }

    @Test
    public void testWhiteCapturePromotion() {
        game.getBoard().setPiece(51, whitePawn);
        game.getBoard().setPiece(58, new Piece(Colour.BLACK, PieceType.QUEEN));
        Set<Move> legalMoves = generator.generatePseudoLegalMoves(game, 51);
        Assertions.assertEquals(
                Set.of(Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(51).endSquare(59)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build(),
                        Move.builder().startSquare(51).endSquare(58)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(51).endSquare(58)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(51).endSquare(58)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(51).endSquare(58)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build()),
                legalMoves
        );
    }

    @Test
    public void testBlackCapturePromotion() {
        game.getBoard().setPiece(15, blackPawn);
        game.getBoard().setPiece(6, new Piece(Colour.WHITE, PieceType.BISHOP));
        Set<Move> legalMoves = generator.generatePseudoLegalMoves(game, 15);
        Assertions.assertEquals(
                Set.of(Move.builder().startSquare(15).endSquare(7)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(15).endSquare(7)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(15).endSquare(7)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(15).endSquare(7)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build(),
                        Move.builder().startSquare(15).endSquare(6)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.QUEEN).build()).build(),
                        Move.builder().startSquare(15).endSquare(6)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.ROOK).build()).build(),
                        Move.builder().startSquare(15).endSquare(6)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.BISHOP).build()).build(),
                        Move.builder().startSquare(15).endSquare(6)
                                .type(MoveType.PROMOTION)
                                .promotionConfig(PromotionConfig.builder().promotionPieceType(PieceType.KNIGHT).build()).build()),
                legalMoves
        );
    }

    private void assertLegalSquares(int startSquare, Piece pawn, Set<Integer> expectedLegalSquares) {
        game.getBoard().setPiece(startSquare, pawn);
        Set<Integer> legalSquares = generator.generatePseudoLegalMoves(game, startSquare).stream()
                .map(Move::getEndSquare)
                .collect(Collectors.toSet());
        Assertions.assertEquals(expectedLegalSquares, legalSquares);
        game.getBoard().unsetPiece(startSquare);
    }

}