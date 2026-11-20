package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseException;
import dataaccess.InvalidLoginException;
import records.GameDAO;
import service.ServiceProvider;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import websocket.commands.ConnectCommand;
import websocket.commands.ResignOrLeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import java.io.IOException;

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
        //connectionManager.add(ctx.session, null, null, ctx.header("authorization"));
        //LOGGER.info("Started Websocket connection with session "  + ctx.header("authorization"));
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        UserGameCommand msg = serializer.fromJson(ctx.message(), UserGameCommand.class);
        try {
            switch (msg.getCommandType()) {
                case CONNECT -> messageConnect(ctx);
                case MAKE_MOVE -> messageMakeMove(ctx);
                case LEAVE -> messageLeaveGame(ctx);
                case RESIGN -> messageResign(ctx);
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to handle incoming message");
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connectionManager.remove(ctx.session);
    }

    private void messageConnect(WsMessageContext ctx) throws IOException {
        ConnectCommand command = serializer.fromJson(ctx.message(), ConnectCommand.class);
        try {
            LoadGameMessage message = new LoadGameMessage(server.getGame(command.getAuthToken(), command.getGameID()));
            ctx.session.getRemote().sendString(serializer.toJson(message));
            String username = server.authSession(command.getAuthToken()).username();
            GameDAO game;
            if (command.isPlaying() == true) {
                game = server.getGame(command.getAuthToken(), command.getGameID());
                if (game.whiteUsername().equals(game.blackUsername())) {
                    connectionManager.notify(String.format("%s joined the game. They are both players."), command.getGameID(), command.getAuthToken());
                } else if (game.whiteUsername().equals(username)) {
                    connectionManager.notify(String.format("%s joined the game as White.", username), command.getGameID(), command.getAuthToken());
                } else {
                    connectionManager.notify(String.format("%s joined the game as Black.", username), command.getGameID(), command.getAuthToken());
                }
            } else {
                connectionManager.notify(String.format("%s is now observing the game.", username), command.getGameID(), command.getAuthToken());
            }
            connectionManager.add(ctx.session, command.getGameID(), command.isPlaying(), command.getAuthToken());
        } catch (InvalidLoginException e) {
            ErrorMessage message = new ErrorMessage("Invalid Credentials");
            ctx.session.getRemote().sendString(serializer.toJson(message));
        } catch (DatabaseException | DataAccessException e) {
            ErrorMessage message = new ErrorMessage("Server error. Game ID likely invalid");
            ctx.session.getRemote().sendString(serializer.toJson(message));
        }
    }
    private void messageMakeMove(WsMessageContext ctx) throws IOException {
        try {
            MakeMoveCommand command = serializer.fromJson(ctx.message(), MakeMoveCommand.class);
            server.makeMove(command.getAuthToken(), command.getGameID(), command.getChessMove());
            GameDAO game = server.getGame(command.getAuthToken(), command.getGameID());
            connectionManager.notify("Move made!", command.getGameID(), command.getAuthToken());
            connectionManager.publishGameChange(game);
            String message = "";
            if (game.game().isInCheckmate(game.game().getTeamTurn())) {
                if (game.game().getTeamTurn() == ChessGame.TeamColor.WHITE) {
                    message = String.format("White player %s is in checkmate. Black player %s wins!", game.whiteUsername(), game.blackUsername());
                } else {
                    message = String.format("Black player %s is in checkmate. White player %s wins!", game.blackUsername(), game.whiteUsername());
                }
                connectionManager.notify(message, command.getGameID(), "null");
            }
        } catch (InvalidLoginException e) {
            ErrorMessage msg = new ErrorMessage("Invalid credentials");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        } catch (InvalidMoveException e) {
            ErrorMessage msg = new ErrorMessage("Invalid move attempted");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        } catch (DatabaseException | DataAccessException e) {
            ErrorMessage msg = new ErrorMessage("Server error. Game ID likely invalid");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        }
    }
    private void messageLeaveGame(WsMessageContext ctx) throws IOException {
        try {
            ResignOrLeaveGameCommand command = serializer.fromJson(ctx.message(), ResignOrLeaveGameCommand.class);
            GameDAO game = server.getGame(command.getAuthToken(), command.getGameID());
            String username = server.authSession(command.getAuthToken()).username();
            if ((game.whiteUsername() != null && game.whiteUsername().equals(username)) ||
                    (game.blackUsername() != null && game.blackUsername().equals(username))) {
                ChessGame.TeamColor color = server.leaveGame(command.getAuthToken(), command.getColor(), command.getGameID());
                if (color == ChessGame.TeamColor.WHITE) {
                    connectionManager.notify(username + ", playing as White, has left the game", command.getGameID(), command.getAuthToken());
                } else {
                    connectionManager.notify(username + ", playing as Black, has left the game", command.getGameID(), command.getAuthToken());
                }
            } else {
                connectionManager.notify("An observer has left the game", command.getGameID(), command.getAuthToken());
            }
            connectionManager.remove(ctx.session);
            ctx.session.close();
        } catch (InvalidLoginException e) {
            ErrorMessage msg = new ErrorMessage("Invalid credentials");
            ctx.session.getRemote().sendString(serializer.toJson(msg));
        } catch (DatabaseException | DataAccessException e) {
            ErrorMessage msg = new ErrorMessage("Server error. Game ID likely invalid");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        }
    }
    private void messageResign(WsMessageContext ctx) throws IOException {
        try {
            ResignOrLeaveGameCommand command = serializer.fromJson(ctx.message(), ResignOrLeaveGameCommand.class);
            server.resign(command.getAuthToken(), command.getGameID(), command.getColor());
            String username = server.authSession(command.getAuthToken()).username();
            if (command.getColor() == ChessGame.TeamColor.WHITE) {
                connectionManager.notify(String.format("White player %s has resigned. Black has won!", username), command.getGameID(), "null");
            } else {
                connectionManager.notify(String.format("Black player %s has resigned. White has won!", username), command.getGameID(), "null");
            }
        } catch (InvalidLoginException e) {
            ErrorMessage msg = new ErrorMessage("Invalid credentials");
            ctx.session.getRemote().sendString(serializer.toJson(msg));
        } catch (InvalidMoveException e) {
            ErrorMessage msg = new ErrorMessage("You attempted illegal resigning");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        } catch (DatabaseException | DataAccessException e) {
            ErrorMessage msg = new ErrorMessage("Server error. Game ID likely invalid");
            ctx.session.getRemote().sendString(serializer.toJson(msg, ErrorMessage.class));
        }
    }
}
