package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.CustomerAddress;

public class AddAddressController {
    @FXML
    private TextField addressField;

    @FXML
    private TextField townField;

    @FXML
    private TextField postcodeField;

    @FXML
    private TextField storeField;

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
        copyItem.setOnAction(event -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(event -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(event -> AppUtils.clearText(currentTextField));
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(addressField, contextMenu);
        setupTextFieldContextMenu(townField, contextMenu);
        setupTextFieldContextMenu(postcodeField, contextMenu);
        setupTextFieldContextMenu(storeField, contextMenu);

    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }


    // Μέθοδος για την αποθήκευση του νέου login
    @FXML
    public void handleSaveAddress(ActionEvent event) {
        String address = addressField.getText();
        String town = townField.getText();
        String postcode = postcodeField.getText();
        String store = storeField.getText();

        CustomerAddress newCustomerAddress = new CustomerAddress(customer.getCode(), address, town, postcode, store);

        if (!address.isEmpty() && !town.isEmpty() && !postcode.isEmpty() && !store.isEmpty()) {
            DBHelper.getAddressDao().addAddress(customer.getCode(), newCustomerAddress); // Υποθέτοντας ότι έχεις αυτή τη μέθοδο στον DBHelper

            // Κλείσιμο του διαλόγου
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Η νέα διεύθυνση προστέθηκε επιτυχώς!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

            // Καθαρισμός των πεδίων
            addressField.clear();
            townField.clear();
            postcodeField.clear();
            storeField.clear();
        } else {
            // Μήνυμα σφάλματος αν κάποια πεδία είναι κενά
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Παρακαλώ συμπληρώστε όλα τα πεδία!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        }
    }
}
