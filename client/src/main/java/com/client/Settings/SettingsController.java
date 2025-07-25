package com.client.Settings;

import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.SignedUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private Button editProfileButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button backButton;
    @FXML
    private Label phoneNumberLabel;
    @FXML private CheckBox messageNotifCheckbox, soundNotifCheckbox;
    @FXML private CheckBox readReceiptsCheckbox, lastSeenCheckbox;

    private static boolean soundNotifSelected = false;
    private static boolean messageNotifSelected = false;

    private static boolean TCP_mode = false;
    private static boolean UDP_mode = false;


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        editProfileButton.setOnAction(this::handleEditProfile);
        logoutButton.setOnAction(this::handleLogout);
        backButton.setOnAction(this::handleBack);

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


        //Privacy Part
        readReceiptsCheckbox.setSelected(TCP_mode);
        readReceiptsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            TCP_mode = newVal;
        });

        lastSeenCheckbox.setSelected(UDP_mode);
        lastSeenCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            UDP_mode = newVal;
        });
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
            SignedUser.logout();
            Sender.sender.logout(SignedUser.phone);
            new Page().Goto(Pages.LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleBack(ActionEvent event) {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/chat.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
