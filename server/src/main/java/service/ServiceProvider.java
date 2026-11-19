package service;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.*;
import dataaccess.sql.DatabaseManager;
import records.*;
import server.websocket.WebSocketHandler;

import java.util.List;

public class ServiceProvider {

    private DataAccess db;
    public ServiceProvider() {
        db = new DatabaseManager();
    }

    public AuthDAO registerUser(String username, String password, String email) throws DataAccessException, DatabaseException {
        AuthDAO result = db.addUser(username, password, email);
        if (result == null) {
            throw new AlreadyTakenException("Username already taken");
        } else {
            return result;
        }
    }

    public AuthDAO loginUser(String username, String password) throws InvalidLoginException, DatabaseException {
        AuthDAO result = db.newSession(username, password);
        if (result == null) {
            throw new InvalidLoginException("Invalid username and/or password");
        } else {
            return result;
        }
    }

    public AuthDAO authSession(String authToken) throws InvalidLoginException, DatabaseException {
        if (authToken == null) {
            throw new InvalidLoginException("Invalid credentials");
        }
        AuthDAO result = db.getSession(authToken);
        if (result == null) {
            throw new InvalidLoginException("Invalid credentials");
        } else {
            return result;
        }
    }

    public void logoutSession(String authToken) throws InvalidLoginException, DatabaseException {
        if (!db.endSession(authToken)) {
            throw new InvalidLoginException("Invalid credentials");
        }
    }

    public int createGame(String authToken, String gameName) throws InvalidLoginException, DataAccessException, DatabaseException {
        authSession(authToken);
        if (gameName == null) {
            throw new DataAccessException("Must send game name");
        }
        int id = db.addGame(gameName);
        // if (id == -1) throw new DataAccessException("Game name already in use"); - Removed - two games actually can have the same name
        return id;
    }

    public List<GameDAO> listGames(String authToken) throws InvalidLoginException, DatabaseException {
        authSession(authToken);
        return db.getAllGames();
    }

    public void joinToGame(String authToken, ChessGame.TeamColor color, int gameID) throws
            InvalidLoginException, DataAccessException, DatabaseException {
        AuthDAO identity = authSession(authToken);
        GameDAO game = db.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Bad request - invalid gameID");
        }
        if (game.game().isInCheckmate(ChessGame.TeamColor.BLACK) || game.game().isInCheckmate(ChessGame.TeamColor.WHITE) ||
            game.game().getResigned() != null) {
            throw new AlreadyTakenException("User attempted to join a game that is already over");
        }
        if (color == null) {
            throw new DataAccessException("Bad request - invalid color");
        }
        if (!db.updateGame(identity.username(), color, gameID)) {
            throw new AlreadyTakenException("Invalid game edit. Username already filled");
        }
    }

    public ChessGame.TeamColor leaveGame(String authToken, ChessGame.TeamColor ccolor, int gameID) throws
            InvalidLoginException, DataAccessException, DatabaseException {
        AuthDAO identity = authSession(authToken);
        ChessGame.TeamColor color = null;
        GameDAO game = db.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Bad request - invalid gameID");
        }
        if (game.whiteUsername() != null && game.whiteUsername().equals(identity.username())) {
            color = ChessGame.TeamColor.WHITE;
        } else if (game.blackUsername() != null && game.blackUsername().equals(identity.username())) {
            color = ChessGame.TeamColor.BLACK;
        }
        if (color == null) {
            throw new DataAccessException("Bad request - invalid color");
        }
        if (!db.updateGame(null, color, gameID)) {
            throw new DataAccessException("Failed to remove user " + identity.username() + " from game");
        }
        return color;
    }
    public void makeMove(String authToken, int gameID, ChessMove moveToPlay) throws
            InvalidLoginException, DataAccessException, DatabaseException, InvalidMoveException {
        AuthDAO identity = authSession(authToken);
        GameDAO gameD = db.getGame(gameID);
        ChessGame game = gameD.game();
        if (game.getResigned() != null) {
            throw new InvalidMoveException("Attempted to move after the game had ended from resigning");
        }
        if ((game.getTeamTurn() == ChessGame.TeamColor.WHITE && gameD.whiteUsername().equals(identity.username())) ||
                (game.getTeamTurn() == ChessGame.TeamColor.BLACK && gameD.blackUsername().equals(identity.username()))) {
            game.makeMove(moveToPlay);
            db.changeGame(game, gameID);
        } else {
            throw new InvalidLoginException("Attempted to move a piece of the wrong color, or from an empty square");
        }
    }
    public void resign(String authToken, int gameID, ChessGame.TeamColor color) throws
            InvalidLoginException, DataAccessException, DatabaseException, InvalidMoveException {
        AuthDAO identity = authSession(authToken);
        GameDAO gameD = db.getGame(gameID);
        ChessGame game = gameD.game();
        if (game.getResigned() != null) {
            throw new InvalidMoveException("Attemped to resign in a game that had already ended");
        }
        if (gameD.whiteUsername().equals(identity.username())) {
            game.resign(ChessGame.TeamColor.WHITE);
            db.changeGame(game, gameID);
        } else if (gameD.blackUsername().equals(identity.username())) {
            game.resign(ChessGame.TeamColor.BLACK);
            db.changeGame(game, gameID);
        } else {
            throw new InvalidMoveException("User attempted resigning under the wrong color");
        }
    }
    public GameDAO getGame(String authToken, int gameID) throws InvalidLoginException, DataAccessException, DatabaseException {
        authSession(authToken);
        GameDAO game = db.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Attempted to get game with non-existent ID");
        }
        return game;
    }
    public void clear() throws DatabaseException {
        db.clearAll();
    }
}
