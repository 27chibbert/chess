package client;

import java.util.Scanner;

import chess.*;
import records.AuthDAO;
import records.ServerMessage;
import records.UserDAO;
import records.UserLogin;

import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ChessClient {
    enum UserState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        HELP,
        AUTHENTICATEDHELP,
        EXIT
    }

    private static final ServerFacade webClient = new ServerFacade();
    private UserState state;
    private String username;
    private String authToken;
    private static final Logger logger = Logger.getLogger("Server Log");

    BiFunction<String, Integer, String> getToken = (input, n) -> {
        if (input == null) return "";
        String[] tokens = input.trim().split("\\s+");
        if (n >= 0 && n < tokens.length) {
            return tokens[n];
        } else {
            return "";
        }
    };

    public ChessClient() {
        try {
            FileHandler fd = new FileHandler("logs/client.log", true);
            fd.setFormatter(new SimpleFormatter());
            logger.addHandler(fd);
        } catch (Exception e) {
            System.out.println("Server logger failed to start");
        }
    }

    public void startClient() {
        state = UserState.UNAUTHENTICATED;
        username = null;
        authToken = null;
        Repl();
    }

    public void Repl() {

        while (state != UserState.EXIT) {
            Scanner stdin = new Scanner(System.in);
            switch (state) {
                case UNAUTHENTICATED -> {
                    System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
                    String input = stdin.nextLine();
                    String command = getToken.apply(input, 0);
                    if (command == null) {
                        System.out.println("Invalid command. Try again\n");
                    } else if (command.equals("help")) {
                        state = UserState.HELP;
                    } else if (command.equals("register")) {
                        register(input);
                    } else if (command.equals("login")) {
                        login(input);
                    } else if (command.equals("quit")) {
                        state = UserState.EXIT;
                    }
                }
                case AUTHENTICATED -> {
                    System.out.println("You've logged in! Enter commands to play. Enter 'help' to see available commands.");
                    String input = stdin.nextLine();
                    String command = getToken.apply(input, 0);
                    state = UserState.UNAUTHENTICATED;
                }
                case AUTHENTICATEDHELP -> {
                    printAuthHelp();
                    state = UserState.AUTHENTICATED;
                }
                case HELP -> {
                    printHelp();
                    state = UserState.UNAUTHENTICATED;
                }
            }
        }
    }

    private void register(String input) {
        if (!getToken.apply(input, 1).isEmpty() && !getToken.apply(input, 2).isEmpty() && !getToken.apply(input, 3).isEmpty()) {
            UserDAO user = new UserDAO(getToken.apply(input, 1), getToken.apply(input, 2), getToken.apply(input, 3));
            ServerMessage result = webClient.register(user);
            resolveResponse(result);
        } else {
            System.out.println("Bad command format. Type 'help' for more info");
        }
    }
    private void login(String input) {
        if (!getToken.apply(input, 1).isEmpty() && !getToken.apply(input, 2).isEmpty()) {
            UserLogin login = new UserLogin(getToken.apply(input, 1), getToken.apply(input, 2));
            ServerMessage result = webClient.login(login);
            resolveResponse(result);
        } else {
            System.out.println("Bad command format. Type 'help' for more info");
        }
    }
    private void resolveResponse(ServerMessage msg) {
        if (msg.data().getClass() == String.class) {
            String err = (String) msg.data();
            err = String.format("Error: %s", err);
            System.out.println(err);
        } else if (msg.data().getClass() == AuthDAO.class) {
            AuthDAO auth = (AuthDAO) msg.data();
            username = auth.username();
            authToken = auth.authToken();
            String out = String.format("You logged in as user %s!\n", username);
            System.out.println(out);
            state = UserState.AUTHENTICATED;
            logger.info(String.format("Logged in as user %s with authToken %s", username, authToken));
        }
    }
    private void printHelp() {
        System.out.println(
                "Commands available:\n" +
                "'help' - Display available commands\n" +
                "'quit' - Exit the program\n" +
                "'login <USERNAME> <PASSWORD>' - Login to your account\n" +
                "'register <USERNAME> <PASSWORD> <EMAIL>' - Create a new account\n"
        );
    }
    private void printAuthHelp() {
        System.out.println(
                "Commands available:\n" +
                "'help' - Display available commands\n" +
                "'logout' - Log out of your account\n" +
                "'quit' - Quit the chess app\n" +
                "'create <NAME>' - Start a new game" +
                "'list' - See all existing games on the server\n" +
                "'join <ID>' - Join a game and play\n" +
                "'observe <ID>' - Observe an active game\n"
        );
    }
}
