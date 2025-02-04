import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;


public class ChatFrame extends JFrame {
    private final JTextArea textAreaChat;
    private final JTextField textFieldMessage;
    private final ServerConnection serverConnection;
    private final String recipient;
    private final String sender;

    public ChatFrame(ServerConnection serverConnection, String recipient, String sender,Map<String, ChatFrame> openChats) {
        super("Диалог с " + recipient);
        this.serverConnection = serverConnection;
        this.recipient = recipient;
        this.sender = sender;
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openChats.remove(recipient); // Удаляем окно из мапы
            }
        });
        // Текстовая область для сообщений
        textAreaChat = new JTextArea(15, 0);
        textAreaChat.setEditable(false);
        final JScrollPane scrollPaneChat = new JScrollPane(textAreaChat);

        // Поле ввода сообщения
        textFieldMessage = new JTextField();
        final JButton sendButton = new JButton("Отправить");

        // Обработчик отправки сообщения
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Компоновка элементов
        final GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(scrollPaneChat)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(textFieldMessage)
                        .addComponent(sendButton)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(scrollPaneChat)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(textFieldMessage)
                        .addComponent(sendButton)));

        pack();


    }

    // Отправка сообщения
    private void sendMessage() {
        String message = textFieldMessage.getText().trim();
        if (!message.isEmpty() ) {
            String formattedMessage = "MESSAGE " + sender + " " + recipient + " " + message;
            serverConnection.sendMessage(formattedMessage);
            appendMessage("Я", message);
            textFieldMessage.setText("");
        }

    }

    // Добавление сообщения в окно чата
    public void appendMessage(String sender, String message) {
        textAreaChat.append(sender + " -> " + message + "\n");
    }
    public void updateStatus(String status)
    {
        this.setTitle("Диалог с " + recipient +"("+status+")");
    }
}

