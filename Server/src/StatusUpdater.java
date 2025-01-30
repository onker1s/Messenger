import java.util.Map;

class StatusUpdater implements Runnable {
    private Map<String, ClientHandler> userActivityMap; // Map<username, ClientHandler>

    public StatusUpdater(Map<String, ClientHandler> userActivityMap) {
        this.userActivityMap = userActivityMap;
    }

    @Override
    public void run() {
        while (true) {

            try {
                Thread.sleep(5000); // Обновляем статусы каждые 5 секунд
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
