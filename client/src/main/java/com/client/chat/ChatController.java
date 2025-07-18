package com.client.chat;

//import com.fasterxml.jackson.core.json.DupDetector;
import com.client.util.Page;
import com.client.util.Pages;
import com.db.User;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;            // ← JavaFX ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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


    @FXML
    private Button dotsButton;
    @FXML
    private Button blockUserButton;  // we'll still call this if you want to reuse its handler

    // our ContextMenu and MenuItem
    private ContextMenu dotsMenu;
    private MenuItem blockUserItem;

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
//        searchIcon.setPickOnBounds(false);

        // build the popup menu
        dotsMenu = new ContextMenu();
        blockUserItem = new MenuItem("Block User");
        dotsMenu.getItems().add(blockUserItem);

        // what happens when "Block User" is clicked
        blockUserItem.setOnAction(e -> {
            onBlockUserClicked();
        });

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
//        try {
//            // Load the login page
//            new Page().Goto(Pages.SETTINGS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    public void onSearchClicked(MouseEvent mouseEvent) {
        String query = searchField.getText().trim();
        if(query.length() == 11){
            query = "+88" + query;
        }
        if (mouseEvent.getSource() == searchIcon && !query.isEmpty()) {
            System.out.println("Searching for: " + query);
            try {
                User u = User.Find(query);
                System.out.println("Found user: " + u.getName());
            } catch (Exception e) {
                System.out.println("Error during search: " + e.getMessage());
            }
        }
    }

    public void onClearSearchClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getSource() == clearSearch) {
            searchField.clear();
        }
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
}

