package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.sql.*;

public class ProfilePage {
    // UI color constants matching MyEventsPage
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String DANGER_COLOR = "#F44336";
    private static final String WARNING_COLOR = "#FF9800";
    
    private TextField nameField;
    private TextField emailField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label userTypeLabel;
    private Button editBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private Button changePassBtn;
    private Button deleteBtn;
    private int userID;
    private String attendeeName = "";
    private Stage stage;
    private Stage previousStage; // Store the previous stage to go back to
    
    // Store original values for cancel functionality
    private String originalName = "";
    private String originalEmail = "";
    private String originalUsername = "";
    
    public void show(Stage stage, int userID) {
        show(stage, userID, null);
    }
    
    public void show(Stage stage, int userID, Stage previousStage) {
        this.userID = userID;
        this.stage = stage;
        this.previousStage = previousStage;
        
        // Fetch attendee name for display - FIXED to use 'name' column
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                attendeeName = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(25);
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Back Button Section
        HBox backButtonSection = createBackButtonSection();
        
        // Page Header
        VBox headerSection = createHeaderSection();
        
        // Profile Content Section
        VBox profileSection = createProfileSection();
        
        contentPane.getChildren().addAll(backButtonSection, headerSection, profileSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - My Profile");
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createBackButtonSection() {
        HBox backSection = new HBox();
        backSection.setAlignment(Pos.CENTER_LEFT);
        backSection.setPadding(new Insets(0, 0, 10, 0));
        
        Button backButton = new Button("← Back");
        backButton.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        // Hover effects
        backButton.setOnMouseEntered(e ->
            backButton.setStyle(
                "-fx-background-color: #8593C1;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            )
        );
        
        backButton.setOnMouseExited(e ->
            backButton.setStyle(
                "-fx-background-color: " + BLUE_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            )
        );
        
        // Back button action - FIXED
        backButton.setOnAction(e -> {
            if (previousStage != null) {
                previousStage.show();
                stage.close();
            } else {
                // Determine where to go based on user type
                String userType = getUserType(userID);
                
                if ("Organizer".equalsIgnoreCase(userType)) {
                    // Go to organizer dashboard
                    OrganizerDashboard organizerDashboard = new OrganizerDashboard();
                    organizerDashboard.show(stage, userID);
                } else {
                    // Default to attendee dashboard
                    AttendeeDashboard dashboard = new AttendeeDashboard();
                    dashboard.show(stage, userID);
                }
            }
        });
        
        backSection.getChildren().add(backButton);
        return backSection;
    }
    
    // Helper method to get user type
    private String getUserType(int userID) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT userType FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userType");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Attendee"; // Default fallback
    }
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        
        // Main title
        Label titleLabel = new Label("My Profile");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Welcome message
        Label welcomeLabel = new Label("Manage your account settings and preferences");
        welcomeLabel.setStyle("-fx-font-size: 16px;" +
                             "-fx-text-fill: #666666;" +
                             "-fx-padding: 0 0 5px 0;");
        
        headerSection.getChildren().addAll(titleLabel, welcomeLabel);
        return headerSection;
    }
    
    private VBox createProfileSection() {
        VBox profileSection = new VBox(20);
        
        // Profile container with blue background
        VBox profileContainer = new VBox(20);
        profileContainer.setPadding(new Insets(25));
        profileContainer.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                                 "-fx-background-radius: 12px;");
        
        Label profileTitle = new Label("Profile Information");
        profileTitle.setStyle("-fx-font-size: 18px;" +
                             "-fx-font-weight: bold;" +
                             "-fx-text-fill: white;");
        
        // Profile form container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                              "-fx-background-radius: 8px;");
        
        // Create form fields
        GridPane formGrid = createFormGrid();
        
        // Button container
        VBox buttonContainer = createButtonContainer();
        
        formContainer.getChildren().addAll(formGrid, buttonContainer);
        profileContainer.getChildren().addAll(profileTitle, formContainer);
        profileSection.getChildren().add(profileContainer);
        
        return profileSection;
    }
    
    private GridPane createFormGrid() {
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(15);
        formGrid.setPadding(new Insets(10));
        
        // Initialize fields
        nameField = createStyledTextField();
        emailField = createStyledTextField();
        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();
        userTypeLabel = new Label();
        
        // Style user type label
        userTypeLabel.setStyle("-fx-font-size: 14px;" +
                              "-fx-text-fill: " + DARK_TEXT + ";" +
                              "-fx-padding: 8px 12px;" +
                              "-fx-background-color: " + YELLOW_COLOR + ";" +
                              "-fx-background-radius: 6px;" +
                              "-fx-font-weight: bold;");
        
        // Set fields to non-editable initially
        nameField.setEditable(false);
        emailField.setEditable(false);
        usernameField.setEditable(false);
        passwordField.setEditable(false);
        
        // Fetch user info
        loadUserProfile();
        
        // Create labels
        Label nameLabel = createFieldLabel("Full Name:");
        Label emailLabel = createFieldLabel("Email Address:");
        Label usernameLabel = createFieldLabel("Username:");
        Label passwordLabel = createFieldLabel("Password:");
        Label typeLabel = createFieldLabel("Account Type:");
        
        // Add to grid
        formGrid.addRow(0, nameLabel, nameField);
        formGrid.addRow(1, emailLabel, emailField);
        formGrid.addRow(2, usernameLabel, usernameField);
        formGrid.addRow(3, passwordLabel, passwordField);
        formGrid.addRow(4, typeLabel, userTypeLabel);
        
        // Set column constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(120);
        labelCol.setPrefWidth(120);
        
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(250);
        
        formGrid.getColumnConstraints().addAll(labelCol, fieldCol);
        
        return formGrid;
    }
    
    private TextField createStyledTextField() {
        TextField field = new TextField();
        field.setStyle("-fx-font-size: 14px;" +
                      "-fx-padding: 10px;" +
                      "-fx-background-color: #F8F9FA;" +
                      "-fx-border-color: #E0E0E0;" +
                      "-fx-border-radius: 6px;" +
                      "-fx-background-radius: 6px;");
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-font-size: 14px;" +
                              "-fx-padding: 10px;" +
                              "-fx-background-color: " + WHITE_COLOR + ";" +
                              "-fx-border-color: " + BLUE_COLOR + ";" +
                              "-fx-border-width: 2px;" +
                              "-fx-border-radius: 6px;" +
                              "-fx-background-radius: 6px;");
            } else {
                field.setStyle("-fx-font-size: 14px;" +
                              "-fx-padding: 10px;" +
                              "-fx-background-color: #F8F9FA;" +
                              "-fx-border-color: #E0E0E0;" +
                              "-fx-border-radius: 6px;" +
                              "-fx-background-radius: 6px;");
            }
        });
        
        return field;
    }
    
    private PasswordField createStyledPasswordField() {
        PasswordField field = new PasswordField();
        field.setStyle("-fx-font-size: 14px;" +
                      "-fx-padding: 10px;" +
                      "-fx-background-color: #F8F9FA;" +
                      "-fx-border-color: #E0E0E0;" +
                      "-fx-border-radius: 6px;" +
                      "-fx-background-radius: 6px;");
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-font-size: 14px;" +
                              "-fx-padding: 10px;" +
                              "-fx-background-color: " + WHITE_COLOR + ";" +
                              "-fx-border-color: " + BLUE_COLOR + ";" +
                              "-fx-border-width: 2px;" +
                              "-fx-border-radius: 6px;" +
                              "-fx-background-radius: 6px;");
            } else {
                field.setStyle("-fx-font-size: 14px;" +
                              "-fx-padding: 10px;" +
                              "-fx-background-color: #F8F9FA;" +
                              "-fx-border-color: #E0E0E0;" +
                              "-fx-border-radius: 6px;" +
                              "-fx-background-radius: 6px;");
            }
        });
        
        return field;
    }
    
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px;" +
                      "-fx-font-weight: bold;" +
                      "-fx-text-fill: " + DARK_TEXT + ";");
        return label;
    }
    
    private VBox createButtonContainer() {
        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(15, 0, 0, 0));
        
        // Create buttons
        editBtn = createStyledButton("Edit Profile", SUCCESS_COLOR);
        saveBtn = createStyledButton("Save Changes", SUCCESS_COLOR);
        cancelBtn = createStyledButton("Cancel", "#95A5A6");
        changePassBtn = createStyledButton("Change Password", WARNING_COLOR);
        deleteBtn = createStyledButton("Delete Account", DANGER_COLOR);
        
        // Main action buttons (shown when not editing)
        HBox mainButtons = new HBox(15);
        mainButtons.setAlignment(Pos.CENTER);
        mainButtons.getChildren().addAll(editBtn, changePassBtn, deleteBtn);
        
        // Edit mode buttons (shown when editing)
        HBox editButtons = new HBox(15);
        editButtons.setAlignment(Pos.CENTER);
        editButtons.getChildren().addAll(saveBtn, cancelBtn);
        
        // Initially show main buttons, hide edit buttons
        mainButtons.setVisible(true);
        mainButtons.setManaged(true);
        editButtons.setVisible(false);
        editButtons.setManaged(false);
        
        // Button actions
        editBtn.setOnAction(e -> enterEditMode(mainButtons, editButtons));
        cancelBtn.setOnAction(e -> exitEditMode(mainButtons, editButtons));
        saveBtn.setOnAction(e -> saveProfile(mainButtons, editButtons));
        changePassBtn.setOnAction(e -> showChangePasswordDialog());
        deleteBtn.setOnAction(e -> confirmDelete());
        
        buttonContainer.getChildren().addAll(mainButtons, editButtons);
        return buttonContainer;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            String hoverColor = getHoverColor(color);
            button.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            );
        });
        
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8px;"
            )
        );
        
        return button;
    }
    
    private String getHoverColor(String originalColor) {
        switch (originalColor) {
            case SUCCESS_COLOR: return "#45A049";
            case DANGER_COLOR: return "#D32F2F";
            case WARNING_COLOR: return "#F57C00";
            default: return "#7F8C8D";
        }
    }
    
    // FIXED: Updated to use 'name' column instead of firstName/lastName
    private void loadUserProfile() {
        try (Connection conn = DBConnection.getConnection()) {
            // Use the correct column names based on your database schema
            String sql = "SELECT name, email, username, userType FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String userType = rs.getString("userType");
                
                nameField.setText(name != null ? name : "");
                emailField.setText(email != null ? email : "");
                usernameField.setText(username != null ? username : "");
                passwordField.setText("••••••••"); // Hide actual password
                userTypeLabel.setText(userType != null ? userType : "");
                
                // Store original values for cancel functionality
                originalName = name != null ? name : "";
                originalEmail = email != null ? email : "";
                originalUsername = username != null ? username : "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load profile", e.getMessage());
        }
    }
    
    private void enterEditMode(HBox mainButtons, HBox editButtons) {
        // Store current values as originals
        originalName = nameField.getText();
        originalEmail = emailField.getText();
        originalUsername = usernameField.getText();
        
        // Make fields editable (except password in edit mode)
        nameField.setEditable(true);
        emailField.setEditable(true);
        usernameField.setEditable(true);
        passwordField.setEditable(false); // Don't edit password in regular edit mode
        
        // Switch button visibility
        mainButtons.setVisible(false);
        mainButtons.setManaged(false);
        editButtons.setVisible(true);
        editButtons.setManaged(true);
        
        // Focus on first field
        nameField.requestFocus();
    }
    
    private void exitEditMode(HBox mainButtons, HBox editButtons) {
        // Restore original values
        nameField.setText(originalName);
        emailField.setText(originalEmail);
        usernameField.setText(originalUsername);
        passwordField.setText("••••••••");
        
        // Make fields non-editable
        nameField.setEditable(false);
        emailField.setEditable(false);
        usernameField.setEditable(false);
        passwordField.setEditable(false);
        
        // Switch button visibility
        editButtons.setVisible(false);
        editButtons.setManaged(false);
        mainButtons.setVisible(true);
        mainButtons.setManaged(true);
    }
    
    // FIXED: Updated to use 'name' column instead of firstName/lastName
    private void saveProfile(HBox mainButtons, HBox editButtons) {
        // Validate fields
        if (nameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() || 
            usernameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Please fill in all required fields.");
            return;
        }
        
        // Validate email format
        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Validation Error", "Please enter a valid email address.");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // Update user profile using 'name' column
            String sql = "UPDATE [User] SET name = ?, email = ?, username = ? WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, email);
            stmt.setString(3, usernameField.getText().trim());
            stmt.setInt(4, userID);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showSuccess("Profile updated successfully!");
                
                // Update original values with new ones
                originalName = nameField.getText().trim();
                originalEmail = emailField.getText().trim();
                originalUsername = usernameField.getText().trim();
                
                // Exit edit mode
                passwordField.setText("••••••••");
                nameField.setEditable(false);
                emailField.setEditable(false);
                usernameField.setEditable(false);
                passwordField.setEditable(false);
                
                editButtons.setVisible(false);
                editButtons.setManaged(false);
                mainButtons.setVisible(true);
                mainButtons.setManaged(true);
            } else {
                showError("Update Failed", "Could not update your profile. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("UNIQUE constraint failed") || 
                e.getMessage().contains("Violation of UNIQUE KEY constraint")) {
                showError("Update Failed", "Username or email already exists. Please choose different values.");
            } else {
                showError("Update Failed", "Database error: " + e.getMessage());
            }
        }
    }
    
    private void showChangePasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        
        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        PasswordField newPass = createStyledPasswordField();
        newPass.setPromptText("Enter new password");
        PasswordField confirmPass = createStyledPasswordField();
        confirmPass.setPromptText("Confirm new password");
        
        content.getChildren().addAll(
            createFieldLabel("New Password:"), newPass,
            createFieldLabel("Confirm Password:"), confirmPass
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (!newPass.getText().equals(confirmPass.getText())) {
                    showError("Password Error", "Passwords do not match!");
                    return null;
                }
                if (newPass.getText().length() < 6) {
                    showError("Password Error", "Password must be at least 6 characters long!");
                    return null;
                }
                return newPass.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword != null) {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "UPDATE [User] SET password = ? WHERE userID = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, newPassword);
                    stmt.setInt(2, userID);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        showSuccess("Password changed successfully!");
                    } else {
                        showError("Update Failed", "Could not update password.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Update Failed", "Database error: " + e.getMessage());
                }
            }
        });
    }
    
    // FIXED: Improved delete functionality with proper error handling
    private void confirmDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Delete Your Account");
        confirm.setContentText("Are you sure you want to delete your account?\n\nThis action is permanent and cannot be undone.\nAll your data will be permanently removed.");
        
        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deleteUser(userID)) {
                    showSuccess("Account deleted successfully.");
                    // Return to login page
                    try {
                        new LoginPage(new Stage());
                        stage.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // If LoginPage constructor fails, just close current stage
                        stage.close();
                    }
                } else {
                    showError("Error", "Failed to delete account. Please try again.");
                }
            }
        });
    }
    
    // FIXED: Improved delete method with better error handling and transaction support
    private boolean deleteUser(int id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete in proper order to avoid foreign key constraint violations
            
            // 1. Delete event registrations first
            String deleteRegistrations = "DELETE FROM EventRegistration WHERE userID = ?";
            PreparedStatement stmt1 = conn.prepareStatement(deleteRegistrations);
            stmt1.setInt(1, id);
            stmt1.executeUpdate();
            System.out.println("Deleted event registrations for user " + id);
            
            // 2. Delete events created by this user (if organizer)
            String deleteEvents = "DELETE FROM Event WHERE organizerID = ?";
            PreparedStatement stmt2 = conn.prepareStatement(deleteEvents);
            stmt2.setInt(1, id);
            int eventsDeleted = stmt2.executeUpdate();
            System.out.println("Deleted " + eventsDeleted + " events for user " + id);
            
            // 3. Finally delete the user
            String deleteUser = "DELETE FROM [User] WHERE userID = ?";
            PreparedStatement stmt3 = conn.prepareStatement(deleteUser);
            stmt3.setInt(1, id);
            int rowsAffected = stmt3.executeUpdate();
            System.out.println("Deleted user " + id + ", rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                conn.commit(); // Commit transaction
                return true;
            } else {
                conn.rollback(); // Rollback if user deletion failed
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Delete user error: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Restore auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
}