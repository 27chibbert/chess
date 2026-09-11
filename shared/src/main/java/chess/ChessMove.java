package chess;


import java.util.Objects;
/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition start;
    private ChessPosition end;
    private ChessPiece.PieceType promotion;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        start = startPosition;
        end = endPosition;
        promotion = promotionPiece;
    }
    /**
     * Determines if the given object is identical to itself
     * @param o   the reference object with which to compare.
     * @return true or false depending on answer
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() != getClass()) return false;
        ChessMove move = (ChessMove) o;
        return toString().equals(o.toString());
    }
    /**
     * Hashes string representation
     * @return hash of toString method
     */
    @Override
    public int hashCode() {
        //System.out.println(Objects.hash(start, end));
        return Objects.hash(toString());
    }
    /**
     * Returns string representation as (#,#)->(#,#) with optional |[piece] if promotion is done
     * <p>
     * Examples: (1,2)->(1,4) , (2,7)->(2,8)|[White Queen]
     */
    @Override
    public String toString() {
        if (promotion != null) {
            return start.toString() + "->" + end.toString() + "|" + promotion.toString();
        } else {
            return start.toString() + "->" + end.toString();
        }
    }
    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotion;
    }
}
