package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional;

public class AddressViewController {

    @FXML
    private Label customerLabel;

    @FXML
    private TableView<CustomerAddress> addressTable;

    @FXML
    private TableColumn<CustomerAddress, String> addressColumn;
    @FXML
    private TableColumn<CustomerAddress, String> townColumn;

    @FXML
    private TableColumn<CustomerAddress, String> postcodeColumn;
    @FXML
    private TableColumn<CustomerAddress, String> storeColumn;


    Customer customer;

    private ObservableList<CustomerAddress> customerAddressList;

    @FXML
    public void initialize() {
        customerAddressList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        // Ρύθμιση στήλης username
        townColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTown()));
        // Ρύθμιση στήλης tag
        postcodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPostcode()));
        // Ρύθμιση στήλης Τηλέφωνο
        storeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStore()));

        addressTable.setItems(customerAddressList);

        addressTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                handleEditLogin(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadAddressForCustomer(int customerId) {
        customerAddressList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        customerAddressList.addAll(dbHelper.getCustomerAddresses(customerId));
        if (addressTable.getItems().size() == 1)
            addressTable.getSelectionModel().select(0);
    }
    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addAddress.fxml"));
            DialogPane dialogPane = loader.load();

            AddAddressController addAddressController = loader.getController();
            addAddressController.setCustomer(customer); // Ορίζει τον πελάτη

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέας διεύθυνσης");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addAddressController.handleSaveAddress(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadAddressForCustomer(customer.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        CustomerAddress selectedCustomerAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedCustomerAddress == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε κάποια διεύθυνση προς διαγραφή."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς διαγραφή.");
            return;
        }

        // Εμφάνιση παραθύρου επιβεβαίωσης
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε την επιλεγμένη διεύθυνση;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Διαγραφή από τη βάση
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteAddress(selectedCustomerAddress.getAddressId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            addressTable.getItems().remove(selectedCustomerAddress);
        }
    }

    public void handleEditLogin(ActionEvent event) {
        CustomerAddress selectedCustomerAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedCustomerAddress == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε μία διεύθυνση προς επεξεργασία."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editAddress.fxml"));
            DialogPane dialogPane = loader.load();

            EditAddressController editController = loader.getController();
            editController.setAddress(selectedCustomerAddress);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία διεύθυνσης");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                CustomerAddress updatedCustomerAddress = editController.getUpdatedAddress();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                dbHelper.updateAddress(updatedCustomerAddress);

                // Ενημέρωση του πίνακα
                addressTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadAddressForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
