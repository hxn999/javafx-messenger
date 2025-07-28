//package com.client.Settings.AccountDetails;
//
//import com.client.login.LoginController;
//import com.client.util.Page;
//import com.controller.ClientHandler;
//import com.db.SignedUser;
//import com.db.User;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.fxml.Initializable;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.KeyCode;
//import javafx.scene.shape.Circle;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ResourceBundle;
//
////import static com.Application.App.currentUser;
//
//import static com.db.SignedUser.isLoggedIn;
//
//public class AccountDetailsController implements Initializable {
//    public Circle clipCircle;
//
//    @FXML
//    private TextField phoneField;
//
//    @FXML
//    private TextField usernameField;
//
//    @FXML
//    public Button saveButton;
//    @FXML
//    public Button backButton;
//    @FXML
//    private ImageView profileImageView;
//    @FXML
//    private Button changePhotoButton;
//    @FXML
//    private TextField newPasswordField;
//
//
//    @FXML
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//
//        clipCircle.radiusProperty()
//                .bind(profileImageView.layoutBoundsProperty()
//                        .map(b -> b.getWidth() / 2));
//
//        // center = half‑width, half‑height
//        clipCircle.centerXProperty()
//                .bind(profileImageView.layoutBoundsProperty()
//                        .map(b -> b.getWidth() / 2));
//        clipCircle.centerYProperty()
//                .bind(profileImageView.layoutBoundsProperty()
//                        .map(b -> b.getHeight() / 2));
//
//        URL imgUrl = getClass().getResource("/images/account-avatar.png");
//        if (imgUrl == null) {
//            System.err.println(">>> cannot find /images/account-avatar.png on classpath");
//        } else {
//            Image img = new Image(imgUrl.toExternalForm());
//            profileImageView.setImage(img);
//        }
//
//        changePhotoButton.setOnAction(evt -> {
//            FileChooser chooser = new FileChooser();
//            chooser.setTitle("Select Profile Photo");
//            chooser.getExtensionFilters().addAll(
//                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
//            );
//            File chosen = chooser.showOpenDialog(profileImageView.getScene().getWindow());
//            if (chosen != null) {
//                Image img = new Image(chosen.toURI().toString(), 100, 100, true, true);// preserve ratio + smooth
//                profileImageView.setImage(img);
//            }
//        });
//
//        backButton.setOnAction(this::handleBack);
//
//        saveButton.setOnAction(this::handleSave);
//
//    }
//
//
//    // Handle the back button action
//    public void handleBack(ActionEvent actionEvent) {
//        try {
//            // Navigate back to the settings page
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
//            Parent root = loader.load();
//            Scene scene = new Scene(root, 1000, 600);
//            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
//            stage.setScene(scene);
//            stage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void handleSave(ActionEvent actionEvent) {
//        if (usernameField.getText() != null && !usernameField.getText().isEmpty()) {
//            if(isLoggedIn()) {
//                SignedUser signedUser = new SignedUser();
//                SignedUser.Load();
//                signedUser.setName(usernameField.getText());
//                SignedUser.Save(signedUser.toString());
//            }
//        }
//        if(Page.isValidBDNumber(phoneField.getText())) {
//                // to reset user phone number
//                String Phone = phoneField.getText();
//                if(Phone.length() == 11) {
//                    Phone = "+88" + phoneField.getText();
//                }
//
//            if(isLoggedIn()) {
//                SignedUser signedUser = new SignedUser();
//                SignedUser.Load();
//                signedUser.setPhone(Phone);
//                SignedUser.Save(signedUser.toString());
//            }
//        }
//        if(newPasswordField.getText() != null && !newPasswordField.getText().isEmpty()) {
//            if(isLoggedIn()) {
//                SignedUser signedUser = new SignedUser();
//                SignedUser.Load();
//                signedUser.setPassword(newPasswordField.getText());
//                SignedUser.Save(signedUser.toString());
//            }
//        }
//
//
////        System.out.println("UsernameField: " + usernameField.getText() + " PhoneField: " + phoneField.getText());
//        try {
//            // Navigate back to the settings page
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/settings.fxml"));
//            Parent root = loader.load();
//            Scene scene = new Scene(root, 1000, 600);
//            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
//            stage.setScene(scene);
//            stage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
