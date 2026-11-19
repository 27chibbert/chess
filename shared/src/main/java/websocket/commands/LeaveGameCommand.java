package websocket.commands;

import chess.ChessGame;

public class LeaveGameCommand extends UserGameCommand{
    private ChessGame.TeamColor color;

    public LeaveGameCommand(String authToken, int gameID, ChessGame.TeamColor color) {
        super(CommandType.LEAVE, authToken, gameID);
        this.color = color;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}
