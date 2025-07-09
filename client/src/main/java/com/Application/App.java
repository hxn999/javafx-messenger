package com.Application;

import com.api.Sender;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 600);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icons8-chat-message-100.png")));
        stage.setTitle("Messenger");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Sender sender = null;
        Socket socket=null;

        try{
            socket= new Socket("127.0.0.1", 5000);
            sender = new Sender("Sender-Thread",socket);
        }catch(IOException e){
            e.printStackTrace();
        }
        sender.start();

        System.out.println(System.currentTimeMillis());
        System.out.println(System.getProperty("javafx.runtime.version"));
        launch();
    }
}