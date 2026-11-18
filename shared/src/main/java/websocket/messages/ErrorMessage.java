package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private String message;

    public ErrorMessage(ServerMessageType messageType, String errorMessage) {
        super(messageType);
        message = errorMessage;
    }

    public String getError() {
        return message;
    }
}
