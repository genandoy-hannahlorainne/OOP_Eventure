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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationPage {
    // UI color constants matching other pages
    private static final String BLUE_COLOR = "#97A9D1";
    private static final String YELLOW_COLOR = "#F1D747";
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String DANGER_COLOR = "#F44336";
    private static final String INFO_COLOR = "#2196F3";
    private static final String WARNING_COLOR = "#FF9800";
    
    private int userID;
    private String userName = "";
    private ListView<NotificationItem> notificationListView;
    private TextArea messageArea;
    private Button markReadBtn;
    private Button markUnreadBtn;
    private Button deleteBtn;
    private Button clearAllBtn;
    private Label unreadCountLabel;
    
    public void show(Stage stage, int userID) {
        this.userID = userID;
        
        // Fetch user name from database
        loadUserName();
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(20));
        
        VBox contentPane = new VBox(25);
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 15px;");
        
        // Page Header
        VBox headerSection = createHeaderSection();
        
        // Notifications Content
        HBox notificationContent = createNotificationContent();
        
        contentPane.getChildren().addAll(headerSection, notificationContent);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Notifications");
        stage.setScene(scene);
        stage.show();
        
        loadNotifications();
    }
    
    private void loadUserName() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userName = rs.getString("firstName") + " " + rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            userName = "User";
        }
    }
    
    private HBox createNavBar() {
        HBox navBar = new HBox(12);
        navBar.setPadding(new Insets(8, 15, 8, 15));
        navBar.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
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
            navBar.getChildren().add(logoContainer);
        } catch (Exception e) {
            StackPane logoContainer = new StackPane();
            Circle whiteCircle = new Circle(20);
            whiteCircle.setFill(Color.WHITE);
            
            Label logoPlaceholder = new Label("E");
            logoPlaceholder.setStyle("-fx-text-fill: " + BLUE_COLOR + "; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            logoContainer.getChildren().addAll(whiteCircle, logoPlaceholder);
            navBar.getChildren().add(logoContainer);
        }
        
        // Spacer
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Navigation Buttons
        Button dashboardBtn = createNavButton("Dashboard");
        Button switchAccountBtn = createNavButton("Switch Account");
        Button calendarBtn = createNavButton("Calendar");
        Button notificationsBtn = createNavButton("Notification");
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
        
        // Spacer
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        navBar.getChildren().addAll(
            spacer1, 
            dashboardBtn, switchAccountBtn, calendarBtn, notificationsBtn, profileBtn,
            spacer2
        );
        
        // Navigation actions
        dashboardBtn.setOnAction(e -> {
            AttendeeDashboard dashboard = new AttendeeDashboard();
            dashboard.show(new Stage(), userID);
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            currentStage.close();
        });
        
        switchAccountBtn.setOnAction(e -> {
            new LoginPage(new Stage());
            Stage currentStage = (Stage) switchAccountBtn.getScene().getWindow();
            currentStage.close();
        });
        
        calendarBtn.setOnAction(e -> {
            CalendarPage calendarPage = new CalendarPage();
            calendarPage.show(new Stage(), userID);
            Stage currentStage = (Stage) calendarBtn.getScene().getWindow();
            currentStage.close();
        });
        
        profileBtn.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage();
            profilePage.show(new Stage(), userID);
            Stage currentStage = (Stage) profileBtn.getScene().getWindow();
            currentStage.close();
        });
        
        return navBar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
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
    
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(15);
        
        // Main title with notification icon
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📢 Notifications");
        titleLabel.setStyle("-fx-font-size: 28px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-text-fill: " + DARK_TEXT + ";");
        
        unreadCountLabel = new Label();
        unreadCountLabel.setStyle("-fx-background-color: " + DANGER_COLOR + ";" +
                                 "-fx-text-fill: white;" +
                                 "-fx-font-weight: bold;" +
                                 "-fx-font-size: 12px;" +
                                 "-fx-padding: 4px 8px;" +
                                 "-fx-background-radius: 10px;");
        
        titleBox.getChildren().addAll(titleLabel, unreadCountLabel);
        
        // Welcome message
        Label welcomeLabel = new Label("Stay updated with your latest notifications, " + userName + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px;" +
                             "-fx-text-fill: #666666;" +
                             "-fx-padding: 0 0 5px 0;");
        
        // Action buttons header
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        
        clearAllBtn = new Button("🗑️ Clear All Read");
        clearAllBtn.setStyle(
            "-fx-background-color: " + WARNING_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        clearAllBtn.setOnAction(e -> clearAllReadNotifications());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: " + INFO_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        refreshBtn.setOnAction(e -> loadNotifications());
        
        actionBox.getChildren().addAll(clearAllBtn, refreshBtn);
        
        headerSection.getChildren().addAll(titleBox, welcomeLabel, actionBox);
        return headerSection;
    }
    
    private HBox createNotificationContent() {
        HBox contentBox = new HBox(20);
        contentBox.setPrefHeight(500);
        
        // Left Panel - Notification List
        VBox leftPanel = createNotificationListPanel();
        
        // Right Panel - Message Details
        VBox rightPanel = createMessageDetailPanel();
        
        contentBox.getChildren().addAll(leftPanel, rightPanel);
        return contentBox;
    }
    
    private VBox createNotificationListPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPrefWidth(400);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                          "-fx-background-radius: 12px;");
        
        Label listTitle = new Label("📋 Notification List");
        listTitle.setStyle("-fx-font-size: 18px;" +
                          "-fx-font-weight: bold;" +
                          "-fx-text-fill: white;");
        
        // Styled notification list
        notificationListView = new ListView<>();
        notificationListView.setPrefHeight(400);
        notificationListView.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8px;"
        );
        
        notificationListView.setCellFactory(param -> new ListCell<NotificationItem>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create custom cell layout
                    VBox cellBox = new VBox(5);
                    cellBox.setPadding(new Insets(10));
                    
                    HBox headerBox = new HBox(10);
                    headerBox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Status indicator
                    Label statusIndicator = new Label(item.isRead ? "📖" : "📩");
                    statusIndicator.setStyle("-fx-font-size: 14px;");
                    
                    // Title
                    Label titleLabel = new Label(item.title);
                    titleLabel.setStyle("-fx-font-weight: bold;" +
                                       "-fx-font-size: 14px;" +
                                       "-fx-text-fill: " + DARK_TEXT + ";");
                    if (!item.isRead) {
                        titleLabel.setStyle(titleLabel.getStyle() + "-fx-text-fill: " + INFO_COLOR + ";");
                    }
                    
                    headerBox.getChildren().addAll(statusIndicator, titleLabel);
                    
                    // Date/time
                    Label dateLabel = new Label(formatDateTime(item.createdAt));
                    dateLabel.setStyle("-fx-font-size: 11px;" +
                                      "-fx-text-fill: #888888;");
                    
                    // Message preview (first 50 characters)
                    String preview = item.message.length() > 50 ? 
                        item.message.substring(0, 50) + "..." : item.message;
                    Label previewLabel = new Label(preview);
                    previewLabel.setStyle("-fx-font-size: 12px;" +
                                         "-fx-text-fill: #666666;" +
                                         "-fx-wrap-text: true;");
                    
                    cellBox.getChildren().addAll(headerBox, dateLabel, previewLabel);
                    setGraphic(cellBox);
                    setText(null);
                    
                    // Style based on read status
                    if (!item.isRead) {
                        setStyle("-fx-background-color: #E3F2FD;" +
                                "-fx-border-color: " + INFO_COLOR + ";" +
                                "-fx-border-width: 0 0 0 3px;");
                    } else {
                        setStyle("-fx-background-color: " + WHITE_COLOR + ";");
                    }
                }
            }
        });
        
        notificationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                messageArea.setText(newSel.message);
                updateButtons(newSel.isRead);
                deleteBtn.setDisable(false);
            } else {
                messageArea.clear();
                markReadBtn.setDisable(true);
                markUnreadBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });
        
        leftPanel.getChildren().addAll(listTitle, notificationListView);
        return leftPanel;
    }
    
    private VBox createMessageDetailPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPrefWidth(500);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setStyle("-fx-background-color: " + BLUE_COLOR + ";" +
                           "-fx-background-radius: 12px;");
        
        Label detailTitle = new Label("💌 Message Details");
        detailTitle.setStyle("-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;");
        
        // Message area with better styling
        VBox messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(15));
        messageContainer.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                                 "-fx-background-radius: 8px;");
        
        Label messageLabel = new Label("Full Message:");
        messageLabel.setStyle("-fx-font-weight: bold;" +
                             "-fx-font-size: 14px;" +
                             "-fx-text-fill: " + DARK_TEXT + ";");
        
        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefHeight(200);
        messageArea.setStyle(
            "-fx-background-color: #F8F9FA;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + DARK_TEXT + ";"
        );
        messageArea.setPromptText("Select a notification to view its details...");
        
        messageContainer.getChildren().addAll(messageLabel, messageArea);
        
        // Action buttons with modern styling
        HBox buttonsBox = createActionButtons();
        
        rightPanel.getChildren().addAll(detailTitle, messageContainer, buttonsBox);
        return rightPanel;
    }
    
    private HBox createActionButtons() {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        markReadBtn = new Button("✅ Mark as Read");
        markReadBtn.setStyle(
            "-fx-background-color: " + SUCCESS_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        markUnreadBtn = new Button("📩 Mark as Unread");
        markUnreadBtn.setStyle(
            "-fx-background-color: " + INFO_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle(
            "-fx-background-color: " + DANGER_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        
        deleteBtn.setDisable(true);
        
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
                // Show confirmation dialog
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Notification");
                confirm.setHeaderText("Delete this notification?");
                confirm.setContentText("This action cannot be undone.");
                
                // Style the dialog
                DialogPane dialogPane = confirm.getDialogPane();
                dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteNotification(selected.id);
                        refreshList();
                    }
                });
            }
        });
        
        buttonsBox.getChildren().addAll(markReadBtn, markUnreadBtn, deleteBtn);
        return buttonsBox;
    }
    
    private void updateButtons(boolean isRead) {
        markReadBtn.setDisable(isRead);
        markUnreadBtn.setDisable(!isRead);
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
        String sql = "SELECT notificationID, title, message, createdAt, isRead FROM Notification WHERE userID = ? ORDER BY createdAt DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            
            List<NotificationItem> notifications = new ArrayList<>();
            int unreadCount = 0;
            
            while (rs.next()) {
                NotificationItem item = new NotificationItem(
                        rs.getInt("notificationID"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("createdAt"),
                        rs.getBoolean("isRead")
                );
                notifications.add(item);
                if (!item.isRead) unreadCount++;
            }
            
            if (notifications.isEmpty()) {
                // Show empty state
                NotificationItem emptyItem = new NotificationItem(0, "No notifications yet", 
                    "You're all caught up! New notifications will appear here.", "", true);
                notificationListView.getItems().add(emptyItem);
                unreadCountLabel.setVisible(false);
            } else {
                notificationListView.getItems().addAll(notifications);
                if (unreadCount > 0) {
                    unreadCountLabel.setText(String.valueOf(unreadCount) + " unread");
                    unreadCountLabel.setVisible(true);
                } else {
                    unreadCountLabel.setVisible(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledError("Failed to load notifications: " + e.getMessage());
        }
    }
    
    private void clearAllReadNotifications() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Read Notifications");
        confirm.setHeaderText("Clear all read notifications?");
        confirm.setContentText("This will permanently delete all notifications that have been marked as read.");
        
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM Notification WHERE userID = ? AND isRead = 1";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userID);
                    int deleted = stmt.executeUpdate();
                    
                    loadNotifications();
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText(null);
                    success.setContentText(deleted + " read notifications have been cleared.");
                    success.showAndWait();
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                    showStyledError("Failed to clear notifications: " + e.getMessage());
                }
            }
        });
    }
    
    private void updateNotificationReadStatus(int notificationID, boolean markAsRead) {
        String sql = "UPDATE Notification SET isRead = ? WHERE notificationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, markAsRead);
            stmt.setInt(2, notificationID);
            stmt.executeUpdate();
            
            // Update unread count
            loadNotifications();
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
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";");
        
        alert.showAndWait();
    }
    
    private String formatDateTime(String dateTimeStr) {
        try {
            // Assuming the format from database is "yyyy-MM-dd HH:mm:ss"
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr.substring(0, 19), inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            return dateTimeStr; // Return original if parsing fails
        }
    }
    
    // ✅ Updated method to match SQL table
    public void insertNotification(int userID, String name, String title, String message, String notificationType) {
        String sql = "INSERT INTO Notification (userID, name, title, message, createdAt, isRead, notificationType) " +
                     "VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, name);
            stmt.setString(3, title);
            stmt.setString(4, message);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setString(6, notificationType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledError("Failed to insert notification: " + e.getMessage());
        }
    }
    
    private static class NotificationItem {
        int id;
        String title;
        String message;
        String createdAt;
        boolean isRead;
        
        NotificationItem(int id, String title, String message, String createdAt, boolean isRead) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }
        
        @Override
        public String toString() {
            return title;
        }
    }
}