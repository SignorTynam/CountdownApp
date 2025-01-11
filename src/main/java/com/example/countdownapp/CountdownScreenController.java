package com.example.countdownapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CountdownScreenController {
    @FXML
    private Label countdownLabel;
    @FXML
    private Label congregationLabel;

    @SuppressWarnings("exports")
    public Label getCountdownLabel() {
        return countdownLabel;
    }

    public void setCongregationName(String name) {
        congregationLabel.setText(name);
    }
}
