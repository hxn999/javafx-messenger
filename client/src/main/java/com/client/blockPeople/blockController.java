package com.client.blockPeople;

//import com.api.Response;
import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.SignedUser;
import com.db.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
//import static com.Application.App.currentUser;
import static com.db.SignedUser.isLoggedIn;
import static java.lang.System.exit;

public class blockController implements Initializable {
    public TextField phoneNumberField;
    public Label userInfoLabel;
    public Button blockUserButton;
    public Button ChatBackBtn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


//    public void onSearchClicked() {
//        String phoneNumber = phoneNumberField.getText();
//        if(phoneNumber.length() == 11) {
//            phoneNumber = "+88" + phoneNumber;
//        }
//
//        if (isLoggedIn()) {
//            SignedUser signedUser = new SignedUser();
//            SignedUser.Load(); // Assuming Load() returns an instance of SignedUser
//            SignedUser.Save(signedUser.toString());
//
//            Sender.sender.searchUserToBlock(signedUser.getPhone(), phoneNumber);
////                Sender.sender.searchUserToBlock(phoneNumber);
//
//            // receiving the response through async function
//            CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
//                Response response = null;
//                try {
//
//                    String statusString = Sender.receive.readLine();
//
//                    response = new Response(statusString);
//
//                    if (response.statusCode == 200) {
//
//                        response.body = Sender.receive.readLine();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return response;
//            });
//
//            asyncResponse.thenApply((res) -> {
//
//                System.out.println(res);
//                if (res.statusCode != 200) {
////                    Platform.runLater(() -> showError("Invalid phone number or password"));
//                    blockUserButton.setDisable(true);
//                } else {
//                    Platform.runLater(() -> {
//                        blockUserButton.setDisable(false);
//                    });
//                }
//
//                return res;
//            });
//        }
//    }

//    public void onBlockClicked() {
//        String phoneNumber = phoneNumberField.getText();
//        if(phoneNumber.length() == 11) {
//            phoneNumber = "+88" + phoneNumber;
//        }
//        if (isLoggedIn()) {
//                SignedUser signedUser = new SignedUser();
//                SignedUser.Load(); // Assuming Load() returns an instance of SignedUser
//                List<String> blockList = signedUser.getBlocklist();
//
//            Sender.sender.searchUserToBlock(signedUser.getPhone(), phoneNumber);
////                Sender.sender.searchUserToBlock(phoneNumber);
//
//            // receiving the response through async function
//            CompletableFuture<Response> asyncResponse = CompletableFuture.supplyAsync(() -> {
//                Response response = null;
//                try {
//
//                    String statusString = Sender.receive.readLine();
//
//                    response = new Response(statusString);
//
//                    if (response.statusCode == 200) {
//
//                        response.body = Sender.receive.readLine();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return response;
//            });
//
//            String finalPhoneNumber = phoneNumber;
//            asyncResponse.thenApply((res) -> {
//
//                System.out.println(res);
//                if (res.statusCode != 200) {
////                    Platform.runLater(() -> showError("Invalid phone number or password"));
//                    blockUserButton.setDisable(true);
//                } else {
//                    Platform.runLater(() -> {
//                        blockUserButton.setDisable(false);
//                        if (Page.isValidBDNumber(finalPhoneNumber)) {
//                            blockList.add(finalPhoneNumber);
//
//                        }
//                    });
//                }
//
//                return res;
//            });
//
//                signedUser.setBlocklist(blockList);
//                SignedUser.Save(signedUser.toString());
//
//        }
//    }
//

    public void ChatBackHandler(ActionEvent actionEvent) {
            try {
                new Page().Goto(Pages.CHAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}
