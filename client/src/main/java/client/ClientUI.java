package client;

import chess.*;
import records.ClientMessage;
import records.GameDAO;

import java.util.NoSuchElementException;
import java.util.Scanner;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

public class ClientUI {

    public enum UserState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        OBSERVING,
        PLAYING
    }

    private UserState state;
    private ChessClient client;
    private ChessGame.TeamColor printAs;
    private GameDAO liveGame;

    public ClientUI(ChessClient client) {
        state = UserState.UNAUTHENTICATED;
        this.client = client;
        System.out.println(SET_BG_COLOR_BLACK);
        printAs = null;
        liveGame = null;
    }

    public void repl() {
        while (true) {
            ClientMessage result = new ClientMessage(false, "", null);
            printAs = null;
            Scanner stdin = new Scanner(System.in);
            switch (state) {
                case UNAUTHENTICATED -> {
                    System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
                    String input = stdin.nextLine();
                    //System.out.println(ERASE_SCREEN);
                    String command = null;
                    try {
                        command = new Scanner(input).next();
                    } catch (NoSuchElementException e) {
                        command = null;
                    }
                    if (command == null) {
                        System.out.println("Invalid command. Try again\n");
                    } else if (command.equals("help")) {
                        printHelp();
                    } else if (command.equals("register")) {
                        result = client.register(input);
                    } else if (command.equals("login")) {
                        result = client.login(input);
                    } else if (command.equals("quit")) {
                        return;
                    } else {
                        System.out.println("Invalid command.");
                    }
                }
                case AUTHENTICATED -> {
                    System.out.println("You're logged in. Enter commands to play. Enter 'help' to see available commands.");
                    String input = stdin.nextLine();
                    System.out.println(ERASE_SCREEN);
                    String command = new Scanner(input).next();
                    if (command == null) {
                        System.out.println("Invalid command, try again\n");
                    } else if (command.equals("logout")) {
                        result = client.logout();
                    } else if (command.equals("help")) {
                        printAuthHelp();
                    } else if (command.equals("quit")) {
                        client.logout();
                        System.out.println("Goodbye. Play again soon!");
                        return;
                    } else if (command.equals("create")) {
                        result = client.createGame(input);
                    } else if (command.equals("join")) {
                        result = client.joinGame(input);
                        if (result.gamesToPrint() != null) {
                            if (input.toUpperCase().contains("WHITE")) {
                                printAs = WHITE;
                            } else {
                                printAs = BLACK;
                            }
                        }
                    } else if (command.equals("list")) {
                        result = client.listGames();
                    } else if (command.equals("observe")) {
                        state = ClientUI.UserState.OBSERVING;
                        printAs = WHITE;
                        result = client.observeGame(input);
                        if (result.gamesToPrint() == null) {
                            result = new ClientMessage(true, "No game has that number", null);
                        } else {
                            result = observeLiveLoop(result);
                        }
                    };
                }
            }
            if (result.authd()) {
                state = UserState.AUTHENTICATED;
            } else {
                state = UserState.UNAUTHENTICATED;
            }
            System.out.println(printResult(result, printAs));
        }
    }

    public ClientMessage observeLiveLoop(ClientMessage print) {
        liveGame = print.gamesToPrint()[0];
        System.out.println(printResult(print, WHITE));
        String command = "";
        Scanner stdin = new Scanner(System.in);
        while (!command.equals("leave")) {
            System.out.println(EMPTY + "Now seeing a live game. Type \"help\" for options");
            command = stdin.next();
            if (command.equals("help")) {
                System.out.println("leave - Stop observing");
            }
        }
        return new ClientMessage(true, "Stopped observing game", null);
    }


    private String printResult(ClientMessage msg, ChessGame.TeamColor printAs) {
        StringBuilder str = new StringBuilder();
        if (msg.toPrint() == null) {
            return "";
        }
        str.append(msg.toPrint()).append('\n');
        if (msg.gamesToPrint() == null) {
            return str.toString();
        }
        for (GameDAO game: msg.gamesToPrint()) {
            str.append(printGame(game, printAs)).append('\n');
        }
        return str.toString();
    }
    private String printGame(GameDAO game, ChessGame.TeamColor printAs) {
        StringBuilder str = new StringBuilder();
        ChessGame.TeamColor color = game.game().getTeamTurn();
        if (printAs == null) {
            printAs = game.game().getTeamTurn();
        }
        str.append(SET_TEXT_COLOR_BLUE).append(SET_TEXT_BLINKING).append(SET_TEXT_BOLD).append("------ Game # ").append(game.gameID()).append(" - ")
                .append(game.gameName()).append(" ------\n").append(RESET_TEXT_BOLD_FAINT).append(RESET_TEXT_BLINKING).append(RESET_TEXT_COLOR);
        if (game.whiteUsername() == null) {
            str.append("White player: -- open -- \n");
        } else {
            str.append("White player: ").append(game.whiteUsername()).append('\n');
        }
        if (game.blackUsername() == null) {
            str.append("Black player: -- open -- \n");
        } else {
            str.append("Black player: ").append(game.blackUsername()).append('\n');
        }
        if (game.game().getTeamTurn() == WHITE) {
            str.append("White's turn\n");
        } else {
            str.append("Black's turn\n");
        }
        if (printAs == WHITE) {
            str.append("  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
            for (int rank = 8; rank >= 1; rank -= 1) {
                str.append(rank).append(" ");
                for (int file = 1; file <= 8; file += 1) {
                    if ((file + rank) % 2 == 0) {
                        str.append(SET_BG_COLOR_DARK_GREY);
                    } else {
                        str.append(SET_BG_COLOR_LIGHT_GREY);
                    }
                    str.append(printSpace(game, rank, file));
                }
                str.append(SET_BG_COLOR_BLACK + " ").append(rank).append('\n');
            }
            str.append("  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
        } else {
            str.append("   H\u3000 G\u3000 F\u3000 E\u3000 D\u3000 C\u3000 B\u3000 A  \n");
            for (int rank = 1; rank <= 8; rank += 1) {
                str.append(rank).append(" ");
                for (int file = 8; file >= 1; file -= 1) {
                    if ((file + rank - 1) % 2 == 0) {
                        str.append(SET_BG_COLOR_DARK_GREY);
                    } else {
                        str.append(SET_BG_COLOR_LIGHT_GREY);
                    }
                    str.append(printSpace(game, rank, file));
                }
                str.append(SET_BG_COLOR_BLACK + " ").append(rank).append('\n');
            }
            str.append("   H\u3000 G\u3000 F\u3000 E\u3000 D\u3000 C\u3000 B\u3000 A  \n");
        }
        return str.toString();
    }
    private String printSpace(GameDAO game, int rank, int file) {
        ChessPiece piece = game.game().getBoard().getPiece(new ChessPosition(rank ,file));
        if (piece == null) {
            return EMPTY;
        }
        if (piece.getTeamColor() == WHITE) {
            switch (piece.getPieceType()) {
                case PAWN -> {
                    return WHITE_PAWN;
                }
                case KNIGHT -> {
                    return WHITE_KNIGHT;
                }
                case BISHOP -> {
                    return WHITE_BISHOP;
                }
                case ROOK -> {
                    return WHITE_ROOK;
                }
                case QUEEN -> {
                    return WHITE_QUEEN;
                }
                case KING -> {
                    return WHITE_KING;
                }
            }
        } else {
            switch (piece.getPieceType()) {
                case PAWN -> {
                    return BLACK_PAWN;
                }
                case KNIGHT -> {
                    return BLACK_KNIGHT;
                }
                case BISHOP -> {
                    return BLACK_BISHOP;
                }
                case ROOK -> {
                    return BLACK_ROOK;
                }
                case QUEEN -> {
                    return BLACK_QUEEN;
                }
                case KING -> {
                    return BLACK_KING;
                }
            }
        }
        return "BROKEN";
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
                        "'join <ID> [WHITE|BLACK]' - Join a game and play\n" +
                        "'observe <ID>' - Observe an active game\n"
        );
    }
}
