package com.client.chat;

//import com.fasterxml.jackson.core.json.DupDetector;


import com.api.ResponseManager;
import com.api.Sender;
import com.client.util.*;
//import com.db.ClientChat;
import com.db.*;
import com.server.Response;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
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
import java.util.*;
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

    private Button blockUserButton;  // we'll still call this if you want to reuse its handler

    @FXML
    private ImageView userImage;
    @FXML
    private Label userName;


    // our ContextMenu and MenuItem
    @FXML
    private ContextMenu dotsMenu;
    @FXML
    private MenuItem blockUserItem;
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
    public static Map<String,Integer> receiverMap;
    //
    boolean hadChat = false;
    public static Integer currentChatId=null;
    private String currentReceiverPhone;
    public static List<User> allChatUser;
//    private List<ClientChat> chats;


    public static Map<Integer, Chat> allChats = new HashMap<>();


    public static void addOrUpdateUser(User user) {
        for (int i = 0; i < allChatUser.size(); i++) {
            if (allChatUser.get(i).getPhone().equals(user.getPhone())) {
                allChatUser.set(i, user); // update
                return;
            }
        }
        allChatUser.add(user); // add if not found
    }

    public static Optional<User> findUser(String phone) {
        return allChatUser.stream()
                .filter(u -> u.getPhone().equals(phone))
                .findFirst();
    }

    /**
     * This must be annotated @FXML so FXMLLoader sees it.
     */
    @FXML
    private void initialize() {
        allChatUser= new ArrayList<>();
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
        dotsMenu = new ContextMenu();
        blockUserItem = new MenuItem("Block User");
        dotsMenu.getItems().add(blockUserItem);

        // what happens when "Block User" is clicked
        blockUserItem.setOnAction(e -> {
            onBlockUserClicked();
        });

        userName.setText(SignedUser.name);
//        Base64ImageHelper.getImageViewFromBase64(userImage,SignedUser.url,50,50);
//        System.out.println(SignedUser.url);
        userImage.setImage(Base64ImageHelper.getImageViewFromBase64(SignedUser.url));
        // load all chats from file

//        loadAllChat();
        receiverMap = new HashMap<>();
        // loading all chats
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (int chatId : SignedUser.chatList) {
            String requestId = UUID.randomUUID().toString();
            CompletableFuture<Response> future = new CompletableFuture<>();
            ResponseManager.register(requestId, future);
//            Sender.sender.fetchChat(chatId, requestId);

            CompletableFuture<Response> chainedFuture = future.thenCompose(res -> {
                if (res.getStatusCode() == 200) {
                    Chat chat = (Chat) res.getBody();
                    allChats.put(chatId, chat);
                  chat.printMessages();
                    String receiverPhone = ReceiverPhone.get(chat);
                    receiverMap.put(receiverPhone, chatId);

                    // Now fetch the receiver user
                    String userReqId = UUID.randomUUID().toString();
                    CompletableFuture<Response> userFuture = new CompletableFuture<>();
                    ResponseManager.register(userReqId, userFuture);
                    System.out.println("receiverPhone = " + receiverPhone);
                    Sender.sender.searchSingle(receiverPhone, userReqId);

                    return userFuture.thenApply(userRes -> {
                        System.out.println("fetching user for phone: " + receiverPhone);
                        if (userRes.getStatusCode() == 200) {
                            User receiver = (User) userRes.getBody();
                        System.out.println(userRes.getStatusCode());
                            System.out.println("Fetched user: " + receiver.getName());
                            addOrUpdateUser(receiver); // Add to ChatController.allChatUser
                        } else {
                            System.err.println("❌ Failed to fetch user for phone: " + receiverPhone);
                        }
                        return userRes;
                    });
                } else {
                    System.err.println("❌ Failed to fetch chat for chatId: " + chatId);
                    return CompletableFuture.completedFuture(res);
                }
            });

            futures.add(chainedFuture);
        }

// 🔁 Run after ALL chats + users are fetched
        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(this::populateChatList));





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
        List<User> foundUsers;
        if (!query.isEmpty()) {

            // Create a request ID
            String requestId = UUID.randomUUID().toString();

            // Create future & register it
            CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
            ResponseManager.register(requestId, asyncResponse);

            Sender.sender.searchUser(query, requestId);

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
        populateChatList();
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
        receiverImage.setImage(Base64ImageHelper.getImageViewFromBase64(user.getUrl()));
        receiverName.setText(user.getName());
        currentReceiverPhone = user.getPhone();
        Integer chatId = receiverMap.get(user.getPhone());

        // Clear message container when switching chats
        messageContainer.getChildren().clear();

        if(chatId != null) {
            hadChat = true;
            currentChatId=chatId;
            populateChat(allChats.get(chatId));
        }else{
            hadChat = false;
        }

        populateChatList();
    }

    public void populateChat(Chat chat)
    {
        messageContainer.getChildren().clear();
        Set<String> shownMessages = new HashSet<>();
        for (Message msg:chat.getMessages())
        {
            // Prevent duplicate messages
            String msgKey = msg.getSender() + msg.getTimestamp() + msg.getMessage();
            if (shownMessages.contains(msgKey)) continue;
            shownMessages.add(msgKey);

            boolean mine= msg.getSender().equals(SignedUser.phone);
            addMessageBubble(msg.getMessage(),mine);
        }
    }


    @FXML
    public void onSendClicked(ActionEvent event) {

//        if (currentChatPhone == null) return;

        String text = messageField.getText().trim();

        if (text.isEmpty()) return;
        Message msg = new Message(SignedUser.phone,currentReceiverPhone, System.currentTimeMillis() / 1000L,text);
        if(!hadChat) {
            System.out.println("first message");
            msg.setFirstMsg(true);

        }
        else{
            System.out.println("not first message");
            msg.setChatId(currentChatId);
        }
        // Create a request ID
        String requestId = UUID.randomUUID().toString();

        // Create future & register it
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
        ResponseManager.register(requestId, asyncResponse);

        // 1) send to server
        Sender.sender.sendMessage( msg ,requestId);

        asyncResponse.thenApply((res) -> {
            System.out.println("hiiiiiiiiiiiiii");
            Chat chat =(Chat) res.getBody();

            if (res.getStatusCode() != 200) {
//                Platform.runLater(() -> showError("Invalid phone number or password"));
            } else {
                Platform.runLater(() -> {

                    allChats.put(chat.getChatId(),chat);
                    populateChatList();
                    System.out.println(chat.getChatId());
                    SignedUser.chatList.add(chat.getChatId());
                    SignedUser.saveToFile();
                });


            }

            return res;
        });


        // 2) echo locally
        addMessageBubble(text, true);

        // 3) clear input & scroll to bottom
        messageField.clear();
        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));

    }

    public void onDotsClicked() {
        blockUserButton.setVisible(false);
        dotsMenu.show(dotsButton, Side.BOTTOM, 0, 0);
    }

    public void onBlockUserClicked() {
        System.out.println("Blocking user...");
        // ... your block‐user code here ...
        try {
            new Page().Goto(Pages.BLOCK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // then hide the menu
        dotsMenu.hide();
    }

    public void addMessageBubble(String text, boolean mine) {

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


    public void populateChatList() {
        chatList.getChildren().clear();

        for (int chatId : SignedUser.chatList) {
            String phone = MapUtils.getKeyFromValue(receiverMap, chatId);

            if (phone == null) {
                System.err.println("No phone found for chatId: " + chatId);
                continue;
            }

            User user = FindUser.findUserByPhone(allChatUser, phone);
            if (user == null) {
                // Skip if user not found
                continue;
            }

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
            hbox.setOnMouseClicked(event -> startChat(event, user));

            chatList.getChildren().add(hbox);
        }
    }


}
