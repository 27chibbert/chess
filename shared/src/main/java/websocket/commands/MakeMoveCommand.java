package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private ChessMove move;

    public MakeMoveCommand(CommandType command, String authToken, int gameID, ChessMove move) {
        super(command, authToken, gameID);
        this.move = move;
    }

    public ChessMove getChessMove() {
        return move;
    }
}
