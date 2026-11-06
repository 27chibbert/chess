package client;

import java.util.Scanner;

import chess.*;

import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChessClient {
    enum UserState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        REGISTERING,
        AUTHENTICATING,
        HELP,
        AUTHENTICATEDHELP,
        EXIT
    }

    private UserState state;
    private String username;
    private String password;
    private String authToken;

    BiFunction<String, Integer, String> getToken = (input, n) -> {
        if (input == null) return "";
        String[] tokens = input.trim().split("\\s+");
        if (n >= 0 && n < tokens.length) {
            return tokens[n];
        } else {
            return "";
        }
    };
    public void startClient() {
        state = UserState.UNAUTHENTICATED;
        username = null;
        password = null;
        authToken = null;
        Repl();
    }

    public void Repl() {

        while (state != UserState.EXIT) {
            Scanner stdin = new Scanner(System.in);
            switch (state) {
                case UNAUTHENTICATED -> {
                    System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
                    String inputLine = stdin.nextLine();
                    String input = getToken.apply(inputLine, 0);
                    if (input == null) {
                        System.out.println("Invalid command. Try again\n");
                    } else if (input.equals("help")) {
                        state = UserState.HELP;
                    } else if (input.equals("register")) {
                        state = UserState.REGISTERING;
                    } else if (input.equals("login")) {
                        login(inputLine);
                        state = UserState.AUTHENTICATING;
                    } else if (input.equals("quit")) {
                        state = UserState.EXIT;
                    }
                }
                case AUTHENTICATED -> {
                    System.out.println("You've logged in! Enter commands to play. Enter 'help' to see available commands.");
                    state = UserState.UNAUTHENTICATED;
                }
                case REGISTERING -> {
                    System.out.println("Placeholder");
                    state = UserState.UNAUTHENTICATED;
                }
                case AUTHENTICATING -> {
                    System.out.println("Placeholder");
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

    private void login(String input) {
        if (getToken.apply(input, 1) != null && getToken.apply(input, 2) != null) {
            username = getToken.apply(input, 1);
            password = getToken.apply(input,2);
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
                "'quit' - Quit the chess app\n"
                "'create <NAME>' - Start a new game" +
                "'list' - See all existing games on the server\n" +
                "'join <ID>' - Join a game and play\n" +
                "'observe <ID>' - Observe an active game\n"
        );
    }
}
