package ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainPage {
    public MainPage(Stage stage) {
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setOnAction(e -> new LoginPage(stage));
        registerBtn.setOnAction(e -> new RegistrationPage(stage));

        VBox layout = new VBox(20, loginBtn, registerBtn);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center;");
        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Main Page");
        stage.show();
    }
}
