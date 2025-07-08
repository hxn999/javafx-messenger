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
import javafx.scene.media.AudioClip;

public class SettingsController {
    public Button editProfileButton;
    public Button logoutButton;
    public Button backButton;
    @FXML
    private Label phoneNumberLabel;
    @FXML private CheckBox messageNotifCheckbox, soundNotifCheckbox;
    @FXML private CheckBox readReceiptsCheckbox, lastSeenCheckbox;

    private static boolean soundNotifSelected = false;
    private static boolean messageNotifSelected = false;


    @FXML
    public void initialize() {
        editProfileButton.setOnAction(this::handleEditProfile);
        logoutButton.setOnAction(this::handleLogout);

        // Initialize checkboxes with saved preferences
//        readReceiptsCheckbox.setSelected(true); // Default value, can be changed based on user preference


        // Sound Notification handling
        soundNotifCheckbox.setSelected(soundNotifSelected);
        soundNotifCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            soundNotifSelected = newVal;
        });

        //message Notification handling
        messageNotifCheckbox.setSelected(messageNotifSelected);
        messageNotifCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            messageNotifSelected = newVal;
        });


        // Read Receipts and Last Seen visibility handling(Privacy CheckBox Part)
//        readReceiptsCheckbox.setSelected(UserSettings.isReadReceiptsEnabled());
//        lastSeenCheckbox.setSelected(UserSettings.isLastSeenVisible());
//
//        // Save changes when toggled
//        readReceiptsCheckbox.setOnAction(e ->
//                UserSettings.setReadReceiptsEnabled(readReceiptsCheckbox.isSelected())
//        );
//        lastSeenCheckbox.setOnAction(e ->
//                UserSettings.setLastSeenVisible(lastSeenCheckbox.isSelected())
//        );
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

    public void playNotificationSound() {
        if (soundNotifCheckbox.isSelected()) {
            AudioClip sound = new AudioClip(getClass().getResource("/sounds/notification.wav").toString());
            sound.play();
        }
    }

    public void playNotificationMessageSound() {
        if (messageNotifCheckbox.isSelected()) {
            AudioClip sound = new AudioClip(getClass().getResource("/sounds/message_notification.wav").toString());
            sound.play();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleBack(ActionEvent actionEvent) {

    }
}
