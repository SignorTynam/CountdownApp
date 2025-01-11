package com.example.countdownapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
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
    @FXML
    private Label countdownLabel;
    @FXML
    private Label countdownLabel2;

    private volatile boolean countdownRunning = false;
    private Stage countdownStage;
    private final List<Congregation> congregations = new ArrayList<>();
    private static final double MAX_FONT_SIZE = 700;
    private static final double MIN_FONT_SIZE = 10;
    private double currentFontSize = 350;
    private CountdownApp app = new CountdownApp();

    @FXML
    public void initialize() {
        congregations.add(new Congregation("Cesena Torre del Moro",
                new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(10, 0)}));

        congregations.add(new Congregation("Cesena Stadio",
                new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.SATURDAY},
                new LocalTime[]{LocalTime.of(20, 15), LocalTime.of(20, 30)}));

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

        findAndSelectNextMeeting();
    }

    private void findAndSelectNextMeeting() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        Congregation nextCongregation = null;
        LocalDate nextMeetingDate = null;
        LocalTime nextMeetingTime = null;

        for (Congregation congregation : congregations) {
            for (int i = 0; i < congregation.getMeetingDays().length; i++) {
                DayOfWeek meetingDay = congregation.getMeetingDays()[i];
                LocalTime meetingTime = congregation.getMeetingTimes()[i];

                LocalDate meetingDate = today.with(meetingDay);
                if (meetingDay.getValue() < today.getDayOfWeek().getValue() || 
                    (meetingDay == today.getDayOfWeek() && meetingTime.isBefore(now))) {
                    meetingDate = meetingDate.plusWeeks(1);
                }

                if (nextMeetingDate == null || 
                    meetingDate.isBefore(nextMeetingDate) || 
                    (meetingDate.isEqual(nextMeetingDate) && meetingTime.isBefore(nextMeetingTime))) {
                    nextMeetingDate = meetingDate;
                    nextMeetingTime = meetingTime;
                    nextCongregation = congregation;
                }
            }
        }

        if (nextCongregation != null) {
            congregationComboBox.setValue(nextCongregation.getName());
            app.showDialog("Prossima adunanza", "La prossima adunanza è della congregazione: " +
                    nextCongregation.getName() + " il " + nextMeetingDate.getDayOfWeek() +
                    " alle " + nextMeetingTime, AlertType.INFORMATION);
        } else {
            app.showDialog("Nessuna adunanza trovata", "Non ci sono adunanze programmate.", AlertType.ERROR);
        }
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
            app.showDialog("Errore", "Seleziona una congregazione valida.", AlertType.ERROR);
            congregationComboBox.setDisable(false);
            return;
        }

        DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();

        if (!selectedCongregation.hasMeetingToday(currentDayOfWeek)) {
            app.showDialog("Nessuna adunanza oggi", "La congregazione non ha in programma un'adunanza oggi.", AlertType.ERROR);
            congregationComboBox.setDisable(false);
            return;
        }

        LocalTime targetTime = selectedCongregation.getNextMeetingTime(currentDayOfWeek, LocalTime.now());

        if (targetTime == null) {
            app.showDialog("Adunanza già passata", "L'orario dell'adunanza è già passato.", AlertType.WARNING);
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
                            if (countdownLabel != null) {
                                countdownLabel.setText(formattedTime);
                                countdownLabel.setStyle("-fx-text-fill: white;");
                                countdownLabel.setFont(new Font(currentFontSize));
                            }
                
                            if (countdownLabel2 != null) {
                                countdownLabel2.setText(formattedTime);
                                countdownLabel2.setStyle("-fx-text-fill: #5b3c88; -fx-font-weight: bold;");
                                countdownLabel2.setFont(new Font(40));
                            }
                        });
                
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                
                        remaining--;
                    }
                
                    Platform.runLater(() -> {
                        if (countdownLabel != null) countdownLabel.setText("00:00");
                        if (countdownLabel2 != null) countdownLabel2.setText("00:00");
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

}
