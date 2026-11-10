package client;

import chess.ChessGame;
import records.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.HashMap;

public class ChessClient {
    enum UserState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        HELP,
        AUTHENTICATEDHELP,
        EXIT
    }

    private ClientUI ui;
    private static ServerFacade webClient = new ServerFacade(8080);
    private UserState state;
    private String username;
    private String authToken;
    private HashMap<Integer, Integer> listToID;
    private static Logger logger = Logger.getLogger("Server Log");

    BiFunction<String, Integer, String> getToken = (input, n) -> {
        if (input == null) {
            return "";
        }
        String[] tokens = input.trim().split("\\s+");
        if (n >= 0 && n < tokens.length) {
            return tokens[n];
        } else {
            return "";
        }
    };

    public ChessClient() {
        try {
            FileHandler fd = new FileHandler("logs/client.log", true);
            fd.setFormatter(new SimpleFormatter());
            logger.addHandler(fd);
        } catch (Exception e) {
            System.out.println("Server logger failed to start");
        }
    }

    public void startClient() {
        state = UserState.UNAUTHENTICATED;
        username = null;
        authToken = null;
        ui = new ClientUI(this);
        ui.repl();
    }

    public ClientMessage register(String input) {
        if (!getToken.apply(input, 1).isEmpty() && !getToken.apply(input, 2).isEmpty() && !getToken.apply(input, 3).isEmpty()) {
            UserDAO user = new UserDAO(getToken.apply(input, 1), getToken.apply(input, 2), getToken.apply(input, 3));
            ServerMessage result = webClient.register(user);
            return resolveResponse(result);
        } else {
            return new ClientMessage(authed(), "Bad command format. Type 'help' for more info", null);
        }
    }
    public ClientMessage login(String input) {
        if (!getToken.apply(input, 1).isEmpty() && !getToken.apply(input, 2).isEmpty()) {
            UserLogin login = new UserLogin(getToken.apply(input, 1), getToken.apply(input, 2));
            ServerMessage result = webClient.login(login);
            return resolveResponse(result);
        } else {
            return new ClientMessage(authed(), "Bad command format. Type 'help' for more info", null);
        }
    }
    public ClientMessage logout() {
        ServerMessage result = webClient.logout(authToken);
        if (result.data() == null) {
            String out = "Successfully logged out";
            logger.info(String.format("User %s with token %s successfully logged out", username, authToken));
            username = null;
            authToken = null;
            state = UserState.UNAUTHENTICATED;
            return new ClientMessage(authed(), out, null);
        } else {
            return resolveResponse(result);
        }
    }
    public ClientMessage createGame(String input) {
        if (!getToken.apply(input, 1).isEmpty() && getToken.apply(input, 2).isEmpty()) {
            String gameName = getToken.apply(input, 1);
            ServerMessage result = webClient.createGame(authToken, gameName);
            return resolveResponse(result);
        } else {
            return new ClientMessage(authed(), "Bad command format. Type 'help' for more info", null);
        }
    }
    public ClientMessage joinGame(String input) {
        if (!getToken.apply(input, 1).isEmpty() && !getToken.apply(input, 2).isEmpty()) {
            int gameID;
            ChessGame.TeamColor color;
            try {
                gameID = Integer.parseInt(getToken.apply(input, 1));
            } catch (NumberFormatException e) {
                return new ClientMessage(authed(), "Bad command format. ID wasn't an integer", null);
            }
            gameID = listToID.get(gameID);
            if (getToken.apply(input, 2).equalsIgnoreCase("WHITE")) {
                color = ChessGame.TeamColor.WHITE;
            } else if (getToken.apply(input, 2).equalsIgnoreCase("BLACK")) {
                color = ChessGame.TeamColor.BLACK;
            } else {
                return new ClientMessage(authed(), "Bad command format. Color must be 'WHITE' or 'BLACK'", null);
            }
            ServerMessage result = webClient.joinGame(authToken, gameID, color);
            if (result.data() == null) {
                return new ClientMessage(authed(), String.format("Successfully joined game %d", gameID), null);
            }
            return resolveResponse(result);
        } else {
            return new ClientMessage(authed(), "Bad command format. Type 'help' for more info", null);
        }
    }
    public ClientMessage listGames() {
        ServerMessage result = webClient.listGames(authToken);
        return resolveResponse(result);
    }
    public ClientMessage observeGame(String input) {
        if (!getToken.apply(input, 1).isEmpty()) {
            int gameID;
            try {
                gameID = Integer.parseInt(getToken.apply(input, 1));
            } catch (NumberFormatException e) {
                return new ClientMessage(authed(), "Bad command format. ID wasn't a number", null);
            }
            gameID = listToID.get(gameID);
            ServerMessage result = webClient.listGames(authToken);
            GameDAO[] games = (GameDAO[]) result.data();
            for (GameDAO game: games) {
                if (game.gameID() == gameID) {
                    GameDAO[] gameToPrint = { game };
                    return new ClientMessage(authed(), "Observing game", gameToPrint);
                }
            }
            return new ClientMessage(authed(), "Game not found", null);
        } else {
            return new ClientMessage(authed(), "Bad command format. Type 'help' for more info", null);
        }
    }
    private ClientMessage resolveResponse(ServerMessage msg) {
        if (msg.data().getClass() == String.class) {
            String err = (String) msg.data();
            err = String.format("Error: %s", err);
            return new ClientMessage(authed(), err, null);
        } else if (msg.data().getClass() == AuthDAO.class) {
            AuthDAO auth = (AuthDAO) msg.data();
            username = auth.username();
            authToken = auth.authToken();
            String out = String.format("You logged in as user %s!\n", username);
            state = UserState.AUTHENTICATED;
            logger.info(String.format("Logged in as user %s with authToken %s", username, authToken));
            ServerMessage result = webClient.listGames(authToken);
            mapGames((GameDAO[]) result.data());
            return new ClientMessage(authed(), out, null);
        } else if (msg.data().getClass() == Integer.class) {
            return new ClientMessage(authed(), String.format("New game created with ID %d", msg.data()), null);
        } else if (msg.data().getClass() == GameDAO[].class) {
            return new ClientMessage(authed(), "Current games on the server:\n", mapGames((GameDAO[]) msg.data()));
        } else {
            return new ClientMessage(authed(), "A client-side error has occured", null);
        }
    }
    private boolean authed() {
        return authToken != null;
    }
    private GameDAO[] mapGames(GameDAO[] games) {
        listToID = new HashMap<>();
        List<GameDAO> orderedGames = new ArrayList<>();
        int i = 1;
        for (GameDAO game : games) {
            listToID.put(i, game.gameID());
            orderedGames.add(new GameDAO(i, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
            i++;
        }
        return (GameDAO[]) orderedGames.toArray(GameDAO[]::new);
    }
}
