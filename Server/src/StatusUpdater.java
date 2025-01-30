import java.io.IOException;
import java.util.Map;
import java.util.Set;

class StatusUpdater implements Runnable {
    private Map<String, ClientHandler> userActivityMap; // Активные пользователи
    private final DBconnector dbConnector = new DBconnector();

    public StatusUpdater(Map<String, ClientHandler> userActivityMap) {
        this.userActivityMap = userActivityMap;

    }

    @Override
    public void run() {
        while (true) {
            try {
                updateUserStatusesForClients();
                Thread.sleep(10000); // Отправляем статусы каждые 5 секунд
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserStatusesForClients() throws IOException {
        for (Map.Entry<String, ClientHandler> entry : userActivityMap.entrySet()) {
            String username = entry.getKey();
            ClientHandler clientHandler = entry.getValue();

            // Получаем список собеседников клиента из БД
            Set<String> dialogUsers = dbConnector.getUserDialogPartners(username);

            // Формируем статусное сообщение
            StringBuilder statusMessage = new StringBuilder("USER_STATUS_UPDATE ");

            for (String dialogUser : dialogUsers) {
                if (userActivityMap.containsKey(dialogUser)) {
                    statusMessage.append(dialogUser).append(":online ");
                } else {
                    statusMessage.append(dialogUser).append(":offline ");
                }
            }

            // Отправляем обновление статусов клиенту
            clientHandler.sendMessage(statusMessage.toString().trim());
        }
    }

    public void updateStatus(Map<String, ClientHandler> userActivityMap) {
        this.userActivityMap = userActivityMap;
    }
}
