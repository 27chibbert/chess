package service;

import chess.ChessGame;
import dataAccess.DataAccessException;
import dataAccess.InvalidLoginException;
import org.junit.jupiter.api.*;
import records.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ServiceProviderTests {

    private static ServiceProvider service;
    private static List<UserDAO> users;
    private static List<UserDAO> fakeUsers;
    private static List<AuthDAO> sessions;
    private static List<AuthDAO> fakeSessions;
    private static List<String> gameNames;
    private static List<GameDAO> fullGames;

    private void prepareData() {
        service = new ServiceProvider();
        users = new ArrayList<>();
        fakeUsers = new ArrayList<>();
        sessions = new ArrayList<>();
        fakeSessions = new ArrayList<>();
        gameNames = new ArrayList<>();
        fullGames = new ArrayList<>();
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
        fullGames.add(new GameDAO(1, null, null, "Game1", new ChessGame()));
        fullGames.add(new GameDAO(2, null, null, "Game2", new ChessGame()));
        fullGames.add(new GameDAO(3, null, null, "Game3", new ChessGame()));
    }

    @Test
    @DisplayName("User Registration")
    public void testUserRegistration() throws DataAccessException, InvalidLoginException {
        prepareData();
        for (UserDAO user : users) {
            sessions.add(service.registerUser(user.username(), user.password(), user.email()));
        }
        for (UserDAO user : users) {
            assertNotNull(service.loginUser(user.username(), user.password()));
        }
    }

    @Test
    @DisplayName("User Logout")
    public void testUserLogout() throws DataAccessException, InvalidLoginException {
        prepareData();
        for (UserDAO user : users) {
            sessions.add(service.registerUser(user.username(), user.password(), user.email()));
        }
        for (AuthDAO session : sessions) {
            service.logoutSession(session.authToken());
        }
        for (AuthDAO session : sessions) {
            assertThrows(InvalidLoginException.class, () -> service.logoutSession(session.authToken()));
        }
    }

    @Test
    @DisplayName("User Login")
    public void testUserLogin() throws DataAccessException, InvalidLoginException {
        prepareData();
        for (UserDAO user : users) {
            sessions.add(service.registerUser(user.username(), user.password(), user.email()));
        }
        for (AuthDAO session : sessions) {
            service.logoutSession(session.authToken());
        }
        for (UserDAO user : users) {
            assertNotNull(service.loginUser(user.username(), user.password()));
        }
        for (UserDAO user : fakeUsers) {
            assertThrows(InvalidLoginException.class, () -> service.loginUser(user.username(), user.password()));
        }
    }

    @Test
    @DisplayName("Create Game")
    public void testCreateGame() throws DataAccessException, InvalidLoginException {
        prepareData();
        UserDAO user = users.getFirst();
        AuthDAO session = service.registerUser(user.username(), user.password(), user.email());
        for (String gameName : gameNames) {
            service.createGame(session.authToken(), gameName);
        }
        assertThrows(InvalidLoginException.class, () -> service.createGame(fakeSessions.getFirst().authToken(), gameNames.getFirst()));
    }

    @Test
    @DisplayName("List Games")
    public void testListGames() throws DataAccessException, InvalidLoginException {
        prepareData();
        UserDAO user = users.getFirst();
        AuthDAO session = service.registerUser(user.username(), user.password(), user.email());
        for (String gameName : gameNames) {
            service.createGame(session.authToken(), gameName);
        }
        List<GameDAO> games = service.listGames(session.authToken());
        int i = 0;
        while (i < 3) {
            assertEquals(games.get(i).toString(), fullGames.get(i).toString());
            i++;
        }
        assertThrows(InvalidLoginException.class, () -> service.listGames(fakeSessions.getFirst().authToken()));
    }

    @Test
    @DisplayName("Join User to Game")
    public void testJoinToGame() throws DataAccessException, InvalidLoginException {
        prepareData();
        UserDAO user = users.getFirst();
        AuthDAO session = service.registerUser(user.username(), user.password(), user.email());
        UserDAO user2 = users.get(2);
        AuthDAO session2 = service.registerUser(user2.username(), user2.password(), user2.email());
        int gameID = service.createGame(session.authToken(), gameNames.getFirst());
        GameDAO joinedGame = new GameDAO(gameID, "user", "3user", "Game1", new ChessGame());
        service.joinToGame(session.authToken(), ChessGame.TeamColor.WHITE, gameID);
        service.joinToGame(session2.authToken(), ChessGame.TeamColor.BLACK, gameID);
        assertEquals(joinedGame.toString(), service.listGames(session2.authToken()).getFirst().toString());
        assertThrows(DataAccessException.class, () -> service.joinToGame(session.authToken(), ChessGame.TeamColor.BLACK, gameID));
    }
}
