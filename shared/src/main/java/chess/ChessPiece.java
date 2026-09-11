package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static chess.ChessGame.TeamColor.WHITE;

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
    /**
     * Returns string representation as [color piece]
     * <p>
     * Examples: [Black King], [White Rook]
     */
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

    /**
     * Hashes string representation
     * @return hash of toString method
     */
    @Override
    public int hashCode() {
        return Objects.hash(toString());
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
        ChessPiece move = (ChessPiece) o;
        return toString().equals(o.toString());
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

    /**
     * Fills List object with possible moves for a king
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void kingMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        while (check != 8) {
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
                case 4 -> {
                    row++;
                }
                case 5 -> {
                    col--;
                }
                case 6 -> {
                    row--;
                }
                case 7 -> {
                    col++;
                }
            }
            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                if (chkSpace == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                } else if (chkSpace.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
    /**
     * Fills List object with possible moves for a queen
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void queenMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        // the check variable determines what direction the next space to check will be in. 0 is Up-right, and clockwise from there
        while (check != 8) {
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
                case 4 -> {
                    row++;
                }
                case 5 -> {
                    col--;
                }
                case 6 -> {
                    row--;
                }
                case 7 -> {
                    col++;
                }
            }
            // while loop breaks when row or column escapes the board.
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                if (chkSpace == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    //System.out.println("Added " + new ChessMove(myPosition, new ChessPosition(row, col), null).toString());
                } else if (chkSpace.getTeamColor() != this.color) {
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
                    case 4 -> {
                        row++;
                    }
                    case 5 -> {
                        col--;
                    }
                    case 6 -> {
                        row--;
                    }
                    case 7 -> {
                        col++;
                    }
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
    /**
     * Fills List object with possible moves for a rook
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void rookMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        // the check variable determines what direction the next space to check will be in. 0 is right, and clockwise from there
        while (check != 4) {
            switch (check) {
                case 0 -> {
                    row++;
                }
                case 1 -> {
                    col--;
                }
                case 2 -> {
                    row--;
                }
                case 3 -> {
                    col++;
                }
            }
            // while loop breaks when row or column escapes the board.
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
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
                    }
                    case 1 -> {
                        col--;
                    }
                    case 2 -> {
                        row--;
                    }
                    case 3 -> {
                        col++;
                    }
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
    /**
     * Fills List object with possible moves for a bishop
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void bishopMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        // the check variable determines what direction the next space to check will be in. 0 is Up-right, and clockwise from there
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
            // while loop breaks when row or column escapes the board.
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                if (chkSpace == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                    //System.out.println("Added " + new ChessMove(myPosition, new ChessPosition(row, col), null).toString());
                } else if (chkSpace.getTeamColor() != this.color) {
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
    /**
     * Fills List object with possible moves for a knight
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void knightMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        while (check != 8) {
            switch (check) {
                case 0 -> {
                    row += 2;
                    col++;
                }
                case 1 -> {
                    row += 2;
                    col--;
                }
                case 2 -> {
                    row -= 2;
                    col++;
                }
                case 3 -> {
                    row -= 2;
                    col--;
                }
                case 4 -> {
                    row++;
                    col += 2;
                }
                case 5 -> {
                    row--;
                    col += 2;
                }
                case 6 -> {
                    row++;
                    col -= 2;
                }
                case 7 -> {
                    row--;
                    col -= 2;
                }
            }
            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                if (chkSpace == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                } else if (chkSpace.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
    /**
     * Fills List object with possible moves for a pawn
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void pawnMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRow = myPosition.getRow();
        int startingCol = myPosition.getColumn();
        int row = startingRow;
        int col = startingCol;
        int check = 0;
        while (check != 4) {
            if (color == WHITE) {
                switch (check) {
                    case 0 -> {
                        row++;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace == null) {
                                if (row == 8) {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                } else {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                }
                            }
                        }
                    }
                    case 1 -> {
                        if (startingRow == 2) {
                            row++;
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace == null) {
                                row++;
                                chkSpace = board.getPiece(new ChessPosition(row, col));
                                if (chkSpace == null) {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                }
                            }
                        }
                    }
                    case 2 -> {
                        row++;
                        col--;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace != null) {
                                if (chkSpace.getTeamColor() != color) {
                                    if (row == 8) {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                    } else {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                    }
                                }
                            }
                        }
                    }
                    case 3 -> {
                        row++;
                        col++;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace != null) {
                                if (chkSpace.getTeamColor() != color) {
                                    if (row == 8) {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                    } else {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                switch (check) {
                    case 0 -> {
                        row--;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace == null) {
                                if (row == 1) {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                } else {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                }
                            }
                        }
                    }
                    case 1 -> {
                        if (startingRow == 7) {
                            row--;
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace == null) {
                                row--;
                                chkSpace = board.getPiece(new ChessPosition(row, col));
                                if (chkSpace == null) {
                                    moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                }
                            }
                        }
                    }
                    case 2 -> {
                        row--;
                        col--;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace != null) {
                                if (chkSpace.getTeamColor() != color) {
                                    if (row == 1) {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                    } else {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                    }
                                }
                            }
                        }
                    }
                    case 3 -> {
                        row--;
                        col++;
                        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                            ChessPiece chkSpace = board.getPiece(new ChessPosition(row, col));
                            if (chkSpace != null) {
                                if (chkSpace.getTeamColor() != color) {
                                    if (row == 1) {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.QUEEN));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.ROOK));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.BISHOP));
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), PieceType.KNIGHT));
                                    } else {
                                        moves.add(new ChessMove(myPosition, new ChessPosition(row, col), null));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            check++;
            row = startingRow;
            col = startingCol;
        }
    }
}
