package com.client.blockPeople;

import com.api.Response;
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


    public void onSearchClicked() {
        String phoneNumber = phoneNumberField.getText();
        if(phoneNumber.length() == 11) {
            phoneNumber = "+88" + phoneNumber;
        }
                // TODO: replace with your real search logic
                Sender.sender.searchUser(phoneNumber);

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

//
                                blockUserButton.setDisable(false);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    return res;
                });

    }

    public void onBlockClicked() {
        String phoneNumber = phoneNumberField.getText();
        if(phoneNumber.length() == 11) {
            phoneNumber = "+88" + phoneNumber;
        }
        User toBlock = null;
        try {
            toBlock = User.Find(phoneNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (isLoggedIn()) {
                SignedUser signedUser = new SignedUser();
                SignedUser.Load(); // Assuming Load() returns an instance of SignedUser
                List<String> blockList = signedUser.getBlocklist();
                blockList.add(String.valueOf(toBlock));
                signedUser.setBlocklist(blockList);
                SignedUser.Save(signedUser.toString());
            }
    }


    public void ChatBackHandler(ActionEvent actionEvent) {
            try {
                new Page().Goto(Pages.CHAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
