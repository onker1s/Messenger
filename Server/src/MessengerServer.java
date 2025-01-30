import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessengerServer {
    private static final int PORT = 4567;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    // Храним активные подключения
    private static final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private static StatusUpdater statusUpdater = new StatusUpdater(activeClients);
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
                threadPool.execute(statusUpdater);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для добавления клиента в список активных
    public static void addClient(String username, ClientHandler handler) {
        activeClients.put(username, handler);
        statusUpdater.updateStatus(activeClients);
    }

    // Метод для удаления клиента при отключении
    public static void removeClient(String username) {
        activeClients.remove(username);
        statusUpdater.updateStatus(activeClients);
    }

    // Метод для получения ClientHandler по имени пользователя
    public static ClientHandler getClientHandler(String username) {
        return activeClients.get(username);
    }
}
