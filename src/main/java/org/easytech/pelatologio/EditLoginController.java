package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditLoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField tagField;
    @FXML
    private TextField phoneField;

    private Logins login;

    // Μέθοδος για να ρυθμίσεις το login προς επεξεργασία
    public void setLogin(Logins login) {
        this.login = login;
        usernameField.setText(login.getUsername());
        passwordField.setText(login.getPassword());
        tagField.setText(login.getTag());
        phoneField.setText(login.getPhone());
    }

    // Επιστρέφει το επεξεργασμένο login
    public Logins getUpdatedLogin() {
        login.setUsername(usernameField.getText());
        login.setPassword(passwordField.getText());
        login.setTag(tagField.getText());
        login.setPhone(phoneField.getText());
        return login;
    }
}
