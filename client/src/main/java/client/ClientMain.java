package client;

import chess.*;

import java.util.Scanner;

enum UserState {
    UNAUTHENTICATED,
    AUTHENTICATED,
    REGISTERING,
    AUTHENTICATING,
    HELP,
    AUTHENTICATEDHELP,
    EXIT
}

public class ClientMain {
    public static void main(String[] args) {
        Repl();
        //var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        //System.out.println("♕ 240 Chess Client: " + piece);
    }

    public static void Repl() {
        UserState state = UserState.UNAUTHENTICATED;

        while (state != UserState.EXIT) {
            Scanner stdin = new Scanner(System.in);
            switch (state) {
                case UNAUTHENTICATED -> {
                    System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
                    String input = stdin.nextLine();
                    if (input.equals("help")) {
                        state = UserState.HELP;
                    } else if (input.equals("register")) {
                        state = UserState.REGISTERING;
                    } else if (input.equals("login")) {
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
                    System.out.println("Commands available:\n" +
                            "'help' - Display available commands\n" +
                            "'logout' - Log out of your account\n" +
                            "'create game' - Start a new game" +
                            "'list games' - See all existing games on the server\n" +
                            "'play game' - Join a game and play\n" +
                            "'observe game' - Observe an active game\n"
                    );
                    state = UserState.AUTHENTICATED;
                }
                case HELP -> {
                    System.out.println("" +
                            "Commands available:\n" +
                            "'help' - Display available commands\n" +
                            "'quit' - Exit the program\n" +
                            "'login' - Login to your account\n" +
                            "'register' - Create a new account\n"
                    );
                    state = UserState.UNAUTHENTICATED;
                }
            }
        }
    }
}
