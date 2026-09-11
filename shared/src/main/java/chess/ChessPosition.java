package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int file;
    private int rank;
    public ChessPosition(int row, int col) {
        file = row;
        rank = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return file;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left column
     */
    public int getColumn() {
        return rank;
    }
}
