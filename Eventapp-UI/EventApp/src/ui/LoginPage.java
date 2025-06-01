package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage {
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String LIGHTER_BLUE = "#B8C5E2";
    private static final String BACKGROUND_COLOR = "#F5F7FA";

    public LoginPage(Stage stage) {
        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        mainLayout.setPrefSize(1200, 800);

        VBox leftSection = new VBox(30);
        leftSection.setAlignment(Pos.CENTER);
        leftSection.setPrefWidth(600);
        leftSection.setPadding(new Insets(60, 40, 60, 40));

        VBox logoSection = new VBox(20);
        logoSection.setAlignment(Pos.CENTER);

        try {
            File logoFile = new File("resources/logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(new FileInputStream(logoFile));
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(120);
                logoView.setPreserveRatio(true);
                logoSection.getChildren().add(logoView);
            }
        } catch (Exception e) {
            Label logoPlaceholder = new Label("Eventure");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 40px; -fx-font-weight: bold;");
            logoSection.getChildren().add(logoPlaceholder);
        }

        Label welcomeTitle = new Label("Welcome Back!");
        welcomeTitle.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 30px; -fx-font-weight: bold;");

        Label welcomeSubtitle = new Label("		   Sign in to continue your event journey");
        welcomeSubtitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px;");
        welcomeSubtitle.setWrapText(true);
        welcomeSubtitle.setMaxWidth(500);

        Label featureText = new Label("• Discover amazing events\n• Create memorable experiences\n• Connect with like-minded people");
        featureText.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-line-spacing: 5px;");

        leftSection.getChildren().addAll(logoSection, welcomeTitle, welcomeSubtitle, featureText);

        VBox rightSection = new VBox();
        rightSection.setAlignment(Pos.CENTER);
        rightSection.setPrefWidth(600);
        rightSection.setStyle("-fx-background-color: white;");

        VBox loginSection = new VBox(25);
        loginSection.setAlignment(Pos.TOP_CENTER);
        loginSection.setPadding(new Insets(80, 80, 80, 80));
        loginSection.setStyle("-fx-background-color: " + BLUE_COLOR + "; -fx-background-radius: 20px;");
        loginSection.setPrefWidth(500);
        loginSection.setMaxWidth(500);

        Label loginTitle = new Label("LOG IN");
        loginTitle.setStyle("-fx-text-fill: black; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label loginSubtitle = new Label("Enter your credentials to access your account");
        loginSubtitle.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        loginSubtitle.setWrapText(true);
        loginSubtitle.setMaxWidth(400);
        loginSubtitle.setAlignment(Pos.CENTER);

        VBox formFields = new VBox(20);
        formFields.setAlignment(Pos.CENTER);
        formFields.setPadding(new Insets(30, 0, 20, 0));

        Label usernameLabel = new Label("USERNAME");
        usernameLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your username");
        emailField.setPrefHeight(45);
        emailField.setPrefWidth(320);
        emailField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; " + 
                            "-fx-text-fill: black; " +
                            "-fx-prompt-text-fill: #666666; " +
                            "-fx-font-size: 16px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-padding: 0 15 0 15;");

        Label passwordLabel = new Label("PASSWORD");
        passwordLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setPrefWidth(320);
        passwordField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; " + 
                               "-fx-text-fill: black; " +
                               "-fx-prompt-text-fill: #666666; " +
                               "-fx-font-size: 16px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 0 15 0 15;");

        formFields.getChildren().addAll(usernameLabel, emailField, passwordLabel, passwordField);

        Button loginBtn = new Button("LOGIN");
        loginBtn.setPrefHeight(50);
        loginBtn.setPrefWidth(320);
        loginBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; " +
                          "-fx-text-fill: black; " +
                          "-fx-font-size: 18px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-background-radius: 8px; " +
                          "-fx-cursor: hand;");

        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #E8C63A; " +
                                                          "-fx-text-fill: black; " +
                                                          "-fx-font-size: 18px; " +
                                                          "-fx-font-weight: bold; " +
                                                          "-fx-background-radius: 8px; " +
                                                          "-fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; " +
                                                         "-fx-text-fill: black; " +
                                                         "-fx-font-size: 18px; " +
                                                         "-fx-font-weight: bold; " +
                                                         "-fx-background-radius: 8px; " +
                                                         "-fx-cursor: hand;"));

        Label errorMsgLabel = new Label();
        errorMsgLabel.setStyle("-fx-text-fill: #FF3333; -fx-font-size: 14px; -fx-font-weight: bold;");
        errorMsgLabel.setWrapText(true);
        errorMsgLabel.setMaxWidth(400);
        errorMsgLabel.setAlignment(Pos.CENTER);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                errorMsgLabel.setText("Please enter both username and password.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT userID, userType FROM [User] WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    String userType = rs.getString("userType");

                    if ("Organizer".equalsIgnoreCase(userType)) {
                        new OrganizerDashboard().show(stage, userID);
                    } else if ("Attendee".equalsIgnoreCase(userType)) {
                        new AttendeeDashboard().show(stage, userID);
                    } else {
                        errorMsgLabel.setText("Unknown user type: " + userType);
                    }
                } else {
                    errorMsgLabel.setText("Invalid username or password. Please try again.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorMsgLabel.setText("Connection error. Please try again later.");
            }
        });

        passwordField.setOnAction(e -> loginBtn.fire());
        emailField.setOnAction(e -> passwordField.requestFocus());

        loginSection.getChildren().addAll(
            loginTitle, 
            loginSubtitle,
            formFields,
            loginBtn,
            errorMsgLabel
        );

        rightSection.getChildren().add(loginSection);
        mainLayout.getChildren().addAll(leftSection, rightSection);

        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Eventure - Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        emailField.requestFocus();
    }
}
