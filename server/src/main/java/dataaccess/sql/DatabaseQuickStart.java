package dataaccess.sql;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseQuickStart {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "unDERgrad28")) {
            conn.setCatalog("chess");
            try (var preparedStatement = conn.prepareStatement("SELECT id, username, email FROM users")) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var id = rs.getInt("id");
                        var username = rs.getString("username");
                        var email = rs.getString("email");
                        System.out.println(id + " " + username + " " + email);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
