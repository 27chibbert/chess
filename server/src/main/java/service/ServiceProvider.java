package service;
import chess.ChessGame;
import dataaccess.*;
import records.*;

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

    private AuthDAO authSession(String authToken) throws InvalidLoginException, DatabaseException {
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

    public void joinToGame(String authToken, ChessGame.TeamColor color, int gameID) throws InvalidLoginException, DataAccessException, DatabaseException {
        AuthDAO identity = authSession(authToken);
        if (color == null) {
            throw new DataAccessException("Bad request - invalid color");
        }
        if (db.getGame(gameID) == null) {
            throw new DataAccessException("Bad request - invalid gameID");
        }
        if (!db.updateGame(identity.username(), color, gameID)) {
            throw new AlreadyTakenException("Invalid game edit. Username already filled");
        }
    }

    public void clear() throws DatabaseException {
        db.clearAll();
    }
}
