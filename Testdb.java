import java.sql.*;

public class Testdb {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/todo_db?serverTimezone=UTC";
        String USER = "root";
        String PASSWORD = "root"; // Change if needed

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL Driver
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connection Successful!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver Not Found! " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Connection Failed: " + e.getMessage());
        }
    }
}
