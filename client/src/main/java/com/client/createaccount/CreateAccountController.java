package com.client.createaccount;

import com.api.Response;
import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            }
        });
    }

    @FXML
    public void loginBackHandler() {
        try {
            new Page().Goto(Pages.LOGIN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void createBtnHandler()
    {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        Sender.sender.createAccount(name,phone,password);

        // receiving the response through async function
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            Response response = null;
            try {
                System.out.println("Hi");
                String statusString = Sender.receive.readLine();
                System.out.println("Hi2");
                response = new Response(statusString);

                if (response.statusCode == 200) {

                    response.body = Sender.receive.readLine();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        });

        asyncResponse.thenApply((res) -> {

            System.out.println(res);
            if (res.statusCode != 200) {
//                Platform.runLater(() -> showError("Error Occured"));
                System.out.println("Error");
            } else {
                Platform.runLater(() -> {
                    try {
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

