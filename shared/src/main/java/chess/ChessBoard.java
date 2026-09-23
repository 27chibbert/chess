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

    public ChessBoard(ChessBoard copy) {
        for (int i = 1; i <= 8; i++) {
            for (int g = 1; g <= 8; g++) {
                if (copy.getPiece(new ChessPosition(i, g)) == null) {
                    board[i-1][g-1] = null;
                } else {
                    board[i - 1][g - 1] = new ChessPiece(copy.getPiece(new ChessPosition(i, g)).getTeamColor(), copy.getPiece(new ChessPosition(i, g)).getPieceType());
                }
            }
        }
    }
    /**
     * Returns string representation of the chess board. Creates an 8x8 grid with cells divided by newlines and '|' characters
     */
    public String toString() {
        String str = "";
        for (int i = 8; i >= 1; i--) {
            str += "|";
            for (int g = 1; g <= 8; g++) {
                String addition = "";
                if (getPiece(new ChessPosition(g, i)) == null) {
                    str += " ";
                } else {
                    ChessPiece.PieceType piece = getPiece(new ChessPosition(g, i)).getPieceType();
                    ChessGame.TeamColor color = getPiece(new ChessPosition(g, i)).getTeamColor();
                    switch (piece) {
                        case PAWN -> addition = "P";
                        case KNIGHT -> addition = "N";
                        case BISHOP -> addition = "B";
                        case ROOK -> addition = "R";
                        case QUEEN -> addition = "Q";
                        case KING -> addition = "K";
                    }
                    if (color == ChessGame.TeamColor.BLACK) {
                        addition = addition.toLowerCase();
                    }
                }
                str += addition + "|";
            }
            str += "\n";
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
        ChessBoard obj = (ChessBoard) o;
        return toString().equals(obj.toString());
    }
    /**
     * Hashes string representation
     * @return hash of toString method
     */
    public int hashCode() {
        return Objects.hash(toString());
    }

    public void executeMove(ChessMove move) {
        ChessPiece piece = getPiece(move.getStartPosition());
        clearPosition(move.getStartPosition());
        if (move.getPromotionPiece() == null) {
            addPiece(move.getEndPosition(), piece);
        } else {
            addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRank() - 1][position.getFile() - 1] = piece;
    }

    private void clearPosition(ChessPosition position) {
        board[position.getRank() - 1][position.getFile() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRank() - 1][position.getFile() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        board[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        board[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }
        for (int g = 2; g < 6; g++) {
            for (int i = 0; i < 8; i++) {
                board[g][i] = null;
            }
        }
        for (int i = 0; i < 8; i++) {
            board[6][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        board[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        board[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        board[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        //System.out.println(toString());
    }
}
