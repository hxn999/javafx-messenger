package com.client.util;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import static com.Application.App.globalStage;

public class Page {


    // changes page

    public  void Goto(Pages page) throws Exception {


        FXMLLoader loader = null;
        switch (page) {
            case LOGIN:
                loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                break;
            case SETTINGS:
                loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
                break;
            case CREATE_ACCOUNT:
                loader = new FXMLLoader(getClass().getResource("/views/createAccount.fxml"));
                break;
            case CHAT:
                loader = new FXMLLoader(getClass().getResource("/views/chat.fxml"));
                break;

            case BLOCK:
                loader = new FXMLLoader(getClass().getResource("/views/blockUser.fxml"));
                break;
        }

        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 600);


        globalStage.setScene(scene);
        globalStage.show();
    }


    public static boolean isValidBDNumber(String phone) {
        if (phone.startsWith("+880")) {
            return phone.length() == 14 && phone.substring(4).chars().allMatch(Character::isDigit);
        }
        if (phone.startsWith("01")) {
            return phone.length() == 11 && phone.chars().allMatch(Character::isDigit);
        }
        return false;
    }

}
