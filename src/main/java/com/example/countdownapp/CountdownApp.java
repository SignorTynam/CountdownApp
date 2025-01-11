package com.example.countdownapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

public class CountdownApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/countdownapp/CountdownAppView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Countdown App");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showDialog("Error loading the FXML file", e.getMessage(), AlertType.ERROR);
        }

        primaryStage.setOnCloseRequest(_ -> {
            System.exit(0);
        });

    }

    @Override
    public void stop() {
        System.out.println("Application is closing...");
    }

    public void showDialog(String title, String message, AlertType alertType) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
