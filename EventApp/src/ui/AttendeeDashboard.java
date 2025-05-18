package ui;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AttendeeDashboard {
    private int attendeeID;
    
    public void show(Stage stage) {
        // Default method for backward compatibility
        show(stage, 1); // Default attendeeID
    }
    
    public void show(Stage stage, int attendeeID) {
        this.attendeeID = attendeeID;
        
        Label label = new Label("Welcome to Attendee Dashboard!");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label welcomeLabel = new Label("Welcome, Attendee #" + attendeeID);

        VBox layout = new VBox(20, label, welcomeLabel);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Attendee Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}