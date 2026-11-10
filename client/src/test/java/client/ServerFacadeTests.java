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
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final UserDAO USER_1 = new UserDAO("user1", "pass1", "email1");
    private static final UserDAO USER_2 = new UserDAO("user2", "pass2", "email2");
    private static final UserDAO USER_3 = new UserDAO("user3", "pass3", "email3");
    private static final UserDAO BAD_USER = new UserDAO("user1", "asdf", "adsf");
    private static final UserDAO BAD_USER_2 = new UserDAO(null, "asdf", "asdf");
    private static final String ALREADY_TAKEN = "Already Taken";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String CLIENT_ERROR = "Timeout or Client-side Error";
    private static final String SERVER_ERROR = "Server Error";
    private static final GameDAO[] GAME_CHECK = { new GameDAO(1, null, null, "Test Game", new ChessGame()) };
    private static Integer port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clear() {
        try {
            String uri = "http://localhost:" + port.toString() + "/db";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .timeout(java.time.Duration.ofMillis(5000))
                    .DELETE()
                    .build();
            var res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
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
        AuthDAO auth1 = (AuthDAO) facade.register(USER_1).data();
        AuthDAO auth2 = (AuthDAO) facade.register(USER_2).data();
        AuthDAO auth3 = (AuthDAO) facade.register(USER_3).data();
        String message1 = (String) facade.register(BAD_USER).data();
        String message2 = (String) facade.register(BAD_USER_2).data();
        String message3 = (String) facade.register(null).data();
        assertEquals(auth1.username(), USER_1.username());
        assertEquals(auth2.username(), USER_2.username());
        assertEquals(auth3.username(), USER_3.username());
        assertEquals(ALREADY_TAKEN, message1);
        assertEquals(BAD_REQUEST, message2);
        assertEquals(SERVER_ERROR, message3);
    }

    @Test
    public void loginCheck() {
        registerTestUsers();
        AuthDAO login1 = (AuthDAO) facade.login(new UserLogin(USER_1.username(), USER_1.password())).data();
        AuthDAO login2 = (AuthDAO) facade.login(new UserLogin(USER_2.username(), USER_2.password())).data();
        AuthDAO login3 = (AuthDAO) facade.login(new UserLogin(USER_3.username(), USER_3.password())).data();
        String message1 = (String) facade.login(new UserLogin(BAD_USER.username(), BAD_USER.password())).data();
        String message2 = (String) facade.login(new UserLogin(BAD_USER_2.username(), BAD_USER.password())).data();
        String message3 = (String) facade.login(null).data();
        assertEquals(login1.username(), USER_1.username());
        assertEquals(login2.username(), USER_2.username());
        assertEquals(login3.username(), USER_3.username());
        assertEquals(UNAUTHORIZED, message1);
        assertEquals(BAD_REQUEST, message2);
        assertEquals(SERVER_ERROR, message3);
    }

    @Test
    public void logoutCheck() {
        registerTestUsers();
        AuthDAO login1 =  (AuthDAO) facade.login(new UserLogin(USER_1.username(), USER_1.password())).data();
        String message = (String) facade.login(new UserLogin(BAD_USER.username(), BAD_USER.password())).data();
        assertEquals(USER_1.username(), login1.username());
        assertEquals(UNAUTHORIZED, message);
        assertNull(facade.logout(login1.authToken()).data());
        assertEquals(UNAUTHORIZED, (String) facade.logout("a;sdlfja;sdlkfjpqweifhapsduivbapsd").data());
        assertEquals(CLIENT_ERROR, (String) facade.logout(null).data());
    }

    @Test
    public void createGameCheck() {
        registerTestUsers();
        AuthDAO login1 = (AuthDAO) facade.login(new UserLogin(USER_1.username(), USER_1.password())).data();
        int game1 = (Integer) facade.createGame(login1.authToken(), "Test Game").data();
        String message = (String) facade.createGame("asdofapsdfioandsfa", "Bad Game").data();
        String message2 = (String) facade.createGame(null, null).data();
        assertEquals(1, game1);
        assertEquals(UNAUTHORIZED, message);
        assertEquals(CLIENT_ERROR, message2);
    }

    @Test
    public void listGameCheck() {
        registerTestUsers();
        AuthDAO login = (AuthDAO) facade.login(new UserLogin(USER_1.username(), USER_1.password())).data();
        facade.createGame(login.authToken(), "Test Game");
        GameDAO[] games = (GameDAO[]) facade.listGames(login.authToken()).data();
        String message = (String) facade.listGames("asdf").data();
        String gamesString = "";
        String gameCheckString = "";
        for (GameDAO game : games) {
            gamesString += game.toString();
        }
        for (GameDAO game : GAME_CHECK) {
            gameCheckString += game.toString();
        }
        assertEquals(gameCheckString, gamesString);
        assertEquals(UNAUTHORIZED, message);
    }

    @Test
    public void joinGameCheck() {
        registerTestUsers();
        AuthDAO login = (AuthDAO) facade.login(new UserLogin(USER_1.username(), USER_1.password())).data();
        facade.createGame(login.authToken(), "Test Game");
        assertNull(facade.joinGame(login.authToken(), 1, ChessGame.TeamColor.WHITE).data());
        String message = (String) facade.joinGame("asdofipasdoifas", 1, ChessGame.TeamColor.BLACK).data();
        assertEquals(UNAUTHORIZED, message);
        String message2 = (String) facade.joinGame(login.authToken(), 1, ChessGame.TeamColor.WHITE).data();
        assertEquals(ALREADY_TAKEN, message2);
        String message3 = (String) facade.joinGame(login.authToken(), 100, ChessGame.TeamColor.BLACK).data();
        assertEquals(BAD_REQUEST, message3);
    }
    private void registerTestUsers() {
        facade.register(USER_1);
        facade.register(USER_2);
        facade.register(USER_3);
    }
}
