package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import websocket.commands.ConnectCommand;
import websocket.commands.ResignOrLeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class WebSocketFacade extends Endpoint {
    ChessClient client;
    Session session;
    private static Gson serializer = new Gson();
    private static final Logger LOGGER = Logger.getLogger("Client Websocket Log");

    public WebSocketFacade(String url, ChessClient client) {
        this.client = client;
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new RuntimeException("Something broke. Implement this better later");
        }
        try {
            FileHandler fd = new FileHandler("logs/clientWebSocket.log", true);
            fd.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fd);
            LOGGER.setUseParentHandlers(false);
        } catch (Exception e) {
            System.out.println("Server logger failed to start");
        }
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    public void connectToGame(String authToken, int gameID, boolean playing) {
        try {
            ConnectCommand command = new ConnectCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, playing);
            this.session.getBasicRemote().sendText(serializer.toJson(command));
            LOGGER.info("Sent observe game ws request to server with id " + gameID + " and session " + authToken);
        } catch (Exception e) {
            throw new RuntimeException("Observe game websocket broke");
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove moveToPlay) {
        try {
            MakeMoveCommand command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, moveToPlay);
            String message = serializer.toJson(command);
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send make move command");
        }
    }

    public void resign(String authToken, int gameID, ChessGame.TeamColor color) {
        try {
            ResignOrLeaveGameCommand command = new ResignOrLeaveGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, color);
            String message = serializer.toJson(command);
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.warning(String.format("Failed to send resignation message: %s", e.getMessage()));
            throw new RuntimeException("Failed with IOException when attempting to make resignition command:" + e.getMessage());
        }
    }

    public void leaveGame(String authToken, int gameID, ChessGame.TeamColor color) {
        try {
            ResignOrLeaveGameCommand command = new ResignOrLeaveGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, color);
            String message = serializer.toJson(command);
            this.session.getBasicRemote().sendText(message);
            this.session.close();
        } catch (IOException e) {
            LOGGER.warning("Failed with IOException when attempting to make leaveGame command");
            throw new RuntimeException("Failed with IOException when attempting to make leaveGame command");
        }
    }

    private void handleMessage(String message) {
        ServerMessage msg = serializer.fromJson(message, ServerMessage.class);
        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {loadGame(message);}
            case ERROR -> {error(message);}
            case NOTIFICATION -> {notify(message);}
        }
    }

    private void notify(String notification) {
        NotificationMessage msg = serializer.fromJson(notification, NotificationMessage.class);
        client.notify(msg.getNotification());
        LOGGER.info("Receieved notification message");
    }
    private void loadGame(String message) {
        LoadGameMessage msg = serializer.fromJson(message, LoadGameMessage.class);
        client.setLiveGame(msg.getGame());
        LOGGER.info("Received load game message");
    }
    private void error(String message) {
        ErrorMessage msg = serializer.fromJson(message, ErrorMessage.class);
        client.notify(String.format("Error: %s", msg.getError()));
    }

}
