package main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.MainPage;

public class Main extends Application {

    @Override
    //OOP EVENTURE
    public void start(Stage primaryStage) {
        new MainPage(primaryStage); // ✅ Pass the primaryStage here
    }

    public static void main(String[] args) {
        launch(args);
    }
}