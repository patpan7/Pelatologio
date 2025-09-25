package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.CustomerAddress;

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
            if (event.getClickCount() == 2) {
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
        customerAddressList.addAll(DBHelper.getAddressDao().getCustomerAddresses(customerId));
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        CustomerAddress selectedCustomerAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedCustomerAddress == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κάποια διεύθυνση προς διαγραφή.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showWarning();
            });
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
            DBHelper.getAddressDao().deleteAddress(selectedCustomerAddress.getAddressId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            addressTable.getItems().remove(selectedCustomerAddress);
        }
    }

    public void handleEditLogin(ActionEvent event) {
        CustomerAddress selectedCustomerAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedCustomerAddress == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε μία διεύθυνση προς επεξεργασία.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showError();
            });
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
                DBHelper.getAddressDao().updateAddress(updatedCustomerAddress);

                // Ενημέρωση του πίνακα
                addressTable.refresh();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadAddressForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

}
