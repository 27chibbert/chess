package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionManager {
    private record connectionType(Integer gameID, Boolean playing){};
    private ConcurrentHashMap<Session, connectionType> connections = new ConcurrentHashMap<>();

    public void add(Session session, Integer gameID, Boolean playing) {
        connections.put(session, new connectionType(gameID, playing));
    }

    public void remove(Session session) {
        connections.remove(session);
    }
}
