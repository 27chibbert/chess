package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    private boolean playing;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, boolean playing) {
        super(commandType, authToken, gameID);
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }
}
