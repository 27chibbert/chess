package client;

import chess.*;
import records.ClientMessage;
import records.GameDAO;

import java.util.*;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static ui.EscapeSequences.*;

public class ClientUI {

    private final ChessClient client;
    private ChessGame.TeamColor playAndPrintAs;
    private GameDAO liveGame;

    public ClientUI(ChessClient client) {
        this.client = client;
        System.out.println(SET_BG_COLOR_BLACK);
        playAndPrintAs = null;
        liveGame = null;
    }

    public void repl() {
        while (true) {
            ClientMessage result = new ClientMessage(false, "", null);
            playAndPrintAs = null;
            Scanner stdin = new Scanner(System.in);
            System.out.println("Welcome to the chess app. Enter commands to play. Enter 'help' to get help");
            String input = stdin.nextLine();
            //System.out.println(ERASE_SCREEN);
            String command = null;
            try {
                command = new Scanner(input).next();
            } catch (NoSuchElementException _) {}
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
            boolean stop = false;
            if (result.authd()) {
                stop = authenticatedLoop();
            }
            if (stop) {
                return;
            }
            System.out.println(result.toPrint());
        }
    }
    public boolean authenticatedLoop() {
        Scanner stdin = new Scanner(System.in);
        while (true) {
            ClientMessage result = new ClientMessage(false, "", null);
            System.out.println("You're logged in as " + client.getUsername() +
                    ". Enter commands to play. Enter 'help' to see available commands.");
            String input = stdin.nextLine();
            System.out.println(ERASE_SCREEN);
            String command = new Scanner(input).next();
            if (command == null) {
                System.out.println("Invalid command, try again\n");
            } else if (command.equals("logout")) {
                result = client.logout();
                return false;
            } else if (command.equals("help")) {
                printAuthHelp();
            } else if (command.equals("quit")) {
                client.logout();
                System.out.println("Goodbye. Play again soon!");
                return true;
            } else if (command.equals("create")) {
                result = client.createGame(input);
            } else if (command.equals("join")) {
                result = client.joinGame(input);
                if (result.gamesToPrint() != null && input.toUpperCase().contains("WHITE")) {
                    playAndPrintAs = WHITE;
                    result = liveLoop(result, true);
                } else if (result.gamesToPrint() != null && input.toUpperCase().contains("BLACK")) {
                    playAndPrintAs = BLACK;
                    result = liveLoop(result, true);
                }
            } else if (command.equals("list")) {
                result = client.listGames();
            } else if (command.equals("observe")) {
                playAndPrintAs = WHITE;
                result = client.observeGame(input);
                if (result.gamesToPrint() == null) {
                    result = new ClientMessage(true, "No game has that number", null);
                } else {
                    result = liveLoop(result, false);
                }
            }
            if (result.toPrint().isEmpty()) {
                System.out.println("Invalid command");
            } else {
                System.out.println(printResult(result, playAndPrintAs));
            }
        }
    }
    public ClientMessage liveLoop(ClientMessage print, boolean playing) {
        liveGame = print.gamesToPrint()[0];
        String command = "";
        Scanner stdin = new Scanner(System.in);
        while (!command.equals("leave")) {
            System.out.println(EMPTY + "Now seeing a live game. Type \"help\" for options");
            command = stdin.nextLine();
            if (command.startsWith("help")) {
                if (playing) {
                    printGameHelp();
                } else {
                    printObserveHelp();
                }
            } else if (command.startsWith("redraw")) {
                System.out.println(printGame(liveGame, null));
            } else if (command.length() >= 12 && command.startsWith("highlight")) {
                ChessPosition highlight = getPositionFromCommand(command.substring(10,12));
                if (highlight == null) {
                    System.out.println("Bad command format. Write the square in this form 'a1'");
                } else {
                    System.out.println(printGame(liveGame, highlight));
                }
            } else if (playing && command.length() >= 10 && command.startsWith("move")) {
                ChessPosition start = getPositionFromCommand(command.substring(5,7));
                ChessPosition end = getPositionFromCommand(command.substring(8,10));
                ChessPiece.PieceType promotion = null;
                if (liveGame.game().getTeamTurn() != playAndPrintAs) {
                    System.out.println("It's not your turn. You can not make moves yet");
                    continue;
                }
                if (start == null || end == null) {
                    System.out.println("Bad command format. Write the square in this form 'a1'");
                    continue;
                }
                if (liveGame.game().isInCheckmate(playAndPrintAs)) {
                    System.out.println("Game is over. Can not make moves");
                    continue;
                }
                if (liveGame.game().getBoard().getPiece(start) == null) {
                    System.out.println("No piece is located on that square.");
                    continue;
                }
                if ((end.getRank() == 1 || end.getRank() == 8) &&
                        liveGame.game().getBoard().getPiece(start).getPieceType() == ChessPiece.PieceType.PAWN) {
                    promotion = promotionPrompt();
                }
                ChessMove moveToPlay = new ChessMove(start, end, promotion);
                if (liveGame.game().validMoves(start).contains(moveToPlay)) {
                    System.out.println("Making move");
                    client.makeMove(moveToPlay);
                } else {
                    System.out.println("Invalid move");
                }
            } else if (playing && command.startsWith("resign")) {
                System.out.println("Are you sure you want to resign, flip the board, and lose?\nType 'yes' to confirm");
                command = stdin.nextLine();
                if (command.startsWith("yes")) {
                    client.resign();
                } else {
                    System.out.println("Continuing game.");
                }
            }
        }
        client.leaveGame(liveGame.gameID(), playing);
        return new ClientMessage(true, "Stopped observing game", null);
    }

    public void notify(String notification) {
        System.out.println(notification);
    }

    public void loadGame() {
        System.out.println(printGame(liveGame, null));
    }
    public void setLiveGame(GameDAO game) {
        liveGame = game;
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
    private ChessPiece.PieceType promotionPrompt() {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Your pawn is going to promote! Type either 'queen', 'rook', 'knight', or 'bishop' for the piece you want to promote to.");
        while (true) {
            String input = stdin.next().toLowerCase();
            switch (input) {
                case "queen" -> {
                    return ChessPiece.PieceType.QUEEN;
                }
                case "rook" -> {
                    return ChessPiece.PieceType.ROOK;
                }
                case "knight" -> {
                    return ChessPiece.PieceType.KNIGHT;
                }
                case "bishop" -> {
                    return ChessPiece.PieceType.BISHOP;
                }
                default -> System.out.println("Invalid input. Type either 'queen', 'rook', 'knight', or 'bishop'.");
            }
        }
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
            str.append(printGame(game, null)).append('\n');
        }
        return str.toString();
    }
    private String printGame(GameDAO game, ChessPosition highlight) {
        StringBuilder str = new StringBuilder();
        //ChessGame.TeamColor color = game.game().getTeamTurn();
        Collection<ChessPosition> squares = new ArrayList<>();
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
        if (highlight != null && game.game().getBoard().getPiece(highlight) != null) {
            Collection<ChessMove> moves = game.game().validMoves(highlight);
            for (ChessMove move : moves) {
                squares.add(move.getEndPosition());
            }
        }
        if (playAndPrintAs == null || playAndPrintAs == WHITE) {
            str.append("\u3000  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
            for (int rank = 8; rank >= 1; rank -= 1) {
                str.append(" ").append(rank).append(" ");
                for (int file = 1; file <= 8; file += 1) {
                    colorSpace(str, rank, file, squares, highlight);
                    str.append(printSpace(game, rank, file));
                }
                str.append(SET_BG_COLOR_BLACK + " ").append(rank).append('\n');
            }
            str.append("\u3000  \u2009A \u3000B \u3000C \u3000D \u3000E \u3000F \u3000G \u3000H  \n");
        } else {
            str.append("\u3000  H\u3000 G\u3000 F\u3000 E\u3000 D\u3000 C\u3000 B\u3000 A  \n");
            for (int rank = 1; rank <= 8; rank += 1) {
                str.append(" ").append(rank).append(" ");
                for (int file = 8; file >= 1; file -= 1) {
                    colorSpace(str, rank, file, squares, highlight);
                    str.append(printSpace(game, rank, file));
                }
                str.append(SET_BG_COLOR_BLACK + " ").append(rank).append('\n');
            }
            str.append("\u3000  H\u3000 G\u3000 F\u3000 E\u3000 D\u3000 C\u3000 B\u3000 A  \n");
        }
        if (game.game().isInCheck(WHITE)) {
            str.append(String.format("White player %s is in check!\n", game.whiteUsername()));
        }
        if (game.game().isInCheckmate(BLACK)) {
            str.append(String.format("Black player %s is in check!\n", game.blackUsername()));
        }
        if (game.game().getResigned() != null && game.game().getResigned() == WHITE) {
            str.append("White has resigned. Black wins the game!\n");
            return str.toString();
        } else if (game.game().getResigned() == BLACK) {
            str.append("Black has resigned. White wins the game!\n");
            return str.toString();
        }
        if (game.game().isInCheckmate(WHITE)) {
            str.append("White is in Checkmate! Black has won!\n");
            return str.toString();
        } else if (game.game().isInCheckmate(BLACK)) {
            str.append("Black is in Checkmate! White has won!\n");
            return str.toString();
        } else {
            if (game.game().getTeamTurn() == WHITE && game.whiteUsername() != null) {
                str.append(String.format("%s's turn for White\n", game.whiteUsername()));
            } else if (game.game().getTeamTurn() == WHITE && game.whiteUsername() == null) {
                str.append("White's turn. Waiting for player to join as White\n");
            } else if (game.game().getTeamTurn() == BLACK && game.blackUsername() != null) {
                str.append(String.format("%s's turn for Black\n", game.blackUsername()));
            } else if (game.game().getTeamTurn() == BLACK && game.blackUsername() == null) {
                str.append("Black's turn. Waiting for player to join as Black\n");
            }
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
    private void colorSpace(StringBuilder str, int rank, int file, Collection<ChessPosition> squares, ChessPosition highlight) {
        if (highlight != null && rank == highlight.getRank() && file == highlight.getFile()) {
            str.append(SET_BG_COLOR_BLACK);
            return;
        }
        if ((file + rank) % 2 == 0 && squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_DARK_GREEN);
        } else if ((file + rank) % 2 == 0 && !squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_DARK_GREY);
        } else if (squares.contains(new ChessPosition(rank, file))) {
            str.append(SET_BG_COLOR_GREEN);
        } else {
            str.append(SET_BG_COLOR_LIGHT_GREY);
        }
    }
    private void printHelp() {
        System.out.println(
                """
                        Commands available:
                        'help' - Display available commands
                        'quit' - Exit the program
                        'login <USERNAME> <PASSWORD>' - Login to your account
                        'register <USERNAME> <PASSWORD> <EMAIL>' - Create a new account
                        """
        );
    }
    private void printAuthHelp() {
        System.out.println(
                """
                        Commands available:
                        'help' - Display available commands
                        'logout' - Log out of your account
                        'quit' - Quit the chess app
                        'create <NAME>' - Start a new game\
                        'list' - See all existing games on the server
                        'join <ID> [WHITE|BLACK]' - Join a game and play
                        'observe <ID>' - Observe an active game
                        """
        );
    }
    private void printObserveHelp() {
        System.out.println("""
                'leave' - Stop observing
                'redraw' - Redraw the current chess board
                'highlight <square>' - Show the moves the piece on the given square can do. Example: highlight e2
                """);
    }
    private void printGameHelp() {
        System.out.println(
                """
                        Commands available
                        'help' - see this menu
                        'redraw' - redraw the chess board
                        'leave' - leave the game (does not resign)
                        'resign' - resign the game (does not leave)
                        'move' <start square> <end square> - do a move, example: 'move e2 e4'
                        'highlight <square> - shows all possible moves of the piece on that square"""
        );
    }
}
