package client;

import chess.*;
import records.ClientMessage;
import records.GameDAO;

import java.util.Scanner;

import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

public class ClientUI {

    enum UserState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        EXIT
    }

    private UserState state;
    private ChessClient client;

    public ClientUI(ChessClient client) {
        state = UserState.UNAUTHENTICATED;
        this.client = client;
        System.out.println(SET_BG_COLOR_BLACK);
    }

    public void Repl() {
        ClientMessage result = new ClientMessage(false, "", null);
        while (true) {
            Scanner stdin = new Scanner(System.in);
            switch (state) {
                case UNAUTHENTICATED -> {
                    System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
                    String input = stdin.nextLine();
                    String command = new Scanner(input).next();
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
                    } /* else if (command.equals("print")) {
                        try {
                            ChessGame game = new ChessGame();
                            game.makeMove(new ChessMove(new ChessPosition(2, 4), new ChessPosition(4, 4), null));
                            System.out.println(printGame(new GameDAO(10, null, null, "Test", game)));
                        } catch (InvalidMoveException e) {}
                    }

                    /* THE CODE ABOVE WAS ADDED FOR TESTING */
                }
                case AUTHENTICATED -> {
                    System.out.println("You're logged in. Enter commands to play. Enter 'help' to see available commands.");
                    String input = stdin.nextLine();
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
                    } else if (command.equals("list")) {
                        result = client.listGames();
                    } else if (command.equals("observe")) {
                        result = client.observeGame(input);
                    };
                }
            }
            if (result.authd()) {
                state = UserState.AUTHENTICATED;
            } else {
                state = UserState.UNAUTHENTICATED;
            }
            System.out.println(printResult(result));
        }
    }

    private String printResult(ClientMessage msg) {
        StringBuilder str = new StringBuilder();
        if (msg.toPrint() == null) {
            return "";
        }
        str.append(msg.toPrint()).append('\n');
        if (msg.gamesToPrint() == null) {
            return str.toString();
        }
        for (GameDAO game: msg.gamesToPrint()) {
            str.append(printGame(game)).append('\n');
        }
        return str.toString();
    }
    private String printGame(GameDAO game) {
        StringBuilder str = new StringBuilder();
        ChessGame.TeamColor color = game.game().getTeamTurn();
        str.append("------ Game Name: ").append(game.gameName()).append(" ------\n");
        str.append("Game ID: ").append(game.gameID()).append('\n');
        str.append("White player: ").append(game.whiteUsername()).append('\n');
        str.append("Black player: ").append(game.blackUsername()).append('\n');
        if (game.game().getTeamTurn() == WHITE) {
            str.append("White's turn\n");
        } else {
            str.append("Black's turn\n");
        }
        if (color == WHITE) {
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
                str.append(SET_BG_COLOR_BLACK + " \n");
            }
            str.append("  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
        } else {
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
                str.append(SET_BG_COLOR_BLACK + " \n");
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
