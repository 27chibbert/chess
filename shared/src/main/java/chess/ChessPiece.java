package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private ChessPiece.PieceType piece;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        piece = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return piece;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        switch (piece) {
            case KING -> {
                kingMoves(board, myPosition, moves);
            }
            case QUEEN -> {
                queenMoves(board, myPosition, moves);
            }
            case ROOK -> {
                rookMoves(board, myPosition, moves);
            }
            case BISHOP -> {
                bishopMoves(board, myPosition, moves);
            }
            case KNIGHT -> {
                knightMoves(board, myPosition, moves);
            }
            case PAWN -> {
                pawnMoves(board, myPosition, moves);
            }
            default -> {
                return moves;
            }
        }
        return moves;
    }
    private void kingMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void queenMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void rookMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void bishopMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void knightMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void pawnMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
}
