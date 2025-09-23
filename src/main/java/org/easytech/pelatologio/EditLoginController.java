package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.models.Logins;

public class EditLoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private ComboBox tagField;
    @FXML
    private TextField phoneField;
    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private Logins login;

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(usernameField, contextMenu);
        setupTextFieldContextMenu(passwordField, contextMenu);
        setupTextFieldContextMenu(phoneField, contextMenu);

        copyItem.setOnAction(e -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(e -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(e -> AppUtils.clearText(currentTextField));
    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    // Μέθοδος για να ρυθμίσεις το login προς επεξεργασία
    public void setLogin(Logins login) {
        this.login = login;
        usernameField.setText(login.getUsername());
        passwordField.setText(login.getPassword());
        tagField.setValue(login.getTag());
        phoneField.setText(login.getPhone());
    }

    // Επιστρέφει το επεξεργασμένο login
    public Logins getUpdatedLogin() {
        login.setUsername(usernameField.getText());
        login.setPassword(passwordField.getText());
        login.setTag(tagField.getValue().toString());
        login.setPhone(phoneField.getText());
        return login;
    }
}
