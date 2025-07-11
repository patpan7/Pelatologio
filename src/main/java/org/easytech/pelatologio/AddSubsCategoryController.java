package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.SubsCategory;

public class AddSubsCategoryController {
    @FXML
    private TextField nameField;


    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private Customer customer;

    // Μέθοδος για να ορίσεις τον πελάτη
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        copyItem.setOnAction(event -> copyText());
        pasteItem.setOnAction(event -> pasteText());
        clearItem.setOnAction(event -> clearText());
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(nameField, contextMenu);

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
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();

        SubsCategory newSubsCategory = new SubsCategory(0,name);

        if (!name.isEmpty()) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.saveSubsCategory(newSubsCategory); // Υποθέτοντας ότι έχεις αυτή τη μέθοδο στον DBHelper

            // Κλείσιμο του διαλόγου
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Η κατηγορία προστέθηκε επιτυχώς!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

            // Καθαρισμός των πεδίων
            nameField.clear();
        } else {
            // Μήνυμα σφάλματος αν κάποια πεδία είναι κενά
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Παρακαλώ συμπληρώστε όλα τα πεδία!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        }
    }
}
