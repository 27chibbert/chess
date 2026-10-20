package records;

import chess.ChessGame;

public record GameDAO(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

}
