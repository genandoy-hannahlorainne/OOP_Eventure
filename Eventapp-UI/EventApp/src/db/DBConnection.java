package db;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://MichaelMosquito:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true";
    private static final String USER = "LMS_Admin";
    private static final String PASSWORD = "moks123";

    // ✅ Add this method for use in LoginPage
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean registerUser(String name, String email, String username, String password, String role) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO [User] (name, email, username, password, userType) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.setString(5, role);
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static ResultSet getUserByID(int userID) throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String sql = "SELECT * FROM [User] WHERE userID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userID);
        return stmt.executeQuery(); // caller must close connection
    }

    public static boolean updateUserProfile(int userID, String name, String email, String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "UPDATE [User] SET name = ?, email = ?, username = ?, password = ? WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.setInt(5, userID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

