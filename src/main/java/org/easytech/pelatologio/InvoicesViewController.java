package org.easytech.pelatologio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Invoice;

public class InvoicesViewController {

    @FXML
    private TableView<Invoice> table;

    @FXML
    private TableColumn<Invoice, String> invoceDateColumn;
    @FXML
    private TableColumn<Invoice, String> invoceTypeColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceNumberColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceAmountColumn;
    @FXML
    private TableColumn<Invoice, String> invoicePaidColumn;
    @FXML
    private TableColumn<Invoice, String> invoiceParColumn;

    Customer customer;

    private ObservableList<Invoice> invoceList;

    @FXML
    public void initialize() {

        invoceList = FXCollections.observableArrayList();
        invoceDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        invoceTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        invoiceNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumber()));
        invoiceAmountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAmount()));
        invoicePaidColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPliromi()));
        invoiceParColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPar()));

        table.setItems(invoceList);
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadLoginsForCustomer(String afm) {
        invoceList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        invoceList.addAll(dbHelper.getInvoices(afm));
        if (table.getItems().size() == 1)
            table.getSelectionModel().select(0);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getAfm()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    private void showErrorNotification(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showError();
    }
}
