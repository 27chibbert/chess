package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import records.GameDAO;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;


public class ConnectionManager {
    private record connectionType(Integer gameID, Boolean playing){};
    private ConcurrentHashMap<Session, connectionType> connections = new ConcurrentHashMap<>();
    private static Gson serializer = new Gson();
    private static final Logger LOGGER = Logger.getLogger("Server Log");

    public ConnectionManager() {
        try {
            FileHandler fd = new FileHandler("logs/serverWebSocket.log", true);
            fd.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fd);
            LOGGER.setUseParentHandlers(false);
        } catch (Exception e) {
            System.out.println("Server logger failed to start");
        }
    }

    public void add(Session session, Integer gameID, Boolean playing) {
        connections.put(session, new connectionType(gameID, playing));
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void notify(String notification, int gameID) {
        for (Map.Entry<Session, connectionType> entry : connections.entrySet()) {
            if (entry.getValue().gameID() == gameID) {
                try {
                    entry.getKey().getRemote().sendString(serializer.toJson(new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification)));
                } catch (IOException e) {
                    LOGGER.warning(String.format("Failed to send notification of game %d", gameID));
                }
            }
        }
    }

    public void publishGameChange(GameDAO game) {
        for (Map.Entry<Session, connectionType> entry : connections.entrySet()) {
            if (entry.getValue().gameID() == game.gameID()) {
                try {
                    entry.getKey().getRemote().sendString(serializer.toJson(new LoadGameMessage(game)));
                } catch (IOException e) {
                    LOGGER.warning(String.format("Failed to send load game message for game %d", game.gameID()));
                }
            }
        }
    }
}
