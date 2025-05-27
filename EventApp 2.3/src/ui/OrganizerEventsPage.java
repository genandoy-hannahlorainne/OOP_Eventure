package ui;

import db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class OrganizerEventsPage {
    private final TableView<EventRow> table = new TableView<>();

    public void show(Stage stage, int organizerID) {
        stage.setTitle("My Events");

        TableColumn<EventRow, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(data -> data.getValue().eventNameProperty());

        // Remove session column — no session rows shown in this table
        // TableColumn<EventRow, String> sessionCol = new TableColumn<>("Session");
        // sessionCol.setCellValueFactory(data -> data.getValue().sessionTitleProperty());

        TableColumn<EventRow, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setOnAction(e -> {
                    EventRow row = getTableView().getItems().get(getIndex());
                    // Always edit event (sessions shown inside EditEventPage)
                    EditEventPage eventPage = new EditEventPage();
                    eventPage.show(new Stage(), row.toEvent(), () -> refreshData(organizerID));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        TableColumn<EventRow, Void> deleteCol = new TableColumn<>("Remove");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    EventRow row = getTableView().getItems().get(getIndex());
                    deleteEvent(row.getEventID());
                    refreshData(organizerID);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().clear();
        table.getColumns().addAll(eventCol, editCol, deleteCol);

        VBox layout = new VBox(10, new Label("Your Events:"), table);
        layout.setPadding(new Insets(15));

        refreshData(organizerID);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshData(int organizerID) {
        table.getItems().clear();
        ObservableList<EventRow> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT eventID, name, startDate, endDate FROM Event WHERE organizerID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, organizerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int eventID = rs.getInt("eventID");
                String eventName = rs.getString("name");
                String startDate = rs.getString("startDate");
                String endDate = rs.getString("endDate");

                // Add only event rows
                data.add(new EventRow(eventID, eventName, startDate, endDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        table.setItems(data);
    }

    private void deleteEvent(int eventID) {
        try (Connection conn = DBConnection.getConnection()) {
            // Delete sessions first (due to foreign key)
            PreparedStatement sessionStmt = conn.prepareStatement("DELETE FROM Session WHERE eventID = ?");
            sessionStmt.setInt(1, eventID);
            sessionStmt.executeUpdate();

            PreparedStatement eventStmt = conn.prepareStatement("DELETE FROM Event WHERE eventID = ?");
            eventStmt.setInt(1, eventID);
            eventStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update your EventRow class accordingly, e.g.:

    public static class EventRow {
        private final int eventID;
        private final String eventName;
        private final String startDate;
        private final String endDate;

        public EventRow(int eventID, String eventName, String startDate, String endDate) {
            this.eventID = eventID;
            this.eventName = eventName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getEventID() {
            return eventID;
        }

        public javafx.beans.property.SimpleStringProperty eventNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(eventName);
        }

        // Convert to your event object for passing to EditEventPage
        public MyEventsPage.Event toEvent() {
            return new MyEventsPage.Event(eventID, eventName, startDate, endDate);
        }
    }
}
