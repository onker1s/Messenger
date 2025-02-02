import java.sql.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

// Класс для работы с базой данных
public class DBconnector {

    // Метод для получения подключения к базе данных
    public static Connection getConnection() throws SQLException, IOException {
        Properties props = new Properties();

        // Загружаем настройки подключения из файла
        try (InputStream in = Files.newInputStream(Paths.get("src\\database.properties"))) {
            props.load(in);
        }

        // Получаем URL, имя пользователя и пароль
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        // Возвращаем подключение
        return DriverManager.getConnection(url, username, password);
    }

    // Метод для регистрации пользователя в базе данных
    public boolean registerUser(String username, String password, String IP) {
        String query = "INSERT INTO users (user_name, user_password, user_IP, user_status, user_last_online_time) VALUES (?, ?, ?, '0', '2022-05-12 11:35:27')";
        if (!isUserExists(username)) {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                // Устанавливаем параметры запроса
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3,IP);

                // Выполняем запрос
                statement.executeUpdate();
                return true;
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    // Метод для регистрации пользователя в базе данных
    public boolean loginUser(String username, String password, String currentIP) {
        String query1 = "SELECT user_password FROM users WHERE user_name = ?";
        String query2= "UPDATE messenger.users SET user_IP = ? WHERE (user_name = ?)";
        if (isUserExists(username)) {
            try (Connection connection = getConnection();
                 PreparedStatement statement1 = connection.prepareStatement(query1);
                 PreparedStatement statement2 = connection.prepareStatement(query2)) {

                // Устанавливаем параметры запроса
                statement1.setString(1, username);
                // Выполняем запрос
                ResultSet correctpassword = statement1.executeQuery();
                // Проверяем, есть ли результат
                if (correctpassword.next()) { // Перемещаем курсор к первой строке результата
                    String storedPassword = correctpassword.getString("user_password");
                    boolean passwordEquals =  Objects.equals(storedPassword, password);
                    if(Objects.equals(storedPassword, password)) {
                        statement2.setString(1, currentIP);
                        statement2.setString(2,username);
                        statement2.executeUpdate();
                        return true;
                    }
                    else{return false;}
                } else {
                    return false; // Если нет результата, возвращаем false
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    // Пример метода для проверки пользователя (можно расширить функционал)
    public boolean isUserExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE user_name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметры запроса
            statement.setString(1, username);

            // Выполняем запрос
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Если пользователь существует
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isDialogExists(String user_1_name,String user_2_name) {
        String query = "SELECT COUNT(*) FROM dialogs WHERE user_1_name = ? OR user_2_name = ? AND user_2_name = ? OR user_1_name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметры запроса
            statement.setString(1, user_1_name);
            statement.setString(2, user_2_name);
            statement.setString(3, user_1_name);
            statement.setString(4, user_2_name);

            // Выполняем запрос
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Если диалог существует
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUserOnline(String username) {
        String query = "SELECT user_status FROM users WHERE user_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_status") == 1;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveMessage(String sender, String recipient, String message) {
        String query = "INSERT INTO messages (message_sender, message_recipient, message_text, delivered_status) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            System.out.println(sender+recipient);
            statement.setString(1, sender);
            statement.setString(2, recipient);
            statement.setString(3, message);
            statement.setInt(4, isUserOnline(recipient) ? 1 : 0); // Если получатель онлайн, сообщение помечается как доставленное

            statement.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserStatus(String username, int status) {
        String query = "UPDATE users SET user_status = ?, user_last_online_time = NOW() WHERE user_name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, status);
            statement.setString(2, username);
            statement.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void markMessagesAsDelivered(String username) {
        String query = "UPDATE messages SET delivered_status = 1 WHERE message_recipient = ? AND delivered_status = 0";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    public String getUnreadMessages(String username) {
        StringBuilder messages = new StringBuilder();
        String query = "SELECT message_sender, message_text FROM messages WHERE message_recipient = ? AND delivered_status = 0";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                messages.append(resultSet.getString("message_sender"))
                        .append(": ")
                        .append(resultSet.getString("message_text"))
                        .append("\n");
            }

            // После отправки сообщений помечаем их как "доставленные"
            markMessagesAsDelivered(username);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return messages.toString();
    }

    public Set<String> getUserDialogPartners(String username) {
        Set<String> dialogUsers = new HashSet<>();
        String sql = "SELECT DISTINCT CASE " +
                "WHEN message_sender = ? THEN message_recipient " +
                "WHEN message_recipient = ? THEN message_sender " +
                "END AS dialog_partner " +
                "FROM messages WHERE message_sender = ? OR message_recipient = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
            stmt.setString(4, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dialogUsers.add(rs.getString("dialog_partner"));
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return dialogUsers;
    }

}
