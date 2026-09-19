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
        switch (getTeamColor()) {
            case WHITE -> str += "White ";
            case BLACK -> str += "Black ";
            default -> str += "Neutral ";
        }
        switch (getPieceType()) {
            case PAWN -> str += "Pawn]";
            case KNIGHT -> str += "Knight]";
            case BISHOP -> str += "Bishop]";
            case ROOK -> str += "Rook]";
            case QUEEN -> str += "Queen]";
            case KING -> str += "King]";
            default -> str += "Mystery]";
        }
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
        ChessPiece obj = (ChessPiece) o;
        return toString().equals(obj.toString());
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
        switch (getPieceType()) {
            case PAWN -> pawnMoves(board, myPosition, moves);
            case KNIGHT -> knightMoves(board, myPosition, moves);
            case BISHOP -> bishopMoves(board, myPosition, moves);
            case ROOK -> rookMoves(board, myPosition, moves);
            case QUEEN -> queenMoves(board, myPosition, moves);
            case KING -> kingMoves(board, myPosition, moves);
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
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        while (check < 8) {
            switch (check) {
                case 0 -> {
                    file++;
                    rank++;
                }
                case 1 -> {
                    file++;
                    rank--;
                }
                case 2 -> {
                    file--;
                    rank--;
                }
                case 3 -> {
                    file--;
                    rank++;
                }
                case 4 -> {
                    file++;
                }
                case 5 -> {
                    rank--;
                }
                case 6 -> {
                    file--;
                }
                case 7 -> {
                    rank++;
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    moves.add(newMove);
                } else if (space.getTeamColor() != getTeamColor()) {
                    moves.add(newMove);
                }
            }
            check++;
            file = startingFile;
            rank = startingRank;
        }
    }
    /**
     * Fills List object with possible moves for a queen
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void queenMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        boolean flag = false;
        while (check < 8) {
            switch (check) {
                case 0 -> {
                    file++;
                    rank++;
                }
                case 1 -> {
                    file++;
                    rank--;
                }
                case 2 -> {
                    file--;
                    rank--;
                }
                case 3 -> {
                    file--;
                    rank++;
                }
                case 4 -> {
                    file++;
                }
                case 5 -> {
                    rank--;
                }
                case 6 -> {
                    file--;
                }
                case 7 -> {
                    rank++;
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    moves.add(newMove);
                } else if (space.getTeamColor() != getTeamColor()) {
                    moves.add(newMove);
                    flag = true;
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }
            if (flag) {
                flag = false;
                check++;
                file = startingFile;
                rank = startingRank;
            }
        }
    }
    /**
     * Fills List object with possible moves for a rook
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void rookMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        boolean flag = false;
        while (check < 4) {
            switch (check) {
                case 0 -> {
                    file++;
                }
                case 1 -> {
                    rank--;
                }
                case 2 -> {
                    file--;
                }
                case 3 -> {
                    rank++;
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    moves.add(newMove);
                } else if (space.getTeamColor() != getTeamColor()) {
                    moves.add(newMove);
                    flag = true;
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }
            if (flag) {
                flag = false;
                check++;
                file = startingFile;
                rank = startingRank;
            }
        }
    }
    /**
     * Fills List object with possible moves for a bishop
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void bishopMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        boolean flag = false;
        while (check < 4) {
            switch (check) {
                case 0 -> {
                    file++;
                    rank++;
                }
                case 1 -> {
                    file++;
                    rank--;
                }
                case 2 -> {
                    file--;
                    rank--;
                }
                case 3 -> {
                    file--;
                    rank++;
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    moves.add(newMove);
                } else if (space.getTeamColor() != getTeamColor()) {
                    moves.add(newMove);
                    flag = true;
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }
            if (flag) {
                flag = false;
                check++;
                file = startingFile;
                rank = startingRank;
            }
        }
    }
    /**
     * Fills List object with possible moves for a knight
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void knightMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        while (check < 8) {
            switch (check) {
                case 0 -> {
                    file++;
                    rank += 2;
                }
                case 1 -> {
                    file++;
                    rank -= 2;
                }
                case 2 -> {
                    file--;
                    rank += 2;
                }
                case 3 -> {
                    file--;
                    rank -= 2;
                }
                case 4 -> {
                    rank++;
                    file += 2;
                }
                case 5 -> {
                    rank++;
                    file -= 2;
                }
                case 6 -> {
                    rank--;
                    file += 2;
                }
                case 7 -> {
                    rank--;
                    file -= 2;
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    moves.add(newMove);
                } else if (space.getTeamColor() != getTeamColor()) {
                    moves.add(newMove);
                }
            }
            check++;
            file = startingFile;
            rank = startingRank;
        }
    }
    /**
     * Fills List object with possible moves for a pawn
     * @param board ChessBoard object of current gameboard configuration
     * @param myPosition ChessPosition object of the current position of the piece
     * @param moves The List object that discovered moves will be added to
     */
    private void pawnMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves) {
        int startingRank = myPosition.getColumn();
        int startingFile = myPosition.getRow();
        int file = startingFile;
        int rank = startingRank;
        int check = 0;
        while (check < 4) {
            if (getTeamColor() == ChessGame.TeamColor.WHITE) {
                switch (check) {
                    case 0 -> rank++;
                    case 1 -> rank += 2;
                    case 2 -> {
                        rank++;
                        file++;
                    }
                    case 3 -> {
                        rank++;
                        file--;
                    }
                }
            } else {
                switch (check) {
                    case 0 -> rank--;
                    case 1 -> rank -= 2;
                    case 2 -> {
                        rank--;
                        file++;
                    }
                    case 3 -> {
                        rank--;
                        file--;
                    }
                }
            }
            if (file <= 8 && file >= 1 && rank >= 1 && rank <= 8) {
                ChessPiece space = board.getPiece(new ChessPosition(rank, file));
                ChessMove newMove = new ChessMove(myPosition, new ChessPosition(rank, file), null);
                if (space == null) {
                    if (check == 0) {
                        if (rank == 1 || rank == 8) {
                            moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.ROOK));
                        } else {
                            moves.add(newMove);
                        }
                    } else if (check == 1) {
                        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
                            if (startingRank == 2 && board.getPiece(new ChessPosition(rank - 1, file)) == null) {
                                moves.add(newMove);
                            }
                        } else {
                            if (startingRank == 7 && board.getPiece(new ChessPosition(rank + 1, file)) == null) {
                                moves.add(newMove);
                            }
                        }
                    }
                } else if (space.getTeamColor() != getTeamColor() && (check == 2 || check == 3)) {
                    if (rank == 1 || rank == 8) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(rank, file), PieceType.ROOK));
                    } else {
                        moves.add(newMove);
                    }
                }
            }
            check++;
            file = startingFile;
            rank = startingRank;
        }
    }
}
