import java.io.IOException;

class MessageHandler {
    public static void processMessage(String sender, String recipient, String message, DBconnector db, ClientHandler senderHandler) {
        if (db.isUserOnline(recipient)) {
            ClientHandler recipientHandler = MessengerServer.getClientHandler(recipient);
            if (recipientHandler != null) {
                try {
                    recipientHandler.sendMessage("MESSAGE: " + sender + " " + message);
                    senderHandler.sendMessage("MESSAGE_DELIVERED");
                    db.saveMessage(sender, recipient, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            db.saveMessage(sender, recipient, message);

        }
    }
}
