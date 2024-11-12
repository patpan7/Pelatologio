package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class AddLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private ComboBox tagField;
    @FXML
    private TextField phoneField;
    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private Customer customer;

    // Μέθοδος για να ορίσεις τον πελάτη
    public void setCustomer(Customer customer) {
        this.customer = customer;
        phoneField.setText(customer.getMobile());
    }

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

    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    // Μέθοδοι για τις ενέργειες
    private void copyText() {
        if (currentTextField != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentTextField.getText());  // Replace with the desired text
            clipboard.setContent(content);
        }
    }

    private void pasteText() {
        if (currentTextField != null) {
            currentTextField.paste();
        }
    }

    private void clearText() {
        if (currentTextField != null) {
            currentTextField.clear();
        }
    }

    // Μέθοδος για την αποθήκευση του νέου login
    @FXML
    public void handleSaveLogin(ActionEvent event, int appicationId) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String tag = tagField.getSelectionModel().getSelectedItem().toString();
        String phone = phoneField.getText();
        Logins newLogin = new Logins(username, password, tag, phone);

        if (!username.isEmpty() && !password.isEmpty()) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.addLogin(customer.getCode(),newLogin,appicationId); // Υποθέτοντας ότι έχεις αυτή τη μέθοδο στον DBHelper

            // Κλείσιμο του διαλόγου
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Το νέο login προστέθηκε επιτυχώς!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

            // Καθαρισμός των πεδίων
            usernameField.clear();
            passwordField.clear();
            tagField.setValue(null);
            phoneField.clear();
        } else {
            // Μήνυμα σφάλματος αν κάποια πεδία είναι κενά
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Παρακαλώ συμπληρώστε όλα τα πεδία!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        }
    }
}
