package chess;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RulesEngine {

    public static boolean confirmCheck(ChessBoard board, ChessGame.TeamColor color) {
        ChessPiece piece;
        ChessPosition kingPosition = getKingPosition(board, color);
        ChessGame.TeamColor opColor = ChessGame.TeamColor.WHITE;
        if (color == ChessGame.TeamColor.WHITE) opColor = ChessGame.TeamColor.BLACK;
        Collection<ChessMove> possibleMoves = getAllMoves(board, opColor);
        //System.out.println(board.toString());
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().equals(kingPosition)) {
                //System.out.println("In check!");
                return true;
            }
        }
        //System.out.println("Not in Check");
        return false;
    }

    public static boolean confirmCheckmate(ChessBoard board, ChessGame.TeamColor color) {
        Collection<ChessMove> movesToCheck = getAllMoves(board, color);
        for (ChessMove move : movesToCheck) {
            ChessBoard tempBoard = new ChessBoard(board);
            ChessPiece piece = tempBoard.getPiece(move.getStartPosition());
            tempBoard.addPiece(move.getEndPosition(), piece);
            tempBoard.clearPosition(move.getStartPosition());
            if (!confirmCheck(tempBoard, color)) {
                //System.out.println(board.toString() + " Not in checkmate");
                return false;
            }
        }
        //System.out.println(board.toString() + "Checkmate!");
        return true;
    }

    public static Collection<ChessMove> filterValidMoves(Collection<ChessMove> moves, ChessBoard board) {
        List<ChessMove> validMoves = new ArrayList<>();
        //System.out.println(board.toString());
        for (ChessMove move: moves) {
            ChessBoard tempBoard = new ChessBoard(board);
            ChessPiece piece = tempBoard.getPiece(move.getStartPosition());
            ChessGame.TeamColor color = piece.getTeamColor();
            tempBoard.addPiece(move.getEndPosition(), piece);
            tempBoard.clearPosition(move.getStartPosition());
            if (!confirmCheck(tempBoard, color)) {
                validMoves.add(move);
                //System.out.println(move.toString());
            }
        }
        return validMoves;
    }

    private static ChessPosition getKingPosition(ChessBoard board, ChessGame.TeamColor color) {
        ChessPiece piece = null;
        for (int i = 1; i <= 8; i++) {
            for (int g = 1; g <= 8; g++) {
                piece = board.getPiece(new ChessPosition(i, g));
                if (piece != null) {
                    if (piece.getTeamColor() == color && piece.getPieceType() == ChessPiece.PieceType.KING) {
                        return new ChessPosition(i, g);
                    }
                }
            }
        }
        return null;
    }

    private static Collection<ChessMove> getAllMoves(ChessBoard board, ChessGame.TeamColor color) {
        ChessPiece piece = null;
        List<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            for (int g = 1; g <= 8; g++) {
                piece = board.getPiece(new ChessPosition(i, g));
                if (piece != null) {
                    if (piece.getTeamColor() == color) {
                        possibleMoves.addAll(piece.pieceMoves(board, new ChessPosition(i, g)));
                    }
                }
            }
        }
        return possibleMoves;
    }
}
