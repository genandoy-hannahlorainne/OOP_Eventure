package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true";
    private static final String USER = "LMS_Admin";
    private static final String PASSWORD = "moks123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Add the missing registerUser method
    public static boolean registerUser(String username, String password, String userType) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO [User] (username, password, userType) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, userType);
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}