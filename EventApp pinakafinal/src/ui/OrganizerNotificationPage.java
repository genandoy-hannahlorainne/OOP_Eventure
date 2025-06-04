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

public class OrganizerNotificationPage {
    // UI color constants matching dashboard
    private static final String BLUE_COLOR = "#97A9D1";  // Nav bar and main background color
    private static final String YELLOW_COLOR = "#F1D747"; // Event cards and content background
    private static final String WHITE_COLOR = "#FFFFFF";
    private static final String DARK_TEXT = "#333333";
    
    private int userID;
    private String organizerName = "";
    private Stage currentStage;
    private ListView<NotificationItem> notificationListView;
    private TextArea messageArea;
    private Button markReadBtn;
    private Button markUnreadBtn;
    private Button deleteBtn;
    private ComboBox<String> filterComboBox;

    public void show(Stage stage, int userID) {
        this.userID = userID;
        this.currentStage = stage;
        
        // Fetch organizer name from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT firstName, lastName FROM [User] WHERE userID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                organizerName = rs.getString("firstName") + " " + rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // --- Top Navigation Bar ---
        HBox navBar = createNavBar();
        mainLayout.setTop(navBar);
        
        // --- Main Content ---
        // Create a blue background pane for the content
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: " + BLUE_COLOR + ";");
        contentWrapper.setPadding(new Insets(15));
        
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(15));
        contentPane.setStyle("-fx-background-color: " + WHITE_COLOR + ";" +
                            "-fx-background-radius: 10px;");
        
        // Page Title
        Label titleLabel = new Label("Notifications Management");
        titleLabel.setStyle("-fx-background-color: " + YELLOW_COLOR + ";" +
                           "-fx-padding: 12px 18px;" +
                           "-fx-background-radius: 8px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 16px;");

        // Main content area
        HBox mainContent = createMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        contentPane.getChildren().addAll(titleLabel, mainContent);
        contentWrapper.getChildren().add(contentPane);
        mainLayout.setCenter(contentWrapper);

        Scene scene = new Scene(mainLayout, 1200, 800);
        stage.setTitle("Eventure - Organizer Notifications");
        stage.setScene(scene);
        stage.show();

        loadNotifications();
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
        Button profileBtn = createNavButton("Profile");
        
        navButtonsSection.getChildren().addAll(dashboardBtn, eventsBtn, myEventsBtn, createEventBtn, attendeesBtn, profileBtn);
        
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
            dashboard.show(currentStage, userID);
        });
        
        eventsBtn.setOnAction(e -> {
            EventListPage eventsPage = new EventListPage(false);
            eventsPage.show(currentStage, userID);
        });
        
        myEventsBtn.setOnAction(e -> {
            OrganizerEventsPage page = new OrganizerEventsPage();
            page.show(currentStage, userID);
        });
        
        createEventBtn.setOnAction(e -> {
            CreateEventPage page = new CreateEventPage();
            page.show(currentStage, userID);  // Use currentStage instead of new Stage()
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
            profilePage.show(currentStage, userID);
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

    private HBox createMainContent() {
        HBox mainContent = new HBox(15);
        mainContent.setPadding(new Insets(10, 0, 0, 0));
        
        // Left side - Notifications List
        VBox leftSection = createNotificationsListSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        // Right side - Message Details and Actions
        VBox rightSection = createMessageDetailsSection();
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftSection, rightSection);
        return mainContent;
    }
    
    private VBox createNotificationsListSection() {
        VBox leftSection = new VBox(12);
        leftSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label notificationLabel = new Label("Notifications");
        notificationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Filter section
        HBox filterBox = new HBox(8);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All", "Unread", "Read");
        filterComboBox.setValue("Unread");
        filterComboBox.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 5px;"
        );
        filterComboBox.setOnAction(e -> loadNotifications());
        
        filterBox.getChildren().addAll(filterLabel, filterComboBox);
        
        // Notifications ListView with custom styling
        notificationListView = new ListView<>();
        notificationListView.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 5px;"
        );
        notificationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String status = item.isRead ? "[Read] " : "[Unread] ";
                    setText(status + item.title + " (" + item.createdAt + ")");
                    
                    // Style based on read status
                    if (!item.isRead) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: " + DARK_TEXT + ";");
                    } else {
                        setStyle("-fx-text-fill: #666666;");
                    }
                    
                    // Hover effect
                    setOnMouseEntered(e -> setStyle(getStyle() + "-fx-background-color: " + WHITE_COLOR + ";"));
                    setOnMouseExited(e -> setStyle(getStyle().replace("-fx-background-color: " + WHITE_COLOR + ";", "")));
                }
            }
        });
        
        VBox.setVgrow(notificationListView, Priority.ALWAYS);
        notificationListView.setMinHeight(400);
        
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
        
        leftSection.getChildren().addAll(notificationLabel, filterBox, notificationListView);
        return leftSection;
    }
    
    private VBox createMessageDetailsSection() {
        VBox rightSection = new VBox(12);
        rightSection.setStyle(
            "-fx-background-color: " + BLUE_COLOR + ";" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label messageLabel = new Label("Message Details");
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Message display area
        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setStyle(
            "-fx-background-color: " + YELLOW_COLOR + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px;" +
            "-fx-font-size: 12px;"
        );
        VBox.setVgrow(messageArea, Priority.ALWAYS);
        messageArea.setMinHeight(300);
        
        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        markReadBtn = new Button("Mark as Read");
        markUnreadBtn = new Button("Mark as Unread");
        deleteBtn = new Button("Delete");
        
        styleActionButton(markReadBtn);
        styleActionButton(markUnreadBtn);
        styleActionButton(deleteBtn);
        
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
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Notification");
                confirmDialog.setHeaderText("Are you sure you want to delete this notification?");
                confirmDialog.setContentText("This action cannot be undone.");
                
                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteNotification(selected.id);
                        refreshList();
                    }
                });
            }
        });
        
        buttonsBox.getChildren().addAll(markReadBtn, markUnreadBtn, deleteBtn);
        
        rightSection.getChildren().addAll(messageLabel, messageArea, buttonsBox);
        return rightSection;
    }
    
    private void styleActionButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + WHITE_COLOR + ";" +
            "-fx-text-fill: " + DARK_TEXT + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 8px 16px;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(
                "-fx-background-color: " + YELLOW_COLOR + ";" +
                "-fx-text-fill: " + DARK_TEXT + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 8px 16px;"
            )
        );
        
        button.setOnMouseExited(e ->
            button.setStyle(
                "-fx-background-color: " + WHITE_COLOR + ";" +
                "-fx-text-fill: " + DARK_TEXT + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5px;" +
                "-fx-padding: 8px 16px;"
            )
        );
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

        String filter = filterComboBox.getValue();
        String sqlBase = "SELECT TOP (1000) [notificationID], [title], [message], [createdAt], [isRead], " +
                         "[notificationType], [userID] " +
                         "FROM [EventManagementSystem].[dbo].[Notification] WHERE [userID] = ? AND [notificationType] = 'Organizer' ";
        String sqlFilter = switch (filter) {
            case "Unread" -> "AND [isRead] = 0 ";
            case "Read" -> "AND [isRead] = 1 ";
            default -> "";
        };
        String sqlOrder = "ORDER BY [createdAt] DESC";

        String sql = sqlBase + sqlFilter + sqlOrder;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();

            while (rs.next()) {
                notifications.add(new NotificationItem(
                        rs.getInt("notificationID"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt"),
                        rs.getBoolean("isRead"),
                        rs.getString("notificationType"),
                        rs.getInt("userID")
                ));
            }

            if (notifications.isEmpty()) {
                messageArea.clear();
                messageArea.setPromptText("No notifications found for the selected filter.");
            }

            notificationListView.setItems(notifications);

        } catch (SQLException e) {
            e.printStackTrace();
            messageArea.setText("Error loading notifications. Please try again.");
        }
    }

    private void updateNotificationReadStatus(int notificationID, boolean markAsRead) {
        String sql = "UPDATE [EventManagementSystem].[dbo].[Notification] SET [isRead] = ? WHERE [notificationID] = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, markAsRead);
            stmt.setInt(2, notificationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteNotification(int notificationID) {
        String sql = "DELETE FROM [EventManagementSystem].[dbo].[Notification] WHERE [notificationID] = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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