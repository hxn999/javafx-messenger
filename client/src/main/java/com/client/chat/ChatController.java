package com.client.chat;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.event.KeyEvent;

public class ChatController {
    @FXML private TextField searchField;
    @FXML
    private VBox contactsBox;

    @FXML
    private void onSearchKeyReleased(KeyEvent e) {
        // filter logic...
    }

}

