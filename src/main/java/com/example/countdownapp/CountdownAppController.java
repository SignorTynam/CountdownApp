package com.example.countdownapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    private boolean countdownRunning = false;
    private Stage countdownStage;
    private final List<Congregation> congregations = new ArrayList<>();
    @FXML
    private Label countdownLabel;

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
        congregationComboBox.setValue(congregations.getFirst().getName());
    }

    @FXML
    private void toggleCountdown() {
        if (countdownRunning) {
            countdownRunning = false;
            startButton.setText("Inizia il countdown");
            if (countdownStage != null) {
                countdownStage.close();
            }
            increaseFontSizeButton.setVisible(false);
            decreaseFontSizeButton.setVisible(false);
            congregationComboBox.setDisable(false);
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

        LocalTime targetTime = selectedCongregation.getMeetingTime(currentDayOfWeek);

        if (targetTime == null || LocalTime.now().isAfter(targetTime)) {
            displayWarningMessage("Adunanza già passata", "L'orario della prossima adunanza è già passato.");
            congregationComboBox.setDisable(false);
            return;
        }

        Duration duration = Duration.between(LocalTime.now(), targetTime);
        long secondsRemaining = duration.getSeconds();
        countdownRunning = true;

        Platform.runLater(() -> {
            if (countdownStage == null) {
                countdownStage = new Stage(StageStyle.UNDECORATED);
                countdownStage.setOnCloseRequest(event -> {
                    countdownRunning = false;
                    startButton.setText("Inizia il countdown");
                    increaseFontSizeButton.setVisible(false);
                    decreaseFontSizeButton.setVisible(false);
                });
            }

            startButton.setText("Chiudi il countdown");

            Rectangle2D bounds = Screen.getScreens().size() > 1
                    ? Screen.getScreens().get(1).getBounds()
                    : Screen.getPrimary().getBounds();

            countdownStage.setX(bounds.getMinX());
            countdownStage.setY(bounds.getMinY());
            countdownStage.setWidth(bounds.getWidth());
            countdownStage.setHeight(bounds.getHeight());

            VBox vbox = new VBox(10);
            vbox.setAlignment(Pos.CENTER);
            countdownLabel = new Label();
            countdownLabel.setFont(new Font(550));
            vbox.getChildren().add(countdownLabel);

            countdownStage.setScene(new Scene(vbox));
            countdownStage.show();

            // I pulsanti per cambiare la dimensione del font devono essere visibili ora
            increaseFontSizeButton.setVisible(true);
            decreaseFontSizeButton.setVisible(true);

            // Il thread del countdown
            new Thread(() -> {
                long remaining = secondsRemaining;

                while (remaining >= 0 && countdownRunning) {
                    long hours = remaining / 3600;
                    long minutes = (remaining % 3600) / 60;
                    long seconds = remaining % 60;

                    String formattedTime = (hours > 0 ? String.format("%02d:", hours) : "")
                            + String.format("%02d:%02d", minutes, seconds);

                    String textColor;
                    if (remaining <= 15) {
                        textColor = "#A53E1E";
                    } else if (remaining <= 60) {
                        textColor = "#D78C2D";
                    } else {
                        textColor = "rgba(74, 109, 167, 1)";
                    }

                    Platform.runLater(() -> {
                        countdownLabel.setText(formattedTime);
                        countdownLabel.setStyle("-fx-text-fill: " + textColor + ";");
                        countdownStage.getScene().getRoot()
                                .setStyle("-fx-border-color: " + textColor + "; -fx-border-width: 12px;");
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    remaining--;
                }

                Platform.runLater(() -> countdownLabel.setText("00:00"));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    countdownStage.close();
                    startButton.setText("Inizia il countdown");
                });
            }).start();
        });
    }

    private Congregation getCongregationByName(String name) {
        for (Congregation congregation : congregations) {
            if (congregation.getName().equals(name)) {
                return congregation;
            }
        }
        return null;
    }

    @FXML
    private void increaseFontSize() {
        if (countdownLabel != null) {
            Font currentFont = countdownLabel.getFont();
            countdownLabel.setFont(new Font(currentFont.getSize() + 10));
        }
    }

    @FXML
    private void decreaseFontSize() {
        if (countdownLabel != null) {
            Font currentFont = countdownLabel.getFont();
            countdownLabel.setFont(new Font(Math.max(currentFont.getSize() - 10, 10)));
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
