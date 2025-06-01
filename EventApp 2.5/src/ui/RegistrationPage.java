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
        VBox mainLayout = new VBox(30); // Increased spacing for larger window
        mainLayout.setAlignment(Pos.CENTER); // Center everything for 1200x800
        mainLayout.setStyle("-fx-background-color: white; -fx-padding: 40;"); // Increased padding

        // Logo section
        VBox logoSection = new VBox(15);
        logoSection.setAlignment(Pos.CENTER);
        logoSection.setPadding(new Insets(20, 0, 30, 0)); // Adjusted padding

        try {
            File logoFile = new File("resources/logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(new FileInputStream(logoFile));
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(120); // Increased for 1200x800
                logoView.setPreserveRatio(true);
                logoSection.getChildren().add(logoView);
            }
        } catch (Exception e) {
            Label logoPlaceholder = new Label("EVENTURE");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 32px; -fx-font-weight: bold;"); // Increased font size
            logoSection.getChildren().add(logoPlaceholder);
        }

        // Registration form section - reduced from 500px to 420px width as specified
        VBox registrationSection = new VBox(18); // Adjusted spacing
        registrationSection.setAlignment(Pos.TOP_CENTER);
        registrationSection.setPadding(new Insets(25, 50, 35, 50)); // Adjusted padding
        registrationSection.setStyle("-fx-background-color: " + BLUE_COLOR + "; -fx-background-radius: 15;"); // Added rounded corners
        registrationSection.setPrefWidth(420); // Reduced from 500px to 420px as specified

        Label registrationTitle = new Label("REGISTER");
        registrationTitle.setStyle("-fx-text-fill: black; -fx-font-size: 28px; -fx-font-weight: bold;"); // Increased font size

        // Input fields - reduced from 400px to 320px width as specified
        TextField nameField = new TextField();
        nameField.setPromptText("NAME");
        nameField.setPrefHeight(45); // Slightly reduced height
        nameField.setPrefWidth(320); // Reduced from 400px to 320px as specified
        nameField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 16px; -fx-background-radius: 8;"); // Increased font size and added radius

        TextField emailField = new TextField();
        emailField.setPromptText("EMAIL");
        emailField.setPrefHeight(45);
        emailField.setPrefWidth(320);
        emailField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 16px; -fx-background-radius: 8;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("USERNAME");
        usernameField.setPrefHeight(45);
        usernameField.setPrefWidth(320);
        usernameField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 16px; -fx-background-radius: 8;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("PASSWORD");
        passwordField.setPrefHeight(45);
        passwordField.setPrefWidth(320);
        passwordField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 16px; -fx-background-radius: 8;");

        TextField roleField = new TextField();
        roleField.setPromptText("Role (organizer/attendee)");
        roleField.setPrefHeight(45);
        roleField.setPrefWidth(320);
        roleField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; -fx-text-fill: black; -fx-prompt-text-fill: #555555; -fx-font-size: 16px; -fx-background-radius: 8;");

        Button registerBtn = new Button("REGISTER");
        registerBtn.setPrefHeight(50); // Increased height
        registerBtn.setPrefWidth(320);
        registerBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 25;"); // Increased font size and added radius

        Label errorMsgLabel = new Label();
        errorMsgLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-size: 14px;");
        errorMsgLabel.setWrapText(true);
        errorMsgLabel.setMaxWidth(320);

        // Back button
        Button backBtn = new Button("← Back to Main");
        backBtn.setPrefHeight(40);
        backBtn.setPrefWidth(200);
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: " + BLUE_COLOR + "; -fx-border-radius: 20; -fx-background-radius: 20;");
        backBtn.setOnAction(e -> new MainPage(stage)); // Navigate back to main page

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleField.getText().trim().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                errorMsgLabel.setText("Please fill in all fields.");
                errorMsgLabel.setTextFill(Color.RED);
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                errorMsgLabel.setText("Please enter a valid email address.");
                errorMsgLabel.setTextFill(Color.RED);
                return;
            }

            if (!role.equals("organizer") && !role.equals("attendee")) {
                errorMsgLabel.setText("Role must be either 'organizer' or 'attendee'.");
                errorMsgLabel.setTextFill(Color.RED);
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
                    errorMsgLabel.setTextFill(Color.RED);
                    return;
                }

                checkSql = "SELECT COUNT(*) FROM [User] WHERE email = ?";
                checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, email);
                rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    errorMsgLabel.setText("Email already exists.");
                    errorMsgLabel.setTextFill(Color.RED);
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
                    errorMsgLabel.setText("Registration successful! Redirecting to login...");

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
                    errorMsgLabel.setTextFill(Color.RED);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorMsgLabel.setText("Database error: " + ex.getMessage());
                errorMsgLabel.setTextFill(Color.RED);
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

        mainLayout.getChildren().addAll(logoSection, registrationSection, backBtn);

        // Updated scene size to 1200x800 as specified
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Eventure - Registration");
        stage.setResizable(false);
        stage.show();
    }
}