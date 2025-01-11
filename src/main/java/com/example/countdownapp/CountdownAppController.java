package com.example.countdownapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CountdownAppController {
    @FXML
    private ComboBox<String> congregationComboBox;
    @FXML
    private Button startButton;
    @FXML
    private Button increaseFontSizeButton;
    @FXML
    private Button decreaseFontSizeButton;

    private volatile boolean countdownRunning = false;
    private Stage countdownStage;
    private final List<Congregation> congregations = new ArrayList<>();
    @FXML
    private Label countdownLabel;

    private static final double MAX_FONT_SIZE = 700;
    private static final double MIN_FONT_SIZE = 10;
    private double currentFontSize = 350;

    @FXML
    public void initialize() {
        congregations.add(new Congregation("Cesena Torre del Moro",
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(10, 0)}));

        congregations.add(new Congregation("Cesena Stadio",
                new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(17, 30)}));

        congregations.add(new Congregation("Cesena Borgo Paglia",
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(17, 0)}));

        congregations.add(new Congregation("Cesena Cervese",
                new DayOfWeek[]{DayOfWeek.THURSDAY, DayOfWeek.SUNDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(17, 0)}));

        congregations.add(new Congregation("Cesena Ippodromo",
                new DayOfWeek[]{DayOfWeek.THURSDAY, DayOfWeek.SUNDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(10, 0)}));

        for (Congregation congregation : congregations) {
            congregationComboBox.getItems().add(congregation.getName());
        }
        congregationComboBox.setValue(congregations.get(0).getName());
    }

    @FXML
    private void toggleCountdown() {
        if (countdownRunning) {
            stopCountdown();
            return;
        }

        congregationComboBox.setDisable(true);

        String selectedCongregationName = congregationComboBox.getValue();
        Congregation selectedCongregation = getCongregationByName(selectedCongregationName);

        if (selectedCongregation == null) {
            displayWarningMessage("Errore", "Seleziona una congregazione valida.");
            congregationComboBox.setDisable(false);
            return;
        }

        DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();

        if (!selectedCongregation.hasMeetingToday(currentDayOfWeek)) {
            displayWarningMessage("Nessuna adunanza oggi", "La congregazione non ha in programma un'adunanza oggi.");
            congregationComboBox.setDisable(false);
            return;
        }

        LocalTime targetTime = selectedCongregation.getNextMeetingTime(currentDayOfWeek, LocalTime.now());

        if (targetTime == null) {
            displayWarningMessage("Adunanza già passata", "L'orario dell'adunanza è già passato.");
            congregationComboBox.setDisable(false);
            return;
        }

        Duration duration = Duration.between(LocalTime.now(), targetTime);
        long secondsRemaining = duration.getSeconds();
        countdownRunning = true;

        Platform.runLater(() -> {
            try {
                if (countdownStage == null) {
                    countdownStage = new Stage(StageStyle.UNDECORATED);
                    countdownStage.setOnCloseRequest(_ -> stopCountdown());
                }
        
                startButton.setText("Chiudi il countdown");
        
                Rectangle2D bounds = Screen.getScreens().size() > 1
                        ? Screen.getScreens().get(1).getBounds()
                        : Screen.getPrimary().getBounds();
        
                countdownStage.setX(bounds.getMinX());
                countdownStage.setY(bounds.getMinY());
                countdownStage.setWidth(bounds.getWidth());
                countdownStage.setHeight(bounds.getHeight());
        
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/countdownapp/CountdownScreen.fxml"));
                VBox root = loader.load();
                CountdownScreenController screenController = loader.getController();
        
                // Imposta il nome della congregazione e il timer
                screenController.setCongregationName(selectedCongregationName);
                countdownLabel = screenController.getCountdownLabel();
        
                countdownStage.setScene(new Scene(root));
                countdownStage.show();
        
                increaseFontSizeButton.setVisible(true);
                decreaseFontSizeButton.setVisible(true);
        
                new Thread(() -> {
                    long remaining = secondsRemaining;
                
                    while (remaining >= 0 && countdownRunning) {
                        long hours = remaining / 3600;
                        long minutes = (remaining % 3600) / 60;
                        long seconds = remaining % 60;
                
                        String formattedTime = (hours > 0 ? String.format("%02d:", hours) : "")
                                + String.format("%02d:%02d", minutes, seconds);
                
                        Platform.runLater(() -> {
                            countdownLabel.setText(formattedTime);
                            countdownLabel.setStyle("-fx-text-fill: white;");
                            countdownLabel.setFont(new Font(currentFontSize)); // Use the updated font size
                        });
                
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                
                        remaining--;
                    }
                
                    Platform.runLater(() -> {
                        countdownLabel.setText("00:00");
                        stopCountdown();
                    });
                }).start();                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });        
    }

    private void stopCountdown() {
        countdownRunning = false;
        if (countdownStage != null) {
            countdownStage.close();
        }
        startButton.setText("Inizia il countdown");
        increaseFontSizeButton.setVisible(false);
        decreaseFontSizeButton.setVisible(false);
        congregationComboBox.setDisable(false);
    }

    private Congregation getCongregationByName(String name) {
        return congregations.stream()
                .filter(congregation -> congregation.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @FXML
private void increaseFontSize() {
    if (countdownLabel != null) {
        currentFontSize = Math.min(currentFontSize + 10, MAX_FONT_SIZE);
        countdownLabel.setFont(new Font(currentFontSize));
    }
}

@FXML
private void decreaseFontSize() {
    if (countdownLabel != null) {
        currentFontSize = Math.max(currentFontSize - 10, MIN_FONT_SIZE);
        countdownLabel.setFont(new Font(currentFontSize));
    }
}

    private void displayWarningMessage(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
