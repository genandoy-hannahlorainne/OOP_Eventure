package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OrganizerDashboard {
    private int organizerID;

    public void show(Stage stage) {
        show(stage, 1); 
    }

    public void show(Stage stage, int organizerID) {
        this.organizerID = organizerID;

        Label title = new Label("Organizer Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label welcomeLabel = new Label("Welcome, Organizer #" + organizerID);

        Button createEventButton = new Button("Create Event");
        createEventButton.setOnAction(e -> {
            CreateEventPage createEventPage = new CreateEventPage();
            createEventPage.show(new Stage(), organizerID); // Pass organizerID and use a new stage
        });

        Button viewAttendeesButton = new Button("View Attendees");
        viewAttendeesButton.setOnAction(e -> {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Event ID");
            dialog.setHeaderText("Enter Event ID");
            dialog.setContentText("Please enter the event ID:");

            dialog.showAndWait().ifPresent(eventID -> {
                try {
                    int id = Integer.parseInt(eventID);
                    ViewAttendeesPage viewAttendeesPage = new ViewAttendeesPage();
                    viewAttendeesPage.show(new Stage(), id); 
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number for Event ID.");
                    alert.showAndWait();
                }
            });
        });

        Button viewEventsButton = new Button("View Events");
        viewEventsButton.setOnAction(e -> {
            ViewEventsPage viewEventsPage = new ViewEventsPage();
            viewEventsPage.show(new Stage(), organizerID);
        });

        VBox layout = new VBox(15, title, welcomeLabel, createEventButton, viewAttendeesButton, viewEventsButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 400, 400);
        stage.setTitle("Organizer Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}