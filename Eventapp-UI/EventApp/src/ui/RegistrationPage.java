package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationPage {
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String LIGHTER_BLUE = "#B8C5E2";

    public RegistrationPage(Stage stage) {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: white; -fx-padding: 0;");

        VBox logoSection = new VBox(10);
        logoSection.setAlignment(Pos.CENTER);
        logoSection.setPadding(new Insets(30, 0, 20, 0));

        try {
            File logoFile = new File("resources/logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(new FileInputStream(logoFile));
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(100);
                logoView.setPreserveRatio(true);
                logoSection.getChildren().add(logoView);
            }
        } catch (Exception e) {
            Label logoPlaceholder = new Label("Eventure");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 24px; -fx-font-weight: bold;");
            logoSection.getChildren().add(logoPlaceholder);
        }

        VBox registrationSection = new VBox(15);
        registrationSection.setAlignment(Pos.TOP_CENTER);
        registrationSection.setPadding(new Insets(20, 40, 30, 40));
        registrationSection.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        registrationSection.setPrefWidth(350);

        Label registrationTitle = new Label("REGISTER");
        registrationTitle.setStyle("-fx-text-fill: black; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("NAME");
        nameField.setPrefHeight(40);
        nameField.setPrefWidth(270);
        nameField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("EMAIL");
        emailField.setPrefHeight(40);
        emailField.setPrefWidth(270);
        emailField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 14px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("USERNAME");
        usernameField.setPrefHeight(40);
        usernameField.setPrefWidth(270);
        usernameField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("PASSWORD");
        passwordField.setPrefHeight(40);
        passwordField.setPrefWidth(270);
        passwordField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 14px;");

        TextField roleField = new TextField();
        roleField.setPromptText("Role (organizer/attendee)");
        roleField.setPrefHeight(40);
        roleField.setPrefWidth(270);
        roleField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 14px;");

        Button registerBtn = new Button("REGISTER");
        registerBtn.setPrefHeight(40);
        registerBtn.setPrefWidth(270);
        registerBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label errorMsgLabel = new Label();
        errorMsgLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-size: 14px;");

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleField.getText().trim().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                errorMsgLabel.setText("Please fill in all fields.");
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                errorMsgLabel.setText("Please enter a valid email address.");
                return;
            }

            if (!role.equals("organizer") && !role.equals("attendee")) {
                errorMsgLabel.setText("Role must be either 'organizer' or 'attendee'.");
                return;
            }

            role = role.substring(0, 1).toUpperCase() + role.substring(1);

            try (Connection conn = DBConnection.getConnection()) {
                String checkSql = "SELECT COUNT(*) FROM [User] WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    errorMsgLabel.setText("Username already exists.");
                    return;
                }

                checkSql = "SELECT COUNT(*) FROM [User] WHERE email = ?";
                checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, email);
                rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    errorMsgLabel.setText("Email already exists.");
                    return;
                }

                String sql = "INSERT INTO [User] (name, email, username, password, userType) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, username);
                stmt.setString(4, password);
                stmt.setString(5, role);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    errorMsgLabel.setTextFill(Color.GREEN);
                    errorMsgLabel.setText("Registration successful! Please login.");

                    nameField.clear();
                    emailField.clear();
                    usernameField.clear();
                    passwordField.clear();
                    roleField.clear();

                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            javafx.application.Platform.runLater(() -> new LoginPage(stage));
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    errorMsgLabel.setText("Registration failed. Please try again.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorMsgLabel.setText("Database error: " + ex.getMessage());
            }
        });

        registrationSection.getChildren().addAll(
            registrationTitle,
            nameField,
            emailField,
            usernameField,
            passwordField,
            roleField,
            registerBtn,
            errorMsgLabel
        );

        mainLayout.getChildren().addAll(logoSection, registrationSection);

        Scene scene = new Scene(mainLayout, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Eventure - Registration");
        stage.setResizable(false);
        stage.show();
    }
}
