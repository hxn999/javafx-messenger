package com.client.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class SettingsController {
    @FXML
    private Label phoneNumberLabel;
    @FXML private CheckBox messageNotifCheckbox, soundNotifCheckbox;
    @FXML private CheckBox readReceiptsCheckbox, lastSeenCheckbox;

    @FXML
    private void handleEditProfile(ActionEvent event) {
        // your code here
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // your code here
    }
    public void handleBack(ActionEvent actionEvent) {

    }
}
