package com.client.blockPeople;

import com.db.User;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class blockController implements Initializable {
    public TextField phoneNumberField;
    public Label userInfoLabel;
    public Button blockUserButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    public void onSearchClicked() {
        String phoneNumber = phoneNumberField.getText();
        if(phoneNumber.length() == 11) {
            phoneNumber = "+88" + phoneNumber;
        }
        try {
            User toBlock = User.Find(phoneNumber);
            blockUserButton.setDisable(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onBlockClicked() {
        String phoneNumber = phoneNumberField.getText();
        if(phoneNumber.length() == 11) {
            phoneNumber = "+88" + phoneNumber;
        }
        User toBlock = null;
        try {
            toBlock = User.Find(phoneNumber);
            blockUserButton.setDisable(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<User> blockList = toBlock.getBlocklist();
        blockList.add(toBlock);
        toBlock.setBlocklist(blockList);
    }


}
