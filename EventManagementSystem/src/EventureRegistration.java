import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EventureRegistration extends JFrame {

    private JTextField nameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JRadioButton attendeeButton, organizerButton;
    private JButton registerButton;

    public EventureRegistration() {
        setTitle("Eventure - Register");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel titleLabel = new JLabel("REGISTER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(130, 20, 200, 30);
        add(titleLabel);

        nameField = new JTextField();
        nameField.setBounds(70, 70, 250, 35);
        nameField.setBorder(BorderFactory.createTitledBorder("FULL NAME"));
        add(nameField);

        emailField = new JTextField();
        emailField.setBounds(70, 120, 250, 35);
        emailField.setBorder(BorderFactory.createTitledBorder("EMAIL"));
        add(emailField);

        passwordField = new JPasswordField();
        passwordField.setBounds(70, 170, 250, 35);
        passwordField.setBorder(BorderFactory.createTitledBorder("PASSWORD"));
        add(passwordField);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(70, 220, 250, 35);
        confirmPasswordField.setBorder(BorderFactory.createTitledBorder("CONFIRM PASSWORD"));
        add(confirmPasswordField);

        attendeeButton = new JRadioButton("Attendee");
        organizerButton = new JRadioButton("Organizer");
        ButtonGroup group = new ButtonGroup();
        group.add(attendeeButton);
        group.add(organizerButton);

        attendeeButton.setBounds(100, 270, 100, 30);
        organizerButton.setBounds(200, 270, 100, 30);
        add(attendeeButton);
        add(organizerButton);

        registerButton = new JButton("REGISTER");
        registerButton.setBounds(120, 330, 150, 40);
        registerButton.setBackground(Color.YELLOW);
        registerButton.setFocusPainted(false);
        add(registerButton);

        registerButton.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String userType = attendeeButton.isSelected() ? "attendee" : (organizerButton.isSelected() ? "organizer" : "");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || userType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true",
                "LMS_Admin",
                "moks123"
            );

            String sql = "INSERT INTO [User] (username, password, name, email, userType) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setString(5, userType);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful!");
                clearForm();
            }

            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        attendeeButton.setSelected(false);
        organizerButton.setSelected(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventureRegistration().setVisible(true));
    }
}
