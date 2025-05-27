package ui;

import db.DBConnection;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class EditSessionPage {
    public void show(Stage stage, int sessionID) {
        stage.setTitle("Edit Session");

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();

        Label speakerLabel = new Label("Speaker:");
        TextField speakerField = new TextField();

        Label startLabel = new Label("Start Time:");
        TextField startField = new TextField();

        Label endLabel = new Label("End Time:");
        TextField endField = new TextField();

        Button updateBtn = new Button("Update Session");

        VBox layout = new VBox(10,
                titleLabel, titleField,
                speakerLabel, speakerField,
                startLabel, startField,
                endLabel, endField,
                updateBtn
        );
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout, 400, 350);
        stage.setScene(scene);
        stage.show();

        // Load existing session details
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Session WHERE sessionID = ?");
            stmt.setInt(1, sessionID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titleField.setText(rs.getString("title"));
                speakerField.setText(rs.getString("speaker"));
                startField.setText(rs.getString("startTime"));
                endField.setText(rs.getString("endTime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update session on button click
        updateBtn.setOnAction(e -> {
            String newTitle = titleField.getText();
            String newSpeaker = speakerField.getText();
            String newStart = startField.getText();
            String newEnd = endField.getText();

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Session SET title = ?, speaker = ?, startTime = ?, endTime = ? WHERE sessionID = ?");
                stmt.setString(1, newTitle);
                stmt.setString(2, newSpeaker);
                stmt.setString(3, newStart);
                stmt.setString(4, newEnd);
                stmt.setInt(5, sessionID);
                stmt.executeUpdate();

                stage.close(); // Close after update
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}
