package chess;

public class GameFlags {
    private boolean blackACastle;
    private boolean whiteACastle;
    private boolean blackHCastle;
    private boolean whiteHCastle;

    public enum GameFlag {WhiteA, WhiteH, BlackA, BlackH};

    public GameFlags() {
        blackACastle = true;
        blackHCastle = true;
        whiteACastle = true;
        whiteHCastle = true;
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
}
