package ui;
import java.io.File;
import java.io.FileInputStream;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainPage {
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    
    public MainPage(Stage stage) {
        VBox layout = new VBox(20); // Spacing adjusted for larger window
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-padding: 40;"); // Increased padding for 1200x800
        
        // Create a placeholder for the logo (purple graduation cap icon with calendar)
        Label logoPlaceholder = new Label("EVENTURE");
        logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 48px; -fx-font-weight: bold;"); // Increased for 1200x800
        
        // Try to load the logo if it exists
        try {
            File logoFile = new File("resources/logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(new FileInputStream(logoFile));
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(120); // Increased for 1200x800
                logoView.setPreserveRatio(true);
                layout.getChildren().add(logoView);
            } else {
                // If logo not found, use the placeholder
                layout.getChildren().add(logoPlaceholder);
                System.out.println("Logo file not found at: " + logoFile.getAbsolutePath());
            }
        } catch (Exception e) {
            // If any error occurs, use the placeholder
            layout.getChildren().add(logoPlaceholder);
            System.out.println("Logo loading failed: " + e.getMessage());
        }
        
        // Welcome text
        Label welcomeLabel = new Label("Welcome to EVENTURE!");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 800;"); // Increased for 1200x800
        
        // Subtitle text
        Label subtitleLabel = new Label("Your Gateway to Seamless Event Journey");
        subtitleLabel.setStyle("-fx-font-size: 18px;"); // Increased for 1200x800
        
        // Login button with custom styling
        Button loginBtn = new Button("LOGIN");
        loginBtn.setPrefWidth(250); // Increased for 1200x800
        loginBtn.setPrefHeight(50); // Increased for 1200x800
        loginBtn.setStyle("-fx-background-color: " + BLUE_COLOR + "; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 18px; " + // Increased for 1200x800
                          "-fx-font-weight: bold; " +
                          "-fx-background-radius: 35;"); // Increased for 1200x800
        loginBtn.setOnAction(e -> new LoginPage(stage));
        
        // Register button with custom styling
        Button registerBtn = new Button("REGISTER");
        registerBtn.setPrefWidth(250); // Increased for 1200x800
        registerBtn.setPrefHeight(50); // Increased for 1200x800
        registerBtn.setStyle("-fx-background-color: " + YELLOW_COLOR + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " + // Increased for 1200x800
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 35;"); // Increased for 1200x800
        registerBtn.setOnAction(e -> new RegistrationPage(stage));
        
        // Add components to layout with proper spacing
        layout.getChildren().addAll(welcomeLabel, subtitleLabel, loginBtn, registerBtn);
        
        // Add more space between title and buttons (reduced margin)
        VBox.setMargin(loginBtn, new Insets(30, 0, 0, 0)); // Increased margin for 1200x800
        
        // Create scene with reduced dimensions
        Scene scene = new Scene(layout, 1200, 800); // Updated to 1200x800
        
        stage.setScene(scene);
        stage.setTitle("Eventure");
        stage.show();
    }
}