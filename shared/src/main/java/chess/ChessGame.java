package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor turn;
    private ChessBoard board;
    private GameFlags flags;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
        flags = new GameFlags();
    }

    @Override
    public String toString() {
        String str = board.toString();
        str += "\n";
        switch (turn) {
            case WHITE -> str += "White's";
            case BLACK -> str += "Black's";
        }
        str += " turn";
        return str;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        ChessGame obj = (ChessGame) o;
        return toString().equals(obj.toString());
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        moves = RulesEngine.filterValidMoves(moves, board, flags);
        return moves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) throw new InvalidMoveException("Attempted a move beginning on an empty space");
        if (board.getPiece(move.getStartPosition()).getTeamColor() != turn) throw new InvalidMoveException("Attempted to move opposite colored piece");
        if (!RulesEngine.filterValidMoves(piece.pieceMoves(board, move.getStartPosition()), board, flags).contains(move)) throw new InvalidMoveException("Attempted illegal move");
        board.executeMove(move);
        if (move.getPromotionPiece() == ChessPiece.PieceType.KING) {
            if (turn == TeamColor.WHITE) {
                if (move.getEndPosition().getFile() == 7) {
                    board.executeMove(new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6), null));
                } else if (move.getEndPosition().getFile() == 3) {
                    board.executeMove(new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4), null));
                }
            } else {
                if (move.getEndPosition().getFile() == 7) {
                    board.executeMove(new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6), null));
                } else if (move.getEndPosition().getFile() == 3) {
                    board.executeMove(new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4), null));
                }
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (turn == TeamColor.WHITE) {
                flags.dropFlag(GameFlags.GameFlag.WhiteA);
                flags.dropFlag(GameFlags.GameFlag.WhiteH);
            } else {
                flags.dropFlag(GameFlags.GameFlag.BlackA);
                flags.dropFlag(GameFlags.GameFlag.BlackH);
            }
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (move.getStartPosition().getFile() == 1) {
                if (turn == TeamColor.WHITE) {
                    flags.dropFlag(GameFlags.GameFlag.WhiteA);
                } else {
                    flags.dropFlag(GameFlags.GameFlag.BlackA);
                }
            }
            if (move.getStartPosition().getFile() == 8) {
                if (turn == TeamColor.WHITE) {
                    flags.dropFlag(GameFlags.GameFlag.WhiteH);
                } else {
                    flags.dropFlag(GameFlags.GameFlag.BlackH);
                }
            }
        }
        if (turn == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        if (RulesEngine.confirmCheck(board, teamColor)) {
            return true;
        } else {
            return false;
        }
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (RulesEngine.confirmCheckmate(board, teamColor)) {
            return true;
        } else {
            return false;
        }
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;
        return RulesEngine.confirmStalemate(board, teamColor, flags);
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
