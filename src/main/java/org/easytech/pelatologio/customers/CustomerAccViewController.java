package org.easytech.pelatologio.customers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.easytech.pelatologio.settings.AppSettings;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Accountant;
import org.easytech.pelatologio.models.Customer;

import java.io.IOException;

public class CustomerAccViewController {
    @FXML
    public TableColumn nameColumn, titleColumn, afmColumn, phone1Column, phone2Column, mobileColumn, townColumn, emailColumn;

    @FXML
    private TableView<Customer> customerTable;
    private ObservableList<Customer> customersList;


    Accountant accountant;


    @FXML
    public void initialize() {

        customersList = FXCollections.observableArrayList();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        afmColumn.setCellValueFactory(new PropertyValueFactory<>("afm"));
        phone1Column.setCellValueFactory(new PropertyValueFactory<>("phone1"));
        phone2Column.setCellValueFactory(new PropertyValueFactory<>("phone2"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        townColumn.setCellValueFactory(new PropertyValueFactory<>("town"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        customerTable.setItems(customersList);

        customerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
                DBHelper dbHelper = new DBHelper();
                if (selectedCustomer.getCode() == 0) {
                    return;
                }
                try {
                    String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                    if (res.equals("unlocked")) {
                        dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                        Parent root = loader.load();

                        Stage stage = new Stage();
                        stage.setTitle("Λεπτομέρειες Πελάτη");
                        stage.setScene(new Scene(root));
                        stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                        AddCustomerController controller = loader.getController();

                        // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                        controller.setCustomerData(selectedCustomer);

                        stage.show();
                        stage.setOnCloseRequest(evt -> {
                            System.out.println("Το παράθυρο κλείνει!");
                            dbHelper.customerUnlock(selectedCustomer.getCode());
                        });
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Προσοχή");
                        alert.setContentText(res);
                        alert.showAndWait();
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
                }
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadCustomersForAcc(int accId) {
        customersList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        customersList.addAll(dbHelper.getCustomersByAcc(accId));
    }

    public void setAccountnat(Accountant accountant) {
        this.accountant = accountant;
        customersList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        customersList.addAll(dbHelper.getCustomersByAcc(accountant.getId()));
    }
}
