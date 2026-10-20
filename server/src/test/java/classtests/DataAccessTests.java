package classtests;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import records.*;
import dataaccess.MemoryDataAccess;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DataAccessTests {
    private static MemoryDataAccess testDatabase;
    private static List<UserDAO> users;
    private static List<UserDAO> fakeUsers;
    private static List<AuthDAO> sessions;
    private static List<AuthDAO> fakeSessions;
    private static List<String> gameNames;
    private static List<GameDAO> fullGames;
    private static List<GameDAO> fakeGames;

    public static void prepareDataAccess() {
        testDatabase = new MemoryDataAccess();
        users = new ArrayList<>();
        fakeUsers = new ArrayList<>();
        sessions = new ArrayList<>();
        fakeSessions = new ArrayList<>();
        gameNames = new ArrayList<>();
        fullGames = new ArrayList<>();
        fakeGames = new ArrayList<>();
        users.add(new UserDAO("user", "email", "password"));
        users.add(new UserDAO("user2", "email2", "password2"));
        users.add(new UserDAO("3user", "3email", "3password"));
        fakeSessions.add(new AuthDAO("hacker1", UUID.randomUUID().toString()));
        fakeSessions.add(new AuthDAO("hacker2", UUID.randomUUID().toString()));
        fakeSessions.add(new AuthDAO("hacker3", UUID.randomUUID().toString()));
        fakeUsers.add(new UserDAO("hacker1", "email", "hash"));
        fakeUsers.add(new UserDAO("hacker2", "email2", "▲gar↔ble▼"));
        fakeUsers.add(new UserDAO("hacker3", "3email", "☺☺☺☺☺☺"));
        gameNames.add("Game1");
        gameNames.add("Game2");
        gameNames.add("Game3");
        fullGames.add(new GameDAO(0, null, null, "Game1", new ChessGame()));
        fullGames.add(new GameDAO(1, null, null, "Game2", new ChessGame()));
        fullGames.add(new GameDAO(2, null, null, "Game3", new ChessGame()));
        fakeGames.add(new GameDAO(4, null, null, "Cheat1", new ChessGame()));
        fakeGames.add(new GameDAO(347592836, null, null, "Cheat2", new ChessGame()));
        fakeGames.add(new GameDAO(-1, null, null, "Cheat3", new ChessGame()));
    }

    @Test
    @DisplayName("Add and authenticate user")
    public void addAuthUsers() {
        prepareDataAccess();
        for (UserDAO user : users) {
            sessions.add(testDatabase.addUser(user.username(), user.password(), user.email()));
        }
        for (UserDAO user : users) {
            assertNotNull(testDatabase.newSession(user.username(), user.password()));
        }
        for (UserDAO user : fakeUsers) {
            assertNull(testDatabase.newSession(user.username(), user.password()));
        }
        for (UserDAO user : users) {
            assertNull(testDatabase.addUser(user.username(), user.password(), user.email()));
        }
    }

    @Test
    @DisplayName("Authenticate Session")
    public void authSession() {
        prepareDataAccess();
        for (UserDAO user : users) {
            sessions.add(testDatabase.addUser(user.username(), user.password(), user.email()));
        }
        for (AuthDAO session : sessions) {
            assertNotNull(testDatabase.getSession(session.authToken()));
        }
        for (AuthDAO session : fakeSessions) {
            assertNull(testDatabase.getSession(session.authToken()));
        }
    }

    @Test
    @DisplayName("Remove Session")
    public void removeSession() {
        prepareDataAccess();
        for (UserDAO user : users) {
            sessions.add(testDatabase.addUser(user.username(), user.password(), user.email()));
        }
        for (AuthDAO session : sessions) {
            assertTrue(testDatabase.endSession(session.authToken()));
            assertNull(testDatabase.getSession(session.authToken()));
        }
        for (AuthDAO session : fakeSessions) {
            assertNull(testDatabase.getSession(session.authToken()));
        }
    }

    @Test
    @DisplayName("Add and Get Games")
    public void addGetGames() {
        prepareDataAccess();
        for (String name : gameNames) {
            int id = testDatabase.addGame(name);
            assertNotEquals(-1, id);
        }
        for (GameDAO game : fullGames) {
            assertEquals(testDatabase.getGame(game.gameID()).toString(), game.toString());
        }
        for (GameDAO game : fakeGames) {
            assertNull(testDatabase.getGame(game.gameID()));
        }
        assertEquals(-1, testDatabase.addGame("Game1"));
    }

    @Test
    @DisplayName("List Games")
    public void listGames() {
        addGetGames();
        List<GameDAO> gameList = testDatabase.getAllGames();
        assertEquals(gameList.toString(), fullGames.toString());
    }

    @Test
    @DisplayName("Edit Games")
    public void editGames() {
        addGetGames();
        GameDAO editedGame = new GameDAO(0, "White", "Black", "Game1", new ChessGame());
        assertTrue(testDatabase.updateGame("White", ChessGame.TeamColor.WHITE, 0));
        assertTrue(testDatabase.updateGame("Black", ChessGame.TeamColor.BLACK, 0));
        assertEquals(editedGame.toString(), testDatabase.getGame(0).toString());
        assertFalse(testDatabase.updateGame("Hack", ChessGame.TeamColor.WHITE, 100));
    }

    @Test
    @DisplayName("Clear Database")
    public void clear() {
        addGetGames();
        for (UserDAO user : users) {
            sessions.add(testDatabase.addUser(user.username(), user.password(), user.email()));
        }
        assertTrue(testDatabase.clearAll());
        for (GameDAO game : fullGames) {
            assertNull(testDatabase.getGame(game.gameID()));
        }
        for (AuthDAO session : sessions) {
            assertNull(testDatabase.getSession(session.authToken()));
        }
        for (UserDAO user : users) {
            assertNull(testDatabase.newSession(user.username(), user.password()));
        }
    }
}
