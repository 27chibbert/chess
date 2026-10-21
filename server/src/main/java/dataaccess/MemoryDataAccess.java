package dataaccess;

import chess.ChessGame;
import records.AuthDAO;
import records.GameDAO;
import records.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {

    private List<UserDAO> users;
    private List<AuthDAO> sessions;
    private List<GameDAO> games;

    public MemoryDataAccess() {
        users = new ArrayList<>();
        sessions = new ArrayList<>();
        games = new ArrayList<>();
    }

    private boolean getUser(String username) {
        for (UserDAO user : users) {
            if ((user.username().equals(username))) return true;
        }
        return false;
    }

    @Override
    public AuthDAO addUser(String username, String password, String email) {
        if (!getUser(username)) {
            users.add(new UserDAO(username, password, email));
        } else {
            return null;
        }
        AuthDAO newAuth = new AuthDAO(username, UUID.randomUUID().toString());
        sessions.add(newAuth);
        return newAuth;
    }

    @Override
    public AuthDAO newSession(String username, String password) {
        for (UserDAO user : users) {
            if ((user.username().equals(username))) {
                if ((user.password().equals(password))) {
                    AuthDAO newAuth = new AuthDAO(username, UUID.randomUUID().toString());
                    sessions.add(newAuth);
                    return newAuth;
                }
            };
        }
        return null;
    }

    @Override
    public AuthDAO getSession(String authToken) {
        for (AuthDAO sess : sessions) {
            if (authToken.equals(sess.authToken())) return sess;
        }
        return null;
    }

    @Override
    public boolean endSession(String authToken) {
        for (AuthDAO sess : sessions) {
            if (authToken.equals(sess.authToken())) {
                sessions.remove(sess);
                return true;
            }
        }
        return false;
    }

    @Override
    public int addGame(String gameName) {
        int id = 1;
        for (GameDAO g : games) {
//            if (g.gameName().equals(gameName)) return -1; - Removed because it was decided two games can have the same name
            if (g.gameID() >= id) id = g.gameID() + 1;
        }
        games.add(new GameDAO(id, null, null, gameName, new ChessGame()));
        return id;
    }

    @Override
    public GameDAO getGame(int ID) {
        for (GameDAO g : games) {
            if (g.gameID() == ID) return g;
        }
        return null;
    }

    @Override
    public List<GameDAO> getAllGames() {
        return games;
    }

    @Override
    public boolean updateGame(String username, ChessGame.TeamColor color, int gameID) {
        for (GameDAO g : games) {
            if (g.gameID() == gameID) {
                GameDAO game = null;
                if (color == ChessGame.TeamColor.WHITE && g.whiteUsername() == null) {
                    games.remove(g);
                    games.add(new GameDAO(gameID, username, g.blackUsername(), g.gameName(), g.game()));
                    return true;
                } else if (color == ChessGame.TeamColor.BLACK && g.blackUsername() == null) {
                    games.remove(g);
                    games.add(new GameDAO(gameID, g.whiteUsername(), username, g.gameName(), g.game()));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean clearAll() {
        users = new ArrayList<>();
        sessions = new ArrayList<>();
        games = new ArrayList<>();
        return true;
    }
}
