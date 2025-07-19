package com.Application;

import com.api.MessageReceiver;
import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.SignedUser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class App extends Application {

    // making the stage global for accessing
    public static Stage globalStage;

    @Override
    public void start(Stage stage) throws IOException {
        globalStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 600);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icons8-chat-message-100.png")));
        stage.setTitle("Messenger");
        stage.setScene(scene);
        stage.show();

        if (SignedUser.isLoggedIn()) {
            try {
                new Page().Goto(Pages.CHAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {


        Sender sender = null;
        Socket socket = null;
        MessageReceiver msgReceiver = null;

        try {
            socket = new Socket("127.0.0.1", 5000);
            sender = new Sender("Sender-Thread", socket);
            msgReceiver = new MessageReceiver("Receiver-Thread", socket);
            sender.start();
            msgReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        launch();
    }
}