package com.client.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SettingsController {
    public Button editProfileButton;
    @FXML
    private Label phoneNumberLabel;
    @FXML private CheckBox messageNotifCheckbox, soundNotifCheckbox;
    @FXML private CheckBox readReceiptsCheckbox, lastSeenCheckbox;


    @FXML
    public void initialize() {
        editProfileButton.setOnAction(this::handleEditProfile);
    }
    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {

            Parent accountDetailsRoot = FXMLLoader.load(getClass().getResource("/views/accountDetails.fxml"));
            Scene accountDetailsScene = new Scene(accountDetailsRoot, 1000, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(accountDetailsScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // your code here
    }
    public void handleBack(ActionEvent actionEvent) {

    }
}
