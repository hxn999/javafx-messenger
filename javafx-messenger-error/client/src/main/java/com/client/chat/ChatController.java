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
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatController implements Initializable {
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            ResponseManager.register(requestId,future);
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

    // Real-time message polling
    private volatile boolean isPolling = false;
    private CompletableFuture<Void> pollingTask;

    public void startRealTimePolling() {
        if (isPolling) return;

        isPolling = true;
        pollingTask = CompletableFuture.runAsync(() -> {
            while (isPolling) {
                try {
                    // Poll for new messages every 2 seconds
                    Thread.sleep(2000);
                    if (currentChatId != null) {
                        fetchLatestMessages(currentChatId);
                    }
                    // Also check for new chats
                    // checkForNewChats(); // Commented out - not necessary for local implementation
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in polling: " + e.getMessage());
                }
            }
        });
    }

    public void stopRealTimePolling() {
        isPolling = false;
        if (pollingTask != null) {
            pollingTask.cancel(true);
        }
    }

    private void fetchLatestMessages(int chatId) {
        System.out.println("🔄 Polling for new messages in chat: " + chatId);
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseManager.register(requestId, future);

        // Fetch updated chat from server
//            Sender.sender.fetchChat(chatId, requestId);
        Sender.sender.fetchChat(chatId, requestId);

        future.thenAccept(res -> {
            if (res.getStatusCode() == 200) {
                Chat updatedChat = (Chat) res.getBody();
                if (updatedChat != null) {
                    Chat currentChat = allChats.get(chatId);

                    // Check if there are new messages
                    if (currentChat == null || hasNewMessages(currentChat, updatedChat)) {
                        allChats.put(chatId, updatedChat);

                        // Update UI on JavaFX thread
                        Platform.runLater(() -> {
                            if (currentChatId != null && currentChatId.equals(chatId)) {
                                populateChat(updatedChat);
                                messageScrollPane.setVvalue(1.0); // Scroll to bottom
                            }
                            populateChatList(); // Update chat list with latest message
                            System.out.println("The chat has been fetched heeeeeeeeeeeeeeeeeeeeeeeeeeeeeyyyyyyyyyyyyyyyyyyyyyyyyy: " + updatedChat.getMessages());
                        });
                    }
                }
            }
        });
        System.out.println("Reaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaach herrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrre");
    }

    private boolean hasNewMessages(Chat oldChat, Chat newChat) {
        if (oldChat.getMessages() == null && newChat.getMessages() != null) {
            return !newChat.getMessages().isEmpty();
        }
        if (oldChat.getMessages() == null || newChat.getMessages() == null) {
            return false;
        }
        return newChat.getMessages().size() > oldChat.getMessages().size();
    }

    // Commented out - not necessary for local implementation
    /*
    private void checkForNewChats() {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseManager.register(requestId, future);

        future.thenAccept(res -> {
            if (res.getStatusCode() == 200) {
                @SuppressWarnings("unchecked")
                List<Integer> serverChatList = (List<Integer>) res.getBody();

                if (serverChatList != null) {
                    for (Integer chatId : serverChatList) {
                        if (!SignedUser.chatList.contains(chatId)) {
                            SignedUser.chatList.add(chatId);
                            loadNewChat(chatId);
                        }
                    }
                }
            }
        });
    }

    private void loadNewChat(int chatId) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseManager.register(requestId, future);

        Sender.sender.fetchChat(chatId, requestId);

        future.thenCompose(res -> {
            if (res.getStatusCode() == 200) {
                Chat chat = (Chat) res.getBody();
                allChats.put(chatId, chat);
                String receiverPhone = ReceiverPhone.get(chat);
                receiverMap.put(receiverPhone, chatId);

                String userReqId = UUID.randomUUID().toString();
                CompletableFuture<Response> userFuture = new CompletableFuture<>();
                ResponseManager.register(userReqId, userFuture);
                Sender.sender.searchSingle(receiverPhone, userReqId);

                return userFuture.thenApply(userRes -> {
                    if (userRes.getStatusCode() == 200) {
                        User receiver = (User) userRes.getBody();
                        addOrUpdateUser(receiver);

                        Platform.runLater(() -> {
                            populateChatList();
                            showNewMessageNotification(receiver.getName());
                        });
                    }
                    return userRes;
                });
            }
            return CompletableFuture.completedFuture(res);
        });
    }
    */




    public void startChat(MouseEvent mouseEvent, User user) {
        // Null check for user
        if (user == null) {
            System.err.println("User is null in startChat");
            return;
        }

        // Stop any existing polling
        stopRealTimePolling();

        receiverImage.setImage(Base64ImageHelper.getImageViewFromBase64(user.getUrl()));
        receiverName.setText(user.getName());
        currentReceiverPhone = user.getPhone();
        Integer chatId = receiverMap.get(user.getPhone());

        // Clear message container when switching chats
        messageContainer.getChildren().clear();

        if (chatId != null) {
            hadChat = true;
            currentChatId = chatId;
            // Null check for chat
            Chat chat = allChats.get(chatId);
            if (chat != null) {
                populateChat(chat);
            } else {
                System.err.println("Chat not found for chatId: " + chatId);
            }
        } else {
            hadChat = false;
            currentChatId = null; // Reset current chat ID
        }

        populateChatList();

        // Start real-time polling for this chat
        if (currentChatId != null) {
            startRealTimePolling();
        }
    }

    public void populateChat(Chat chat) {
        if (chat == null) {
            System.err.println("Chat is null in populateChat");
            return;
        }

        messageContainer.getChildren().clear();
        Set<String> shownMessages = new HashSet<>();

        // Null check for messages
        if (chat.getMessages() != null) {
            for (Message msg : chat.getMessages()) {
                if (msg == null) continue; // Skip null messages

                // Prevent duplicate messages - create more robust key
                String msgKey = (msg.getSender() != null ? msg.getSender() : "") +
                        msg.getTimestamp() +
                        (msg.getMessage() != null ? msg.getMessage() : "");
                if (shownMessages.contains(msgKey)) continue;
                shownMessages.add(msgKey);

                boolean mine = msg.getSender() != null && msg.getSender().equals(SignedUser.phone);
                addMessageBubble(msg.getMessage(), mine);
            }
        }

        // Scroll to bottom after populating
        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
    }

    @FXML
    public void onSendClicked(ActionEvent event) {
        // Check if we have a valid receiver
        if (currentReceiverPhone == null || currentReceiverPhone.trim().isEmpty()) {
            System.err.println("No receiver selected");
            return;
        }

        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        // Null check for SignedUser.phone
        if (SignedUser.phone == null) {
            System.err.println("SignedUser.phone is null");
            return;
        }

        Message msg = new Message(SignedUser.phone, currentReceiverPhone, System.currentTimeMillis() / 1000L, text);

        if (!hadChat) {
            System.out.println("first message");
            msg.setFirstMsg(true);
        } else {
            System.out.println("not first message");
            msg.setChatId(currentChatId);
        }

        // Create a request ID
        String requestId = UUID.randomUUID().toString();

        // Create future & register it
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
        ResponseManager.register(requestId, asyncResponse);

        // 1) send to server
        Sender.sender.sendMessage(msg, requestId);

        asyncResponse.thenApply((res) -> {
            System.out.println("Response received");

            if (res == null) {
                System.err.println("Response is null");
                return null;
            }

            if (res.getStatusCode() != 200) {
                Platform.runLater(() -> {
                    System.err.println("Server error: " + res.getStatusCode());
                    // Show error to user
                });
            } else {
                Chat chat = (Chat) res.getBody();
                if (chat != null) {
                    Platform.runLater(() -> {
                        allChats.put(chat.getChatId(), chat);
                        currentChatId = chat.getChatId(); // Update current chat ID
                        hadChat = true; // Update chat status

                        // Update receiver map
                        receiverMap.put(currentReceiverPhone, chat.getChatId());

                        populateChatList();
                        System.out.println("Chat ID: " + chat.getChatId());

                        // Avoid duplicates in chat list
                        if (!SignedUser.chatList.contains(chat.getChatId())) {
                            SignedUser.chatList.add(chat.getChatId());
                        }

                        SignedUser.saveToFile();

                        // Start polling if not already started
                        if (!isPolling) {
                            startRealTimePolling();
                        }
                    });
                }
            }
            return res;
        }).exceptionally(throwable -> {
            System.err.println("Error in async response: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });

        // 2) echo locally
        addMessageBubble(text, true);

        // 3) clear input & scroll to bottom
        messageField.clear();
        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
    }

    public void onDotsClicked() {
        if (blockUserButton != null) {
            blockUserButton.setVisible(false);
        }
        if (dotsMenu != null && dotsButton != null) {
            dotsMenu.show(dotsButton, Side.BOTTOM, 0, 0);
        }
    }

    public void onBlockUserClicked() {
        System.out.println("Blocking user...");
        try {
            // Stop polling when navigating away
            stopRealTimePolling();
            new Page().Goto(Pages.BLOCK);
        } catch (Exception e) {
            System.err.println("Error navigating to block page: " + e.getMessage());
            e.printStackTrace();
        }

        if (dotsMenu != null) {
            dotsMenu.hide();
        }
    }

    public void addMessageBubble(String text, boolean mine) {
        // Null check for text
        if (text == null) {
            text = ""; // Default to empty string
        }

        HBox messageContainer = new HBox();
        messageContainer.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        TextFlow messageBubble = new TextFlow();
        messageBubble.setPadding(new Insets(10));
        messageBubble.setStyle(mine ?
                "-fx-background-color: #0084ff; -fx-background-radius: 15 0 15 15;" :
                "-fx-background-color: #e4e6eb; -fx-background-radius: 0 15 15 15;");

        Text messageText = new Text(text);
        messageText.setStyle("-fx-fill: " + (mine ? "white" : "black") + "; -fx-font-size: 14;");
        messageBubble.getChildren().add(messageText);

        // Add timestamp
        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.setStyle("-fx-text-fill: " + (mine ? "#aad4ff" : "#666") + "; -fx-font-size: 10;");

        VBox messageContent = new VBox(5, messageBubble, timeLabel);
        messageContent.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageContent);

        // Null check for this.messageContainer
        if (this.messageContainer != null) {
            this.messageContainer.getChildren().add(messageContainer);
        }
    }

    public void populateChatList() {
        if (chatList == null) {
            System.err.println("chatList is null");
            return;
        }

        chatList.getChildren().clear();

        // Null check for SignedUser.chatList
        if (SignedUser.chatList == null) {
            System.err.println("SignedUser.chatList is null");
            return;
        }

        for (int chatId : SignedUser.chatList) {
            String phone = MapUtils.getKeyFromValue(receiverMap, chatId);

            if (phone == null) {
                System.err.println("No phone found for chatId: " + chatId);
                continue;
            }

            // Fix: Use 'phone' instead of 'currentReceiverPhone'
            User user = FindUser.findUserByPhone(allChatUser, phone);
            if (user == null) {
                System.out.println("User not found for phone: " + phone);
                continue;
            }

            System.out.println("Adding user to chat list: " + user.getName());

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setPrefWidth(334);
            hbox.setPrefHeight(72);

            // Null check for user URL
            String imageUrl = user.getUrl() != null ? user.getUrl() : "";
            ImageView avatar = new ImageView(Base64ImageHelper.getImageViewFromBase64(imageUrl));
            avatar.setFitWidth(55);
            avatar.setFitHeight(50);

            VBox vbox = new VBox();
            vbox.setPrefWidth(231);
            vbox.setPrefHeight(100);

            // Null check for user name
            String userName = user.getName() != null ? user.getName() : "Unknown User";
            Label nameLabel = new Label(userName);
            nameLabel.setPrefWidth(131);
            nameLabel.setPrefHeight(35);
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            VBox.setMargin(nameLabel, new Insets(8, 0, 0, 0));
            nameLabel.setAlignment(Pos.CENTER);

            // Get last message for preview
            String lastMessageText = "Click to message";
            Chat chat = allChats.get(chatId);
            if (chat != null && chat.getMessages() != null && !chat.getMessages().isEmpty()) {
                Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
                if (lastMessage != null && lastMessage.getMessage() != null) {
                    lastMessageText = lastMessage.getMessage().length() > 30 ?
                            lastMessage.getMessage().substring(0, 30) + "..." :
                            lastMessage.getMessage();
                }
            }

            Label messageLabel = new Label(lastMessageText);
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

    // Add cleanup method to be called when controller is destroyed
    public void cleanup() {
        stopRealTimePolling();
    }
}