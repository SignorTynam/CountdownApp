package com.example.countdownapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CountdownScreenController {
    @FXML
    private Label countdownLabel;

    @SuppressWarnings("exports")
    public Label getCountdownLabel() {
        return countdownLabel;
    }
}
