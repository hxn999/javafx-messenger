package com.client.chat;

import com.fasterxml.jackson.core.json.DupDetector;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;         // ← JavaFX KeyEvent
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatController {
    public Button settingsButton;
    @FXML
    private TextField searchField;

    @FXML
    private VBox contactsBox;

    /**
     * This must be annotated @FXML so FXMLLoader sees it.
     */
    @FXML
    private void initialize() {
        // optional setup after FXML is loaded
        settingsButton.setOnAction(this::onSettingsClicked);

    }

    /**
     * Use javafx.scene.input.KeyEvent, not AWT KeyEvent
     */
    @FXML
    private void onSearchKeyReleased(KeyEvent e) {
        // Your filter logic…
        String text = searchField.getText().toLowerCase();
        contactsBox.getChildren().stream()
                .filter(node -> {
                    // assume each child has a Label you can inspect...
                    return node.lookup(".label").lookupAll(".text").toString().toLowerCase().contains(text);
                })
                .forEach(node -> node.setVisible(true));
        // hide non-matching ones similarly…
    }

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
}


