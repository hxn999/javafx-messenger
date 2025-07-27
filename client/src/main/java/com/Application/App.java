package com.Application;

import com.api.Receiver;
import com.api.Sender;
import com.client.chat.ChatController;
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
    public static ChatController chatController;
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

        // If user is logged in, go to chat page and set chatController
        if (SignedUser.isLoggedIn()) {
            try {
                FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/views/chat.fxml"));
                Parent chatRoot = chatLoader.load();
                chatController = chatLoader.getController();
                Scene chatScene = new Scene(chatRoot, 1000, 600);
                globalStage.setScene(chatScene);
                globalStage.show();
                // Networking should be initialized after chatController is set
                initNetworking(chatController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Networking should be initialized after UI is ready (login page)
            initNetworking(null);
        }
    }

    private void initNetworking(ChatController chatController) {
        try {
            Socket socket = new Socket("127.0.0.1", 5000);
            Sender sender = new Sender("Sender-Thread", socket);
            Receiver msgReceiver = new Receiver("Receiver-Thread", socket, chatController);
            Sender.sender = sender;
            sender.start();
            msgReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SignedUser.loadFromFile();
        System.out.println(SignedUser.chatList.size());
        launch(args);
    }
}
