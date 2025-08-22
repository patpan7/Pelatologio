package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CustomerTabController;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.LoginAutomator;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.openqa.selenium.By;

import java.io.IOException;

public class AddLoginController implements CustomerTabController {

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
    private Runnable onDataSaved;

    // Μέθοδος για να ορίσεις τον πελάτη
    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        phoneField.setText(customer.getMobile());
    }

    @Override
    public void setOnDataSaved(Runnable callback) {
        this.onDataSaved = callback;
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

        // Ενέργειες για τα copy, paste, clear items στο βασικό contextMenu
        copyItem.setOnAction(e -> copyText());
        pasteItem.setOnAction(e -> pasteText());
        clearItem.setOnAction(e -> clearText());
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
        String phone = phoneField.getText();

        if (!username.isEmpty() && !password.isEmpty()) {
            String tag = tagField.getSelectionModel().getSelectedItem().toString();
            Logins newLogin = new Logins(username, password, tag, phone);
            DBHelper dbHelper = new DBHelper();
            int loginId = DBHelper.getLoginDao().addLogin(customer.getCode(), newLogin, appicationId); // Υποθέτοντας ότι έχεις αυτή τη μέθοδο στον DBHelper
            System.out.println("Login added successfully with ID: " + loginId);
            if (loginId != 0 && tag.contains("Cash") || tag.contains("Rest"))
                DBHelper.getSimplyStatusDao().addSimplySetupProgress(loginId);
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

    public void setUsername(String email) {
        usernameField.setText(email);
    }

    public void tempLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Συμπλήρωσε το Username και το Password.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/saadeapps3/comregistry/#!/arxiki",
                    username,
                    password,
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public boolean validateInputs() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        Object selectedTag = tagField.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || selectedTag == null) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ συμπληρώστε όλα τα πεδία.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return false;
        }
        return true;
    }
}
