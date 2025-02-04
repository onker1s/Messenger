import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import javax.swing.*;

public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static String sender;
    private final ServerConnection serverConnection;
    private final DefaultListModel<String> dialogsListModel;
    private final JList<String> dialogsList;
    // Хранение открытых окон диалогов
    private final HashMap<String, ChatFrame> openDialogs = new HashMap<>();

    public MainFrame(ServerConnection connection, String sender) {
        super(FRAME_TITLE);
        serverConnection = connection;
        MainFrame.sender = sender;
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        //Отображение списка существующих диалогов
        dialogsListModel = new DefaultListModel<>();
        dialogsList = new JList<>(dialogsListModel);
        JScrollPane scrollPaneDialogs = new JScrollPane(dialogsList);
        dialogsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = dialogsList.getSelectedValue();
                if (selectedUser != null) {
                    String[] parts = selectedUser.split(" ");
                    if (parts[0] != null) {
                        try {
                            openChatWindow(parts[0], true);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                dialogsList.clearSelection();
            }
        });


        // Кнопка "Начать диалог"
        final JButton startChatButton = new JButton("Начать диалог");
        startChatButton.addActionListener(e -> {
            try {
                startNewDialog();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Компоновка элементов
        final GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPaneDialogs)
                        .addComponent(startChatButton))
                .addContainerGap());
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneDialogs)
                .addGap(10)
                .addComponent(startChatButton)
                .addContainerGap());
        requestDialogsList();
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Запуск потока для прослушивания сервера
        new Thread(new IncomingMessageListener()).start();
    }

    private void updateDialogList(String newDialogUser) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = newDialogUser.split(" ");
            if (!dialogsListModel.contains(parts[0]+" (online)") && !dialogsListModel.contains(parts[0]+" (offline)") && !dialogsListModel.contains(parts[0])) {
                dialogsListModel.addElement(parts[0]);
            }
        });
    }
    // Метод для открытия нового окна чата
    private void startNewDialog() throws IOException {
        String recipient = JOptionPane.showInputDialog(this, "Введите имя получателя:");
        if (recipient != null && !recipient.trim().isEmpty() && !Objects.equals(sender, recipient)) {
            checkUserExist(recipient);

        }
        else if(Objects.equals(recipient, sender)){
            JOptionPane.showMessageDialog(MainFrame.this,"Нельзя начать диалог с самим собой", "Ошибка", JOptionPane.ERROR_MESSAGE);          }
    }



    // Метод для открытия окна чата с конкретным пользователем
    private void openChatWindow(String recipient, boolean isUserExist) throws IOException {

        if (!openDialogs.containsKey(recipient) && isUserExist) {
            isUserExist = false;
            updateDialogList(recipient);
            ChatFrame chatFrame = new ChatFrame(serverConnection, recipient, sender,openDialogs);
            openDialogs.put(recipient, chatFrame);
            getMessagesFromServer(recipient);
            chatFrame.setVisible(true);
        } else if(openDialogs.containsKey(recipient)){
            JOptionPane.showMessageDialog(this, "Диалог с этим пользователем уже открыт.");
        }
        else if(!isUserExist){
            JOptionPane.showMessageDialog(this, "Пользователя с именем " + recipient + " не существует");
        }
        else{
            JOptionPane.showMessageDialog(MainFrame.this,
                    "ERROR!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void getMessagesFromServer(String recipient){
        String formattedMessage = "GET_MESSAGES " + recipient + " " + sender;
        serverConnection.sendMessage(formattedMessage);
    }
    private void checkUserExist(String recipient) throws IOException {
        String formattedMessage = "CHECK_USER_EXIST " + recipient;
        serverConnection.sendMessage(formattedMessage);
    }
    private void requestDialogsList() {
        serverConnection.sendMessage("GET_DIALOGS " + sender);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final SignFrame frame = new SignFrame();
            }
        });
    }
    private class IncomingMessageListener implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    String message = serverConnection.receiveMessage();
                    if (message != null && message.startsWith("MESSAGE")) {
                        // Формат сообщения: MESSAGE <отправитель> <сообщение>
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String sender = parts[1];
                            String text = parts[2];

                            // Если окно диалога с отправителем ещё не открыто, открываем его
                            SwingUtilities.invokeLater(() -> {
                                if (!openDialogs.containsKey(sender)) {
                                    try {
                                        updateDialogList(sender);
                                        openChatWindow(sender,true);
                                        synchronized (this) {
                                            try {
                                                wait(500); // Ждем немного, пока сервер ответит
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                            }
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                            });
                        }
                    }
                    else if(message != null && message.startsWith("USER_STATUS_UPDATE")){
                             processStatusUpdate(message);
                    }
                    else if(message!= null && message.startsWith("USER_EXIST"))
                    {
                            String[] parts = message.split(" ", 3);
                            openChatWindow(parts[2],Objects.equals(parts[1], "TRUE"));
                    }
                    else if (message!= null && message.startsWith("DIALOGS_LIST")) {
                        String[] parts = message.split(" ");
                        if(parts.length >= 2) {
                            SwingUtilities.invokeLater(() -> {
                                dialogsListModel.clear();
                                for (int i = 1; i < parts.length; i++) {
                                    dialogsListModel.addElement(parts[i]);
                                }
                            });
                        }
                    }
                    else if(message!= null && message.startsWith("GETTING_MESSAGES"))
                    {
                        String recipient = message.split(" ")[1];
                        String cleanedMessage = message.replace("GETTING_MESSAGES " + recipient + " ", "").trim();
                        String[] messages = cleanedMessage.split("YhUI10125789@fFg6");
                        for(int i = 0; i < messages.length-1; i+=2){
                            if(Objects.equals(messages[i], sender)) {openDialogs.get(recipient).appendMessage("Я", messages[i+1]);}
                            else {openDialogs.get(recipient).appendMessage(messages[i], messages[i+1]);}
                        }

                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка связи с сервером: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }
        private void processStatusUpdate(String message) {
            // Убираем префикс
            String statusData = message.replace("USER_STATUS_UPDATE ", "").trim();
            // Разбираем статусы
            String[] statusEntries = statusData.split(" ");
            for (String entry : statusEntries) {
                String[] parts = entry.split(":");

                if (parts.length == 2) {
                    String username = parts[0];
                    String status = parts[1];

                    // Если есть открытый чат с этим пользователем — обновляем статус
                    if (openDialogs.containsKey(username)) {
                        openDialogs.get(username).updateStatus(status);
                    }
                    updateDialogListWithStatus(username, status);
                }
            }
        }
        private void updateDialogListWithStatus(String username, String status) {
            boolean found = false;

            // Проходим по списку, проверяя, есть ли уже этот пользователь
            for (int i = 0; i < dialogsListModel.getSize(); i++) {
                String currentEntry = dialogsListModel.getElementAt(i);

                // Проверяем, не добавлен ли уже пользователь (без учета статуса)
                if (currentEntry.startsWith(username)) {

                    dialogsListModel.setElementAt(username + " (" + status + ")", i); // Обновляем статус
                    found = true;
                    break;
                }
            }

            // Если пользователя не было в списке, добавляем его
            if (!found) {
                dialogsListModel.addElement(username + " (" + status + ")");
            }
        }

    }
}

