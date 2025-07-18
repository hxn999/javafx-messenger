package com.client.chat;

//import com.fasterxml.jackson.core.json.DupDetector;
import com.api.Response;
import com.api.Sender;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.SignedUser;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ChatController {
    public Button settingsButton;

    @FXML
    private ImageView searchIcon;
    @FXML
    private TextField searchField;
    @FXML
    private ImageView clearSearch;

    @FXML
    private VBox contactsBox;

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
        if (!query.isEmpty()) {
            // TODO: replace with your real search logic
            Sender.sender.searchUser(query);

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

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                return res;
            });
        }


    }

    public void onClearSearchClicked(MouseEvent mouseEvent) {
        searchField.clear();
    }

//    public void onSearchClicked(MouseEvent mouseEvent) {
//    }
}

