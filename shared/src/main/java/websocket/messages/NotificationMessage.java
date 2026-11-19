package websocket.messages;

public class NotificationMessage extends ServerMessage {

    private String message;

    public NotificationMessage(ServerMessageType messageType, String notification) {
        super(messageType);
        this.message = notification;
    }

    public String getNotification() {
        return message;
    }
}
