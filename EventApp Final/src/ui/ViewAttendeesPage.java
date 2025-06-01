package ui;
import db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.sql.*;
public class ViewAttendeesPage {
    // UI color constants matching organizer dashboard
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and welcome banner
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private int currentEventID;
    private boolean isOwner;
    private Stage currentStage;
    private String eventName = "";
    
    public void show(Stage stage, int eventID, boolean isOwner) {
        this.currentEventID = eventID;
        this.isOwner = isOwner;
        this.currentStage = stage;
        
        // Fetch event name from database
        fetchEventName(eventID);
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        // Create a blue background pane for the content
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(25));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Title Banner
        Label titleLabel = new Label("Attendees for " + eventName);
        titleLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                           "-fx-padding: 15px 20px;" +
                           "-fx-background-radius: 8px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 18px;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        // Attendees Section
        VBox attendeesSection = createAttendeesSection();
        VBox.setVgrow(attendeesSection, Priority.ALWAYS);
        
        contentPane.getChildren().addAll(titleLabel, attendeesSection);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 900, 600);
        stage.setTitle("Eventure - View Attendees");
        stage.setScene(scene);
        stage.show();
    }
    
    private void fetchEventName(int eventID) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM Event WHERE eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                eventName = rs.getString("name");
            } else {
                eventName = "Event #" + eventID;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            eventName = "Event #" + eventID;
        }
    }
    
    private HBox createNavBar() {
        HBox navBar = new HBox();
        navBar.setPadding(new Insets(8, 15, 8, 15));
        navBar.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        // Logo section
        HBox logoSection = new HBox(8);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        // Logo with circular white background
        try {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            ImageView logoView = new ImageView(new Image(new FileInputStream("resources/logo.png")));
            logoView.setFitHeight(32);
            logoView.setFitWidth(32);
            logoView.setPreserveRatio(true);
            
            logoContainer.getChildren().addAll(whiteCircle, logoView);
            logoSection.getChildren().add(logoContainer);
        } catch (Exception e) {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            Label logoPlaceholder = new Label("E");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            logoContainer.getChildren().addAll(whiteCircle, logoPlaceholder);
            logoSection.getChildren().add(logoContainer);
        }
        
        // Page title in center
        Label pageTitle = new Label("Attendee Management");
        pageTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Back button - right aligned
        Button backBtn = createNavButton("← Back to Dashboard");
        backBtn.setOnAction(e -> currentStage.close());
        
        // Create spacers for proper distribution
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        navBar.getChildren().addAll(logoSection, leftSpacer, pageTitle, rightSpacer, backBtn);
        
        return navBar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6px 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 12px;"
        );
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + WHITE_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        return button;
    }
    
    private VBox createAttendeesSection() {
        VBox attendeesSection = new VBox(15);
        attendeesSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 15px;" +
            "-fx-background-radius: 8px;"
        );
        
        // Section header with attendee count
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionLabel = new Label("Registered Attendees");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        
        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 0 0 0 10px;");
        
        headerBox.getChildren().addAll(sectionLabel, countLabel);
        
        // Attendees table with styling
        TableView<Attendee> attendeesTable = new TableView<>();
        attendeesTable.setStyle("-fx-background-color: " + YELLOW_COLOR + "; -fx-background-radius: 8px;");
        attendeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(attendeesTable, Priority.ALWAYS);
        
        // Name column
        TableColumn<Attendee, String> nameColumn = new TableColumn<>("Full Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(250);
        nameColumn.setStyle("-fx-font-weight: bold;");
        
        // Email column
        TableColumn<Attendee, String> emailColumn = new TableColumn<>("Email Address");
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailColumn.setPrefWidth(300);
        
        attendeesTable.getColumns().addAll(nameColumn, emailColumn);
        
        // Add actions column if owner
        if (isOwner) {
            TableColumn<Attendee, Void> actionColumn = new TableColumn<>("Actions");
            actionColumn.setPrefWidth(180);
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button removeBtn = new Button("Remove");
                private final HBox box = new HBox(8, editBtn, removeBtn);
                
                {
                    box.setAlignment(Pos.CENTER);
                    styleActionButton(editBtn, false);
                    styleActionButton(removeBtn, true);
                    
                    editBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        showEditDialog(attendee);
                    });
                    
                    removeBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        showRemoveConfirmation(attendee, attendeesTable, countLabel);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
            
            attendeesTable.getColumns().add(actionColumn);
        }
        
        // Load data and update count
        refreshTable(attendeesTable, countLabel);
        
        // Action buttons section
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button refreshBtn = new Button("🔄 Refresh");
        Button exportBtn = new Button("📊 Export List");
        
        styleActionButton(refreshBtn, false);
        styleActionButton(exportBtn, false);
        
        refreshBtn.setOnAction(e -> refreshTable(attendeesTable, countLabel));
        exportBtn.setOnAction(e -> exportAttendeesList());
        
        buttonBox.getChildren().addAll(refreshBtn, exportBtn);
        
        attendeesSection.getChildren().addAll(headerBox, attendeesTable, buttonBox);
        return attendeesSection;
    }
    
    private void styleActionButton(Button button, boolean isDanger) {
        String bgColor = isDanger ? "#FF6B6B" : WHITE_COLOR;
        String textColor = isDanger ? WHITE_COLOR : DARK_TEXT;
        String hoverBg = isDanger ? "#FF5252" : BLUE_COLOR;
        String hoverText = isDanger ? WHITE_COLOR : WHITE_COLOR;
        
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 6px 12px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-text-fill: " + hoverText + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
            )
        );
        
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 6px 12px;"
            )
        );
    }
    
    private void refreshTable(TableView<Attendee> table, Label countLabel) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            // Updated SQL query to use 'name' column instead of concatenating firstName + lastName
            String sql = "SELECT u.name as fullName, u.email FROM Registration r " +
                         "JOIN [User] u ON r.userID = u.userID WHERE r.eventID = ? ORDER BY u.name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentEventID);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                table.getItems().add(new Attendee(rs.getString("fullName"), rs.getString("email")));
                count++;
            }
            
            countLabel.setText("(" + count + " registered)");
            
            if (count == 0) {
                table.setPlaceholder(new Label("No attendees registered for this event yet."));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load attendees: " + e.getMessage());
        }
    }
    
    private void showRemoveConfirmation(Attendee attendee, TableView<Attendee> table, Label countLabel) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Attendee");
        confirmation.setHeaderText("Are you sure you want to remove this attendee?");
        confirmation.setContentText("Attendee: " + attendee.getName() + "\nEmail: " + attendee.getEmail() + 
                                   "\n\nThis action cannot be undone.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removeAttendee(attendee.getEmail());
                refreshTable(table, countLabel);
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Attendee removed successfully.");
            }
        });
    }
    
    private void removeAttendee(String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String getUserID = "SELECT userID FROM [User] WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserID);
            userStmt.setString(1, email);
            ResultSet rs = userStmt.executeQuery();
            
            if (rs.next()) {
                int userID = rs.getInt("userID");
                String deleteSQL = "DELETE FROM Registration WHERE eventID = ? AND userID = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteSQL);
                stmt.setInt(1, currentEventID);
                stmt.setInt(2, userID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove attendee: " + e.getMessage());
        }
    }
    
    private void showEditDialog(Attendee attendee) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Attendee Information");
        
        // Main layout with blue background
        StackPane wrapper = new StackPane();
        wrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        wrapper.setPadding(new Insets(20));
        
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                       "-fx-background-radius: 10px;");
        layout.setAlignment(Pos.CENTER);
        
        // Title
        Label title = new Label("Edit Attendee Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        
        // Form section
        VBox formBox = new VBox(15);
        formBox.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-padding: 20px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label nameLabel = new Label("Full Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField nameField = new TextField(attendee.getName());
        nameField.setStyle("-fx-font-size: 12px; -fx-padding: 8px;");
        
        Label emailLabel = new Label("Email Address:");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField emailField = new TextField(attendee.getEmail());
        emailField.setStyle("-fx-font-size: 12px; -fx-padding: 8px;");
        
        formBox.getChildren().addAll(nameLabel, nameField, emailLabel, emailField);
        
        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = new Button("💾 Save Changes");
        Button cancelBtn = new Button("✖ Cancel");
        
        styleActionButton(saveBtn, false);
        styleActionButton(cancelBtn, true);
        
        saveBtn.setOnAction(e -> handleSaveEdit(attendee, nameField.getText().trim(), 
                                               emailField.getText().trim(), dialog));
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        layout.getChildren().addAll(title, formBox, buttonBox);
        wrapper.getChildren().add(layout);
        
        Scene scene = new Scene(wrapper, 400, 300);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void handleSaveEdit(Attendee attendee, String newName, String newEmail, Stage dialog) {
        if (newName.isEmpty() || newEmail.isEmpty()) {
            showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
            return;
        }
        
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // Updated SQL query to use 'name' column instead of firstName and lastName
            String updateSql = "UPDATE [User] SET name = ?, email = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setString(3, attendee.getEmail());
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                dialog.close();
                // Refresh the main table
                TableView<Attendee> table = (TableView<Attendee>) currentStage.getScene().lookup("TableView");
                if (table != null) {
                    Label countLabel = (Label) currentStage.getScene().lookup("Label");
                    refreshTable(table, countLabel);
                }
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Attendee information updated successfully.");
            } else {
                showStyledAlert(Alert.AlertType.ERROR, "Error", "Failed to update attendee information.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save changes: " + ex.getMessage());
        }
    }
    
    private void exportAttendeesList() {
        // Placeholder for export functionality
        showStyledAlert(Alert.AlertType.INFORMATION, "Export Feature", 
                       "Export functionality will be implemented soon.\nThis will allow you to export the attendee list to CSV or PDF format.");
    }
    
    private void showStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + DARK_TEXT + "; -fx-font-size: 12px;");
        
        alert.showAndWait();
    }
    
    public static class Attendee {
        private final String name;
        private final String email;
        
        public Attendee(String name, String email) {
            this.name = name;
            this.email = email;
        }
        
        public String getName() {
            return name;
        }
        
        public String getEmail() {
            return email;
        }
    }
}