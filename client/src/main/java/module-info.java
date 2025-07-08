module com.client.login {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.desktop;
    requires javafx.media;

    exports com.client.chat;
    exports com.client.Settings;
    exports com.client.login;
    exports com.client.Settings.AccountDetails;

    opens com.client.Settings.AccountDetails to javafx.fxml, javafx.graphics;
    opens com.client.Settings to javafx.graphics, javafx.fxml;

    opens com.client.login to javafx.fxml;


}