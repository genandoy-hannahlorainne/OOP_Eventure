package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationPage {
    private int userID;
    private ListView<NotificationItem> notificationListView;
    private TextArea messageArea;
    private Button markReadBtn;
    private Button markUnreadBtn;
    private Button deleteBtn;
    private Button refreshBtn;

    public void show(Stage stage, int userID) {
        this.userID = userID;

        notificationListView = new ListView<>();
        notificationListView.setPrefWidth(400);
        notificationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.isRead ? "[Read] " : "[Unread] ";
                    setText(status + item.title + " (" + item.createdAt + ")");
                }
            }
        });

        notificationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && newSel.id != 0) {
                // Display the message with event names (already converted)
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

        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefHeight(150);

        markReadBtn = new Button("Mark as Read");
        markUnreadBtn = new Button("Mark as Unread");
        deleteBtn = new Button("Delete");
        deleteBtn.setDisable(true);

        refreshBtn = new Button("Refresh");

        markReadBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead && selected.id != 0) {
                updateNotificationReadStatus(selected.id, true);
                selected.isRead = true;
                refreshList();
                notificationListView.getSelectionModel().select(selected);
            }
        });

        markUnreadBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.isRead && selected.id != 0) {
                updateNotificationReadStatus(selected.id, false);
                selected.isRead = false;
                refreshList();
                notificationListView.getSelectionModel().select(selected);
            }
        });

        deleteBtn.setOnAction(e -> {
            NotificationItem selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.id != 0) {
                deleteNotification(selected.id);
                refreshList();
            }
        });

        refreshBtn.setOnAction(e -> loadNotifications());

        HBox buttonsBox = new HBox(10, markReadBtn, markUnreadBtn, deleteBtn, refreshBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox rightPane = new VBox(10, new Label("Full Message:"), messageArea, buttonsBox);
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(450);

        HBox root = new HBox(10, notificationListView, rightPane);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 400);
        stage.setScene(scene);
        stage.setTitle("Notifications");
        stage.show();

        loadNotifications();
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

            List<NotificationItem> list = new ArrayList<>();
            while (rs.next()) {
                String originalMessage = rs.getString("message");
                // Convert event IDs to event names when loading notifications
                String messageWithEventNames = replaceEventIdWithName(originalMessage);
                
                NotificationItem item = new NotificationItem(
                        rs.getInt("notificationID"),
                        rs.getString("title"),
                        messageWithEventNames, // Use the converted message
                        rs.getString("createdAt"),
                        rs.getBoolean("isRead")
                );
                list.add(item);
            }

            if (list.isEmpty()) {
                notificationListView.getItems().add(new NotificationItem(0, "No notifications", "", "", true));
            } else {
                notificationListView.getItems().addAll(list);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load notifications.");
            alert.showAndWait();
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update notification status.");
            alert.showAndWait();
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete notification.");
            alert.showAndWait();
        }
    }

    // Updated method to handle event ID to name conversion at insertion time
    public void insertNotification(int userID, String name, String title, String message, String notificationType) {
        // Convert event IDs to event names before storing
        String messageWithEventNames = replaceEventIdWithName(message);
        
        String sql = "INSERT INTO Notification (userID, name, title, message, createdAt, isRead, notificationType) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            stmt.setString(2, name);
            stmt.setString(3, title);
            stmt.setString(4, messageWithEventNames); // Store the converted message
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setString(6, notificationType);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to insert notification.");
            alert.showAndWait();
        }
    }

    private String getEventNameById(int eventId) {
        // Try common column name variations for event name
        String[] possibleColumns = {"eventName", "name", "title", "event_name", "event_title"};
        
        for (String columnName : possibleColumns) {
            String sql = "SELECT " + columnName + " FROM Event WHERE eventID = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String result = rs.getString(columnName);
                    if (result != null && !result.trim().isEmpty()) {
                        return result;
                    }
                }
            } catch (SQLException e) {
                // Column doesn't exist, try next one
                continue;
            }
        }
        return "Unknown Event"; // Return a default value if no column found
    }

    private String replaceEventIdWithName(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Enhanced pattern to handle multiple formats
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)event\\s*id\\s*:?\\s*(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String eventIdStr = matcher.group(1);
            int eventId = Integer.parseInt(eventIdStr);
            String eventName = getEventNameById(eventId);
            
            // Replace with event name format
            matcher.appendReplacement(sb, "Event: " + eventName);
        }
        matcher.appendTail(sb);

        return sb.toString();
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