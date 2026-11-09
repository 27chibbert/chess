package records;

public record ClientMessage(boolean authd, String toPrint, GameDAO[] gamesToPrint) {
}
