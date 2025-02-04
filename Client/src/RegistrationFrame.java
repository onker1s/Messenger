import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import javax.swing.*;

public class RegistrationFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private final JTextField nameTextField;
    private final JTextField password1TextField;
    private final JTextField password2TextField;

    public  RegistrationFrame() throws IOException {

        super(FRAME_TITLE);
        setMinimumSize(
                new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));// Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Кнопка для регистрации
        final JButton regButton = new JButton("Регистрация");
        Properties props = new Properties();

        // Загружаем настройки подключения из файла
        try (InputStream in = Files.newInputStream(Paths.get("src\\server.properties"))) {
            props.load(in);
        }

        // Получаем URL, имя пользователя и пароль
        String IP = props.getProperty("IP");
        String port = props.getProperty("port");
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameTextField.getText().isEmpty() && !password1TextField.getText().isEmpty() && Objects.equals(password1TextField.getText(), password2TextField.getText())) {

                    InetAddress localHost;
                    try {
                        localHost = InetAddress.getLocalHost();
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Получаем IP-адрес в виде строки
                    String ipAddress = localHost.getHostAddress();
                    String request = "REGISTER " + nameTextField.getText() + " " + password1TextField.getText() + " " + ipAddress;
                    try {
                        ServerConnection serverConnection = new ServerConnection(IP,Integer.parseInt(port));
                        serverConnection.sendMessage(request);
                        String response = serverConnection.receiveMessage();
                        if (response.equals("REGISTER_SUCCESS")) {
                            JOptionPane.showMessageDialog(RegistrationFrame.this, "Регистрация прошла успешно. Войдите в аккаунт.");
                            dispose();
                            final LoginFrame logframe = new LoginFrame(serverConnection);
                        } else {
                            JOptionPane.showMessageDialog(RegistrationFrame.this, "Имя пользователя занято.");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }


                } else if (nameTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(RegistrationFrame.this, "Введите имя.");
                } else if (password1TextField.getText().isEmpty() && nameTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(RegistrationFrame.this, "Введите имя и пароль.");
                }
                else {
                    JOptionPane.showMessageDialog(RegistrationFrame.this, "Пароли не совпадают.");
                }
            }
        });
        // Подписи полей
        final JLabel labelForName = new JLabel("Имя пользователя");
        final JLabel labelForPassword1 = new JLabel("Пароль");
        final JLabel labelForPassword2 = new JLabel("Повторите пароль");
        nameTextField = new JTextField(30);
        password1TextField = new JTextField(30);
        password2TextField = new JTextField(30);

        // Настройка панели с использованием GroupLayout
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Отступы от краев

        layout.setAutoCreateGaps(true); // Автоматические отступы между элементами
        layout.setAutoCreateContainerGaps(true); // Автоматические отступы от краев контейнера

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(labelForName)
                                .addComponent(labelForPassword1)
                                .addComponent(labelForPassword2))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(nameTextField)
                                .addComponent(password1TextField)
                                .addComponent(password2TextField)
                                .addComponent(regButton, GroupLayout.Alignment.CENTER))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForName)
                                .addComponent(nameTextField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForPassword1)
                                .addComponent(password1TextField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForPassword2)
                                .addComponent(password2TextField))
                        .addGap(20)
                        .addComponent(regButton)
        );
        add(panel);
        setVisible(true);



    }


}
