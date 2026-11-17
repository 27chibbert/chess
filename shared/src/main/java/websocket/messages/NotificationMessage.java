package websocket.messages;

public class NotificationMessage extends ServerMessage {

    private String notification;

    public NotificationMessage(ServerMessageType messageType, String notification) {
        super(messageType);
        this.notification = notification;
    }

    public String getNotification() {
        return notification;
    }
}
