import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;
import javax.swing.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static String sender;
    private final JTextArea textAreaIncoming;
    private final ServerConnection serverConnection;

    // Хранение открытых окон диалогов
    private final HashMap<String, ChatFrame> openDialogs = new HashMap<>();

    public MainFrame(ServerConnection connection, String sender) {
        super(FRAME_TITLE);
        serverConnection = connection;
        this.sender = sender;
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);

        // Текстовая область для отображения полученных сообщений
        textAreaIncoming = new JTextArea(15, 0);
        textAreaIncoming.setEditable(false);
        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);

        // Кнопка "Начать диалог"
        final JButton startChatButton = new JButton("Начать диалог");
        startChatButton.addActionListener(e -> startNewDialog());

        // Компоновка элементов
        final GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPaneIncoming)
                        .addComponent(startChatButton))
                .addContainerGap());
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(10)
                .addComponent(startChatButton)
                .addContainerGap());
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Запуск потока для прослушивания сервера
        new Thread(new IncomingMessageListener()).start();
    }

    // Метод для открытия нового окна чата
    private void startNewDialog() {
        String recipient = JOptionPane.showInputDialog(this, "Введите имя получателя:");
        if (recipient != null && !recipient.trim().isEmpty() && !Objects.equals(sender, recipient)) {
            openChatWindow(recipient);
        }
        else if(Objects.equals(recipient, sender)){
            JOptionPane.showMessageDialog(MainFrame.this,"Нельзя начать диалог с самим собой", "Ошибка", JOptionPane.ERROR_MESSAGE);          }
    }

    // Метод для открытия окна чата с конкретным пользователем
    private void openChatWindow(String recipient) {
        if (!openDialogs.containsKey(recipient)) {
            ChatFrame chatFrame = new ChatFrame(serverConnection, recipient, sender);
            openDialogs.put(recipient, chatFrame);
            chatFrame.setVisible(true);
        } else if(!Objects.equals(sender, recipient)){
            JOptionPane.showMessageDialog(this, "Диалог с этим пользователем уже открыт.");
        }
        else{
            JOptionPane.showMessageDialog(MainFrame.this,
                    "ERROR!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
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
                                    openChatWindow(sender);
                                }
                                openDialogs.get(sender).appendMessage(sender, text);
                            });
                        }
                    } else {
                        // Отображение других сообщений в основной области
                        textAreaIncoming.append(message + "\n");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка связи с сервером: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }
    }
}

