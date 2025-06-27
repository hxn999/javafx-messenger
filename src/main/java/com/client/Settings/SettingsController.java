package com.client.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class SettingsController {
    public Button backButton;
    public Button logoutButton;
    public Button editProfileButton;
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
