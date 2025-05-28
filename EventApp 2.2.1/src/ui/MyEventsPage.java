package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class MyEventsPage {

    private NotificationPage notificationPage = new NotificationPage();

    public void show(Stage stage, int userID) {
        TableView<Event> table = new TableView<>();

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Event, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Event, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Cancel Button Column
        TableColumn<Event, Void> cancelCol = new TableColumn<>("Cancel");
        cancelCol.setPrefWidth(80);
        cancelCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");

            {
                cancelBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Cancel registration for '" + event.getName() + "'?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            cancelRegistration(userID, event.getEventID(), event.getName());
                            getTableView().getItems().remove(event);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelBtn);
            }
        });

        table.getColumns().addAll(nameCol, startDateCol, endDateCol, cancelCol);
        table.setPrefWidth(700);

        loadRegisteredEvents(userID, table);

        Label title = new Label("My Registered Events (User #" + userID + ")");
        title.setStyle("-fx-font-size: 18px; -fx-padding: 10 0 20 0;");

        VBox layout = new VBox(10, title, table);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 750, 400);
        stage.setTitle("My Registered Events");
        stage.setScene(scene);
        stage.show();
    }

    private void loadRegisteredEvents(int userID, TableView<Event> table) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT E.eventID, E.name, E.startDate, E.endDate
                FROM Event E
                JOIN Registration R ON E.eventID = R.eventID
                WHERE R.userID = ?
                """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                table.getItems().add(new Event(
                        rs.getInt("eventID"),
                        rs.getString("name"),
                        rs.getString("startDate"),
                        rs.getString("endDate")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load registered events: " + e.getMessage());
        }
    }

    private void cancelRegistration(int userID, int eventID, String eventName) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM Registration WHERE userID = ? AND eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, eventID);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                notificationPage.insertNotification(
                        userID, eventName, "Event Cancellation",
                        "You canceled your registration for event '" + eventName + "'.", "cancellation"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to cancel registration: " + e.getMessage());
        }
    }

    public void registerForEvent(int userID, int eventID, String eventName) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO Registration (userID, eventID) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, eventID);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                notificationPage.insertNotification(
                        userID, eventName, "Event Registration",
                        "You successfully registered for event '" + eventName + "'.", "registration"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to register for event: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Event {
        private final int eventID;
        private final String name;
        private final String startDate;
        private final String endDate;

        public Event(int eventID, String name, String startDate, String endDate) {
            this.eventID = eventID;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() { return eventID; }
        public String getName() { return name; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
}