package com.client.chat;

//import com.fasterxml.jackson.core.json.DupDetector;


import com.api.Sender;
import com.client.util.Base64ImageHelper;
import com.client.util.Page;
import com.client.util.Pages;
//import com.db.ClientChat;
import com.db.*;
import com.server.Response;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChatController {
    @FXML
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

    @FXML
    private Button dotsButton;

    @FXML
    private Button blockUserButton;

    @FXML
    private MenuItem blockMenuItem;

    @FXML
    private ImageView userImage;
    @FXML
    private Label userName;


    // our ContextMenu and MenuItem
    @FXML
    private ContextMenu dotsMenu;
    @FXML
    private String currentChatPhone;

    @FXML
    private ImageView sendIcon;
    @FXML
    private TextField messageField;
    @FXML
    private ScrollPane messageScrollPane;
    @FXML
    private VBox messageContainer;
    // its content VBox
    @FXML
    private ImageView headerAvatar;            // the avatar in the header bar
    @FXML
    private Label headerNameLabel;           // the username in the header bar// bottom “write here…” TextField
    @FXML
    private Button sendButton;
    @FXML
    private Label receiverName;
    @FXML
    private ImageView receiverImage;

    // maps receiver phone to chat id
    private Map<String,Integer> receiverMap;
    //
    boolean hadChat = false;
    Integer currentChatId=null;
    private String currentReceiverPhone;

//    private List<ClientChat> chats;


    private Map<Integer, Chat> allChats = new HashMap<>();

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
//        searchIcon.setPickOnBounds(false);

        // build the popup menu
//        dotsMenu = new ContextMenu();
//        blockUserItem = new MenuItem("Block User");
//        dotsMenu.getItems().add(blockUserItem);
//
//        // what happens when "Block User" is clicked
//        blockUserItem.setOnAction(e -> {
//            onBlockUserClicked();
//        });

        userName.setText(SignedUser.name);
//        Base64ImageHelper.getImageViewFromBase64(userImage,SignedUser.url,50,50);
//        System.out.println(SignedUser.url);
        userImage.setImage(Base64ImageHelper.getImageViewFromBase64(SignedUser.url));
        // load all chats from file

//        loadAllChat();
        receiverMap = new HashMap<>();
        // loading all chats
        for (int chatId : SignedUser.chatList) {
            Sender.sender.fetchChat(chatId);

            CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
                Response response = null;
                try {
                    try {
                        response = (Response) Sender.receive.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            });

            asyncResponse.thenApply((res) -> {

                System.out.println(res);
                if (res.getStatusCode() != 200) {
//                Platform.runLater(() -> showError("Invalid phone number or password"));
                } else {
                    Platform.runLater(() -> {
                        try {
                            Chat chat = (Chat) res.getBody();
                            allChats.put(chatId, chat);
                            String receiverPhone ;
                            if(!chat.getUser1().equals(SignedUser.phone))
                            {
                                receiverPhone = chat.getUser1();
                            }else {
                                receiverPhone = chat.getUser2();
                            }

                            receiverMap.put(receiverPhone, chatId);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                return res;
            });



        }

        blockMenuItem.setOnAction(e -> onBlockUserClicked());

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

    public void onSearchClicked() {
        String query = searchField.getText().trim();
        List<User> foundUsers;
        if (!query.isEmpty()) {

            Sender.sender.searchUser(query);

            CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
                Response response = null;
                try {
                    try {
                        response = (Response) Sender.receive.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            });

            asyncResponse.thenApply((res) -> {

                System.out.println(res);
                if (res.getStatusCode() != 200) {
//                Platform.runLater(() -> showError("Invalid phone number or password"));
                } else {
                    Platform.runLater(() -> {

                     showSearchResults ((List<User>) res.getBody());
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

    public void showSearchResults(List<User> users) {
        chatList.getChildren().clear();
        if (users.isEmpty()) {
            Label label = new Label("User not found");
            label.setStyle("-fx-text-fill: red;");
            label.setAlignment(Pos.CENTER);
            chatList.getChildren().add(label);
        }


        for (User user : users) {

            System.out.println(user);

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPrefWidth(334);
            hbox.setPrefHeight(72);
            ImageView avatar = new ImageView(Base64ImageHelper.getImageViewFromBase64(user.getUrl()));
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
            hbox.setOnMouseClicked((event) -> {
                startChat(event,user);
            });
            chatList.getChildren().add(hbox);
        }


    }

    public void startChat(MouseEvent mouseEvent, User user) {
//        System.out.println("startChat with"+phone);
//
//        Sender.sender.sendMessage(phone,"initializing chat","null");
            receiverImage.setImage(Base64ImageHelper.getImageViewFromBase64(user.getUrl()));
            receiverName.setText(user.getName());
            currentReceiverPhone = user.getPhone();
            // check if already has conversation to this user
            Integer chatId = receiverMap.get(user.getPhone());

            if(chatId != null) {
                hadChat = true;
                currentChatId=chatId;
                populateChat(allChats.get(chatId));
            }else{
                hadChat = false;
            }









    }


    public void populateChat(Chat chat)
    {
        for (Message msg:chat.getMessages())
        {
            // checking its my message or not
            boolean mine= msg.getSender().equals(SignedUser.phone);
            addMessageBubble(msg.getMessage(),mine);
        }
    }

//    public void loadAllChat()
//    {
//        // requesting server to send all previous chats
//       chats = new ArrayList<>();
//       for (int chatId:SignedUser.chatList)
//        {
//            chats.add(new ClientChat(chatId));
//        }
//
//        Sender.sender.requestChatUpdate();
//
//
//        // receiving the response through async function
//        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
//            Response response = null;
//            try {
//
//                String statusString = Sender.receive.readLine();
//                response = new Response(statusString);
//                StringBuilder receivedData = new StringBuilder();
//                String data;
//                if (response.statusCode == 200) {
//                    while (( data = Sender.receive.readLine())!=null) {
//                        receivedData.append(data);
//
//                    }
//                    response.body = receivedData.toString();
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return response;
//        });
//
//        asyncResponse.thenApply((res) -> {
//            System.out.println(res.body);

    /// /            if (res.statusCode != 200) {
    /// /                Platform.runLater(() -> showError("Invalid phone number or password"));
    /// /            } else {
    /// /                Platform.runLater(() -> {
    /// /                    try {
    /// /                        SignedUser.Save(res.body);
    /// /                        new Page().Goto(Pages.CHAT);
    /// /                    } catch (Exception e) {
    /// /                        e.printStackTrace();
    /// /                    }
    /// /                });
    /// /            }
//
//            return res;
//        });
//
//    }


//    public void onSearchClicked(MouseEvent mouseEvent) {
//    }
    @FXML
    public void onSendClicked(ActionEvent event) {

//        if (currentChatPhone == null) return;

        String text = messageField.getText().trim();
        System.out.println("I am here " + text + " " + currentChatPhone);
        if (text.isEmpty()) return;
        Message msg = new Message(SignedUser.phone,currentReceiverPhone, System.currentTimeMillis() / 1000L,text);
        if(!hadChat) {
            msg.setFirstMsg(true);
        }
        else{
            msg.setChatId(currentChatId);
        }
        // 1) send to server
        Sender.sender.sendMessage( msg);

        // 2) echo locally
        addMessageBubble(text, true);

        // 3) clear input & scroll to bottom
        messageField.clear();
        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));

    }

    @FXML
    private void onDotsClicked(ActionEvent e) {
        // Refresh the “Block/Unblock” text before showing
        updateBlockButtonText(null);
        // Show the menu anchored to the dots button
        dotsMenu.show(dotsButton, Side.BOTTOM, 0, 0);
    }



    public void onBlockUserClicked() {

        System.out.println("Blocking/unblocking user…");

        // Decide based on the *menu‑item* text:
        if (blockMenuItem.getText().equals("Unblock User")) {
            Sender.sender.unblock(SignedUser.phone, currentReceiverPhone);
            handleBlockUnblockResponse(true);
        } else {
            Sender.sender.block(SignedUser.phone, currentReceiverPhone);
            handleBlockUnblockResponse(false);
        }

        // Hide the dots menu after selection
        dotsMenu.hide();
    }

    private void handleBlockUnblockResponse(boolean wasBlocked) {
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            try {
                return (Response) Sender.receive.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });

        asyncResponse.thenAccept(res -> {
            if (res == null) return;

            // Toggle based on result
            Platform.runLater(() -> {
                if (res.getStatusCode() == 200) {
                    // Persist and flip text
                    try { SignedUser.save((User) res.getBody()); }
                    catch (Exception ignored) { }
                    blockMenuItem.setText(wasBlocked ? "Block User" : "Unblock User");
                } else {
                    // On error, leave text as it was
                    blockMenuItem.setText(wasBlocked ? "Unblock User" : "Block User");
                }
            });
        });
    }

    @FXML
    private void updateBlockButtonText(Event ignored) {
        // Send request to server
        Sender.sender.searchforblocking(SignedUser.phone, currentReceiverPhone);

        // Asynchronously wait for the response
        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
            Response response = null;
            try {
                response = (Response) Sender.receive.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return response;
        });

        asyncResponse.thenAccept((res) -> {
            if (res == null) {
                return;
            }

            // Determine blocked status
            boolean isBlocked = res.getStatusCode() != 200;

            System.out.println("The value of boolean : " + isBlocked);

            // Update the button text on JavaFX thread
            Platform.runLater(() -> {
                blockUserButton.setText(isBlocked ? "Unblock User" : "Block User");
            });
        });
    }

    private void addMessageBubble(String text, boolean mine) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        TextFlow messageBubble = new TextFlow();
        messageBubble.setPadding(new Insets(10));
        messageBubble.setStyle(mine ? "-fx-background-color: #0084ff; -fx-background-radius: 15 0 15 15;" : "-fx-background-color: #e4e6eb; -fx-background-radius: 0 15 15 15;");

        Text messageText = new Text(text);
        messageText.setStyle("-fx-fill: " + (mine ? "white" : "black") + "; -fx-font-size: 14;");
        messageBubble.getChildren().add(messageText);

        // Add timestamp
        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.setStyle("-fx-text-fill: " + (mine ? "#aad4ff" : "#666") + "; -fx-font-size: 10;");

        VBox messageContent = new VBox(5, messageBubble, timeLabel);
        messageContent.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageContent);
        this.messageContainer.getChildren().add(messageContainer);
    }


}