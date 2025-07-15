package com.client.login;

import com.api.Response;
import com.db.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.api.Sender;

public class LoginController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private VBox contactsBox;
    @FXML
    private TextField phone;
    @FXML
    private TextField password;
    @FXML
    private Button login;
    @FXML
    private Label errorText = new Label();
    @FXML
    private VBox credentialBox;


//    @FXML
//        private Label selectedOption;
//        private static LoginController instance;
//
//    public LoginController() {
//        instance = this;
//    }
//
//    public static LoginController getInstance() {
//        return instance;
//    }

    //    @FXML
//        protected void onHelloButtonClick() {
//            selectedOption.setText("Welcome to JavaFX Application!");
//        }
//
    @FXML
    public void loginHandler() {

        String phoneText = phone.getText();
        String passwordText = password.getText();
        System.out.println("phone : " + phoneText);
        System.out.println("password : " + passwordText);
        Sender.sender.login(phoneText, passwordText);

        // receiving the response through async function
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            Response response = null;
            try {
                String statusString = Sender.receive.readLine();
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
            if(res.statusCode != 200) {
                Platform.runLater(() -> showError("Invalid phone number or password"));
            }

            return res;
        });


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    void showError(String error) {
        // Set size
        errorText.setText(error);
        System.out.println("error : " + error);
        errorText.setPrefHeight(15.0);
        errorText.setPrefWidth(419.0);

// Set alignment
        errorText.setAlignment(Pos.CENTER);

// Set font size
        errorText.setFont(new Font(14.0));

// Set text color
        errorText.setStyle("-fx-text-fill: #c12c2c;");

// Add CSS class (if using external styles)
        errorText.getStyleClass().add("error-color");
        if(!credentialBox.getChildren().get(3).equals(errorText)){

        credentialBox.getChildren().add(3,errorText);
        }

    }

    //Account details part


}


