package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType piece;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        piece = type;
    }

    @Override
    public String toString() {
        String str = "[";
        switch (color) {
            case BLACK -> str += "Black";
            case WHITE -> str += "White";
            default -> str = str + " ";
        }
        str += " ";
        switch (piece) {
            case KING -> str += "King";
            case QUEEN -> str += "Queen";
            case ROOK -> str += "Rook";
            case BISHOP -> str += "Bishop";
            case KNIGHT -> str += "Knight";
            case PAWN -> str += "Pawn";
            default -> str += " ";
        }
        str += "]";
        return str;
    }
    public int hashCode() {
        return Objects.hash(toString());
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
            case KING -> kingMoves(board, myPosition, moves);
            case QUEEN -> queenMoves(board, myPosition, moves);
            case ROOK -> rookMoves(board, myPosition, moves);
            case BISHOP -> bishopMoves(board, myPosition, moves);
            case KNIGHT -> knightMoves(board, myPosition, moves);
            case PAWN -> pawnMoves(board, myPosition, moves);
            default -> {return moves;}
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
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        while (check != 4) {
            switch (check) {
                case 0 -> {
                    row++;
                    col++;
                }
                case 1 -> {
                    row--;
                    col++;
                }
                case 2 -> {
                    row--;
                    col--;
                }
                case 3 -> {
                    row++;
                    col--;
                }
            }
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                //System.out.println("Checking position " + row + " " + col);
                var chkSpace = board.getPiece(new ChessPosition(row, col));
                if (chkSpace == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    //System.out.println("Added " + new ChessMove(myPosition, new ChessPosition(row, col), null).toString());
                } else if (chkSpace.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    break;
                } else {
                    break;
                }
                switch (check) {
                    case 0 -> {
                        row++;
                        col++;
                    }
                    case 1 -> {
                        row--;
                        col++;
                    }
                    case 2 -> {
                        row--;
                        col--;
                    }
                    case 3 -> {
                        row++;
                        col--;
                    }
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
    private void knightMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
    private void pawnMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {

    }
}
