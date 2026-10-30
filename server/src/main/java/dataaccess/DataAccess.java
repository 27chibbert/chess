package dataaccess;
import chess.ChessGame;
import records.*;

import java.util.List;

public interface DataAccess {
    public AuthDAO addUser(String username, String password, String email) throws DatabaseException;
    public AuthDAO newSession(String user, String password) throws DatabaseException;
    public AuthDAO getSession(String authToken) throws DatabaseException;
    public boolean endSession(String authToken) throws DatabaseException;
    public int addGame(String gameName) throws DatabaseException;
    public GameDAO getGame(int id) throws DatabaseException;
    public List<GameDAO> getAllGames() throws DatabaseException;
    public boolean updateGame(String username, ChessGame.TeamColor color, int gameID) throws DatabaseException;
    public boolean clearAll() throws DatabaseException;
}