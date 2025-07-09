package com.client.login;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.awt.event.KeyEvent;
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
import javafx.stage.FileChooser;

import java.io.File;
import com.api.Sender;

public class LoginController implements Initializable{
    @FXML
    private TextField searchField;
    @FXML
    private VBox    contactsBox;
    @FXML
    private TextField phone;
    @FXML
    private TextField password;
    @FXML
    private Button login;




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
    public void loginHandler()
    {
        String phoneText = phone.getText();
        String passwordText = password.getText();
        System.out.println("phone : " + phoneText);
        System.out.println("password : " + passwordText);
        Sender.sender.login(phoneText,passwordText);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    //Account details part


}


