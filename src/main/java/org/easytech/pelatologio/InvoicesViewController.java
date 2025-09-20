package org.easytech.pelatologio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.CustomerTabController;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Invoice;

public class InvoicesViewController implements CustomerTabController {

    @FXML
    private SplitPane splitPane;
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

    @FXML
    private TableView<Invoice> table1;

    @FXML
    private TableColumn<Invoice, String> invoceDateColumn1;
    @FXML
    private TableColumn<Invoice, String> invoceTypeColumn1;
    @FXML
    private TableColumn<Invoice, String> invoiceNumberColumn1;
    @FXML
    private TableColumn<Invoice, String> invoiceAmountColumn1;
    @FXML
    private TableColumn<Invoice, String> invoicePaidColumn1;
    @FXML
    private TableColumn<Invoice, String> invoiceParColumn1;

    Customer customer;
    private Runnable onDataSaved;

    private ObservableList<Invoice> invoceList;

    private ObservableList<Invoice> invoceList1;

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

        invoceList1 = FXCollections.observableArrayList();
        invoceDateColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        invoceTypeColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        invoiceNumberColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumber()));
        invoiceAmountColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAmount()));
        invoicePaidColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPliromi()));
        invoiceParColumn1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPar()));

        table1.setItems(invoceList1);
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadLoginsForCustomer(String afm) {
        invoceList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        invoceList.addAll(DBHelper.getInvoiceDao().getInvoices(afm));
        if (table.getItems().size() == 1)
            table.getSelectionModel().select(0);

        invoceList1.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        invoceList1.addAll(DBHelper.getInvoiceDao().getInvoices1(afm));
        if (table1.getItems().size() == 1)
            table1.getSelectionModel().select(0);

        // Adjust SplitPane divider
        boolean table1HasItems = !invoceList.isEmpty();
        boolean table2HasItems = !invoceList1.isEmpty();

        if (table1HasItems && table2HasItems) {
            splitPane.setDividerPositions(0.5);
        } else if (table1HasItems) {
            splitPane.setDividerPositions(1.0);
        } else if (table2HasItems) {
            splitPane.setDividerPositions(0.0);
        }
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getAfm()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    @Override
    public void setOnDataSaved(Runnable callback) {
        this.onDataSaved = callback;
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
