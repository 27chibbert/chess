package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RulesEngine {

    public static boolean confirmCheck(ChessBoard board, ChessGame.TeamColor color) {
        ChessPiece piece;
        ChessPosition kingPosition = null;
        List<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            for (int g = 1; g <= 8; g++) {
                piece = board.getPiece(new ChessPosition(i, g));
                if (piece != null) {
                    if (piece.getTeamColor() != color) {
                        possibleMoves.addAll(piece.pieceMoves(board, new ChessPosition(i, g)));
                    } else {
                        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                            kingPosition = new ChessPosition(i, g);
                        }
                    }
                }
            }
        }
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
        ChessPosition kingPosition = getKingPosition(board, color);
        Collection<ChessMove> movesToCheck = board.getPiece(kingPosition).pieceMoves(board, kingPosition);
        for (ChessMove move : movesToCheck) {
            ChessBoard tempBoard = new ChessBoard(board);
            ChessPiece piece = tempBoard.getPiece(move.getStartPosition());
            tempBoard.addPiece(move.getEndPosition(), piece);
            tempBoard.addPiece(move.getStartPosition(), null);
            if (!confirmCheck(tempBoard, color)) {
                System.out.println(board.toString() + "Checkmate!");
                return false;
            }
        }
        System.out.println(board.toString() + "Not in Checkmate");
        return true;
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
}
