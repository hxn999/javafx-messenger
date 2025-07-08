package com.client.Settings;

import com.client.login.LoginPage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SettingPage extends LoginPage {
    @Override
    public void start(Stage stage) throws IOException {
//            FXMLLoader fxmlLoaderChattingPage = new FXMLLoader(getClass().getResource("/views/chat.fxml")); //chatting interference is going to open
        FXMLLoader fxmlLoaderHomePage = new FXMLLoader(getClass().getResource("/views/settings.fxml"));

        URL resource = getClass().getResource("/views/settings.fxml");
        System.out.println("Resource: " + resource);
        boolean whichPagetoOpen = false;
        Parent root = fxmlLoaderHomePage.load();
//            Parent chat = fxmlLoaderChattingPage.load();
        Scene sceneofHomePage = new Scene(root, 1000, 600);
//            Scene sceneChattingPage = new Scene(chat, 1000, 600);
        stage.setTitle("Messenger");
        if(!whichPagetoOpen){
            stage.setScene(sceneofHomePage);

        }
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("javafx.runtime.version"));
        launch();
    }

}
