package ui;
import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrganizerNotificationPage {
    // UI color constants matching the image design
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String DANGER_COLOR = "#F44336";
    private static final String INFO_COLOR = "#2196F3";
    private static final String WARNING_COLOR = "#FF9800";
    private static final String LIGHT_BLUE = "#B8C6E3";
    private static final String ORANGE_COLOR = "#FF6B35";
    private static final String GREEN_COLOR = "#28A745";
    private static final String RED_COLOR = "#DC3545";
    
    private int organizerID;
    private String organizerName = "";
    private Stage currentStage;
    private ListView<NotificationItem> notificationListView;
    private TextArea messageArea;
    private Button markReadBtn;
    private Button markUnreadBtn;
    private Button deleteBtn;
    private Button clearAllBtn;
    private Button refreshBtn;
    private Label unreadCountLabel;
    
    public void show(Stage stage, int organizerID) {
        this.organizerID = organizerID;
        this.currentStage = stage;
        
        // Fetch organizer name from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                organizerName = rs.getString("firstName") + " " + rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");
        
        // --- Top Navigation Bar (keep existing) ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        VBox contentPane = new VBox(20);
        contentPane.setPadding(new Insets(30, 40, 30, 40));
        contentPane.setStyle("-fx-background-color: #f0f0f0;");
        
        // Header Section
        VBox headerSection = createHeaderSection();
        
        // Action Buttons Section (Clear All Read, Refresh)
        HBox actionButtonsSection = createTopActionButtons();
        
        // Main Content Grid
        HBox contentGrid = new HBox(20);
        VBox.setVgrow(contentGrid, Priority.ALWAYS);
        
        // Left side - Notifications List
        VBox notificationsSection = createNotificationsSection();
        HBox.setHgrow(notificationsSection, Priority.ALWAYS);
        
        // Right side - Message Details
        VBox messageSection = createMessageSection();
        HBox.setHgrow(messageSection, Priority.ALWAYS);
        
        contentGrid.getChildren().addAll(notificationsSection, messageSection);
        
        contentPane.getChildren().addAll(headerSection, actionButtonsSection, contentGrid);
        mainLayout.setCenter(contentPane);
        
        // Scene
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Organizer Notifications");
        stage.setScene(scene);
        stage.show();
        
        // Initial data load
        loadNotifications();
    }
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        
        // Notification icon and title
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        // Notification icon
        Label notificationIcon = new Label("🔔");
        notificationIcon.setStyle("-fx-font-size: 24px;");
        
        // Title
        Label titleLabel = new Label("Notifications");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
        
        // Unread badge
        unreadCountLabel = new Label("3 unread");
        unreadCountLabel.setStyle(
            "-fx-background-color: " + ORANGE_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 4px 8px;" +
            "-fx-background-radius: 10px;" +
            "-fx-font-weight: bold;"
        );
        
        titleBox.getChildren().addAll(notificationIcon, titleLabel, unreadCountLabel);
        
        // Subtitle
        Label subtitleLabel = new Label("Stay updated with your latest notifications, User!");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        
        headerSection.getChildren().addAll(titleBox, subtitleLabel);
        return headerSection;
    }
    
    private HBox createTopActionButtons() {
        HBox actionButtonsBox = new HBox(15);
        actionButtonsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Clear All Read button
        clearAllBtn = new Button("🗑 Clear All Read");
        clearAllBtn.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-text-fill: " + DARK_TEXT + ";" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Refresh button
        refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: " + INFO_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Button actions
        clearAllBtn.setOnAction(e -> clearAllReadNotifications());
        refreshBtn.setOnAction(e -> loadNotifications());
        
        actionButtonsBox.getChildren().addAll(clearAllBtn, refreshBtn);
        return actionButtonsBox;
    }
    
    private VBox createNotificationsSection() {
        VBox notificationsSection = new VBox(15);
        
        // Section header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        Label sectionIcon = new Label("📋");
        sectionIcon.setStyle("-fx-font-size: 16px;");
        
        Label sectionTitle = new Label("Notification List");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        headerBox.getChildren().addAll(sectionIcon, sectionTitle);
        
        // Notification list container
        VBox listContainer = new VBox();
        listContainer.setStyle(
            "-fx-background-color: " + LIGHT_BLUE + ";" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 15px;"
        );
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        
        // Add header to container
        listContainer.getChildren().add(headerBox);
        
        // Initialize notification list
        notificationListView = new ListView<>();
        notificationListView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 5px;"
        );
        VBox.setVgrow(notificationListView, Priority.ALWAYS);
        notificationListView.setMinHeight(400);
        
        notificationListView.setCellFactory(param -> new ListCell<NotificationItem>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cellBox = new VBox(8);
                    cellBox.setPadding(new Insets(12, 15, 12, 15));
                    
                    HBox headerRow = new HBox(10);
                    headerRow.setAlignment(Pos.CENTER_LEFT);
                    
                    // Status icon
                    Label statusIcon = new Label("💬");
                    statusIcon.setStyle("-fx-font-size: 14px;");
                    
                    // Title
                    Label titleLabel = new Label(item.title);
                    titleLabel.setStyle(
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + INFO_COLOR + ";"
                    );
                    
                    headerRow.getChildren().addAll(statusIcon, titleLabel);
                    
                    // Date
                    Label dateLabel = new Label(formatDateTime(item.createdAt));
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                    
                    // Message preview
                    String preview = item.message.length() > 50 ? 
                        item.message.substring(0, 50) + "..." : item.message;
                    Label previewLabel = new Label(preview);
                    previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
                    previewLabel.setWrapText(true);
                    
                    cellBox.getChildren().addAll(headerRow, dateLabel, previewLabel);
                    setGraphic(cellBox);
                    setText(null);
                    
                    // Style based on read status
                    if (!item.isRead) {
                        setStyle(
                            "-fx-background-color: #E3F2FD;" +
                            "-fx-border-color: " + INFO_COLOR + ";" +
                            "-fx-border-width: 0 0 0 3px;" +
                            "-fx-background-radius: 5px;"
                        );
                    } else {
                        setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
                    }
                }
            }
        });
        
        notificationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                messageArea.setText(newSel.message);
                updateActionButtons(newSel.isRead);
            } else {
                messageArea.clear();
                messageArea.setPromptText("Select a notification to view its details...");
                disableActionButtons();
            }
        });
        
        listContainer.getChildren().add(notificationListView);
        notificationsSection.getChildren().add(listContainer);
        
        return notificationsSection;
    }
    
    private VBox createMessageSection() {
        VBox messageSection = new VBox(15);
        
        // Section header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        Label sectionIcon = new Label("💬");
        sectionIcon.setStyle("-fx-font-size: 16px;");
        
        Label sectionTitle = new Label("Message Details");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        headerBox.getChildren().addAll(sectionIcon, sectionTitle);
        
        // Message container
        VBox messageContainer = new VBox(15);
        messageContainer.setStyle(
            "-fx-background-color: " + LIGHT_BLUE + ";" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 15px;"
        );
        VBox.setVgrow(messageContainer, Priority.ALWAYS);
        
        // Add header to container
        messageContainer.getChildren().add(headerBox);
        
        // Full Message label
        Label messageTitle = new Label("Full Message:");
        messageTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + DARK_TEXT + ";");
        
        // Message area
        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10px;"
        );
        messageArea.setPromptText("Select a notification to view its details...");
        VBox.setVgrow(messageArea, Priority.ALWAYS);
        messageArea.setMinHeight(200);
        
        // Action buttons
        HBox buttonsBox = createMessageActionButtons();
        
        messageContainer.getChildren().addAll(messageTitle, messageArea, buttonsBox);
        messageSection.getChildren().add(messageContainer);
        
        return messageSection;
    }
    
    private HBox createMessageActionButtons() {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        // Mark as Read button
        markReadBtn = new Button("✓ Mark as Read");
        markReadBtn.setStyle(
            "-fx-background-color: " + GREEN_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Mark as Unread button
        markUnreadBtn = new Button("↩ Mark as Unread");
        markUnreadBtn.setStyle(
            "-fx-background-color: " + INFO_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Delete button
        deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle(
            "-fx-background-color: " + RED_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Initially disable buttons
        disableActionButtons();
        
        // Button actions
        markReadBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead) {
                updateNotificationReadStatus(selected.id, true);
                selected.isRead = true;
                refreshList();
                notificationListView.getSelectionModel().select(selected);
            }
        });
        
        markUnreadBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.isRead) {
                updateNotificationReadStatus(selected.id, false);
                selected.isRead = false;
                refreshList();
                notificationListView.getSelectionModel().select(selected);
            }
        });
        
        deleteBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Notification");
                confirm.setHeaderText("Delete this notification?");
                confirm.setContentText("This action cannot be undone.");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteNotification(selected.id);
                        loadNotifications();
                    }
                });
            }
        });
        
        buttonsBox.getChildren().addAll(markReadBtn, markUnreadBtn, deleteBtn);
        return buttonsBox;
    }
    
    private void updateActionButtons(boolean isRead) {
        markReadBtn.setDisable(isRead);
        markUnreadBtn.setDisable(!isRead);
        deleteBtn.setDisable(false);
    }
    
    private void disableActionButtons() {
        markReadBtn.setDisable(true);
        markUnreadBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }
    
    private void clearAllReadNotifications() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Read");
        confirm.setHeaderText("Delete all read notifications?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM Notification WHERE userID = ? AND notificationType = 'Organizer' AND isRead = 1";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, organizerID);
                    stmt.executeUpdate();
                    loadNotifications();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showStyledError("Failed to clear read notifications: " + e.getMessage());
                }
            }
        });
    }
    
    private void refreshList() {
        NotificationItem current = notificationListView.getSelectionModel().getSelectedItem();
        loadNotifications();
        if (current != null) {
            for (NotificationItem item : notificationListView.getItems()) {
                if (item.id == current.id) {
                    notificationListView.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }
    
    private void loadNotifications() {
        notificationListView.getItems().clear();
        String sql = "SELECT notificationID, title, message, createdAt, isRead, notificationType, userID " +
                "FROM Notification WHERE userID = ? AND notificationType = 'Organizer' " +
                "ORDER BY createdAt DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();
            ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();
            int unreadCount = 0;
            
            while (rs.next()) {
                NotificationItem item = new NotificationItem(
                        rs.getInt("notificationID"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt"),
                        rs.getBoolean("isRead"),
                        rs.getString("notificationType"),
                        rs.getInt("userID")
                );
                notifications.add(item);
                if (!item.isRead) unreadCount++;
            }
            
            // Update unread count
            if (unreadCount > 0) {
                unreadCountLabel.setText(unreadCount + " unread");
                unreadCountLabel.setVisible(true);
            } else {
                unreadCountLabel.setVisible(false);
            }
            
            if (notifications.isEmpty()) {
                messageArea.clear();
                messageArea.setPromptText("No notifications found.");
            }
            
            notificationListView.setItems(notifications);
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledError("Failed to load notifications: " + e.getMessage());
        }
    }
    
    private void updateNotificationReadStatus(int notificationID, boolean markAsRead) {
        String sql = "UPDATE Notification SET isRead = ? WHERE notificationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, markAsRead);
            stmt.setInt(2, notificationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledError("Failed to update notification status: " + e.getMessage());
        }
    }
    
    private void deleteNotification(int notificationID) {
        String sql = "DELETE FROM Notification WHERE notificationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledError("Failed to delete notification: " + e.getMessage());
        }
    }
    
    private void showStyledError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatDateTime(Timestamp timestamp) {
        try {
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    // Keep the existing navigation bar methods
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
            // Create a white circle background
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            // Load the logo image
            ImageView logoView = new ImageView(new Image(new FileInputStream("resources/logo.png")));
            logoView.setFitHeight(32);
            logoView.setFitWidth(32);
            logoView.setPreserveRatio(true);
            
            // Stack the logo on the white circle
            logoContainer.getChildren().addAll(whiteCircle, logoView);
            logoSection.getChildren().add(logoContainer);
        } catch (Exception e) {
            // If logo loading fails, use text placeholder with white background
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            Label logoPlaceholder = new Label("E");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            logoContainer.getChildren().addAll(whiteCircle, logoPlaceholder);
            logoSection.getChildren().add(logoContainer);
        }
        
        // Navigation buttons section - centered
        HBox navButtonsSection = new HBox(10);
        navButtonsSection.setAlignment(Pos.CENTER);
        
        // Navigation Buttons with white background
        Button dashboardBtn = createNavButton("Dashboard");
        Button eventsBtn = createNavButton("Events");
        Button myEventsBtn = createNavButton("My Events");
        Button createEventBtn = createNavButton("Create Event");
        Button attendeesBtn = createNavButton("Attendees");
        Button notificationsBtn = createNavButton("Notifications");
        Button profileBtn = createNavButton("Profile");
        
        // Highlight current page
        notificationsBtn.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6px 10px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 12px;"
        );
        
        navButtonsSection.getChildren().addAll(dashboardBtn, eventsBtn, myEventsBtn, createEventBtn, attendeesBtn, notificationsBtn, profileBtn);
        
        // Switch Account button - right aligned
        Button switchAccountBtn = createNavButton("Switch Account");
        
        // Create spacers for proper distribution
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        // Add all sections to navbar with proper spacing
        navBar.getChildren().addAll(logoSection, leftSpacer, navButtonsSection, rightSpacer, switchAccountBtn);
        
        // Navigation Button Actions
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            currentStage.close();
        });
        
        dashboardBtn.setOnAction(e -> {
            OrganizerDashboard dashboard = new OrganizerDashboard();
            dashboard.show(currentStage, organizerID);
        });
        
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(currentStage, organizerID);
        });
        
        myEventsBtn.setOnAction(e -> {
            OrganizerEventsPage page = new OrganizerEventsPage();
            page.show(currentStage, organizerID);
        });
        
        createEventBtn.setOnAction(e -> {
            CreateEventPage page = new CreateEventPage();
            page.show(new Stage(), organizerID);
        });
        
        attendeesBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Event ID");
            dialog.setHeaderText("Enter Event ID");
            dialog.setContentText("Please enter the event ID:");
            dialog.showAndWait().ifPresent(eventID -> {
                try {
                    int id = Integer.parseInt(eventID);
                    ViewAttendeesPage page = new ViewAttendeesPage();
                    page.show(new Stage(), id, false);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Event ID").showAndWait();
                }
            });
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(currentStage, organizerID);
        });
        
        return navBar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        // Add a white background to nav buttons
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: #333333;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6px 10px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 12px;"
        );
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-text-fill: #333333;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6px 10px;" +
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
                "-fx-padding: 6px 10px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 12px;"
            )
        );
        return button;
    }
    
    private static class NotificationItem {
        int id;
        String title;
        String message;
        Timestamp createdAt;
        boolean isRead;
        String type;
        int userID;
        
        NotificationItem(int id, String title, String message, Timestamp createdAt, boolean isRead, String type, int userID) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
            this.isRead = isRead;
            this.type = type;
            this.userID = userID;
        }
    }
}