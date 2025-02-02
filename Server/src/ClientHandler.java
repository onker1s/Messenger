import java.io.*;
import java.net.Socket;
import java.util.Arrays;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DBconnector db;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.db = new DBconnector();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Received: " + request);
                handleRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnectUser();
        }
    }

    private void handleRequest(String request) throws IOException {
        String[] parts = request.split(" ");

        if (parts.length == 0) return;

        switch (parts[0]) {
            case "REGISTER":
                handleRegister(parts);
                break;
            case "LOGIN":
                handleLogin(parts);
                break;
            case "MESSAGE":
                handleMessage(parts);
                break;
            case "CHECK_USER_EXIST":
                handleCheckUserExist(parts);
                break;
            default:
                sendResponse("UNKNOWN_COMMAND");
        }
    }

    private void handleRegister(String[] parts) throws IOException {
        if (parts.length == 4) {
            String username = parts[1];
            String password = parts[2];
            String IP = parts[3];

            if (db.registerUser(username, password, IP)) {
                sendResponse("REGISTER_SUCCESS");
            } else {
                sendResponse("REGISTER_FAIL");
            }
        } else {
            sendResponse("INVALID_REGISTER_FORMAT");
        }
    }

    private void handleLogin(String[] parts) throws IOException {
        if (parts.length == 4) {
            this.username = parts[1];
            String password = parts[2];
            String currentIP = parts[3];
            if (db.loginUser(username, password,currentIP)) {
                db.setUserStatus(username, 1); // Пользователь онлайн
                MessengerServer.addClient(username, this);
                sendResponse("LOGIN_SUCCESS");

                // Отправляем непрочитанные сообщения
                String messages = db.getUnreadMessages(username);
                if (!messages.isEmpty()) {
                    sendResponse("UNREAD_MESSAGES " + messages);
                }
            } else {
                sendResponse("LOGIN_FAIL");
            }
        } else {
            sendResponse("INVALID_LOGIN_FORMAT");
        }
    }

    private void handleMessage(String[] parts) throws IOException {
        if (parts.length <= 3) {
            sendResponse("INVALID_MESSAGE_FORMAT");
            return;
        }

        // Сохраняем первые три слова
        String senderName = parts[1];
        String recipientName = parts[2];
        System.out.println("ИМЕНА: "+senderName+recipientName);
        // Удаляем первые три слова
        String[] wordsInMessage = Arrays.copyOfRange(parts, 3, parts.length);

        // Преобразуем оставшиеся слова в строку с пробелами
        String message = String.join(" ", wordsInMessage);

        MessageHandler.processMessage(senderName, recipientName, message, db, this);
    }

    private void handleCheckUserExist(String[] parts) throws IOException {
        if(db.isUserExists(parts[1])){
            sendResponse("USER_EXIST TRUE " + parts[1]);
        }
        else{
            sendResponse("USER_EXIST FALSE " + parts[1]);
        }
    }

    public void sendMessage(String message) throws IOException {
        out.write(message);
        out.newLine();
        out.flush();
    }

    public void sendResponse(String response) throws IOException {
        out.write(response);
        out.newLine();
        out.flush();
    }

    private void disconnectUser(){
        if(username != null) {
            db.setUserStatus(username,0);
            MessengerServer.removeClient(username); // Удаляем клиента
        }
        try {
            clientSocket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


}





