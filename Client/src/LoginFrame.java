import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.swing.*;

@SuppressWarnings("serial")
public class LoginFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private final JTextField nameTextField;
    private final JTextField passwordTextField;


    public  LoginFrame() throws IOException {

        super(FRAME_TITLE);
        setMinimumSize(
                new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));// Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Кнопка для регистрации
        final JButton logButton = new JButton("Вход");
        Properties props = new Properties();

        // Загружаем настройки подключения из файла
        try (InputStream in = Files.newInputStream(Paths.get("src\\server.properties"))) {
            props.load(in);
        }

        // Получаем URL, имя пользователя и пароль
        String IP = props.getProperty("IP");
        String port = props.getProperty("port");
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!passwordTextField.getText().isEmpty() && !nameTextField.getText().isEmpty())
                {
                    //отправка имени и пароля в базу, подтверждение уникальности имени, регистрация пользователя
                    String request = "LOGIN " + nameTextField.getText() + " " + passwordTextField.getText();
                    try {
                        ServerConnection serverConnection = new ServerConnection(IP,Integer.parseInt(port));
                        serverConnection.sendMessage(request);
                        String response = serverConnection.receiveMessage();
                        if (response.equals("LOGIN_SUCCESS")) {
                            System.out.println("Login successful!");
                            dispose();
                            final MainFrame mainFrame = new MainFrame(serverConnection, nameTextField.getText());
                        } else {
                            System.out.println("Login failed: " + response);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else if(passwordTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести пароль
                }
                else if(nameTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести имя
                }
                else if(passwordTextField.getText().isEmpty() && nameTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести имя и пароль
                }
            }
        });
        // Подписи полей
        final JLabel labelForName = new JLabel("Имя пользователя");
        final JLabel labelForPassword1 = new JLabel("Пароль");

        nameTextField = new JTextField(30);
        passwordTextField = new JTextField(30);


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
                                .addComponent(labelForPassword1))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(nameTextField)
                                .addComponent(passwordTextField)
                                .addComponent(logButton, GroupLayout.Alignment.CENTER))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForName)
                                .addComponent(nameTextField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForPassword1)
                                .addComponent(passwordTextField))
                        .addGap(20)
                        .addComponent(logButton)
        );
        add(panel);
        setVisible(true);



    }

    public LoginFrame(ServerConnection serverConnection) {

        super(FRAME_TITLE);
        setMinimumSize(
                new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));// Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Кнопка для регистрации
        final JButton logButton = new JButton("Вход");
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!passwordTextField.getText().isEmpty() && !nameTextField.getText().isEmpty())
                {
                    //отправка имени и пароля в базу, подтверждение уникальности имени, регистрация пользователя
                    String request = "LOGIN " + nameTextField.getText() + " " + passwordTextField.getText();
                    try {
                        serverConnection.sendMessage(request);
                        String response = serverConnection.receiveMessage();
                        if (response.equals("LOGIN_SUCCESS")) {
                            System.out.println("Login successful!");
                            dispose();
                            final MainFrame mainFrame = new MainFrame(serverConnection,nameTextField.getText());
                        } else {
                            System.out.println("Login failed: " + response);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else if(passwordTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести пароль
                }
                else if(nameTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести имя
                }
                else if(passwordTextField.getText().isEmpty() && nameTextField.getText().isEmpty())
                {
                    // Окно с сообщением о том, что надо ввести имя и пароль
                }
            }
        });
        // Подписи полей
        final JLabel labelForName = new JLabel("Имя пользователя");
        final JLabel labelForPassword1 = new JLabel("Пароль");

        nameTextField = new JTextField(30);
        passwordTextField = new JTextField(30);


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
                                .addComponent(labelForPassword1))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(nameTextField)
                                .addComponent(passwordTextField)
                                .addComponent(logButton, GroupLayout.Alignment.CENTER))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForName)
                                .addComponent(nameTextField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelForPassword1)
                                .addComponent(passwordTextField))
                        .addGap(20)
                        .addComponent(logButton)
        );
        add(panel);
        setVisible(true);



    }


}
