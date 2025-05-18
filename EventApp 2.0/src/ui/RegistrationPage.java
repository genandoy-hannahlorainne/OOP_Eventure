package ui;

import db.DBConnection;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationPage {
    public RegistrationPage(Stage stage) {
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Label roleLabel = new Label("Role (organizer/attendee):");
        TextField roleField = new TextField();

        Button registerButton = new Button("Register");
        Label message = new Label();

        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String username = userField.getText();
            String password = passField.getText();
            String role = roleField.getText();

            boolean success = DBConnection.registerUser(name, email, username, password, role);
            message.setText(success ? "Registration successful!" : "Registration failed.");
        });

        VBox root = new VBox(10,
            nameLabel, nameField,
            emailLabel, emailField,
            userLabel, userField,
            passLabel, passField,
            roleLabel, roleField,
            registerButton, message
        );
        root.setStyle("-fx-padding: 20;");
        stage.setScene(new Scene(root, 400, 400));
        stage.setTitle("Register");
        stage.show();
    }
}
