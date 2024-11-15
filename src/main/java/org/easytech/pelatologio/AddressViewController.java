package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class AddressViewController {

    @FXML
    private Label customerLabel;

    @FXML
    private TableView<Address> addressTable;

    @FXML
    private TableColumn<Address, String> addressColumn;
    @FXML
    private TableColumn<Address, String> townColumn;

    @FXML
    private TableColumn<Address, String> postcodeColumn;
    @FXML
    private TableColumn<Address, String> storeColumn;


    Customer customer;

    private ObservableList<Address> addressList;

    @FXML
    public void initialize() {
        addressList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        // Ρύθμιση στήλης username
        townColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTown()));
        // Ρύθμιση στήλης tag
        postcodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPostcode()));
        // Ρύθμιση στήλης Τηλέφωνο
        storeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStore()));

        addressTable.setItems(addressList);

        addressTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                handleEditLogin(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadAddressForCustomer(int customerId) {
        addressList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        addressList.addAll(dbHelper.getCustomerAddresses(customerId));
        if (addressTable.getItems().size() == 1)
            addressTable.getSelectionModel().select(0);
    }
    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addAddress.fxml"));
            DialogPane dialogPane = loader.load();

            AddLoginController addLoginController = loader.getController();
            addLoginController.setCustomer(customer); // Ορίζει τον πελάτη

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέας διεύθυνσης");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addLoginController.handleSaveLogin(event,1);
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
        Address selectedAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedAddress == null) {
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
            dbHelper.deleteLogin(selectedAddress.getAddressId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            addressTable.getItems().remove(selectedAddress);
        }
    }

    public void handleEditLogin(ActionEvent event) {
        Address selectedAddress = addressTable.getSelectionModel().getSelectedItem();
        if (selectedAddress == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε μία διεύθυνση προς επεξεργασία."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addAddress.fxml"));
            DialogPane dialogPane = loader.load();

            EditAddressController editController = loader.getController();
            editController.setLogin(selectedAddress);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Login");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Logins updatedLogin = editController.getUpdatedLogin();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                dbHelper.updateLogin(updatedLogin);

                // Ενημέρωση του πίνακα
                loginTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    public void myposloginOpen(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www.mypos.com/el/login",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("email"),
                    By.id("password"),
                    By.cssSelector("button[data-testid='mypos_login_btn']")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void myposregisterOpen(MouseEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        String myposRegister = AppSettings.loadSetting("myposlink");

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard
            if (selectedLogin != null) {
                String msg ="Στοιχεία myPOS" +
                        "\nΕπωνυμία: " + customer.getName() +
                        "\nΑΦΜ: " + customer.getAfm() +
                        "\nEmail: " + selectedLogin.getUsername() +
                        "\nΚωδικός: " + selectedLogin.getPassword() +
                        "\nΚινητό: " + selectedLogin.getPhone();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(msg);  // Replace with the desired text
                clipboard.setContent(content);
                showAlert("Copied to Clipboard", msg);
            } else {
                showAlert("Attention", "Please select a login to copy.");
            }
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Left-click for regular functionality
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openPage(myposRegister);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (selectedLogin == null) {
                Platform.runLater(() -> showAlert("Attention", "Please select a login."));
                return;
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}