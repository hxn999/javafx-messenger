package com.client.chat;

import com.api.Data;
import com.api.Receiver;
import com.api.ResponseManager;
import com.api.Sender;
import com.client.util.*;
import com.db.*;
import com.server.Response;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
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
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private Button blockUserButton;

    @FXML
    private ImageView userImage;
    @FXML
    private Label userName;

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
    public ScrollPane messageScrollPane;
    @FXML
    private VBox messageContainer;
    @FXML
    private ImageView headerAvatar;
    @FXML
    private Label headerNameLabel;
    @FXML
    private Button sendButton;
    @FXML
    private Label receiverName;
    @FXML
    private ImageView receiverImage;

    // maps receiver phone to chat id
    public static Map<String,Integer> receiverMap;
    boolean hadChat = false;
    public static Integer currentChatId=null;
    private String currentReceiverPhone;
    public static List<User> allChatUser;

    private Receiver receiver;
    public static Map<Integer, Chat> allChats = new HashMap<>();

    // Global polling mechanism
    private ScheduledExecutorService globalPollingService;
    private volatile boolean isGlobalPollingActive = false;

    // Store the last message count for each chat to detect new messages
    private Map<Integer, Integer> lastMessageCount = new HashMap<>();

    // Method to handle rapid successive messages
    private final Map<String, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private final long UPDATE_THROTTLE_MS = 100; // Minimum time between updates

    public static void addOrUpdateUser(User user) {
        for (int i = 0; i < allChatUser.size(); i++) {
            if (allChatUser.get(i).getPhone().equals(user.getPhone())) {
                allChatUser.set(i, user);
                return;
            }
        }
        allChatUser.add(user);
    }

    public static Optional<User> findUser(String phone) {
        return allChatUser.stream()
                .filter(u -> u.getPhone().equals(phone))
                .findFirst();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allChatUser = new ArrayList<>();

        settingsButton.setOnAction(this::onSettingsClicked);

        Receiver.processPendingMessagesStatic(this);

        clearSearch.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !searchField.getText().trim().isEmpty(),
                        searchField.textProperty()
                )
        );

        clearSearch.managedProperty().bind(clearSearch.visibleProperty());
        clearSearch.visibleProperty().bind(searchField.textProperty().isNotEmpty());
        clearSearch.managedProperty().bind(clearSearch.visibleProperty());
        clearSearch.mouseTransparentProperty().bind(clearSearch.visibleProperty().not());
        clearSearch.setPickOnBounds(false);

        // Build the popup menu
        dotsMenu = new ContextMenu();
        blockUserItem = new MenuItem("Block User");
        dotsMenu.getItems().add(blockUserItem);

        blockUserItem.setOnAction(e -> {
            onBlockUserClicked();
        });

        userName.setText(SignedUser.name);
        userImage.setImage(Base64ImageHelper.getImageViewFromBase64(SignedUser.url));

        receiverMap = new HashMap<>();

        Platform.runLater(() -> {
            // Small delay to ensure everything is fully loaded
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Process pending messages
            try {
                Receiver.processPendingMessagesStatic(this);
                System.out.println("✅ ChatController ready for real-time messages");
            } catch (Exception e) {
                System.err.println("❌ Error processing pending messages: " + e.getMessage());
            }
        });

        // Load all chats
        loadAllChats();
    }

    private void loadAllChats() {
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (int chatId : SignedUser.chatList) {
            String requestId = UUID.randomUUID().toString();
            CompletableFuture<Response> future = new CompletableFuture<>();
            ResponseManager.register(requestId, future);

            CompletableFuture<Response> chainedFuture = future.thenCompose(res -> {
                if (res.getStatusCode() == 200) {
                    Chat chat = (Chat) res.getBody();
                    allChats.put(chatId, chat);

                    // Initialize message count tracking
                    lastMessageCount.put(chatId, chat.getMessages() != null ? chat.getMessages().size() : 0);

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

        // Start global polling after all chats are loaded
        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Platform.runLater(() -> {
                    populateChatList();
                    startGlobalMessagePolling(); // Start global polling here
                }));
    }

    /**
     * Start global message polling that checks all chats for new messages
     */
    private void startGlobalMessagePolling() {
        if (isGlobalPollingActive) return;

        isGlobalPollingActive = true;
        globalPollingService = Executors.newSingleThreadScheduledExecutor();

        globalPollingService.scheduleAtFixedRate(() -> {
            try {
                checkAllChatsForNewMessages();
            } catch (Exception e) {
                System.err.println("Error in global polling: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.SECONDS); // Poll every 3 seconds

        System.out.println("🔄 Global message polling started");
    }

    /**
     * Check all chats for new messages
     */
    private void checkAllChatsForNewMessages() {
        if (SignedUser.chatList == null || SignedUser.chatList.isEmpty()) {
            return;
        }

        for (Integer chatId : SignedUser.chatList) {
            checkChatForNewMessages(chatId);
        }
    }

    /**
     * Check a specific chat for new messages
     */
    private void checkChatForNewMessages(int chatId) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseManager.register(requestId, future);

        Sender.sender.fetchChat(chatId, requestId);

        future.thenAccept(res -> {
            if (res.getStatusCode() == 200) {
                Chat updatedChat = (Chat) res.getBody();
                if (updatedChat != null) {
                    Chat currentChat = allChats.get(chatId);

                    // Check if there are new messages
                    if (hasNewMessages(chatId, updatedChat)) {
                        allChats.put(chatId, updatedChat);

                        // Update message count
                        lastMessageCount.put(chatId, updatedChat.getMessages() != null ?
                                updatedChat.getMessages().size() : 0);

                        // Update UI on JavaFX thread
                        Platform.runLater(() -> {
                            // If this is the currently active chat, update the message view
                            if (currentChatId != null && currentChatId.equals(chatId)) {
                                populateChat(updatedChat);
                                messageScrollPane.setVvalue(1.0);
                            }

                            // Always update the chat list to show latest message preview
                            populateChatList();

                            // Show notification for new messages (optional)
                            if (currentChatId == null || !currentChatId.equals(chatId)) {
                                showNewMessageNotification(chatId, updatedChat);
                            }
                        });
                    }
                }
            }
        }).exceptionally(throwable -> {
            System.err.println("Error checking chat " + chatId + " for new messages: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Enhanced handleIncomingMessage method with immediate UI updates
     */
    public void handleIncomingMessage(Message incomingMessage) {
        if (incomingMessage == null) {
            System.err.println("Received null message");
            return;
        }

        String senderPhone = incomingMessage.getSender();
        if (senderPhone == null || senderPhone.trim().isEmpty()) {
            System.err.println("Sender phone is null or empty");
            return;
        }

        System.out.println("🚀 IMMEDIATE handling of incoming message from: " + senderPhone);

        // IMMEDIATE UI update - show sender name at top of chat list
        Platform.runLater(() -> {
            // First, ensure the sender appears in the chat list immediately
            immediatelyShowSenderInChatList(senderPhone, incomingMessage);
        });

        // Check if this is a new chat or existing chat
        Integer existingChatId = receiverMap.get(senderPhone);

        if (existingChatId == null) {
            // This is a new chat - handle it and immediately update UI
            handleNewChatFromIncomingMessage(incomingMessage);
        } else {
            // Existing chat - update it and immediately refresh UI
            updateExistingChatWithMessage(existingChatId, incomingMessage);
        }
    }

    // Immediately show sender in chat list before full chat loading
    private void immediatelyShowSenderInChatList(String senderPhone, Message message) {
        try {
            // Check if sender is already in our user list
            Optional<User> existingSender = ChatController.allChatUser.stream()
                    .filter(user -> user.getPhone().equals(senderPhone))
                    .findFirst();

            if (existingSender.isPresent()) {
                // User exists, immediately update the chat list
                User sender = existingSender.get();

                // Create or update a temporary chat entry for immediate display
                createTemporaryChatEntry(sender, message);

                System.out.println("⚡ IMMEDIATE display update for existing user: " + sender.getName());
            } else {
                // User doesn't exist, fetch info and then update
                fetchSenderInfoAndUpdateImmediately(senderPhone, message);
            }

        } catch (Exception e) {
            System.err.println("Error in immediate sender display: " + e.getMessage());
        }
    }

    // Create temporary chat entry for immediate display
    private void createTemporaryChatEntry(User sender, Message message) {
        Platform.runLater(() -> {
            try {
                // Check if this sender already has a chat entry
                Integer existingChatId = receiverMap.get(sender.getPhone());

                if (existingChatId != null) {
                    // Move existing chat to top
                    if (SignedUser.chatList.contains(existingChatId)) {
                        SignedUser.chatList.remove(Integer.valueOf(existingChatId));
                        SignedUser.chatList.add(0, existingChatId);
                    }
                }

                // Immediately refresh the chat list to show the sender at the top
                populateChatList();

                // Highlight the sender's chat entry
                highlightSenderInChatList(sender.getPhone());

                System.out.println("✨ Temporary chat entry created for: " + sender.getName());

            } catch (Exception e) {
                System.err.println("Error creating temporary chat entry: " + e.getMessage());
            }
        });
    }

    // Fetch sender info and update immediately
    private void fetchSenderInfoAndUpdateImmediately(String senderPhone, Message message) {
        String userRequestId = UUID.randomUUID().toString();
        CompletableFuture<Response> userFuture = new CompletableFuture<>();
        ResponseManager.register(userRequestId, userFuture);

        Sender.sender.searchSingle(senderPhone, userRequestId);

        userFuture.thenAccept(userRes -> {
            if (userRes.getStatusCode() == 200) {
                User senderUser = (User) userRes.getBody();
                if (senderUser != null) {
                    // Add sender to our user list
                    addOrUpdateUser(senderUser);

                    // Immediately update the chat list
                    Platform.runLater(() -> {
                        createTemporaryChatEntry(senderUser, message);
                        System.out.println("⚡ IMMEDIATE display after fetching user: " + senderUser.getName());
                    });
                }
            }
        }).exceptionally(throwable -> {
            System.err.println("Error fetching sender for immediate display: " + throwable.getMessage());

            // Even if we can't fetch user info, show something
            return null;
        });
    }


    // Highlight specific sender in chat list
    private void highlightSenderInChatList(String senderPhone) {
        Platform.runLater(() -> {
            try {
                for (Node node : chatList.getChildren()) {
                    if (node instanceof HBox) {
                        HBox chatItem = (HBox) node;
                        if (senderPhone.equals(chatItem.getId())) {
                            // Apply highlighting
                            chatItem.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2px; -fx-border-radius: 5px;");

                            // Add pulsing animation for attention
                            addPulsingAnimation(chatItem);

                            // Remove highlighting after 5 seconds
                            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                                chatItem.setStyle("");
                            }));
                            timeline.play();

                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error highlighting sender: " + e.getMessage());
            }
        });
    }

    // Add pulsing animation to draw attention
    private void addPulsingAnimation(HBox chatItem) {
        try {
            // Create scale transition for pulsing effect
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), chatItem);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.setCycleCount(4); // Pulse 4 times
            scaleTransition.setAutoReverse(true);

            scaleTransition.play();

        } catch (Exception e) {
            System.err.println("Error adding pulse animation: " + e.getMessage());
        }
    }

    private void handleNewChatFromIncomingMessage(Message incomingMessage) {
        String senderPhone = incomingMessage.getSender();

        // IMMEDIATELY show something in the UI
        Platform.runLater(() -> {
            System.out.println("⚡ IMMEDIATE new chat indicator shown");
        });

        // First, fetch sender's user information
        String userRequestId = UUID.randomUUID().toString();
        CompletableFuture<Response> userFuture = new CompletableFuture<>();
        ResponseManager.register(userRequestId, userFuture);

        Sender.sender.searchSingle(senderPhone, userRequestId);

        userFuture.thenAccept(userRes -> {
            if (userRes.getStatusCode() == 200) {
                User senderUser = (User) userRes.getBody();
                if (senderUser != null) {
                    // Update user info and refresh UI immediately
                    addOrUpdateUser(senderUser);

                    Platform.runLater(() -> {
                        populateChatList();
                        highlightSenderInChatList(senderPhone);
                        System.out.println("⚡ UI updated with sender info: " + senderUser.getName());
                    });

                    // Now fetch the chat information
                    fetchChatForNewMessage(incomingMessage, senderUser);
                } else {
                    System.err.println("Failed to fetch sender user information");
                }
            } else {
                System.err.println("Failed to search for sender: " + userRes.getStatusCode());
            }
        }).exceptionally(throwable -> {
            System.err.println("Error fetching sender user: " + throwable.getMessage());
            return null;
        });
    }

    private void fetchChatForNewMessage(Message incomingMessage, User senderUser) {
        // If the message has a chatId, use it to fetch the chat
        if (incomingMessage.getChatId() > 0) {
            int chatId = incomingMessage.getChatId();

            String chatRequestId = UUID.randomUUID().toString();
            CompletableFuture<Response> chatFuture = new CompletableFuture<>();
            ResponseManager.register(chatRequestId, chatFuture);

            Sender.sender.fetchChat(chatId, chatRequestId);

            chatFuture.thenAccept(chatRes -> {
                if (chatRes.getStatusCode() == 200) {
                    Chat chat = (Chat) chatRes.getBody();
                    if (chat != null) {
                        // Add this chat to our collections
                        Platform.runLater(() -> {
                            addNewChatToUI(chat, senderUser);
                        });
                    }
                } else {
                    System.err.println("Failed to fetch chat: " + chatRes.getStatusCode());
                }
            }).exceptionally(throwable -> {
                System.err.println("Error fetching chat: " + throwable.getMessage());
                return null;
            });
        } else {
            // If no chatId, this might be a brand new conversation
            Platform.runLater(() -> {
                createTemporaryChatForNewMessage(incomingMessage, senderUser);
            });
        }
    }

    private void addNewChatToUI(Chat chat, User senderUser) {
        if (chat == null || senderUser == null) {
            return;
        }

        int chatId = chat.getChatId();
        String senderPhone = senderUser.getPhone();

        // Add to our collections
        allChats.put(chatId, chat);
        receiverMap.put(senderPhone, chatId);

        // Initialize message count tracking
        lastMessageCount.put(chatId, chat.getMessages() != null ? chat.getMessages().size() : 0);

        // Add to SignedUser's chat list if not already present
        if (!SignedUser.chatList.contains(chatId)) {
            SignedUser.chatList.add(0, chatId); // Add at the beginning for recent chats
            SignedUser.saveToFile(); // Save the updated chat list
        }

        // Update the UI
        populateChatList();

        // Show notification if this chat is not currently active
        if (currentChatId == null || !currentChatId.equals(chatId)) {
            showNewMessageNotification(chatId, chat);
        }

        System.out.println("✅ New chat added from " + senderUser.getName() + " (" + senderPhone + ")");
    }

    private void createTemporaryChatForNewMessage(Message incomingMessage, User senderUser) {
        System.out.println("📝 Creating temporary chat entry for message from " + senderUser.getName());
        refreshUserChatList();
    }

    private void refreshUserChatList() {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> future = new CompletableFuture<>();
        ResponseManager.register(requestId, future);

        future.thenAccept(res -> {
            if (res.getStatusCode() == 200) {
                Platform.runLater(() -> {
                    loadAllChats();
                });
            }
        });
    }

    private void updateExistingChatWithMessage(int chatId, Message incomingMessage) {
        // IMMEDIATELY move chat to top of list and refresh UI
        Platform.runLater(() -> {
            if (SignedUser.chatList.contains(chatId)) {
                SignedUser.chatList.remove(Integer.valueOf(chatId));
                SignedUser.chatList.add(0, chatId);
                SignedUser.saveToFile();
            }

            // Immediate UI refresh
            populateChatList();

            // Highlight the chat
            String senderPhone = incomingMessage.getSender();
            highlightSenderInChatList(senderPhone);

            System.out.println("⚡ IMMEDIATE chat list update for existing chat: " + chatId);
        });

        // Then do the full chat update
        String chatRequestId = UUID.randomUUID().toString();
        CompletableFuture<Response> chatFuture = new CompletableFuture<>();
        ResponseManager.register(chatRequestId, chatFuture);

        Sender.sender.fetchChat(chatId, chatRequestId);

        chatFuture.thenAccept(chatRes -> {
            if (chatRes.getStatusCode() == 200) {
                Chat updatedChat = (Chat) chatRes.getBody();
                if (updatedChat != null) {
                    Platform.runLater(() -> {
                        // Update the chat
                        allChats.put(chatId, updatedChat);

                        // Update message count
                        lastMessageCount.put(chatId, updatedChat.getMessages() != null ?
                                updatedChat.getMessages().size() : 0);

                        // Final UI update with complete data
                        populateChatList();

                        // If this is the currently active chat, update the message view
                        if (currentChatId != null && currentChatId.equals(chatId)) {
                            populateChat(updatedChat);
                            messageScrollPane.setVvalue(1.0);
                        } else {
                            // Show notification for new message
                            showNewMessageNotification(chatId, updatedChat);
                        }

                        System.out.println("✅ Full chat update completed for: " + chatId);
                    });
                }
            }
        });
    }

    private void sortChatsByMostRecent() {
        if (SignedUser.chatList == null || SignedUser.chatList.size() <= 1) {
            return;
        }

        SignedUser.chatList.sort((chatId1, chatId2) -> {
            Chat chat1 = allChats.get(chatId1);
            Chat chat2 = allChats.get(chatId2);

            if (chat1 == null || chat2 == null) return 0;

            long timestamp1 = getLastMessageTimestamp(chat1);
            long timestamp2 = getLastMessageTimestamp(chat2);

            return Long.compare(timestamp2, timestamp1);
        });
    }

    private long getLastMessageTimestamp(Chat chat) {
        if (chat.getMessages() == null || chat.getMessages().isEmpty()) {
            return 0;
        }

        Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
        return lastMessage.getTimestamp();
    }

    private boolean hasUnreadMessages(int chatId) {
        return false;
    }

    private boolean hasNewMessages(int chatId, Chat newChat) {
        Integer lastCount = lastMessageCount.get(chatId);
        if (lastCount == null) {
            lastCount = 0;
        }

        int newCount = newChat.getMessages() != null ? newChat.getMessages().size() : 0;
        return newCount > lastCount;
    }

    // Check if chat has recent activity (within last 5 minutes)
    private boolean hasRecentActivity(Chat chat) {
        if (chat == null || chat.getMessages() == null || chat.getMessages().isEmpty()) {
            return false;
        }

        Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
        long currentTime = System.currentTimeMillis() / 1000L;
        long messageTime = lastMessage.getTimestamp();

        // Consider activity recent if within last 5 minutes (300 seconds)
        return (currentTime - messageTime) < 300;
    }

    // Get formatted last message with sender indication
    private String getFormattedLastMessage(Chat chat, User user) {
        if (chat == null || chat.getMessages() == null || chat.getMessages().isEmpty()) {
            return "Click to message";
        }

        Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
        if (lastMessage == null || lastMessage.getMessage() == null) {
            return "Click to message";
        }

        String messageText = lastMessage.getMessage();
        boolean isFromCurrentUser = lastMessage.getSender().equals(SignedUser.phone);

        String prefix = isFromCurrentUser ? "You: " : user.getName() + ": ";
        String truncatedMessage = messageText.length() > 25 ?
                messageText.substring(0, 25) + "..." : messageText;

        return prefix + truncatedMessage;
    }

    // Get time label for last message
    private Label getLastMessageTimeLabel(Chat chat) {
        if (chat == null || chat.getMessages() == null || chat.getMessages().isEmpty()) {
            return null;
        }

        Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
        if (lastMessage == null) {
            return null;
        }

        long timestamp = lastMessage.getTimestamp();
        String timeText = formatTimestamp(timestamp);

        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 10px;");
        timeLabel.setAlignment(Pos.CENTER_RIGHT);

        return timeLabel;
    }

    // Format timestamp for display
    private String formatTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis() / 1000L;
        long diff = currentTime - timestamp;

        if (diff < 60) {
            return "now";
        } else if (diff < 3600) {
            return (diff / 60) + "m ago";
        } else if (diff < 86400) {
            return (diff / 3600) + "h ago";
        } else {
            return (diff / 86400) + "d ago";
        }
    }

    private void showNewMessageNotification(int chatId, Chat chat) {
        try {
            String receiverPhone = ReceiverPhone.get(chat);
            User sender = FindUser.findUserByPhone(allChatUser, receiverPhone);

            if (sender != null && chat.getMessages() != null && !chat.getMessages().isEmpty()) {
                Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
                System.out.println("🔔 New message from " + sender.getName() + ": " + lastMessage.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error showing notification: " + e.getMessage());
        }
    }

    private void stopGlobalMessagePolling() {
        if (globalPollingService != null && !globalPollingService.isShutdown()) {
            globalPollingService.shutdown();
            try {
                if (!globalPollingService.awaitTermination(2, TimeUnit.SECONDS)) {
                    globalPollingService.shutdownNow();
                }
            } catch (InterruptedException e) {
                globalPollingService.shutdownNow();
            }
        }
        isGlobalPollingActive = false;
        System.out.println("🛑 Global message polling stopped");
    }

    @FXML
    private void onSettingsClicked(ActionEvent event) {
        try {
            stopGlobalMessagePolling();
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
            String requestId = UUID.randomUUID().toString();
            CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
            ResponseManager.register(requestId, asyncResponse);

            Sender.sender.searchUser(query, requestId);

            asyncResponse.thenApply((res) -> {
                if (res.getStatusCode() == 200) {
                    Platform.runLater(() -> {
                        showSearchResults((List<User>) res.getBody());
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
                startChat(event, user);
            });
            chatList.getChildren().add(hbox);
        }
    }

    public void startChat(MouseEvent mouseEvent, User user) {
        if (user == null) {
            System.err.println("User is null in startChat");
            return;
        }

        receiverImage.setImage(Base64ImageHelper.getImageViewFromBase64(user.getUrl()));
        receiverName.setText(user.getName());
        currentReceiverPhone = user.getPhone();
        Integer chatId = receiverMap.get(user.getPhone());

        messageContainer.getChildren().clear();

        if (chatId != null) {
            hadChat = true;
            currentChatId = chatId;
            Chat chat = allChats.get(chatId);
            if (chat != null) {
                populateChat(chat);
            }
        } else {
            hadChat = false;
            currentChatId = null;
        }

        populateChatList();
    }

    public void populateChat(Chat chat) {
        if (chat == null) {
            System.err.println("Chat is null in populateChat");
            return;
        }

        // Only clear if this is a completely new chat or different chat
        if (currentChatId == null || !currentChatId.equals(chat.getChatId())) {
            messageContainer.getChildren().clear();
        }

        Set<String> existingMessages = new HashSet<>();

        // Collect existing message keys to avoid duplicates
        if (!messageContainer.getChildren().isEmpty()) {
            // Create a way to identify existing messages (you might need to store message IDs)
            for (Node node : messageContainer.getChildren()) {
                if (node.getId() != null) {
                    existingMessages.add(node.getId());
                }
            }
        }

        if (chat.getMessages() != null) {
            for (Message msg : chat.getMessages()) {
                if (msg == null) continue;

                String msgKey = (msg.getSender() != null ? msg.getSender() : "") +
                        msg.getTimestamp() +
                        (msg.getMessage() != null ? msg.getMessage() : "");

                // Only add message if it doesn't already exist
                if (!existingMessages.contains(msgKey)) {
                    boolean mine = msg.getSender() != null && msg.getSender().equals(SignedUser.phone);
                    HBox messageContainer = createMessageBubble(msg.getMessage(), mine);
                    messageContainer.setId(msgKey); // Set ID for future reference
                    this.messageContainer.getChildren().add(messageContainer);
                    existingMessages.add(msgKey);
                }
            }
        }

        Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
    }

    @FXML
    public void onSendClicked(ActionEvent event) {
        if (currentReceiverPhone == null || currentReceiverPhone.trim().isEmpty()) {
            System.err.println("No receiver selected");
            return;
        }

        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        if (SignedUser.phone == null) {
            System.err.println("SignedUser.phone is null");
            return;
        }

        Message msg = new Message(SignedUser.phone, currentReceiverPhone, System.currentTimeMillis() / 1000L, text);

        if (!hadChat) {
            msg.setFirstMsg(true);
        } else {
            msg.setChatId(currentChatId);
        }

        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Response> asyncResponse = new CompletableFuture<>();
        ResponseManager.register(requestId, asyncResponse);

        Sender.sender.sendMessage(msg, requestId);

        asyncResponse.thenApply((res) -> {
            if (res != null && res.getStatusCode() == 200) {
                Chat chat = (Chat) res.getBody();
                if (chat != null) {
                    Platform.runLater(() -> {
                        allChats.put(chat.getChatId(), chat);
                        currentChatId = chat.getChatId();
                        hadChat = true;

                        // Update message count for this chat
                        lastMessageCount.put(chat.getChatId(), chat.getMessages() != null ?
                                chat.getMessages().size() : 0);

                        receiverMap.put(currentReceiverPhone, chat.getChatId());
                        populateChatList();

                        if (!SignedUser.chatList.contains(chat.getChatId())) {
                            SignedUser.chatList.add(chat.getChatId());
                        }

                        SignedUser.saveToFile();
                    });
                }
            }
            return res;
        });

        addMessageBubble(text, true);
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

    private HBox createMessageBubble(String text, boolean mine) {
        if (text == null) {
            text = "";
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

        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.setStyle("-fx-text-fill: " + (mine ? "#aad4ff" : "#666") + "; -fx-font-size: 10;");

        VBox messageContent = new VBox(5, messageBubble, timeLabel);
        messageContent.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageContainer.getChildren().add(messageContent);

        return messageContainer;
    }

    public void addMessageBubble(String text, boolean mine) {
        HBox messageContainer = createMessageBubble(text, mine);

        if (this.messageContainer != null) {
            this.messageContainer.getChildren().add(messageContainer);
        }
    }

    public void onBlockUserClicked() {
        System.out.println("Blocking user...");
        try {
            stopGlobalMessagePolling();
            new Page().Goto(Pages.BLOCK);
        } catch (Exception e) {
            System.err.println("Error navigating to block page: " + e.getMessage());
            e.printStackTrace();
        }

        if (dotsMenu != null) {
            dotsMenu.hide();
        }
    }

    // Enhanced populateChatList method with better real-time updates
    public void populateChatList() {
        if (chatList == null) {
            System.err.println("chatList is null");
            return;
        }

        Platform.runLater(() -> {
            chatList.getChildren().clear();

            if (SignedUser.chatList == null) {
                System.err.println("SignedUser.chatList is null");
                return;
            }

            // Sort chats by most recent activity
            sortChatsByMostRecent();

            for (int chatId : SignedUser.chatList) {
                String phone = MapUtils.getKeyFromValue(receiverMap, chatId);

                if (phone == null) {
                    System.err.println("No phone found for chatId: " + chatId);
                    continue;
                }

                User user = FindUser.findUserByPhone(allChatUser, phone);
                if (user == null) {
                    System.out.println("User not found for phone: " + phone);
                    continue;
                }

                HBox hbox = createChatListItem(user, chatId);
                chatList.getChildren().add(hbox);
            }
        });
    }

    // Create individual chat list item with enhanced styling
    private HBox createChatListItem(User user, int chatId) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setPrefWidth(334);
        hbox.setPrefHeight(72);
        hbox.setId(user.getPhone());

        // Avatar
        String imageUrl = user.getUrl() != null ? user.getUrl() : "";
        ImageView avatar = new ImageView(Base64ImageHelper.getImageViewFromBase64(imageUrl));
        avatar.setFitWidth(55);
        avatar.setFitHeight(50);

        // Content VBox
        VBox vbox = new VBox();
        vbox.setPrefWidth(231);
        vbox.setPrefHeight(100);

        // User name with prominence for new messages
        String userName = user.getName() != null ? user.getName() : "Unknown User";
        Label nameLabel = new Label(userName);
        nameLabel.setPrefWidth(200);
        nameLabel.setPrefHeight(35);

        // Check if this chat has recent activity (within last 5 minutes)
        Chat chat = allChats.get(chatId);
        boolean hasRecentActivity = hasRecentActivity(chat);

        if (hasRecentActivity) {
            nameLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 17));
            nameLabel.setStyle("-fx-text-fill: #1976d2;"); // Blue color for active chats
        } else {
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            nameLabel.setStyle("-fx-text-fill: #000000;");
        }

        VBox.setMargin(nameLabel, new Insets(8, 0, 0, 0));
        nameLabel.setAlignment(Pos.CENTER_LEFT);

        // Last message with sender info
        String lastMessageText = getFormattedLastMessage(chat, user);
        Label messageLabel = new Label(lastMessageText);
        messageLabel.setPrefWidth(227);
        messageLabel.setPrefHeight(18);

        if (hasRecentActivity) {
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            messageLabel.setStyle("-fx-text-fill: #1976d2;");
        } else {
            messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
            messageLabel.setStyle("-fx-text-fill: #666666;");
        }

        VBox.setMargin(messageLabel, new Insets(0, 0, 0, 0));
        messageLabel.setPadding(new Insets(0, 0, 0, 15));
        messageLabel.setAlignment(Pos.TOP_LEFT);

        // Time label for last message
        Label timeLabel = getLastMessageTimeLabel(chat);
        if (timeLabel != null) {
            VBox.setMargin(timeLabel, new Insets(0, 0, 0, 15));
            vbox.getChildren().addAll(nameLabel, messageLabel, timeLabel);
        } else {
            vbox.getChildren().addAll(nameLabel, messageLabel);
        }

        hbox.getChildren().addAll(avatar, vbox);
        hbox.setOnMouseClicked(event -> {
            // Remove new message indicators when chat is opened
            removeNewMessageIndicators(hbox);
            startChat(event, user);
        });

        // Add visual emphasis for recent activity
        if (hasRecentActivity) {
            hbox.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        }

        return hbox;
    }

    // Remove new message indicators
    private void removeNewMessageIndicators(HBox chatItem) {
        chatItem.getChildren().removeIf(node ->
                node.getId() != null && node.getId().equals("newMessageIndicator"));
        chatItem.setStyle(""); // Remove any highlighting
    }

    // Enhanced method to ensure real-time responsiveness
    public void forceImmediateChatListUpdate() {
        Platform.runLater(() -> {
            try {
                populateChatList();
                System.out.println("🔄 Forced immediate chat list update");
            } catch (Exception e) {
                System.err.println("Error in forced update: " + e.getMessage());
            }
        });
    }

    // Method to handle rapid successive messages with throttling
    public void throttledChatListUpdate(String senderPhone) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTime.get(senderPhone);

        if (lastUpdate == null || (currentTime - lastUpdate) > UPDATE_THROTTLE_MS) {
            lastUpdateTime.put(senderPhone, currentTime);

            Platform.runLater(() -> {
                populateChatList();
                highlightSenderInChatList(senderPhone);
            });
        }
    }

    /**
     * Cleanup method - call this when the controller is destroyed
     */
    public void cleanup() {
        stopGlobalMessagePolling();
        if (receiver != null) {
            receiver.stopReceiver();
        }
    }
}