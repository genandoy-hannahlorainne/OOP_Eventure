package ui;

import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;

public class OrganizerNotificationPage {
    private int userID;
    private ListView<NotificationItem> notificationListView;
    private TextArea messageArea;
    private Button markReadBtn;
    private Button markUnreadBtn;
    private Button deleteBtn;
    private ComboBox<String> filterComboBox;

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

        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefHeight(150);

        markReadBtn = new Button("Mark as Read");
        markUnreadBtn = new Button("Mark as Unread");
        deleteBtn = new Button("Delete");
        deleteBtn.setDisable(true);

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
                deleteNotification(selected.id);
                refreshList();
            }
        });

        HBox buttonsBox = new HBox(10, markReadBtn, markUnreadBtn, deleteBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All", "Unread", "Read");
        filterComboBox.setValue("Unread");
        filterComboBox.setOnAction(e -> loadNotifications());

        VBox leftPane = new VBox(10, new Label("Filter:"), filterComboBox, notificationListView);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(450);

        VBox rightPane = new VBox(10, new Label("Full Message:"), messageArea, buttonsBox);
        rightPane.setPadding(new Insets(10));
        rightPane.setPrefWidth(450);

        HBox root = new HBox(10, leftPane, rightPane);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 920, 450);
        stage.setScene(scene);
        stage.setTitle("Organizer Notifications");
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

        String filter = filterComboBox.getValue();
        String sqlBase = "SELECT notificationID, title, message, createdAt, isRead, notificationType, userID " +
                "FROM Notification WHERE userID = ? AND notificationType = 'Organizer' ";
        String sqlFilter = switch (filter) {
            case "Unread" -> "AND isRead = 0 ";
            case "Read" -> "AND isRead = 1 ";
            default -> "";
        };
        String sqlOrder = "ORDER BY createdAt DESC";

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
            }

            notificationListView.setItems(notifications);

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
