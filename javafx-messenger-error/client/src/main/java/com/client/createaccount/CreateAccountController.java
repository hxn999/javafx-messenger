package com.client.createaccount;

import com.api.Sender;
import com.client.util.DefaultImage;
import com.client.util.ImageBase64Util;
import com.client.util.Page;
import com.client.util.Pages;

import com.db.SignedUser;
import com.db.User;
import com.server.Response;
import javafx.application.Platform;

import javafx.application.Platform;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.api.ResponseManager;
import com.db.SignedUser;

public class CreateAccountController implements Initializable {
    @FXML
    private ImageView logoView;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button choosePicBtn;
    @FXML
    private ImageView avatarPreview;
    @FXML
    private Button createBtn;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField nameField;
    @FXML
    private Button loginBackBtn;


    private String imageUrl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("create controller");
        URL imgUrl = getClass().getResource("/images/account-avatar.png");
        if (imgUrl == null) {
            System.err.println(">>> cannot find /images/account-avatar.png on classpath");
        } else {
            Image img = new Image(imgUrl.toExternalForm());
            avatarPreview.setImage(img);
        }


        choosePicBtn.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File chosen = chooser.showOpenDialog(avatarPreview.getScene().getWindow());
            if (chosen != null) {
                Image img = new Image(chosen.toURI().toString(),
                        100, 100,   // width/height
                        true, true);// preserve ratio + smooth
                avatarPreview.setImage(img);
                imageUrl = chosen.getAbsolutePath();
            }
        });
        loginBackBtn.setOnAction(this::loginBackHandler);
        createBtn.setOnAction(this::createBtnHandler);

    }


    @FXML
    public void loginBackHandler(ActionEvent event) {
        try {
            new Page().Goto(Pages.LOGIN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void createBtnHandler(ActionEvent event) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String url=null;
        if(imageUrl==null){
            url = DefaultImage.url;
        }else{
            url = ImageBase64Util.encodeImageToBase64(imageUrl);
        }

        // Create a request ID
        String requestId = UUID.randomUUID().toString();

        // Create future & register it
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
        ResponseManager.register(requestId, asyncResponse);

        Sender.sender.createAccount(name, phone, password, url, requestId);

        asyncResponse.thenApply((res) -> {

            System.out.println(res);
            if (res.getStatusCode() != 200) {
//                Platform.runLater(() -> showError("Invalid phone number or password"));
            } else {
                Platform.runLater(() -> {
                    try {
                        SignedUser.save((User) res.getBody());
                        new Page().Goto(Pages.CHAT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            return res;
        });


    }


}
