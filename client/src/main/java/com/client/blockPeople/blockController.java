//package com.client.blockPeople;
//
//import com.server.Response;
//import com.api.Sender;
//import com.client.util.Page;
//import com.client.util.Pages;
//import com.db.SignedUser;
//import com.db.User;
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.ResourceBundle;
//import java.util.concurrent.CompletableFuture;
//
//import static com.api.Sender.receive;
//import static com.db.SignedUser.isLoggedIn;
//
//public class blockController implements Initializable {
//    public TextField phoneNumberField;
//    public Label userInfoLabel;
//    public Button blockUserButton;
//    public Button ChatBackBtn;
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        // Initially disable block button until a user is found
//        blockUserButton.setDisable(true);
//    }
//
//    public void onSearchClicked() {
//        String phoneNumber = phoneNumberField.getText().trim();
//        if (phoneNumber.length() == 11) {
//            phoneNumber = "+88" + phoneNumber;
//        }
//
//        if (!isLoggedIn()) {
//            userInfoLabel.setText("Please log in first.");
//            return;
//        }
//
//        // Send block-search request to server
//        Sender.sender.searchforblocking(SignedUser.phone, phoneNumber);
//
//        CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
//            Response response = null;
//            try {
//                try {
//                    response = (Response) Sender.receive.readObject();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
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
//
//            System.out.println(res);
//            if (res.getStatusCode() != 200) {
//                blockUserButton.setDisable(true);
//            } else {
//                Platform.runLater(() -> {
//                    try {
//                        SignedUser.save((User)res.getBody());
//                        System.out.println("This is the body : "+ res.getBody());
//                        blockUserButton.setDisable(false);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//
//            return res;
//        });
//
//    }
//
//    public void onBlockClicked() {
//        String phoneNumber = phoneNumberField.getText().trim();
//        if (phoneNumber.length() == 11) {
//            phoneNumber = "+88" + phoneNumber;
//        }
//
//        // Send block request
//        Sender.sender.block(phoneNumber);
//        CompletableFuture.supplyAsync(() -> {
//            try {
//                Object obj = receive.readObject();
//                return (Response) obj;
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }).thenAccept(res -> {
//            Platform.runLater(() -> {
//                if (res == null || res.getStatusCode() != 200) {
//                    userInfoLabel.setText("Failed to block user.");
//                } else {
//                    try {
//                        SignedUser.save((User) res.getBody());
//                        userInfoLabel.setText("User blocked successfully.");
//                        blockUserButton.setDisable(true);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        });
//    }
//
//    public void ChatBackHandler(ActionEvent actionEvent) {
//        try {
//            new Page().Goto(Pages.CHAT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
