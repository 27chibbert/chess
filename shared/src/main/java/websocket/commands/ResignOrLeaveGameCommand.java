package websocket.commands;

import chess.ChessGame;

public class ResignOrLeaveGameCommand extends UserGameCommand{
    private ChessGame.TeamColor color;

    public ResignOrLeaveGameCommand(CommandType type, String authToken, int gameID, ChessGame.TeamColor color) {
        super(type, authToken, gameID);
        this.color = color;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}
