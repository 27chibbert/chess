package server;
import chess.ChessGame;
import dataaccess.*;
import records.*;

import java.util.List;

public class ServiceProvider {

    private DataAccess db;

    public ServiceProvider() {
        db = new MemoryDataAccess();
    }

    public AuthDAO registerUser(String username, String password, String email) throws DataAccessException {
        AuthDAO result = db.addUser(username, password, email);
        if (result == null) {
            throw new AlreadyTakenException("Username already taken");
        } else {
            return result;
        }
    }

    public AuthDAO loginUser(String username, String password) throws InvalidLoginException {
        AuthDAO result = db.newSession(username, password);
        if (result == null) {
            throw new InvalidLoginException("Invalid username and/or password");
        } else {
            return result;
        }
    }

    private AuthDAO authSession(String authToken) throws InvalidLoginException {
        AuthDAO result = db.getSession(authToken);
        if (result == null) {
            throw new InvalidLoginException("Invalid credentials");
        } else {
            return result;
        }
    }

    public void logoutSession(String authToken) throws InvalidLoginException {
        if (!db.endSession(authToken)) throw new InvalidLoginException("Invalid credentials");
    }

    public int createGame(String authToken, String gameName) throws InvalidLoginException, DataAccessException {
        authSession(authToken);
        int id = db.addGame(gameName);
        if (id == -1) throw new DataAccessException("Game name already in use");
        return id;
    }

    public List<GameDAO> listGames(String authToken) throws InvalidLoginException {
        authSession(authToken);
        return db.getAllGames();
    }

    public void joinToGame(String authToken, ChessGame.TeamColor color, int gameID) throws InvalidLoginException, DataAccessException {
        AuthDAO identity = authSession(authToken);
        if (!db.updateGame(identity.username(), color, gameID)) throw new DataAccessException("Invalid game edit. Username already filled or game nonexistent");
    }

    public void clear() {
        db.clearAll();
    }
}
