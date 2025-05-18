package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage {
    public LoginPage(Stage stage) {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginBtn = new Button("Login");

        Label msgLabel = new Label();

        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                msgLabel.setText("Please enter both fields.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // First fetch userID and userType
                String sql = "SELECT userID, userType FROM [User] WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    String userType = rs.getString("userType");
                    System.out.println("Fetched userType: " + userType);

                    if ("Organizer".equalsIgnoreCase(userType)) {
                        new OrganizerDashboard().show(stage, userID);
                    } else if ("Attendee".equalsIgnoreCase(userType)) {
                        new AttendeeDashboard().show(stage, userID);
                    } else {
                        msgLabel.setText("Unknown user type: " + userType);
                    }
                } else {
                    msgLabel.setText("Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msgLabel.setText("Database error.");
            }
        });

        VBox layout = new VBox(10, userLabel, userField, passLabel, passField, loginBtn, msgLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene scene = new Scene(layout, 350, 300);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}