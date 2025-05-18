package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class ProfilePage {

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
    private Stage stage;

    public void show(Stage stage, int userID) {
        this.userID = userID;
        this.stage = stage;

        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        nameField = new TextField();
        emailField = new TextField();
        usernameField = new TextField();
        passwordField = new PasswordField();
        userTypeLabel = new Label();

        nameField.setEditable(false);
        emailField.setEditable(false);
        usernameField.setEditable(false);
        passwordField.setEditable(false);

        // Fetch user info
        try (ResultSet rs = DBConnection.getUserByID(userID)) {
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                usernameField.setText(rs.getString("username"));
                passwordField.setText(rs.getString("password"));
                userTypeLabel.setText("Role: " + rs.getString("userType"));
            }
            rs.getStatement().getConnection().close();
        } catch (SQLException e) {
            showError("Failed to load profile", e.getMessage());
            return;
        }

        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(10);
        infoGrid.setHgap(10);
        infoGrid.setPadding(new Insets(10));
        infoGrid.addRow(0, new Label("Name:"), nameField);
        infoGrid.addRow(1, new Label("Email:"), emailField);
        infoGrid.addRow(2, new Label("Username:"), usernameField);
        infoGrid.addRow(3, new Label("Password:"), passwordField);
        infoGrid.addRow(4, new Label("User Type:"), userTypeLabel);

        editBtn = new Button("Edit Profile");
        saveBtn = new Button("Save");
        cancelBtn = new Button("Cancel");
        changePassBtn = new Button("Change Password");
        deleteBtn = new Button("Delete Account");

        HBox buttonBox = new HBox(10, editBtn, changePassBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER);

        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);

        editBtn.setOnAction(e -> enterEditMode());
        cancelBtn.setOnAction(e -> exitEditMode());
        saveBtn.setOnAction(e -> saveProfile());
        changePassBtn.setOnAction(e -> showChangePasswordDialog());
        deleteBtn.setOnAction(e -> confirmDelete());

        VBox layout = new VBox(15, title, infoGrid, buttonBox, new HBox(10, saveBtn, cancelBtn));
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 400);
        stage.setTitle("Profile");
        stage.setScene(scene);
        stage.show();
    }

    private void enterEditMode() {
        nameField.setEditable(true);
        emailField.setEditable(true);
        usernameField.setEditable(true);

        editBtn.setVisible(false);
        changePassBtn.setVisible(false);
        deleteBtn.setVisible(false);
        saveBtn.setVisible(true);
        cancelBtn.setVisible(true);
    }

    private void exitEditMode() {
        nameField.setEditable(false);
        emailField.setEditable(false);
        usernameField.setEditable(false);

        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);
        editBtn.setVisible(true);
        changePassBtn.setVisible(true);
        deleteBtn.setVisible(true);
    }

    private void saveProfile() {
        boolean success = DBConnection.updateUserProfile(
                userID,
                nameField.getText(),
                emailField.getText(),
                usernameField.getText(),
                passwordField.getText()
        );
        if (success) {
            showInfo("Profile updated successfully!");
            exitEditMode();
        } else {
            showError("Update Failed", "Could not update your profile.");
        }
    }

    private void showChangePasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        dialog.getDialogPane().setContent(new VBox(10, new Label("New Password:"), newPass));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return newPass.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            passwordField.setText(newPassword);
            saveProfile(); // Auto-save on password change
        });
    }

    private void confirmDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This action is irreversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deleteUser(userID)) {
                    showInfo("Account deleted.");
                    stage.close(); // Close the profile window
                } else {
                    showError("Error", "Failed to delete account.");
                }
            }
        });
    }

    private boolean deleteUser(int id) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=EventManagementSystem;encrypt=true;trustServerCertificate=true",
                "LMS_Admin",
                "moks123")) {
            String sql = "DELETE FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}