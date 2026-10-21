package records;

import chess.ChessGame;

public record GameJoin(ChessGame.TeamColor playerColor, int gameID) {
}
