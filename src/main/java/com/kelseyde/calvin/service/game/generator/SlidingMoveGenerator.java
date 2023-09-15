package com.kelseyde.calvin.service.game.generator;

import com.kelseyde.calvin.model.Board;
import com.kelseyde.calvin.model.Piece;
import com.kelseyde.calvin.model.Game;
import com.kelseyde.calvin.model.move.Move;
import com.kelseyde.calvin.utils.BoardUtils;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SlidingMoveGenerator implements PseudoLegalMoveGenerator {

    // All the possible move 'vectors' for a sliding piece, i.e., the offsets for the directions in which a sliding
    // piece is permitted to move. Bishops will use only the diagonal vectors, rooks only the orthogonal vectors, while
    // queens will use both.
    protected static final Set<Integer> DIAGONAL_MOVE_VECTORS = Set.of(-9, -7, 7, 9);
    protected static final Set<Integer> ORTHOGONAL_MOVE_VECTORS = Set.of(-8, -1, 1, 8);

    // The following sets are exceptions to the initial rule, in scenarios where the sliding piece is placed on the a or h-files.
    // These exceptions prevent the piece from 'wrapping' around to the other side of the board.
    private static final Set<Integer> A_FILE_OFFSET_EXCEPTIONS = Set.of(-9, -1, 7);
    private static final Set<Integer> H_FILE_OFFSET_EXCEPTIONS = Set.of(-7, 1, 9);

    protected abstract Set<Integer> getMoveVectors();

    protected abstract void applyPostGenerationConfig(Piece piece, Set<Move> legalMoves);

    @Override
    public Set<Move> generatePseudoLegalMoves(Game game, int startSquare) {

        Board board = game.getBoard();
        Piece piece = board.getPieceAt(startSquare)
                .filter(p -> p.getType().equals(getPieceType()))
                .orElseThrow(() -> new NoSuchElementException(String.format("There is no %s on square %s!", getPieceType(), startSquare)));

        Set<Move> legalMoves = getMoveVectors().stream()
                .flatMap(vectorOffset -> generatePseudoLegalMovesForVector(board, piece, startSquare, vectorOffset).stream())
                .collect(Collectors.toSet());

        applyPostGenerationConfig(piece, legalMoves);

        return legalMoves;

    }

    private Set<Move> generatePseudoLegalMovesForVector(Board board, Piece piece, int startSquare, int vectorOffset) {

        Set<Move> pseudoLegalMoves = new HashSet<>();
        int targetSquare = startSquare;

        // Limiting the vector iterations will prevent an orthogonal vector from sliding to the other side of the board
        // (For example, a rook moving from a8 to b1).
        int vectorIterations = 0;
        int maxVectorIterations = 7;

        boolean endVector = false;
        while (!endVector) {
            if (BoardUtils.isAFile(targetSquare) && A_FILE_OFFSET_EXCEPTIONS.contains(vectorOffset) ||
                BoardUtils.isHFile(targetSquare) && H_FILE_OFFSET_EXCEPTIONS.contains(vectorOffset)) {
                endVector = true;
                continue;
            }
            targetSquare = targetSquare + vectorOffset;
            vectorIterations++;
            if (vectorIterations > maxVectorIterations) {
                endVector = true;
                continue;
            }
            if (!BoardUtils.isValidSquareCoordinate(targetSquare)) {
                endVector = true;
                continue;
            }
            Optional<Piece> pieceOnTargetSquare = board.getPieceAt(targetSquare);
            if (pieceOnTargetSquare.isEmpty()) {
                pseudoLegalMoves.add(moveBuilder()
                        .startSquare(startSquare)
                        .endSquare(targetSquare)
                        .build());
            }
            else if (pieceOnTargetSquare.get().getColour().isOppositeColour(piece.getColour())) {
                pseudoLegalMoves.add(moveBuilder()
                        .startSquare(startSquare)
                        .endSquare(targetSquare)
                        .isCapture(true)
                        .build());
                endVector = true;
            }
            else if (pieceOnTargetSquare.get().getColour().isSameColour(piece.getColour())) {
                endVector = true;
            }
        }
        return pseudoLegalMoves;
    }

}
