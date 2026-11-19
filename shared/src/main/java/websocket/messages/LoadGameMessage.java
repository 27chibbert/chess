package websocket.messages;

import records.GameDAO;

public class LoadGameMessage extends ServerMessage {

    private final GameDAO game;

    public LoadGameMessage(GameDAO game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameDAO getGame() {
        return game;
    }
}
