package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.easytech.pelatologio.dao.CustomerMyPosDetailsDao;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CustomerMyPosDetails;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MyposAccountsController implements Initializable {
    private TabPane mainTabPane;
    @FXML
    private TextField tfSearchAll;
    @FXML
    private ComboBox<String> cbVerificationStatus;
    @FXML
    private ComboBox<String> cbAccountStatus;
    @FXML
    private Button btnClearFilters;
    @FXML
    private TableView<CustomerMyPosDetails> myposAccountsTable;
    @FXML
    private TableColumn<CustomerMyPosDetails, String> colCustomerName;
    @FXML
    private TableColumn<CustomerMyPosDetails, String> colClientId;
    @FXML
    private TableColumn<CustomerMyPosDetails, String> colVerificationStatus;
    @FXML
    private TableColumn<CustomerMyPosDetails, String> colAccountStatus;
    @FXML
    private TableColumn<CustomerMyPosDetails, Void> colActions; // For buttons or other actions
    @FXML
    private Label lblCounter; // NEW
    private final ObservableList<CustomerMyPosDetails> masterData = FXCollections.observableArrayList();
    private FilteredList<CustomerMyPosDetails> filteredData;
    private CustomerMyPosDetailsDao myPosDetailsDao;

    // Callback to open customer details in MainMenuController
    private Consumer<Integer> openCustomerCallback;

    public void setOpenCustomerCallback(Consumer<Integer> openCustomerCallback) {
        this.openCustomerCallback = openCustomerCallback;
    }

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        myPosDetailsDao = DBHelper.getCustomerMyPosDetailsDao();

        setupTableColumns();
        loadData();
        setupFilters();
        setupActions();
    }

    private void setupTableColumns() {
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colClientId.setCellValueFactory(new PropertyValueFactory<>("myposClientId"));
        colVerificationStatus.setCellValueFactory(new PropertyValueFactory<>("verificationStatus"));
        colAccountStatus.setCellValueFactory(new PropertyValueFactory<>("accountStatus"));
        // colLastSync.setCellValueFactory(new PropertyValueFactory<>("lastSyncDate")); // Assuming a field named lastSyncDate in model

        // Make status columns editable
        colClientId.setCellFactory(TextFieldTableCell.forTableColumn());
        colVerificationStatus.setCellFactory(ComboBoxTableCell.forTableColumn("Verified", "Unverified"));
        colAccountStatus.setCellFactory(ComboBoxTableCell.forTableColumn("Active", "Blocked", "Closed"));

        // Handle edit commit for editable columns
        colClientId.setOnEditCommit(event -> {
            CustomerMyPosDetails details = event.getRowValue();
            details.setMyposClientId(event.getNewValue());
            myPosDetailsDao.saveOrUpdate(details);
        });
        colVerificationStatus.setOnEditCommit(event -> {
            CustomerMyPosDetails details = event.getRowValue();
            details.setVerificationStatus(event.getNewValue());
            myPosDetailsDao.saveOrUpdate(details);
        });
        colAccountStatus.setOnEditCommit(event -> {
            CustomerMyPosDetails details = event.getRowValue();
            details.setAccountStatus(event.getNewValue());
            myPosDetailsDao.saveOrUpdate(details);
        });

        // Add a custom cell factory for the "Actions" column if needed (e.g., for a "View Customer" button)
        colActions.setCellFactory(param -> new TableCell<CustomerMyPosDetails, Void>() {
            private final Button viewButton = new Button("Προβολή");

            {
                viewButton.setOnAction(event -> {
                    CustomerMyPosDetails data = getTableView().getItems().get(getIndex());
                    if (openCustomerCallback != null) {
                        openCustomerCallback.accept(data.getCustomerId());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(myPosDetailsDao.getAll());

        // Initialize filteredData
        filteredData = new FilteredList<>(masterData, p -> true);

        // Wrap the FilteredList in a SortedList.
        SortedList<CustomerMyPosDetails> sortedData = new SortedList<>(filteredData);

        // Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(myposAccountsTable.comparatorProperty());

        // Add sorted (and filtered) data to the table.
        myposAccountsTable.setItems(sortedData);
        lblCounter.setText("Σύνολο: " + filteredData.size()); // Initial count
    }

    private void setupFilters() {
        // Populate ComboBoxes
        cbVerificationStatus.getItems().addAll("Όλες", "Verified", "Unverified");
        cbVerificationStatus.setValue("Όλες");
        cbAccountStatus.getItems().addAll("Όλες", "Active", "Blocked", "Closed");
        cbAccountStatus.setValue("Όλες");

        // Listener for search fields
        tfSearchAll.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Listeners for ComboBoxes
        cbVerificationStatus.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbAccountStatus.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Clear filters button
        btnClearFilters.setOnAction(event -> {
            tfSearchAll.clear();
            cbVerificationStatus.setValue("Όλες");
            cbAccountStatus.setValue("Όλες");
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchAll = tfSearchAll.getText().toUpperCase();
        String verificationStatus = cbVerificationStatus.getValue();
        String accountStatus = cbAccountStatus.getValue();

        filteredData.setPredicate(details -> {
            // Combined search for Client ID and Customer Name
            if (searchAll != null && !searchAll.isEmpty()) {
                String search1 = AppUtils.toEnglish(searchAll);
                String search2 = AppUtils.toGreek(searchAll);

                boolean clientIdMatch = details.getMyposClientId() != null &&
                        (details.getMyposClientId().toUpperCase().contains(search1) ||
                                details.getMyposClientId().toUpperCase().contains(search2));

                boolean customerNameMatch = details.getCustomerName() != null &&
                        (details.getCustomerName().toUpperCase().contains(search1) ||
                                details.getCustomerName().toLowerCase().contains(search2));

                if (!clientIdMatch && !customerNameMatch) {
                    return false;
                }
            }

            // Verification Status filter
            if (verificationStatus != null && !verificationStatus.equals("Όλες")) {
                if (!details.getVerificationStatus().equals(verificationStatus)) {
                    return false;
                }
            }

            // Account Status filter
            if (accountStatus != null && !accountStatus.equals("Όλες")) {
                return details.getAccountStatus().equals(accountStatus);
            }

            return true; // All filters passed
        });
        lblCounter.setText("Σύνολο: " + filteredData.size());
    }

    private void setupActions() {
        // Double-click to open customer details
        myposAccountsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CustomerMyPosDetails selected = myposAccountsTable.getSelectionModel().getSelectedItem();
                if (selected != null && openCustomerCallback != null) {
                    openCustomerCallback.accept(selected.getCustomerId());
                }
            }
        });

        // Implement btnSyncAll and btnExport actions here
        // btnSyncAll.setOnAction(this::handleSyncAll);
        // btnExport.setOnAction(this::handleExport);
    }

    // Placeholder for sync all action
    @FXML
    private void handleSyncAll(ActionEvent event) {
        // Logic to sync all myPOS accounts
        System.out.println("Sync All button clicked!");
        // This would involve iterating through masterData and calling a service to sync each one
    }

    // Placeholder for export action
    @FXML
    private void handleExport(ActionEvent event) {
        // Logic to export data to Excel/CSV
        System.out.println("Export button clicked!");
    }
}
