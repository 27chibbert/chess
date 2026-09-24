package chess;

public class GameFlags {
    private boolean blackACastle;
    private boolean whiteACastle;
    private boolean blackHCastle;
    private boolean whiteHCastle;
    private ChessPosition enPassantCapturable;


    public enum GameFlag {WhiteA, WhiteH, BlackA, BlackH};

    public GameFlags() {
        blackACastle = true;
        blackHCastle = true;
        whiteACastle = true;
        whiteHCastle = true;
        enPassantCapturable = null;
    }

    public void dropFlag(GameFlag flag) {
        switch (flag) {
            case WhiteA -> whiteACastle = false;
            case WhiteH -> whiteHCastle = false;
            case BlackA -> blackACastle = false;
            case BlackH -> blackHCastle = false;
        }
    }
    public boolean getFlag(GameFlag flag) {
        switch (flag) {
            case WhiteA -> {
                return whiteACastle;
            }
            case WhiteH -> {
                return whiteHCastle;
            }
            case BlackA -> {
                return blackACastle;
            }
            case BlackH -> {
                return blackHCastle;
            }
        }
        return false;
    }
    public void setEnPassantCapturable(ChessPosition position) {
        enPassantCapturable = position;
    }
    public ChessPosition getEnPassantCapturable() {
        return enPassantCapturable;
    }
}
