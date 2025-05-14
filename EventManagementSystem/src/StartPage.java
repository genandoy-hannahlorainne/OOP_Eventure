import javax.swing.*;
import java.awt.*;

public class StartPage extends JFrame {

    public StartPage() {
        setTitle("Eventure - Welcome");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel titleLabel = new JLabel("Eventure");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setBounds(130, 50, 200, 40);
        add(titleLabel);

        JLabel subtitle = new JLabel("<html><center>Your Gateway to Seamless Event Journey</center></html>", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setBounds(50, 100, 300, 40);
        add(subtitle);

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(120, 200, 150, 40);
        loginButton.setBackground(new Color(143, 156, 199));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        add(loginButton);

        JButton registerButton = new JButton("REGISTER");
        registerButton.setBounds(120, 260, 150, 40);
        registerButton.setBackground(new Color(247, 220, 111));
        registerButton.setFocusPainted(false);
        add(registerButton);

        // 👇 Event Listeners
        loginButton.addActionListener(e -> {
            dispose(); // close current
            new EventureLogin().setVisible(true); // open login form
        });

        registerButton.addActionListener(e -> {
            dispose(); // close current
            new EventureRegistration().setVisible(true); 
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartPage().setVisible(true));
    }
}
