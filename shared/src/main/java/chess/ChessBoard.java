package chess;

import java.util.Objects;

import static chess.ChessGame.TeamColor;
import static chess.ChessPiece.PieceType;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
        
    }
    /**
     * Returns string representation of the chess board. Creates an 8x8 grid with cells divided by newlines and '|' characters
     */
    public String toString() {
        String str = "";
        String pieceChar = "";
        for (int i = 8; i >= 1; i--) {
            for (int g = 1; g <= 8; g++) {
                str += "|";
                ChessPiece piece = getPiece(new ChessPosition(i, g));
                if (piece == null) {
                    pieceChar = " ";
                } else {
                    switch (piece.getPieceType()) {
                        case KING -> pieceChar = "K";
                        case QUEEN -> pieceChar = "Q";
                        case ROOK -> pieceChar = "R";
                        case BISHOP -> pieceChar = "B";
                        case KNIGHT -> pieceChar = "N";
                        case PAWN -> pieceChar = "P";
                    }
                    if (piece.getTeamColor() == TeamColor.BLACK) {
                        pieceChar = pieceChar.toLowerCase();
                    }
                }
                str += pieceChar + '|';
            }
            str = str + "\n";
        }
        return str;
    }
    /**
     * Determines if the given board is identical to itself
     * @param o   the reference object with which to compare.
     * @return true or false depending on answer
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() != getClass()) return false;
        ChessBoard move = (ChessBoard) o;
        return toString().equals(o.toString());
    }
    /**
     * Hashes string representation
     * @return hash of toString method
     */
    public int hashCode() {
        return Objects.hash(toString());
    }
    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getColumn() - 1][position.getRow() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getColumn() - 1][position.getRow() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board[0][0] = new ChessPiece(TeamColor.WHITE, PieceType.ROOK);
        board[1][0] = new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT);
        board[2][0] = new ChessPiece(TeamColor.WHITE, PieceType.BISHOP);
        board[3][0] = new ChessPiece(TeamColor.WHITE, PieceType.QUEEN);
        board[4][0] = new ChessPiece(TeamColor.WHITE, PieceType.KING);
        board[5][0] = new ChessPiece(TeamColor.WHITE, PieceType.BISHOP);
        board[6][0] = new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT);
        board[7][0] = new ChessPiece(TeamColor.WHITE, PieceType.ROOK);
        for (int i = 0; i < 8; i++) {
            board[i][1] = new ChessPiece(TeamColor.WHITE, PieceType.PAWN);
        }
        for (int i = 2; i < 6; i++) {
            for (int g = 0; g < 8; g++) {
                board[g][i] = null;
            }
        }
        for (int i = 0; i < 8; i++) {
            board[i][6] = new ChessPiece(TeamColor.BLACK, PieceType.PAWN);
        }
        board[0][7] = new ChessPiece(TeamColor.BLACK, PieceType.ROOK);
        board[1][7] = new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT);
        board[2][7] = new ChessPiece(TeamColor.BLACK, PieceType.BISHOP);
        board[3][7] = new ChessPiece(TeamColor.BLACK, PieceType.QUEEN);
        board[4][7] = new ChessPiece(TeamColor.BLACK, PieceType.KING);
        board[5][7] = new ChessPiece(TeamColor.BLACK, PieceType.BISHOP);
        board[6][7] = new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT);
        board[7][7] = new ChessPiece(TeamColor.BLACK, PieceType.ROOK);
    }
}
