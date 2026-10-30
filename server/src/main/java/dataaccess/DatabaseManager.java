package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import records.AuthDAO;
import records.GameDAO;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.lang.RuntimeException;

import static chess.ChessGame.TeamColor.*;

public class DatabaseManager implements DataAccess {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;
    private static Gson serializer = new Gson();

    /*
     * Load the database information for the db.properties file.
     */
    public DatabaseManager() {
        try {
            createDatabase();
        } catch (DatabaseException e) {
            throw new RuntimeException("Database failed to initialize");
        }
    }

    static {
        loadPropertiesFromResources();
    }

    @Override
    public AuthDAO addUser(String username, String password, String email) throws DatabaseException {
        AuthDAO session;
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                query.setString(1, username);
                var result = query.executeQuery();
                if (result.next()) {
                    return null;
                }
            }
            try (var userStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ? ,?)")) {
                userStatement.setString(1, username);
                userStatement.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
                userStatement.setString(3, email);
                var result = userStatement.executeUpdate();
                session = new AuthDAO(username, UUID.randomUUID().toString());
                var sessionStatement = conn.prepareStatement("INSERT INTO sessions (username, authToken) VALUES (?, ?)");
                sessionStatement.setString(1, username);
                sessionStatement.setString(2, session.authToken());
                sessionStatement.executeUpdate();
                return session;
            }
        } catch (DataAccessException | SQLException e) {
            return null;
        }
    }

    @Override
    public AuthDAO newSession(String username, String password) throws DatabaseException {
        AuthDAO session;
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                query.setString(1, username);
                var result = query.executeQuery();
                if (result.next() && BCrypt.checkpw(password, result.getString("password"))) {
                    session = new AuthDAO(username, UUID.randomUUID().toString());
                } else {
                    return null;
                }
            }
            try (var query = conn.prepareStatement("INSERT INTO sessions (username, authToken) VALUES (?, ?)")) {
                query.setString(1, session.username());
                query.setString(2, session.authToken());
                query.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            return null;
        }
        return session;
    }

    @Override
    public AuthDAO getSession(String authToken) throws DatabaseException {
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM sessions WHERE authToken = ?")) {
                query.setString(1, authToken);
                var result = query.executeQuery();
                if (result.next()) {
                    AuthDAO session = new AuthDAO(result.getString("username"), result.getString("authToken"));
                    return session;
                }
                return null;
            }
        } catch (DataAccessException | SQLException e) {
            return null;
        }
    }

    @Override
    public boolean endSession(String authToken) throws DatabaseException {
        try (var conn = getConnection()) {
            try (var statement = conn.prepareStatement("DELETE FROM sessions WHERE authToken = ?")) {
                statement.setString(1, authToken);
                var result = statement.executeUpdate();
                if (result > 0) {
                    return true;
                }
            }
        } catch (DataAccessException | SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public int addGame(String gameName) throws DatabaseException {
        try (var conn = getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO games (gameName, game) VALUES (?, ?)")) {
                statement.setString(1, gameName);
                statement.setString(2, serializer.toJson(new ChessGame()));
                statement.executeUpdate();
            }
            try (var query = conn.prepareStatement("SELECT id FROM games WHERE gameName = ?")) {
                query.setString(1, gameName);
                var result = query.executeQuery();
                int gameID = 0;
                while (result.next()) {
                    gameID = result.getInt("id");
                }
                return gameID;
            }
        } catch (DataAccessException | SQLException e) {
            return -1;
        }
    }

    @Override
    public GameDAO getGame(int id) throws DatabaseException {
        GameDAO game;
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM games WHERE id = ?")) {
                query.setInt(1, id);
                var result = query.executeQuery();
                if (result.next()) {
                    game = new GameDAO(result.getInt(1), result.getString(2),
                            result.getString(3), result.getString(4),
                            serializer.fromJson(result.getString(5), ChessGame.class));
                } else {
                    return null;
                }
            }
        } catch (DataAccessException | SQLException e) {
            return null;
        }
        return game;
    }

    @Override
    public List<GameDAO> getAllGames() throws DatabaseException {
        List<GameDAO> games = new ArrayList<>();
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM games")) {
                var result = query.executeQuery();
                while (result.next()) {
                    games.add(new GameDAO(result.getInt(1), result.getString(2),
                            result.getString(3), result.getString(4),
                            serializer.fromJson(result.getString(5), ChessGame.class)));
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;
        }
        return games;
    }

    @Override
    public boolean updateGame(String username, ChessGame.TeamColor color, int gameID) throws DatabaseException {
        try (var conn = getConnection()) {
            try (var query = conn.prepareStatement("SELECT * FROM games WHERE id = ?")) {
                query.setInt(1, gameID);
                var result = query.executeQuery();
                if (result.next()) {
                    if (color == WHITE && result.getString(2) == null) {
                        var query2 = conn.prepareStatement("UPDATE games SET whiteUsername = ? WHERE id = ?");
                        query2.setString(1, username);
                        query2.setInt(2, gameID);
                        query2.executeUpdate();

                    } else if (color == BLACK && result.getString(3) == null) {
                        var query2 = conn.prepareStatement("UPDATE games SET blackUsername = ? WHERE id = ?");
                        query2.setString(1, username);
                        query2.setInt(2, gameID);
                        query2.executeUpdate();
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (SQLException | DataAccessException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean clearAll() throws DatabaseException {
        try (var conn = getConnection()) {
            conn.prepareStatement("DROP TABLE users;").execute();
            conn.prepareStatement("DROP TABLE sessions;").execute();
            conn.prepareStatement("DROP TABLE games;").execute();
            createDatabase();
        } catch (DataAccessException | SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DatabaseException {
        var dbStatement = "CREATE DATABASE IF NOT EXISTS " + databaseName +";";
        var useStatement = "USE " + databaseName + ";";
        var userStatement = "CREATE TABLE IF NOT EXISTS users ( " +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "username VARCHAR(255) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255), " +
                "PRIMARY KEY (id), " +
                "UNIQUE KEY (username) );";
        var sessionStatement = "CREATE TABLE IF NOT EXISTS sessions (" +
                "username VARCHAR(255) NOT NULL," +
                "authToken VARCHAR(255) NOT NULL," +
                "UNIQUE KEY (authToken) );";
        var gameStatement = "CREATE TABLE IF NOT EXISTS games (" +
                "id INT NOT NULL AUTO_INCREMENT," +
                "whiteUsername VARCHAR(255)," +
                "blackUsername VARCHAR(255)," +
                "gameName VARCHAR(255) NOT NULL," +
                "game VARCHAR(2047)," +
                "PRIMARY KEY (id) );";
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)) {
            conn.prepareStatement(dbStatement).executeUpdate();
            conn.prepareStatement(useStatement).execute();
            conn.prepareStatement(userStatement).executeUpdate();
            conn.prepareStatement(sessionStatement).executeUpdate();
            conn.prepareStatement(gameStatement).executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("failed to create database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException, DatabaseException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DatabaseException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

}
