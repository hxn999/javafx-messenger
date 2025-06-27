package com.client.Settings.AccountDetails;

import com.client.login.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class AccountDetailsController extends LoginController {
    public TextField phoneField;
    public TextField usernameField;

    @FXML
    public Button saveButton;
    @FXML
    private ImageView profileImageView;
    @FXML private Button changePhotoButton;


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
    }
}
