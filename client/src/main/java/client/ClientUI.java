package client;

import chess.*;
import records.ClientMessage;
import records.GameDAO;

import java.util.ArrayList;
import java.util.Collection;
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
                            result = liveLoop(result, true);
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
                            result = liveLoop(result, false);
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

    public ClientMessage liveLoop(ClientMessage print, boolean playing) {
        liveGame = print.gamesToPrint()[0];
        System.out.println(printResult(print, WHITE));
        String command = "";
        Scanner stdin = new Scanner(System.in);
        while (!command.equals("leave")) {
            System.out.println(EMPTY + "Now seeing a live game. Type \"help\" for options");
            command = stdin.nextLine();
            if (command.length() >= 4 && command.substring(0, 4).equals("help")) {
                if (playing) {
                    printGameHelp();
                } else {
                    printObserveHelp();
                }
            } else if (command.length() >= 6 && command.substring(0, 6).equals("redraw")) {
                System.out.println(printGame(liveGame, WHITE, null));
            } else if (command.length() >= 12 && command.substring(0, 9).equals("highlight")) {
                ChessPosition highlight = getPositionFromCommand(command.substring(10,12));
                if (highlight == null) {
                    System.out.println("Bad command format. Write the square in this form 'a1'");
                } else {
                    System.out.println(printGame(liveGame, printAs, highlight));
                }
            } else if (playing && command.length() >= 10 && command.substring(0,4).equals("move")) {
                ChessPosition start = getPositionFromCommand(command.substring(5,7));
                ChessPosition end = getPositionFromCommand(command.substring(8,10));
                if (start == null || end == null) {
                    System.out.println("Bad command format. Write the square in this form 'a1'");
                    continue;
                }
                ChessMove moveToPlay = new ChessMove(start, end, null);
                if (liveGame.game().validMoves(start).contains(moveToPlay)) {
                    System.out.println("Making move");
                } else {
                    System.out.println("Invalid move");
                }
            } else if (playing && command.length() >= 6 && command.substring(0,6).equals("resign")) {

            }
        }
        return new ClientMessage(true, "Stopped observing game", null);
    }

    public ClientMessage playingLiveLoop(ClientMessage print, ChessGame.TeamColor printAs) {
        liveGame = print.gamesToPrint()[0];
        System.out.println(printResult(print, printAs));
        Scanner stdin = new Scanner(System.in);
        String command = "";
        while (!command.equals("leave")) {
            if (command.equals("help")) {
                printGameHelp();
            } else if (true) {

            } else if (command.equals("resign")) {

            } else if (command.equals("move")) {

            } else if (command.equals("highlight")) {

            }
        }
        client.leaveGame();
        return new ClientMessage(true, "Left game", null);
    }

    public void notify(String notification) {
        System.out.println(notification);
    }

    public void loadGame(GameDAO game) {
        System.out.println(printGame(game, printAs, null));
    }

    private ChessPosition getPositionFromCommand(String str) {
        if (str.length() < 2) {
            return null;
        }
        char fileChar = str.toLowerCase().charAt(0);
        int rank = 0;
        try {
            rank = Integer.parseInt(str.substring(1,2));
        } catch (NumberFormatException e) {
            return null;
        }
        int file = 0;
        switch (fileChar) {
            case 'a' -> file = 1;
            case 'b' -> file = 2;
            case 'c' -> file = 3;
            case 'd' -> file = 4;
            case 'e' -> file = 5;
            case 'f' -> file = 6;
            case 'g' -> file = 7;
            case 'h' -> file = 8;
        }
        return new ChessPosition(rank, file);
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
            str.append(printGame(game, printAs, null)).append('\n');
        }
        return str.toString();
    }
    private String printGame(GameDAO game, ChessGame.TeamColor printAs, ChessPosition highlight) {
        StringBuilder str = new StringBuilder();
        ChessGame.TeamColor color = game.game().getTeamTurn();
        Collection<ChessPosition> squares = new ArrayList<>();
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
        if (highlight != null) {
            Collection<ChessMove> moves = game.game().validMoves(highlight);
            for (ChessMove move : moves) {
                squares.add(move.getEndPosition());
            }
        }
        if (printAs == WHITE) {
            str.append("  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
            for (int rank = 8; rank >= 1; rank -= 1) {
                str.append(rank).append(" ");
                for (int file = 1; file <= 8; file += 1) {
                    colorSpace(str, rank, file, squares, highlight, printAs);
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
                    colorSpace(str, rank, file, squares, highlight, printAs);
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
    private void colorSpace(StringBuilder str, int rank, int file, Collection<ChessPosition> squares, ChessPosition highlight, ChessGame.TeamColor color) {
        if (highlight != null && rank == highlight.getRank() && file == highlight.getFile()) {
            str.append(SET_BG_COLOR_BLACK);
            return;
        }
        int offset = 0;
        if (color == BLACK) {
            offset = 1;
        }
        if ((file + rank - offset) % 2 == 0 && squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_DARK_GREEN);
        } else if ((file + rank - offset) % 2 == 0 && !squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_DARK_GREY);
        } else if (squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_GREEN);
        } else {
            str.append(SET_BG_COLOR_LIGHT_GREY);
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
                        "'join <ID> [WHITE|BLACK]' - Join a game and play\n" +
                        "'observe <ID>' - Observe an active game\n"
        );
    }
    private void printObserveHelp() {
        System.out.println("'leave' - Stop observing\n" +
                "'redraw' - Redraw the current chess board\n" +
                "'highlight <square>' - Show the moves the piece on the given square can do. Example: highlight e2\n");
    }
    private void printGameHelp() {
        System.out.println(
                "Commands available\n" +
                        "'help' - see this menu\n" +
                        "'redraw' - redraw the chess board\n" +
                        "'leave' - leave the game (does not resign)\n" +
                        "'resign' - resign the game (does not leave)\n" +
                        "'move' <start square> <end square> - do a move, example: 'move e2 e4'\n" +
                        "'highlight <square> - shows all possible moves of the piece on that square"
        );
    }
}
