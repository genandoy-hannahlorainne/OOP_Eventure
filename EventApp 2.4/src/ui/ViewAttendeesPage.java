package ui;

import db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ViewAttendeesPage {

    public void show(Stage stage, int eventID, boolean isOwner) {
        Label title = new Label("Attendees for Event " + eventID);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Attendee> attendeesTable = new TableView<>();
        attendeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Attendee, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(200);

        TableColumn<Attendee, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailColumn.setPrefWidth(250);

        attendeesTable.getColumns().addAll(nameColumn, emailColumn);

        if (isOwner) {
            TableColumn<Attendee, Void> actionColumn = new TableColumn<>("Actions");
            actionColumn.setPrefWidth(150);
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button removeBtn = new Button("Remove");
                private final HBox box = new HBox(5, editBtn, removeBtn);

                {
                    editBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        showEditDialog(attendee, eventID, () -> refreshTable(attendeesTable, eventID));
                    });

                    removeBtn.setOnAction(e -> {
                        Attendee attendee = getTableView().getItems().get(getIndex());
                        removeAttendee(eventID, attendee.getEmail());
                        refreshTable(attendeesTable, eventID);
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

        refreshTable(attendeesTable, eventID);

        VBox layout = new VBox(15, title, attendeesTable);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 650, 400);
        stage.setTitle("View Attendees");
        stage.setScene(scene);
        stage.show();
    }

    private void refreshTable(TableView<Attendee> table, int eventID) {
        table.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.name, u.email FROM Registration r " +
                         "JOIN [User] u ON r.userID = u.userID WHERE r.eventID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                table.getItems().add(new Attendee(rs.getString("name"), rs.getString("email")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void removeAttendee(int eventID, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String getUserID = "SELECT userID FROM [User] WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserID);
            userStmt.setString(1, email);
            ResultSet rs = userStmt.executeQuery();
            if (rs.next()) {
                int userID = rs.getInt("userID");
                String deleteSQL = "DELETE FROM Registration WHERE eventID = ? AND userID = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteSQL);
                stmt.setInt(1, eventID);
                stmt.setInt(2, userID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void showEditDialog(Attendee attendee, int eventID, Runnable onUpdate) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Attendee");

        TextField nameField = new TextField(attendee.getName());
        TextField emailField = new TextField(attendee.getEmail());

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fields cannot be empty.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String updateSql = "UPDATE [User] SET name = ?, email = ? WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, newName);
                stmt.setString(2, newEmail);
                stmt.setString(3, attendee.getEmail());
                stmt.executeUpdate();
                dialog.close();
                onUpdate.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
            }
        });

        VBox box = new VBox(10, new Label("Name:"), nameField, new Label("Email:"), emailField, saveBtn);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);

        dialog.setScene(new Scene(box, 300, 200));
        dialog.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
