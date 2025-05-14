import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EventureLogin extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public EventureLogin() {
        setTitle("Eventure Login");
        setSize(400, 350);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(155, 170, 210));

        JLabel titleLabel = new JLabel("LOG IN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(150, 30, 100, 30);
        add(titleLabel);

        JLabel emailLabel = new JLabel("EMAIL");
        emailLabel.setBounds(50, 80, 100, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(50, 105, 280, 30);
        add(emailField);

        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setBounds(50, 145, 100, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 170, 280, 30);
        add(passwordField);

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(120, 220, 140, 35);
        loginButton.setBackground(new Color(255, 223, 72));
        add(loginButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(50, 260, 280, 25);
        statusLabel.setForeground(Color.RED);
        add(statusLabel);

        JLabel forgotLabel = new JLabel("<HTML><U>Forgot Password?</U></HTML>");
        forgotLabel.setBounds(130, 290, 150, 20);
        forgotLabel.setForeground(Color.BLACK);
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(forgotLabel);

        loginButton.addActionListener(e -> loginUser());

        setVisible(true);
    }

    private void loginUser() {
        String email = emailField.getText();
        String password = String.valueOf(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true",
                "LMS_Admin", "moks123"
            );

            String query = "SELECT * FROM [User] WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statusLabel.setForeground(Color.GREEN);
                statusLabel.setText("Login successful!");

            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Invalid email or password.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new EventureLogin();
    }
}
