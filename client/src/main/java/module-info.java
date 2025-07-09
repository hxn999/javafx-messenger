module com.client.login {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires com.fasterxml.jackson.core;

    exports com.client.chat;
    exports com.Application;
    exports com.client.Settings;
    exports com.client.login;
    exports com.client.Settings.AccountDetails;
    exports com.client.createaccount;

    opens com.client.Settings.AccountDetails to javafx.fxml, javafx.graphics;
    opens com.client.Settings to javafx.graphics, javafx.fxml;

    opens com.client.login to javafx.fxml;
    opens com.client.chat to javafx.fxml;
    opens com.Application to javafx.fxml;
    opens com.client.createaccount to javafx.fxml;

}