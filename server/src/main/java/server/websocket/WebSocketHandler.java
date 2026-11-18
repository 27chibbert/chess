package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseException;
import dataaccess.InvalidLoginException;
import service.ServiceProvider;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import java.io.IOException;

import static websocket.messages.ServerMessage.ServerMessageType.*;

public class WebSocketHandler implements WsConnectHandler, WsCloseHandler, WsMessageHandler {

    private ServiceProvider server;
    private static Gson serializer = new Gson();
    private ConnectionManager connectionManager = new ConnectionManager();
    private static final Logger LOGGER = Logger.getLogger("Server Log");

    public WebSocketHandler(ServiceProvider server) {
        this.server = server;

        try {
            FileHandler fd = new FileHandler("logs/serverWebSocket.log", true);
            fd.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fd);
            LOGGER.setUseParentHandlers(false);
        } catch (Exception e) {
            System.out.println("Server logger failed to start");
        }
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        connectionManager.add(ctx.session, null, null);
        //LOGGER.info("Started Websocket connection with session "  + ctx.header("authorization"));
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        UserGameCommand msg = serializer.fromJson(ctx.message(), UserGameCommand.class);
        switch (msg.getCommandType()) {
            case CONNECT -> {
                try {
                    messageConnect(ctx);
                } catch (IOException e) {
                    System.out.println("Something websocket broke");
                }
            }
            case MAKE_MOVE -> {}
            case LEAVE -> {}
            case RESIGN -> {}
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connectionManager.remove(ctx.session);
    }

    public void messageConnect(WsMessageContext ctx) throws IOException {
        ConnectCommand command = serializer.fromJson(ctx.message(), ConnectCommand.class);
        connectionManager.add(ctx.session, command.getGameID(), command.isPlaying());
        try {
            LoadGameMessage message = new LoadGameMessage(LOAD_GAME, server.getGame(command.getAuthToken(), command.getGameID()));
            ctx.session.getRemote().sendString(serializer.toJson(message));
            String username = server.authSession(command.getAuthToken()).username();
            connectionManager.notify(String.format("%s joined the game!", username), command.getGameID());
        } catch (InvalidLoginException e) {
            ErrorMessage message = new ErrorMessage(ERROR, "Invalid Credentials");
            ctx.session.getRemote().sendString(serializer.toJson(message));
        } catch (DatabaseException | DataAccessException e) {
            ErrorMessage message = new ErrorMessage(ERROR, "Server error. Game ID likely invalid");
            ctx.session.getRemote().sendString(serializer.toJson(message));
        }
    }
}
