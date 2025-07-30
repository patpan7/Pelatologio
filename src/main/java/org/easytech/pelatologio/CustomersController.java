package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPopup;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.dao.CustomerDao;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.AppItem;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.Recommendation;
import org.openqa.selenium.By;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

public class CustomersController implements Initializable {
    @FXML
    public TableColumn nameColumn, titleColumn, afmColumn, phone1Column, phone2Column, mobileColumn, townColumn, emailColumn;
    public JFXButton btnClean;
    @FXML
    private TableColumn<Customer, BigDecimal> balanceColumn;
    @FXML
    StackPane stackPane;
    @FXML
    TableView<Customer> customerTable;
    @FXML
    Button filterButton;
    @FXML
    TextField filterField;
    @FXML
    JFXComboBox<String> searchFieldComboBox;
    @FXML
    JFXComboBox<AppItem> appComboBox;
    @FXML
    Button btnTaxis, btnMypos, btnSimply, btnEmblem, btnErgani, btnData, openFileButton;
    @FXML
    private Label customerCounterLabel; // The new counter label
    @FXML
    private VBox filterPane; // The new advanced filter pane
    @FXML
    private JFXComboBox<String> statusComboBox;
    @FXML
    private JFXComboBox<Recommendation> recommendationComboBox;
    @FXML
    private JFXComboBox<JobTeam> jobTeamComboBox;
    @FXML
    private JFXButton clearAdvancedFiltersButton;
    private Integer activeAppFilter = null; // null σημαίνει κανένα φίλτρο εφαρμογής

    ObservableList<Customer> observableList;
    FilteredList<Customer> filteredData;
    List<CheckBox> checkBoxes = new ArrayList<>();
    private CustomerDao customerDao;
    private TabPane mainTabPane;  // Θα το περάσουμε από τον MainMenuController

    // Μέθοδος για να περάσουμε το TabPane
    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(btnTaxis, "1) Διαχείριση κωδικών Taxis του πελάτη\n2) Είσοδος με κωδικούς νέου πελάτη");
        setTooltip(btnMypos, "1) Διαχείριση κωδικών myPOS του πελάτη\n2) Είσοδος στο DAS της myPOS");
        setTooltip(btnSimply, "1) Διαχείριση κωδικών Simply του πελάτη\n2) Είσοδος στο DAS της Simply");
        setTooltip(btnEmblem, "1) Διαχείριση κωδικών Emblem του πελάτη\n2) Είσοδος στο DAS της Emblem");
        setTooltip(btnErgani, "Διαχείριση κωδικών Εργάνη του πελάτη");
        setTooltip(btnData, "Άνοιγμα φακέλου με δεδομένα πελάτη");
        setTooltip(openFileButton, "1) Αντιγραφή πληροφοριών\n2) Άνοιγμα φακέλου με δεδομένα");

        this.customerDao = DBHelper.getCustomerDao();

        // Αρχικοποίηση πίνακα
        setupTableColumns();
        initializeTable();
        setupFilterControls();
        loadFilterData();

        // Διπλό κλικ για επεξεργασία πελάτη
        customerTable.setOnMouseClicked(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1 && selectedCustomer != null) {
                btnTaxis.setStyle("-fx-border-color: #005599;");
                btnMypos.setStyle("-fx-border-color: #005599;");
                btnSimply.setStyle("-fx-border-color: #005599;");
                btnEmblem.setStyle("-fx-border-color: #005599;");
                btnErgani.setStyle("-fx-border-color: #005599;");
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή

                if (selectedCustomer.hasApp(1)) {
                    btnMypos.setStyle("-fx-border-color: #FF0000;");
                }
                if (selectedCustomer.hasApp(2)) {
                    btnSimply.setStyle("-fx-border-color: #FF0000;");
                }
                if (selectedCustomer.hasApp(3)) {
                    btnTaxis.setStyle("-fx-border-color: #FF0000;");
                }
                if (selectedCustomer.hasApp(4)) {
                    btnEmblem.setStyle("-fx-border-color: #FF0000;");
                }
                if (selectedCustomer.hasApp(5)) {
                    btnErgani.setStyle("-fx-border-color: #FF0000;");
                }
            }
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedCustomer != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        customerUpdate(new ActionEvent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        customerTable.getSelectionModel().clearSelection();

        openFileButton.setOnAction(event -> {
            ContextMenu contextMenu = new ContextMenu();
            File folder = new File(AppSettings.loadSetting("datafolder") + "\\Docs");

            // Δημιουργία φακέλου αν δεν υπάρχει
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Δημιουργία MenuItem για κάθε αρχείο στον φάκελο
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    String displayName = file.getName().replace(".txt", "");
                    MenuItem fileItem = new MenuItem(displayName);
                    fileItem.setOnAction(e -> copyFileContentToClipboard(file));
                    contextMenu.getItems().add(fileItem);
                }
            }

            // Προσθήκη επιλογής για άνοιγμα του φακέλου
            MenuItem openFolderItem = new MenuItem("Άνοιγμα φακέλου");
            openFolderItem.setOnAction(e -> openFolder(AppSettings.loadSetting("datafolder") + "\\Docs"));
            contextMenu.getItems().add(openFolderItem);

            // Εμφάνιση του ContextMenu πάνω από το κουμπί
            double buttonX = openFileButton.localToScene(openFileButton.getBoundsInLocal()).getMinX();
            double buttonY = openFileButton.localToScene(openFileButton.getBoundsInLocal()).getMinY() - 2 * openFileButton.getHeight();
            contextMenu.show(openFileButton, openFileButton.getScene().getWindow().getX() + buttonX,
                    openFileButton.getScene().getWindow().getY() + buttonY);
        });
    }

    private void setupTableColumns() {
        // Δημιουργία και αρχικοποίηση των στηλών
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        afmColumn.setCellValueFactory(new PropertyValueFactory<>("afm"));
        phone1Column.setCellValueFactory(new PropertyValueFactory<>("phone1"));
        phone2Column.setCellValueFactory(new PropertyValueFactory<>("phone2"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        townColumn.setCellValueFactory(new PropertyValueFactory<>("town"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        //balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        // Ορίζουμε το balanceColumn να κρατά BigDecimal, όχι String
        balanceColumn.setCellValueFactory(cellData -> {
            String balanceStr = cellData.getValue().getBalance();
            balanceStr = balanceStr.replace(",", "."); // <-- αυτή η γραμμή είναι το κλειδί

            try {
                BigDecimal balance = new BigDecimal(balanceStr);
                return new ReadOnlyObjectWrapper<>(balance);
            } catch (NumberFormatException e) {
                return new ReadOnlyObjectWrapper<>(BigDecimal.ZERO); // ή null, ανάλογα με τι προτιμάς
            }
        });

        // Προαιρετικά: μορφοποίηση για εμφάνιση 2 δεκαδικών
        balanceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.compareTo(BigDecimal.ZERO) == 0) {
                    setText(null); // Ή setText("") αν προτιμάς κενό κείμενο
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }

    private void loadFilterData() {
        // Status ComboBox
        statusComboBox.getItems().addAll("Ενεργοί", "Ανενεργοί", "Όλοι");
        statusComboBox.getSelectionModel().select("Ενεργοί");

        // Recommendation ComboBox
        recommendationComboBox.getItems().add(new Recommendation(0, "Όλες")); // Add a 'All' option
        recommendationComboBox.getItems().addAll(customerDao.getRecomedations());
        recommendationComboBox.getSelectionModel().selectFirst();

        // JobTeam ComboBox
        jobTeamComboBox.getItems().add(new JobTeam(0, "Όλες")); // Add a 'All' option
        jobTeamComboBox.getItems().addAll(DBHelper.getJobTeamDao().getJobTeams());
        jobTeamComboBox.getSelectionModel().selectFirst();

        // App ComboBox
        appComboBox.getItems().add(new AppItem(0, "Όλες"));
        appComboBox.getItems().addAll(DBHelper.getAppItemDao().getApplications());
//        appComboBox.getItems().addAll(
//                new AppItem(1, "myPOS"), new AppItem(2, "Simply"), new AppItem(3, "Taxis"),
//                new AppItem(4, "Emblem"), new AppItem(5, "Εργάνη"), new AppItem(6, "Πελατολόγιο"),
//                new AppItem(7, "NinePOS")
//        );

        appComboBox.getSelectionModel().selectFirst();

        // Search Field ComboBox
        searchFieldComboBox.getItems().addAll("Όλα τα πεδία", "Όνομα", "ΑΦΜ", "Πόλη", "Τηλέφωνο");
        searchFieldComboBox.getSelectionModel().selectFirst();
    }

    private void setupFilterControls() {
        // Listener for the main search field
        filterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(newVal));

        // Toggle visibility of the advanced filter pane
        filterButton.setOnAction(event -> {
            boolean isVisible = !filterPane.isVisible();
            filterPane.setVisible(isVisible);
            filterPane.setManaged(isVisible);
        });

        // Add listeners to all ComboBoxes to apply filters automatically
        statusComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(filterField.getText()));
        recommendationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(filterField.getText()));
        jobTeamComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(filterField.getText()));
        appComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(filterField.getText()));

        // Action for the clear filters button
        clearAdvancedFiltersButton.setOnAction(event -> clearAdvancedFilters());
    }

    @FXML
    private void anydeskClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                // This requires the path to anydesk.exe to be configured
                // For now, we assume it's in a known location or in PATH
                Runtime.getRuntime().exec("C:\\Pelatologio\\AnyDesk.exe ");
            } catch (IOException e) {
                e.printStackTrace();
                // Show alert: Anydesk not found
            }
        } else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
                Optional<ButtonType> result = alert.showAndWait();
                return;
            }
            openAnydeskWindow(selectedCustomer);
        }
    }

    // Μέθοδος για να πάρεις τα επιλεγμένα φίλτρα
    private Set<String> getSelectedFilters(List<CheckBox> checkBoxes) {
        Set<String> selectedFilters = new HashSet<>();

        // Ελέγχουμε κάθε checkbox και προσθέτουμε το φίλτρο αν είναι επιλεγμένο
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selectedFilters.add(checkBox.getText());
            }
        }

        return selectedFilters;
    }

    private void clearAdvancedFilters() {
        statusComboBox.getSelectionModel().select("Ενεργοί");
        recommendationComboBox.getSelectionModel().selectFirst();
        jobTeamComboBox.getSelectionModel().selectFirst();
        appComboBox.getSelectionModel().selectFirst();
        applyFilters(filterField.getText());
    }

    private void initializeTable() {
        // Δημιουργία του ObservableList και φόρτωση δεδομένων
        observableList = FXCollections.observableArrayList();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Δημιουργία του FilteredList
        filteredData = new FilteredList<>(observableList, b -> true);

        // Σύνδεση φιλτραρίσματος
        setupFilter();

        // Σύνδεση του SortedList με τον πίνακα
        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);

        // Add a listener to the FilteredList to update the counter
        filteredData.addListener((javafx.collections.ListChangeListener.Change<? extends Customer> c) -> {
            updateCustomerCounter();
        });

        updateCustomerCounter(); // Initial count
    }

    private void refreshTableData() {
        List<TableColumn<Customer, ?>> sortOrder = new ArrayList<>(customerTable.getSortOrder());
        observableList.clear();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        applyFilters(filterField.getText());
        customerTable.getSortOrder().setAll(sortOrder);
    }

    private List<Customer> fetchDataFromMySQL() throws SQLException {
        List<Customer> customers;
        try {
            customers = DBHelper.getCustomerDao().getCustomers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customers;
    }

    private void setupFilter() {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });
        applyFilters(filterField.getText()); // Αρχική εφαρμογή φίλτρου
    }

    private void applyFilters(String filterValue) {
        String filterText = filterField.getText() == null ? "" : filterField.getText().toUpperCase();
        String searchField = searchFieldComboBox.getSelectionModel().getSelectedItem();

        filteredData.setPredicate(customer -> {
            // Status Filter
            String statusFilter = statusComboBox.getSelectionModel().getSelectedItem();
            if (statusFilter != null && !statusFilter.equals("Όλοι")) {
                boolean isActive = statusFilter.equals("Ενεργοί");
                if (customer.getActive() != isActive) {
                    return false;
                }
            }

            // Recommendation Filter
            Recommendation recFilter = recommendationComboBox.getSelectionModel().getSelectedItem();
            if (recFilter != null && recFilter.getId() != 0) {
                if (customer.getRecommendation() != recFilter.getId()) {
                    return false;
                }
            }

            // JobTeam Filter
            JobTeam jobTeamFilter = jobTeamComboBox.getSelectionModel().getSelectedItem();
            if (jobTeamFilter != null && jobTeamFilter.getId() != 0) {
                if (customer.getJobTeam() != jobTeamFilter.getId()) {
                    return false;
                }
            }

            // App Filter
            AppItem appFilter = appComboBox.getSelectionModel().getSelectedItem();
            if(appFilter != null && appFilter.getId() != 0) {
                if(!customer.hasApp(appFilter.getId())) {
                    return false;
                }
            }

            // Free Text Filter
            if (!filterText.isEmpty()) {
                String search1 = filterText; // Simplified for clarity, assuming no greek/english conversion needed for now

                if (searchField == null || searchField.equals("Όλα τα πεδία")) {
                    boolean textMatch = (customer.getName() != null && customer.getName().toUpperCase().contains(search1)) ||
                            (customer.getTitle() != null && customer.getTitle().toUpperCase().contains(search1)) ||
                            (customer.getJob() != null && customer.getJob().toUpperCase().contains(search1)) ||
                            (String.valueOf(customer.getCode()).contains(search1)) ||
                            (customer.getPhone1() != null && customer.getPhone1().contains(search1)) ||
                            (customer.getPhone2() != null && customer.getPhone2().contains(search1)) ||
                            (customer.getMobile() != null && customer.getMobile().contains(search1)) ||
                            (customer.getAfm() != null && customer.getAfm().contains(search1)) ||
                            (customer.getManager() != null && customer.getManager().toUpperCase().contains(search1)) ||
                            (customer.getTown() != null && customer.getTown().toUpperCase().contains(search1));
                    if (!textMatch) {
                        return false;
                    }
                } else {
                    switch (searchField) {
                        case "Όνομα":
                            if (customer.getName() == null || !customer.getName().toUpperCase().contains(search1)) return false;
                            break;
                        case "ΑΦΜ":
                            if (customer.getAfm() == null || !customer.getAfm().contains(search1)) return false;
                            break;
                        case "Πόλη":
                            if (customer.getTown() == null || !customer.getTown().toUpperCase().contains(search1)) return false;
                            break;
                        case "Τηλέφωνο":
                            boolean phoneMatch = (customer.getPhone1() != null && customer.getPhone1().contains(search1)) ||
                                    (customer.getPhone2() != null && customer.getPhone2().contains(search1)) ||
                                    (customer.getMobile() != null && customer.getMobile().contains(search1));
                            if (!phoneMatch) return false;
                            break;
                    }
                }
            }

            return true; // If all filters pass
        });
    }

    public void filterByApp(int appId) {
        this.activeAppFilter = appId;
        applyFilters(filterField.getText());
    }

    public void clearAppFilter() {
        this.activeAppFilter = null;
        applyFilters(filterField.getText());
    }

    public void customerAddNew(ActionEvent actionEvent) throws IOException {
        try {
            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Νέος Πελάτης")) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Parent customerForm = loader.load();
            // Δημιουργία νέου tab για τη δημιουργία του πελάτη
            Tab customerTab = new Tab("Νέος Πελάτης");
            customerTab.setContent(customerForm);

            AddCustomerController controller = loader.getController();
            controller.setMainTabPane(mainTabPane, customerTab);
            controller.setCustomersController(this); // Περνάμε το instance του CustomersController
            String filterValue = filterField.getText();
            if (filterValue != null && filterValue.matches("\\d{9}")) {
                controller.setInitialAFM(filterValue); // Προ-συμπλήρωση ΑΦΜ
            }

            // Προσθήκη του tab στο TabPane
            mainTabPane.getTabs().add(customerTab);
            mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
            customerTab.setOnClosed(event -> {
                refreshTableData(); // Ανανεώνει τη λίστα πελατών
                filteredData = new FilteredList<>(observableList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) ->
                        applyFilters(newValue)
                );

                applyFilters(filterField.getText());

                SortedList<Customer> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
                customerTable.setItems(sortedData);
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void customerUpdate(ActionEvent actionEvent) throws IOException {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        openCustomerInTab(selectedCustomer, null); // null means no specific sub-tab
    }

    public void openCustomerTab(int customerId) {
        System.out.println("customerId: " + customerId);
        refreshTableData(); // Ανανεώνει τη λίστα πελατών
        filteredData = new FilteredList<>(observableList, b -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });

        applyFilters(filterField.getText());

        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);
        // Έλεγχος αν υπάρχει ήδη ανοικτό tab για τον συγκεκριμένο πελάτη
        Customer selectedCustomer = DBHelper.getCustomerDao().getSelectedCustomer(customerId);
        if (selectedCustomer == null) return;
        DBHelper.getCustomerDao().getCustomerDetails(selectedCustomer); // Lazy Loading
        System.out.println("selectedCustomer: " + selectedCustomer);
        try {
            String res = DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                for (Tab tab : mainTabPane.getTabs()) {
                    if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                        mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                        return;
                    }
                }
                // Φόρτωση του FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent customerForm = loader.load();

                // Δημιουργία νέου tab για την ενημέρωση του πελάτη
                Tab customerTab = new Tab(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)));

                customerTab.setContent(customerForm);

                AddCustomerController controller = loader.getController();
                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerForEdit(selectedCustomer);
                controller.setMainTabPane(mainTabPane, customerTab);
                // Προσθήκη του tab στο TabPane
                Platform.runLater(() -> {
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab);
                    System.out.println("Tab added successfully: " + customerTab.getText());
                });

                customerTab.setOnCloseRequest(event -> {
                    DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());

                });

                customerTab.setOnClosed(event -> {
                    refreshTableData(); // Ανανεώνει τη λίστα πελατών
                    filteredData = new FilteredList<>(observableList, b -> true);

                    filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                        applyFilters(newValue);
                    });

                    applyFilters(filterField.getText());

                    SortedList<Customer> sortedData1 = new SortedList<>(filteredData);
                    sortedData1.comparatorProperty().bind(customerTable.comparatorProperty());
                    customerTable.setItems(sortedData1);
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditOption(ActionEvent event) throws IOException {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        customerUpdate(event);
    }


    public void customerDelete(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε τον πελάτη " + selectedCustomer.getName() + ";");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper.getCustomerDao().customerDelete(selectedCustomer.getCode());
            refreshTableData();
        }
    }


    public void customerNewTask(ActionEvent actionEvent) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Εργασίας");
                AddTaskController controller = loader.getController();
                controller.setCustomerId(selectedCustomer.getCode());
                controller.setCustomerName(selectedCustomer.getName());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveTask();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }


    public void customerInfo(ActionEvent actionEvent) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            String msg = "Στοιχεία πελάτη" +
                    "\nΕπωνυμία: " + selectedCustomer.getName() +
                    "\nΤίτλος: " + selectedCustomer.getTitle() +
                    "\nΕπάγγελμα: " + selectedCustomer.getJob() +
                    "\nΔιεύθυνση: " + selectedCustomer.getAddress() +
                    "\nΠόλη: " + selectedCustomer.getTown() +
                    "\nΤ.Κ.: " + selectedCustomer.getPostcode() +
                    "\nΑΦΜ: " + selectedCustomer.getAfm() +
                    "\nEmail: " + selectedCustomer.getEmail() +
                    "\nΤηλέφωνο: " + selectedCustomer.getPhone1() +
                    "\nΚινητό: " + selectedCustomer.getMobile();
            copyTextToClipboard(msg);
        }
    }

    public void customerLabel(ActionEvent actionEvent) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            LabelPrintHelper.printCustomerLabel(selectedCustomer);
        }
    }


    public void viberOpen(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        try {
            File viberPath = new File(System.getenv("LOCALAPPDATA") + "\\Viber\\Viber.exe");
            Desktop.getDesktop().open(viberPath);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedCustomer.getMobile());  // Replace with the desired text
            clipboard.setContent(content);
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();

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
        //ENGLISH_TO_GREEK.put('\u0051', '\u0391');  // uppercase Q
        ENGLISH_TO_GREEK.put('\u0052', '\u03A1');  // uppercase R
        ENGLISH_TO_GREEK.put('\u0053', '\u03A3');  // uppercase S
        ENGLISH_TO_GREEK.put('\u0054', '\u03A4');  // uppercase T
        ENGLISH_TO_GREEK.put('\u0055', '\u0398');  // uppercase U
        ENGLISH_TO_GREEK.put('\u0056', '\u03A9');  // uppercase V
        ENGLISH_TO_GREEK.put('\u0057', '\u03A3');  // uppercase W
        ENGLISH_TO_GREEK.put('\u0058', '\u03A7');  // uppercase X
        ENGLISH_TO_GREEK.put('\u0059', '\u03A5');  // uppercase Y
        ENGLISH_TO_GREEK.put('\u005A', '\u0396');  // uppercase Z
    }

    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
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
        //GREEK_TO_ENGLISH.put('\u0051', '\u0391');  // uppercase Q
        GREEK_TO_ENGLISH.put('\u03A1', '\u0052');  // uppercase Ρ
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase Σ
        GREEK_TO_ENGLISH.put('\u03A4', '\u0054');  // uppercase Τ
        GREEK_TO_ENGLISH.put('\u0398', '\u0055');  // uppercase Θ
        GREEK_TO_ENGLISH.put('\u03A9', '\u0056');  // uppercase Ω
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase ς
        GREEK_TO_ENGLISH.put('\u03A7', '\u0058');  // uppercase Χ
        GREEK_TO_ENGLISH.put('\u03A5', '\u0059');  // uppercase Υ
        GREEK_TO_ENGLISH.put('\u0396', '\u005A');  // uppercase Ζ
    }


    public void taxisClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
                DialogPane dialogPane = loader.load();

                AddLoginController addLoginController = loader.getController();

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Login");
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        addLoginController.tempLogin();
                    }
                    return null;
                });

                dialog.showAndWait();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        } else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            openCustomerInTab(selectedCustomer, "Taxis");
        }
    }

    public void myposClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginFormDas("https://das.mypos.eu/en/login",
                        AppSettings.loadSetting("myposUser"),
                        AppSettings.loadSetting("myposPass"),
                        By.id("username"),
                        By.className("btn-primary"),
                        By.id("password"));
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
        else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            openCustomerInTab(selectedCustomer, "myPOS");
        }
    }

    public void simplyClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm("https://app.simplycloud.gr/Partners",
                        AppSettings.loadSetting("simplyCloudUser"),
                        AppSettings.loadSetting("simplyCloudPass"),
                        By.name("Email"),
                        By.id("Password"),
                        By.id("btnSubmit"));
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

            }
        } else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            openCustomerInTab(selectedCustomer, "Simply");
        }
    }

    public void emblemClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm("https://pool2.emblem.gr/resellers/",
                        AppSettings.loadSetting("emblemUser"),
                        AppSettings.loadSetting("emblemPass"),
                        By.id("inputEmail"),
                        By.id("inputPassword"),
                        By.xpath("//button[@onclick=\"validateLogin()\"]"));
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
        else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            openCustomerInTab(selectedCustomer, "Emblem");
        }
    }

    public void erganiClick(MouseEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        openCustomerInTab(selectedCustomer, "Ergani");
    }

    public void folderClick(ActionEvent event) {
        CustomerFolderManager folderManager = new CustomerFolderManager();

        // Όνομα και ΑΦΜ του πελάτη
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        // Κλήση της μεθόδου για δημιουργία ή άνοιγμα του φακέλου
        folderManager.createOrOpenCustomerFolder(selectedCustomer.getName(), selectedCustomer.getAfm());
    }


    // Μέθοδος για αντιγραφή του περιεχομένου αρχείου στο πρόχειρο
    private void copyFileContentToClipboard(File file) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αντιγραφή.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    // Μέθοδος για άνοιγμα του φακέλου
    private void openFolder(String folderPath) {
        try {
            Desktop.getDesktop().open(new File(folderPath));
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα φακέλου.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void openAnydeskWindow(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anydeskView.fxml"));
            Parent root = loader.load();

            AnydeskViewController controller = loader.getController();
            controller.setCustomer(customer);

            Stage stage = new Stage();
            stage.setTitle("Anydesk IDs for " + customer.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        Notifications notifications = Notifications.create()
                .title("Αντιγραγή στο πρόχειρο")
                .text(msg)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
        notifications.showInformation();
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void clean(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            filterField.setText("");
            customerTable.getSelectionModel().clearSelection();
            btnTaxis.setStyle("-fx-border-color: #005599;");
            btnMypos.setStyle("-fx-border-color: #005599;");
            btnSimply.setStyle("-fx-border-color: #005599;");
            btnEmblem.setStyle("-fx-border-color: #005599;");
            filterField.requestFocus();
        } else if (event.getButton() == MouseButton.SECONDARY) {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
        }
    }

    public void refresh(MouseEvent mouseEvent) {
        refreshTableData();
    }

    public void showBalance(MouseEvent mouseEvent) {
        observableList.removeIf(customer -> customer.getBalance().equals("") || customer.getBalance().isEmpty());
    }

    public void showInactive(MouseEvent mouseEvent) {
        filteredData.setPredicate(customer -> !customer.getActive());
    }

    public void unlock(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
    }

    private void updateCustomerCounter() {
        customerCounterLabel.setText("Πελάτες: " + filteredData.size());
    }

    /**
     * Centralized method to open a customer's details in a new or existing tab.
     * Handles locking, FXML loading, controller initialization, and optional sub-tab selection.
     *
     * @param customer The customer to open.
     * @param tabToSelect The specific sub-tab to select upon opening (e.g., "Taxis", "myPOS"), or null.
     */
    private void openCustomerInTab(Customer customer, String tabToSelect) {
        if (customer == null) {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Δεν έχει επιλεγεί Πελάτης!")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showError();
            return;
        }

        DBHelper.getCustomerDao().getCustomerDetails(customer); // Lazy loading

        try {
            String lockResult = DBHelper.getCustomerDao().checkCustomerLock(customer.getCode(), AppSettings.loadSetting("appuser"));
            if (!lockResult.equals("unlocked")) {
                AlertDialogHelper.showDialog("Προσοχή", lockResult, "", Alert.AlertType.ERROR);
                return;
            }

            DBHelper.getCustomerDao().customerLock(customer.getCode(), AppSettings.loadSetting("appuser"));

            // Find existing tab by checking UserData for the customer's unique ID
            for (Tab tab : mainTabPane.getTabs()) {
                if (Integer.valueOf(customer.getCode()).equals(tab.getUserData())) {
                    mainTabPane.getSelectionModel().select(tab);
                    // If a specific sub-tab needs to be selected, find the controller and call the method
                    if (tabToSelect != null && tab.getContent().getUserData() instanceof AddCustomerController) {
                        AddCustomerController controller = (AddCustomerController) tab.getContent().getUserData();
                        selectSubTab(controller, tabToSelect);
                    }
                    return; // Tab found and selected, exit the method
                }
            }

            // If no tab found, create a new one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Parent customerForm = loader.load();
            AddCustomerController controller = loader.getController();

            String tabTitle = customer.getName().substring(0, Math.min(customer.getName().length(), 18));
            Tab customerTab = new Tab(tabTitle);
            customerTab.setUserData(customer.getCode()); // <-- Set the unique customer ID as user data
            customerTab.setContent(customerForm);
            customerForm.setUserData(controller); // Store controller for later access

            controller.setMainTabPane(mainTabPane, customerTab);
            controller.setCustomerForEdit(customer);

            mainTabPane.getTabs().add(customerTab);
            mainTabPane.getSelectionModel().select(customerTab);

            // Select the specific sub-tab if requested
            if (tabToSelect != null) {
                selectSubTab(controller, tabToSelect);
            }

            customerTab.setOnCloseRequest(event -> {
                DBHelper.getCustomerDao().customerUnlock(customer.getCode());
                if (!controller.handleTabCloseRequest()) {
                    event.consume();
                }
            });

            customerTab.setOnClosed(event -> {
                Customer updatedCustomer = controller.getUpdatedCustomer();
                if (updatedCustomer != null) {
                    int index = -1;
                    for (int i = 0; i < observableList.size(); i++) {
                        if (observableList.get(i).getCode() == updatedCustomer.getCode()) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        observableList.set(index, updatedCustomer);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα της καρτέλας πελάτη.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void selectSubTab(AddCustomerController controller, String tabName) {
        Platform.runLater(() -> {
            switch (tabName) {
                case "Taxis":
                    controller.selectTaxisTab();
                    break;
                case "myPOS":
                    controller.selectMyPOSTab();
                    break;
                case "Simply":
                    controller.selectSimplyTab();
                    break;
                case "Emblem":
                    controller.selectEmbelmTab();
                    break;
                case "Ergani":
                    controller.selectErganiTab();
                    break;
            }
        });
    }
}