package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField tagField;
    @FXML
    private TextField phoneField;

    private Customer customer;

    // Μέθοδος για να ορίσεις τον πελάτη
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
    }

    // Μέθοδος για την αποθήκευση του νέου login
    @FXML
    public void handleSaveLogin(ActionEvent event, int appicationId) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String tag = tagField.getText();
        String phone = phoneField.getText();
        Logins newLogin = new Logins(username, password, tag,phone);

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
            tagField.clear();
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
