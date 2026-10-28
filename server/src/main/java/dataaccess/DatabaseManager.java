package dataaccess;

import chess.ChessGame;
import records.AuthDAO;
import records.GameDAO;
import records.UserDAO;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class DatabaseManager implements DataAccess {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    @Override
    public AuthDAO addUser(String username, String password, String email) {
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
    public AuthDAO newSession(String username, String password) {
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
    public AuthDAO getSession(String authToken) {
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
    public boolean endSession(String authToken) {
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
    public int addGame(String gameName) {
        try (var conn = getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO games (gameName) VALUES (?)")) {
                statement.setString(1, gameName);
                statement.executeUpdate();
            }
            try (var query = conn.prepareStatement("SELECT id FROM games WHERE gameName = ?")) {
                query.setString(1, gameName);
                var result = query.executeQuery();
                int gameID = 0;
                while (!result.next()) {
                    gameID = result.getInt("id");
                }
                return gameID;
            }
        } catch (DataAccessException | SQLException e) {
            return -1;
        }
    }

    @Override
    public GameDAO getGame(int id) {
        return null;
    }

    @Override
    public List<GameDAO> getAllGames() {
        return List.of();
    }

    @Override
    public boolean updateGame(String username, ChessGame.TeamColor color, int gameID) {
        return false;
    }

    @Override
    public boolean clearAll() {
        try (var conn = getConnection()) {
            conn.prepareStatement("DELETE FROM users;").execute();
            conn.prepareStatement("DELETE FROM sessions;").execute();
            conn.prepareStatement("DELETE FROM games;").execute();
        } catch (DataAccessException | SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
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
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
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
