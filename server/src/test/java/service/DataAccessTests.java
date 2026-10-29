package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import org.junit.jupiter.api.*;
import records.*;
import dataaccess.MemoryDataAccess;
import dataaccess.DatabaseManager;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DataAccessTests {
    private static DataAccess testDatabase;
    private static List<UserDAO> users;
    private static List<UserDAO> fakeUsers;
    private static List<AuthDAO> sessions;
    private static List<AuthDAO> fakeSessions;
    private static List<String> gameNames;
    private static List<GameDAO> fullGames;
    private static List<GameDAO> fakeGames;

    public static void prepareDataAccess() {
        testDatabase = new DatabaseManager();
        testDatabase.clearAll();
        users = new ArrayList<>();
        fakeUsers = new ArrayList<>();
        sessions = new ArrayList<>();
        fakeSessions = new ArrayList<>();
        gameNames = new ArrayList<>();
        fullGames = new ArrayList<>();
        fakeGames = new ArrayList<>();
        ServiceProviderTests.prepareLists(users, fakeUsers, fakeSessions, gameNames, fullGames);
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
            assertNotEquals(-1, id); // -1 means a SQL error. Can't happen with memory data organization.
        }
        for (GameDAO game : fullGames) {
            assertEquals(testDatabase.getGame(game.gameID()).toString(), game.toString());
        }
        for (GameDAO game : fakeGames) {
            assertNull(testDatabase.getGame(game.gameID()));
        }
        // assertEquals(-1, testDatabase.addGame("Game1")); - Test removed because it was decided that two games CAN have the same name
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
        GameDAO editedGame = new GameDAO(1, "White", "Black", "Game1", new ChessGame());
        assertTrue(testDatabase.updateGame("White", ChessGame.TeamColor.WHITE, 1));
        assertTrue(testDatabase.updateGame("Black", ChessGame.TeamColor.BLACK, 1));
        assertEquals(editedGame.toString(), testDatabase.getGame(1).toString());
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
