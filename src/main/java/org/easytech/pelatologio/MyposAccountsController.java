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
    private ObservableList<CustomerMyPosDetails> masterData = FXCollections.observableArrayList();
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
                // Apply Greek/Latin conversion
                // Υποστήριξη Ελληνικών/Αγγλικών
                char[] chars1 = searchAll.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = searchAll.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);

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
                if (!details.getAccountStatus().equals(accountStatus)) {
                    return false;
                }
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

    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();
    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('\u0041', '\u0391');  // uppercase A
        ENGLISH_TO_GREEK.put('\u0042', '\u0392');  // uppercase B
        ENGLISH_TO_GREEK.put('\u0043', '\u03A8');  // uppercase C
        ENGLISH_TO_GREEK.put('\u0044', '\u0394');  // uppercase D
        ENGLISH_TO_GREEK.put('\u0045', '\u0395');  // uppercase E
        ENGLISH_TO_GREEK.put('\u0046', '\u03A6');  // uppercase F
        ENGLISH_TO_GREEK.put('\u0047', '\u0393');  // uppercase G
        ENGLISH_TO_GREEK.put('\u0048', '\u0397');  // uppercase H
        ENGLISH_TO_GREEK.put('\u0049', '\u0399');  // uppercase I
        ENGLISH_TO_GREEK.put('\u004A', '\u039E');  // uppercase J
        ENGLISH_TO_GREEK.put('\u004B', '\u039A');  // uppercase K
        ENGLISH_TO_GREEK.put('\u004C', '\u039B');  // uppercase L
        ENGLISH_TO_GREEK.put('\u004D', '\u039C');  // uppercase M
        ENGLISH_TO_GREEK.put('\u004E', '\u039D');  // uppercase N
        ENGLISH_TO_GREEK.put('\u004F', '\u039F');  // uppercase O
        ENGLISH_TO_GREEK.put('\u0050', '\u03A0');  // uppercase P
        ENGLISH_TO_GREEK.put('\u0052', '\u03A1');  // uppercase R
        ENGLISH_TO_GREEK.put('\u0053', '\u03A3');  // uppercase S
        ENGLISH_TO_GREEK.put('\u0054', '\u03A4');  // uppercase T
        ENGLISH_TO_GREEK.put('\u0055', '\u0398');  // uppercase U
        ENGLISH_TO_GREEK.put('\u0056', '\u03A9');  // uppercase V
        ENGLISH_TO_GREEK.put('\u0057', '\u03A3');  // uppercase W (sigma)
        ENGLISH_TO_GREEK.put('\u0058', '\u03A7');  // uppercase X
        ENGLISH_TO_GREEK.put('\u0059', '\u03A5');  // uppercase Y
        ENGLISH_TO_GREEK.put('\u005A', '\u0396');  // uppercase Z

        GREEK_TO_ENGLISH.put('\u0391', '\u0041');  // uppercase Α
        GREEK_TO_ENGLISH.put('\u0392', '\u0042');  // uppercase Β
        GREEK_TO_ENGLISH.put('\u03A8', '\u0043');  // uppercase Ψ
        GREEK_TO_ENGLISH.put('\u0394', '\u0044');  // uppercase Δ
        GREEK_TO_ENGLISH.put('\u0395', '\u0045');  // uppercase Ε
        GREEK_TO_ENGLISH.put('\u03A6', '\u0046');  // uppercase Φ
        GREEK_TO_ENGLISH.put('\u0393', '\u0047');  // uppercase Γ
        GREEK_TO_ENGLISH.put('\u0397', '\u0048');  // uppercase Η
        GREEK_TO_ENGLISH.put('\u0399', '\u0049');  // uppercase Ι
        GREEK_TO_ENGLISH.put('\u039E', '\u004A');  // uppercase Ξ
        GREEK_TO_ENGLISH.put('\u039A', '\u004B');  // uppercase Κ
        GREEK_TO_ENGLISH.put('\u039B', '\u004C');  // uppercase Λ
        GREEK_TO_ENGLISH.put('\u039C', '\u004D');  // uppercase Μ
        GREEK_TO_ENGLISH.put('\u039D', '\u004E');  // uppercase Ν
        GREEK_TO_ENGLISH.put('\u039F', '\u004F');  // uppercase Ο
        GREEK_TO_ENGLISH.put('\u03A0', '\u0050');  // uppercase Π
        GREEK_TO_ENGLISH.put('\u03A1', '\u0052');  // uppercase Ρ
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase Σ
        GREEK_TO_ENGLISH.put('\u03A4', '\u0054');  // uppercase Τ
        GREEK_TO_ENGLISH.put('\u0398', '\u0055');  // uppercase Θ
        GREEK_TO_ENGLISH.put('\u03A9', '\u0056');  // uppercase Ω
        GREEK_TO_ENGLISH.put('\u03A7', '\u0058');  // uppercase Χ
        GREEK_TO_ENGLISH.put('\u03A5', '\u0059');  // uppercase Υ
        GREEK_TO_ENGLISH.put('\u0396', '\u005A');  // uppercase Ζ
    }

    private String convertToGreek(String text) {
        char[] chars = text.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character repl = ENGLISH_TO_GREEK.get(chars[i]);
            if (repl != null) {
                chars[i] = repl;
            }
        }
        return new String(chars);
    }

    private String convertToEnglish(String text) {
        char[] chars = text.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character repl = GREEK_TO_ENGLISH.get(chars[i]);
            if (repl != null) {
                chars[i] = repl;
            }
        }
        return new String(chars);
    }
}
