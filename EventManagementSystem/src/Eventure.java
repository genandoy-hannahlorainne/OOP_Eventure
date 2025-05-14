import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Eventure extends Application {
	@Override
	public void start(Stage stage) {
		Label label = new Label ("welcome to COMP 009!");
		label.setFont (new Font ("Arial", 20));
		Label label1 = new Label("Let's get it on!");
		label1.setFont(new Font ("Arial", 30));
		
		VBox vbox = new VBox ();
		vbox.getChildren().addAll(label, label1);
		vbox.setAlignment(Pos.BASELINE_CENTER);
		
		Scene scene = new Scene(vbox, 400, 200);
		
		stage.setTitle("COMP 009");
		stage.setScene(scene);
		stage.show();
	}
	
	public static void main (String[] args) {
		launch(args);
	}
}