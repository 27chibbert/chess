package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String message;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        message = errorMessage;
    }

    public String getError() {
        return message;
    }
}
