package client;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import records.AuthDAO;
import records.GameDAO;
import records.UserDAO;
import records.UserLogin;
import server.Server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final UserDAO user1 = new UserDAO("user1", "pass1", "email1");
    private static final UserDAO user2 = new UserDAO("user2", "pass2", "email2");
    private static final UserDAO user3 = new UserDAO("user3", "pass3", "email3");
    private static final UserDAO badUser = new UserDAO("user1", "asdf", "adsf");
    private static final UserDAO badUser2 = new UserDAO(null, "asdf", "asdf");
    private static final String alreadyTaken = "Already Taken";
    private static final String badRequest = "Bad Request";
    private static final String unauthorized = "Unauthorized";
    private static final String clientError = "Timeout or Client-side Error";
    private static final String serverError = "Server Error";
    private static final GameDAO[] gameCheck = { new GameDAO(1, null, null, "Test Game", new ChessGame()) };

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clear() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/db"))
                    .timeout(java.time.Duration.ofMillis(5000))
                    .DELETE()
                    .build();
            var res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                System.out.println("Failed to clear Database");
            }
        } catch (Exception e) {
            System.out.println("Failed to clear Database");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void sanityCheck() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerCheck() {
        AuthDAO auth1 = (AuthDAO) facade.register(user1).data();
        AuthDAO auth2 = (AuthDAO) facade.register(user2).data();
        AuthDAO auth3 = (AuthDAO) facade.register(user3).data();
        String message1 = (String) facade.register(badUser).data();
        String message2 = (String) facade.register(badUser2).data();
        String message3 = (String) facade.register(null).data();
        assertEquals(auth1.username(), user1.username());
        assertEquals(auth2.username(), user2.username());
        assertEquals(auth3.username(), user3.username());
        assertEquals(alreadyTaken, message1);
        assertEquals(badRequest, message2);
        assertEquals(serverError, message3);
    }

    @Test
    public void loginCheck() {
        registerTestUsers();
        AuthDAO login1 = (AuthDAO) facade.login(new UserLogin(user1.username(), user1.password())).data();
        AuthDAO login2 = (AuthDAO) facade.login(new UserLogin(user2.username(), user2.password())).data();
        AuthDAO login3 = (AuthDAO) facade.login(new UserLogin(user3.username(), user3.password())).data();
        String message1 = (String) facade.login(new UserLogin(badUser.username(), badUser.password())).data();
        String message2 = (String) facade.login(new UserLogin(badUser2.username(), badUser.password())).data();
        String message3 = (String) facade.login(null).data();
        assertEquals(login1.username(), user1.username());
        assertEquals(login2.username(), user2.username());
        assertEquals(login3.username(), user3.username());
        assertEquals(unauthorized, message1);
        assertEquals(badRequest, message2);
        assertEquals(serverError, message3);
    }

    @Test
    public void logoutCheck() {
        registerTestUsers();
        AuthDAO login1 =  (AuthDAO) facade.login(new UserLogin(user1.username(), user1.password())).data();
        String message = (String) facade.login(new UserLogin(badUser.username(), badUser.password())).data();
        assertEquals(user1.username(), login1.username());
        assertEquals(unauthorized, message);
        assertNull(facade.logout(login1.authToken()).data());
        assertEquals(unauthorized, (String) facade.logout("a;sdlfja;sdlkfjpqweifhapsduivbapsd").data());
        assertEquals(clientError, (String) facade.logout(null).data());
    }

    @Test
    public void createGameCheck() {
        registerTestUsers();
        AuthDAO login1 = (AuthDAO) facade.login(new UserLogin(user1.username(), user1.password())).data();
        int game1 = (Integer) facade.createGame(login1.authToken(), "Test Game").data();
        String message = (String) facade.createGame("asdofapsdfioandsfa", "Bad Game").data();
        String message2 = (String) facade.createGame(null, null).data();
        assertEquals(1, game1);
        assertEquals(unauthorized, message);
        assertEquals(clientError, message2);
    }

    @Test
    public void listGameCheck() {
        registerTestUsers();
        AuthDAO login = (AuthDAO) facade.login(new UserLogin(user1.username(), user1.password())).data();
        facade.createGame(login.authToken(), "Test Game");
        GameDAO[] games = (GameDAO[]) facade.listGames(login.authToken()).data();
        String message = (String) facade.listGames("asdf").data();
        String gamesString = "";
        String gameCheckString = "";
        for (GameDAO game : games) {
            gamesString += game.toString();
        }
        for (GameDAO game : gameCheck) {
            gameCheckString += game.toString();
        }
        assertEquals(gameCheckString, gamesString);
        assertEquals(unauthorized, message);
    }

    @Test
    public void joinGameCheck() {
        registerTestUsers();
        AuthDAO login = (AuthDAO) facade.login(new UserLogin(user1.username(), user1.password())).data();
        facade.createGame(login.authToken(), "Test Game");
        assertNull(facade.joinGame(login.authToken(), 1, ChessGame.TeamColor.WHITE).data());
        String message = (String) facade.joinGame("asdofipasdoifas", 1, ChessGame.TeamColor.BLACK).data();
        assertEquals(unauthorized, message);
        String message2 = (String) facade.joinGame(login.authToken(), 1, ChessGame.TeamColor.WHITE).data();
        assertEquals(alreadyTaken, message2);
        String message3 = (String) facade.joinGame(login.authToken(), 100, ChessGame.TeamColor.BLACK).data();
        assertEquals(badRequest, message3);
    }
    private void registerTestUsers() {
        facade.register(user1);
        facade.register(user2);
        facade.register(user3);
    }
}
