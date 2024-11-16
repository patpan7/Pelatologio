package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class EditAddressController {
    @FXML
    private TextField addressField;
    @FXML
    private TextField townField;
    @FXML
    private TextField postcodeField;
    @FXML
    private TextField storeField;
    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private Address address;

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(addressField, contextMenu);
        setupTextFieldContextMenu(townField, contextMenu);
        setupTextFieldContextMenu(postcodeField, contextMenu);
        setupTextFieldContextMenu(storeField, contextMenu);

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

    // Μέθοδος για να ρυθμίσεις το login προς επεξεργασία
    public void setAddress(Address address) {
        this.address = address;
        addressField.setText(address.getAddress());
        townField.setText(address.getTown());
        postcodeField.setText(address.getPostcode());
        storeField.setText(address.getStore());
    }

    // Επιστρέφει το επεξεργασμένο login
    public Address getUpdatedAddress() {
        address.setAddress(addressField.getText());
        address.setTown(townField.getText());
        address.setPostcode(postcodeField.getText());
        address.setStore(storeField.getText());
        return address;
    }
}
