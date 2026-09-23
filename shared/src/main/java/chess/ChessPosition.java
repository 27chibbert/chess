package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int file;
    private int rank;
    public ChessPosition(int ran, int fil) {
        file = fil;
        rank = ran;
    }
    /**
     * Determines if the given object is identical to itself
     * @param o   the reference object with which to compare.
     * @return true or false depending on answer
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() != getClass()) return false;
        ChessPosition obj = (ChessPosition) o;
        return toString().equals(obj.toString());
    }
    /**
     * Hashes string representation
     * @return hash of toString method
     */
    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public String toString() {
        String str = "(-";
        switch (file) {
            case 0 -> str += "H";
            case 1 -> str += "G";
            case 2 -> str += "F";
            case 3 -> str += "E";
            case 4 -> str += "D";
            case 5 -> str += "C";
            case 6 -> str += "B";
            case 7 -> str += "A";
        }
        str = str + rank + "-)";
        return str;
    }
    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getFile() {
        return file;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left column
     */
    public int getRank() {
        return rank;
    }

    public int getColumn() {return getRank(); }

    public int getRow() { return getFile(); }
}
