package dataaccess;
import chess.ChessGame;
import records.*;

import java.util.List;

public interface DataAccess {
    public AuthDAO addUser(String username, String password, String email);
    public AuthDAO newSession(String user, String password);
    public AuthDAO getSession(String authToken) ;
    public boolean endSession(String authToken) ;
    public int addGame(String gameName);
    public GameDAO getGame(int ID);
    public List<GameDAO> getAllGames() ;
    public boolean updateGame(String username, ChessGame.TeamColor color, int gameID);
    public boolean clearAll();
}