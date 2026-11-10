package client;


public class ClientMain {
    public static void main(String[] args) {
        ChessClient client = new ChessClient();
        client.startClient();
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);
    }
}
