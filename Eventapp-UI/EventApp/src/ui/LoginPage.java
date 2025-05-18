package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage {
    // Colors based on the provided hex codes
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String LIGHTER_BLUE = "#B8C5E2";

    public LoginPage(Stage stage) {
        // Main container
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: white; -fx-padding: 0;");
        
        // Logo section
        VBox logoSection = new VBox(10);
        logoSection.setAlignment(Pos.CENTER);
        logoSection.setPadding(new Insets(30, 0, 20, 0));
        
        // Try to load the logo
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
            // If logo loading fails, use text placeholder
            Label logoPlaceholder = new Label("Eventure");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 24px; -fx-font-weight: bold;");
            logoSection.getChildren().add(logoPlaceholder);
        }
        
        // Login form section with blue background
        VBox loginSection = new VBox(20);
        loginSection.setAlignment(Pos.TOP_CENTER);
        loginSection.setPadding(new Insets(30, 40, 40, 40));
        loginSection.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        loginSection.setPrefWidth(400);
        loginSection.setPrefHeight(350);
        
        // Login Title
        Label loginTitle = new Label("LOG IN");
        loginTitle.setStyle("-fx-text-fill: black; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Email Field
        TextField emailField = new TextField();
        emailField.setPromptText("USERNAME");
        emailField.setPrefHeight(40);
        emailField.setPrefWidth(300);
        emailField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; " + 
                           "-fx-text-fill: black; " +
                           "-fx-prompt-text-fill: #555555; " +
                           "-fx-font-size: 14px;");
        
        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("PASSWORD");
        passwordField.setPrefHeight(40);
        passwordField.setPrefWidth(300);
        passwordField.setStyle("-fx-background-color: " + LIGHTER_BLUE + "; " + 
                              "-fx-text-fill: black; " +
                              "-fx-prompt-text-fill: #555555; " +
                              "-fx-font-size: 14px;");
        
        // Login Button
        Button loginBtn = new Button("LOGIN");
        loginBtn.setPrefHeight(40);
        loginBtn.setPrefWidth(300);
        loginBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; " +
                         "-fx-text-fill: black; " +
                         "-fx-font-size: 16px; " +
                         "-fx-font-weight: bold;");
        
        // Forgot Password Link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setStyle("-fx-text-fill: black; -fx-underline: true; -fx-font-size: 14px;");
        
        // Error message label
        Label errorMsgLabel = new Label();
        errorMsgLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-size: 14px;");
        
        // Add login functionality
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                errorMsgLabel.setText("Please enter both fields.");
                return;
            }
            
            try (Connection conn = DBConnection.getConnection()) {
                // First fetch userID and userType
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
                    errorMsgLabel.setText("Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorMsgLabel.setText("Database error.");
            }
        });
        
        // Add forgot password functionality
        forgotPasswordLink.setOnAction(e -> {
            // Add code to handle forgot password
            System.out.println("Forgot password clicked");
            // You can add a password recovery page here
        });
        
        // Add all components to login section
        loginSection.getChildren().addAll(
            loginTitle, 
            emailField, 
            passwordField, 
            loginBtn, 
            forgotPasswordLink, 
            errorMsgLabel
        );
        
        // Add all sections to main layout
        mainLayout.getChildren().addAll(logoSection, loginSection);
        
        // Create scene and set it to the stage
        Scene scene = new Scene(mainLayout, 400, 600);
        
        stage.setScene(scene);
        stage.setTitle("Eventure - Login");
        stage.setResizable(false);
        stage.show();
    }
}