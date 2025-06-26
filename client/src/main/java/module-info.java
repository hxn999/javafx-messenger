module com.client.login {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.client.login to javafx.fxml;
    exports com.client.login;

}