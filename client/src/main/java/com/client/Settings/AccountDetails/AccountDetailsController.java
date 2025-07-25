package com.client.Settings.AccountDetails;

import com.api.Sender;
import com.client.login.LoginController;
import com.client.util.DefaultImage;
import com.client.util.ImageBase64Util;
import com.client.util.Page;
import com.client.util.Pages;
import com.controller.ClientHandler;
import com.db.SignedUser;
import com.db.User;
import com.server.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

//import static com.Application.App.currentUser;

import static com.db.SignedUser.isLoggedIn;
import static com.db.SignedUser.phone;

public class AccountDetailsController implements Initializable {

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
    @FXML
    private TextField newPasswordField;

    private String imageUrl;


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        URL imgUrl = getClass().getResource("/images/account-avatar.png");
        if (imgUrl == null) {
            System.err.println(">>> cannot find /images/account-avatar.png on classpath");
        } else {
            Image img = new Image(imgUrl.toExternalForm());
            profileImageView.setImage(img);
        }

        changePhotoButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File chosen = chooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (chosen != null) {
                Image img = new Image(chosen.toURI().toString(), 100, 100, true, true);// preserve ratio + smooth
                profileImageView.setImage(img);
                imageUrl = chosen.getAbsolutePath();
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

        String Phone = null;
        String Username = null;
        String Password = null;
        String newimgUrl = null;
        if (usernameField.getText() != null && !usernameField.getText().isEmpty()) {
            if(isLoggedIn()) {
                Username = usernameField.getText();
            }
        }
        if(Page.isValidBDNumber(phoneField.getText())) {
            if(isLoggedIn()) {
                // to reset user phone number
                Phone = phoneField.getText();
                if(Phone.length() == 11) {
                    Phone = "+88" + phoneField.getText();
                }
            }
        }
        if(newPasswordField.getText() != null && !newPasswordField.getText().isEmpty()) {
            Password = newPasswordField.getText();
        }

        if(imageUrl==null){
            newimgUrl = DefaultImage.url;
        }else{
            newimgUrl = ImageBase64Util.encodeImageToBase64(imageUrl);
        }

//        System.out.println("Sign " +SignedUser.phone + " " + Phone + " " + Username + " " + Password + " ");

        Sender.sender.EditACC(SignedUser.phone,Phone, Username, Password, newimgUrl);

        // receiving the response through async function
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            Response response = null;
            try {
                try {
                    System.out.println("Hello Reached Here");
                    response = (Response) Sender.receive.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        });

        asyncResponse.thenApply((res) -> {

            System.out.println(res);
            if (res.getStatusCode() != 200) {
                System.out.println("System Crashed");
            } else {
                Platform.runLater(() -> {
                    try {
                        SignedUser.save((User)res.getBody());
                        System.out.println(res.getBody());
                        new Page().Goto(Pages.SETTINGS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            return res;
        });

//        System.out.println("UsernameField: " + usernameField.getText() + " PhoneField: " + phoneField.getText());
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


