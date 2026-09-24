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
            if (!(piece.getPieceType() == ChessPiece.PieceType.KING && move.getStartPosition().getFile() == 5 && (move.getEndPosition().getFile() == 3 || move.getEndPosition().getFile() == 7))) {
                tempBoard.executeMove(move);
            }
            if (!confirmCheck(tempBoard, color)) {
                //System.out.println(board.toString() + " Not in checkmate");
                return false;
            }
        }
        //System.out.println(board.toString() + "Checkmate!");
        return true;
    }

    public static boolean confirmStalemate(ChessBoard board, ChessGame.TeamColor color, GameFlags flags) {
        return filterValidMoves(getAllMoves(board, color), board, flags).isEmpty();
    }

    public static Collection<ChessMove> filterValidMoves(Collection<ChessMove> moves, ChessBoard board, GameFlags flags) {
        List<ChessMove> validMoves = new ArrayList<>();
        //System.out.println(board.toString());
        for (ChessMove move: moves) {
            ChessBoard tempBoard = new ChessBoard(board);
            ChessPiece piece = tempBoard.getPiece(move.getStartPosition());
            ChessGame.TeamColor color = piece.getTeamColor();
            tempBoard.executeMove(move);
            if (!confirmCheck(tempBoard, color)) {
                if (piece.getPieceType() == ChessPiece.PieceType.KING && move.getStartPosition().getFile() == 5 && (move.getEndPosition().getFile() == 3 || move.getEndPosition().getFile() == 7)) {
                    if (castlingChecks(move, board, color, flags)) {
                        validMoves.add(move);
                    }
                } else if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getStartPosition().getFile() != move.getEndPosition().getFile() && board.getPiece(move.getEndPosition()) == null) {
                    if (flags.getEnPassantCapturable() != null) {
                        //System.out.println("Checked enpassant move " + move.toString() + " with capturable " + flags.getEnPassantCapturable().toString());
                        if (board.getPiece(flags.getEnPassantCapturable()) != null) {
                            if (board.getPiece(flags.getEnPassantCapturable()).getTeamColor() != color) {
                                //System.out.println("Added en passant move " + move.toString());
                                validMoves.add(move);
                            }
                        }
                    }
                } else {
                    validMoves.add(move);
                }
                //System.out.println(move.toString());
            }
        }
        return validMoves;
    }

    private static boolean castlingChecks(ChessMove move, ChessBoard board, ChessGame.TeamColor color, GameFlags flags) {
        if (color == ChessGame.TeamColor.WHITE && !confirmCheck(board, color)) {
            if (move.getEndPosition().getFile() == 3 ) {
                if (flags.getFlag(GameFlags.GameFlag.WhiteA) && board.getPiece(new ChessPosition(1,3)) == null && board.getPiece(new ChessPosition(1,4)) == null){
                    ChessBoard tempBoard = new ChessBoard(board);
                    tempBoard.executeMove(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 4), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    tempBoard.executeMove(new ChessMove(new ChessPosition(1, 4), new ChessPosition(1, 3), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    return true;
                }
            } else if (move.getEndPosition().getFile() == 7) {
                if (flags.getFlag(GameFlags.GameFlag.WhiteH) && board.getPiece(new ChessPosition(1,6)) == null && board.getPiece(new ChessPosition(1,7)) == null){
                    ChessBoard tempBoard = new ChessBoard(board);
                    tempBoard.executeMove(new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 6), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    tempBoard.executeMove(new ChessMove(new ChessPosition(1, 6), new ChessPosition(1, 7), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    return true;
                }
            }
        } else if (color == ChessGame.TeamColor.BLACK && !confirmCheck(board, color)) {
            if (move.getEndPosition().getFile() == 3) {
                if (flags.getFlag(GameFlags.GameFlag.BlackA) && board.getPiece(new ChessPosition(8,3)) == null && board.getPiece(new ChessPosition(8,4)) == null){
                    ChessBoard tempBoard = new ChessBoard(board);
                    tempBoard.executeMove(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 4), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    tempBoard.executeMove(new ChessMove(new ChessPosition(8, 4), new ChessPosition(8, 3), null));
                    if (confirmCheck(tempBoard, color)) return false;
                    return true;
                }
            } else if (move.getEndPosition().getFile() == 7) {
                if (flags.getFlag(GameFlags.GameFlag.BlackH) && board.getPiece(new ChessPosition(8,6)) == null && board.getPiece(new ChessPosition(8,7)) == null){
                    ChessBoard tempBoard = new ChessBoard(board);
                    //System.out.println("Arrived here!");
                    tempBoard.executeMove(new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 6), null));
                    if (confirmCheck(tempBoard, color)) {
                        return false;
                    }
                    tempBoard.executeMove(new ChessMove(new ChessPosition(8, 6), new ChessPosition(8, 7), null));
                    if (confirmCheck(tempBoard, color)) {
                        return false;
                    }
                    //System.out.println("Arrived here too!");
                    return true;
                }
            }
        }
        return false;
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
