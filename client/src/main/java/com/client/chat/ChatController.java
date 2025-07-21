package com.client.chat;

//import com.fasterxml.jackson.core.json.DupDetector;

import com.api.Response;
import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.ClientChat;
import com.db.PublicUser;
import com.db.SignedUser;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatController {
    public Button settingsButton;
    @FXML
    private ImageView searchIcon;
    @FXML
    private TextField searchField;
    @FXML
    private ImageView clearSearch;
    @FXML
    private VBox contactsBox;
    @FXML
    private VBox chatList;
    private int chatId;

    private List<ClientChat> chats;


    /**
     * This must be annotated @FXML so FXMLLoader sees it.
     */
    @FXML
    private void initialize() {
        // optional setup after FXML is loaded
        settingsButton.setOnAction(this::onSettingsClicked);


        clearSearch.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !searchField.getText().trim().isEmpty(),
                        searchField.textProperty()
                )
        );

        // also remove it from layout when invisible
        clearSearch.managedProperty().bind(clearSearch.visibleProperty());

        // so that clicks through the transparent area don't block typing

        clearSearch.visibleProperty().bind(searchField.textProperty().isNotEmpty());
        clearSearch.managedProperty().bind(clearSearch.visibleProperty());
        clearSearch.mouseTransparentProperty().bind(clearSearch.visibleProperty().not());
        clearSearch.setPickOnBounds(false);



        // load all chats from file

//        loadAllChat();

    }

//    @FXML
//    private void onSearchClicked() {
//
//    }
//
//    /** Clears the search field when the “×” button is clicked. */
//    @FXML
//    private void onClearSearchClicked() {
//        searchField.clear();
//    }

    /**
     * Use javafx.scene.input.KeyEvent, not AWT KeyEvent
     */


    /**
     * Use javafx.event.ActionEvent, not AWT ActionEvent
     */
    @FXML
    private void onSettingsClicked(ActionEvent event) {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSearchClicked(ActionEvent mouseEvent) {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            // TODO: replace with your real search logic
            Sender.sender.searchUser(query);

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
                if (res.statusCode != 200) {
//                    Platform.runLater(() -> showError("Invalid phone number or password"));
                } else {
                    Platform.runLater(() -> {
                        try {
                            System.out.println(res.body);
                            showSearchResults(res.body);

//


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                return res;
            });
        }


    }

    public void onClearSearchClicked(MouseEvent mouseEvent) {
        searchField.clear();
        chatList.getChildren().clear();
    }

    public void showSearchResults(String query) {
        chatList.getChildren().clear();
        if (query.isEmpty()) {
            Label label = new Label("User not found");
            label.setStyle("-fx-text-fill: red;");
            label.setAlignment(Pos.CENTER);
            chatList.getChildren().add(label);
        }
        String[] userStrings = query.split(",");

        for (String userString : userStrings) {
            if (userString.isEmpty()) continue;
            PublicUser user = new PublicUser(userString);


            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPrefWidth(334);
            hbox.setPrefHeight(72);
            ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/icons/icons8-avatar-80.png")));
            avatar.setFitWidth(55);
            avatar.setFitHeight(50);
            VBox vbox = new VBox();
            vbox.setPrefWidth(231);
            vbox.setPrefHeight(100);
            Label nameLabel = new Label(user.getName());
            nameLabel.setPrefWidth(131);
            nameLabel.setPrefHeight(35);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            VBox.setMargin(nameLabel, new Insets(8, 0, 0, 0));
            nameLabel.setAlignment(Pos.CENTER);
            Label messageLabel = new Label("Click to message");
            messageLabel.setPrefWidth(227);
            messageLabel.setPrefHeight(18);
            VBox.setMargin(messageLabel, new Insets(0, 0, 0, 0));
            messageLabel.setPadding(new Insets(0, 0, 0, 15));
            messageLabel.setAlignment(Pos.TOP_LEFT);
            vbox.getChildren().addAll(nameLabel, messageLabel);
            hbox.getChildren().addAll(avatar, vbox);
            hbox.setId(user.getPhone());
            hbox.setOnMouseClicked((event) -> {startChat(event,user.getPhone());});
            chatList.getChildren().add(hbox);
        }


    }

    public void startChat(MouseEvent mouseEvent,String phone)
    {
        System.out.println("startChat with"+phone);

        Sender.sender.sendMessage(phone,"initializing chat","null");



    }

    public void loadAllChat()
    {
        // requesting server to send all previous chats
//        chats = new ArrayList<>();
//        for (int chatId:SignedUser.chatList)
//        {
//            chats.add(new ClientChat(chatId));
//        }

        Sender.sender.requestChatUpdate();


        // receiving the response through async function
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            Response response = null;
            try {

                String statusString = Sender.receive.readLine();
                response = new Response(statusString);
                StringBuilder receivedData = new StringBuilder();
                String data;
                if (response.statusCode == 200) {
                    while (( data = Sender.receive.readLine())!=null) {
                        receivedData.append(data);

                    }
                    response.body = receivedData.toString();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        });

        asyncResponse.thenApply((res) -> {
            System.out.println(res.body);
//            if (res.statusCode != 200) {
//                Platform.runLater(() -> showError("Invalid phone number or password"));
//            } else {
//                Platform.runLater(() -> {
//                    try {
//                        SignedUser.Save(res.body);
//                        new Page().Goto(Pages.CHAT);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }

            return res;
        });

    }

    public void populateChatList()
    {
        chatList.getChildren().clear();


    }
//    public void onSearchClicked(MouseEvent mouseEvent) {
//    }
}

