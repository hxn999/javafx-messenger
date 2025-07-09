package com.client.Settings.AccountDetails;

import com.client.login.LoginController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AccountDetailsController extends LoginController {
    @FXML
    private TextField phoneField;

    @FXML
    private TextField usernameField;

    @FXML
    public Button saveButton;
    @FXML
    public Button backButton;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button changePhotoButton;




    public static boolean isValidBDNumber(String phone) {
        if (phone.startsWith("+880")) {
            return phone.length() == 14 && phone.substring(4).chars().allMatch(Character::isDigit);
        }
        if (phone.startsWith("01")) {
            return phone.length() == 11 && phone.chars().allMatch(Character::isDigit);
        }
        return false;
    }




    @FXML
    public void initialize() {
        // Optional: set a default placeholder
        profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));

        changePhotoButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File chosen = chooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (chosen != null) {
                Image img = new Image(chosen.toURI().toString(),
                        100, 100,   // width/height
                        true, true);// preserve ratio + smooth
                profileImageView.setImage(img);
            }
        });

        backButton.setOnAction(this::handleBack);

        saveButton.setOnAction(this::handleSave);

    }


    // Handle the back button action
    public void handleBack(ActionEvent actionEvent) {
        try {
            // Navigate back to the settings page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        if (usernameField.getText() != null && !usernameField.getText().isEmpty()) {
            // to reset user name
        }
        if(isValidBDNumber(phoneField.getText())) {
            // to reset user phone number
        }
        System.out.println("UsernameField: " + usernameField.getText() + " PhoneField: " + phoneField.getText());
        try {
            // Navigate back to the settings page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
