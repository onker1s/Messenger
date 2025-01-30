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
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
@SuppressWarnings("serial")
public class SignFrame extends JFrame {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;


    public  SignFrame() {

        super(FRAME_TITLE);
        setMinimumSize(
                new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));// Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2,
                (kit.getScreenSize().height - getHeight()) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Кнопка для перехода в меню регистрации
        final JButton regButton = new JButton("Регистрация");
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                try {
                    final RegistrationFrame regframe = new RegistrationFrame();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        // Кнопка для перехода в меню входа
        final JButton logButton = new JButton("Вход");
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                try {
                    final LoginFrame logframe = new LoginFrame();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Настраиваем компоновку
        setLayout(new GridBagLayout()); // Централизуем всё
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Отступы вокруг кнопок
        gbc.anchor = GridBagConstraints.CENTER; // Центрирование компонента

        // Добавляем кнопки в окно
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // Две кнопки в колонке с отступами
        buttonPanel.add(regButton);
        buttonPanel.add(logButton);

        add(buttonPanel, gbc);

        setVisible(true);



    }


}