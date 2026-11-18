package websocket.messages;

import records.GameDAO;

public class LoadGameMessage extends ServerMessage {

    private final GameDAO game;

    public LoadGameMessage(ServerMessageType messageType, GameDAO game) {
        super(messageType);
        this.game = game;
    }

    public GameDAO getGame() {
        return game;
    }
}
