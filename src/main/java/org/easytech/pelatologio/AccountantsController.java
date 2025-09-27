package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Accountant;
import org.easytech.pelatologio.models.Supplier;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class AccountantsController implements Initializable {
    @FXML
    public TableColumn nameColumn, phoneColumn, mobileColumn, emailColumn;
    @FXML
    StackPane stackPane;
    @FXML
    TableView<Accountant> accountantTable;
    @FXML
    TextField filterField;
    private AddCustomerController addCustomerController;

    ObservableList<Accountant> observableList;
    FilteredList<Accountant> filteredData;
    DBHelper dbHelper;

    private TabPane mainTabPane;  // Θα το περάσουμε από τον MainMenuController

    // Μέθοδος για να περάσουμε το TabPane
    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    public void setAddCustomerController(AddCustomerController addCustomerController) {
        this.addCustomerController = addCustomerController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());

        dbHelper = new DBHelper();

        // Δημιουργία και αρχικοποίηση των στηλών
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Αρχικοποίηση πίνακα
        initializeTable();

        // Διπλό κλικ για επεξεργασία πελάτη
        accountantTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedAccountant != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        accountantUpdate(new ActionEvent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        accountantTable.getSelectionModel().clearSelection();
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
        SortedList<Accountant> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(accountantTable.comparatorProperty());
        accountantTable.setItems(sortedData);
    }

    private void refreshTableData() {
        // Αποθήκευση των ρυθμίσεων ταξινόμησης
        List<TableColumn<Accountant, ?>> sortOrder = new ArrayList<>(accountantTable.getSortOrder());

        observableList.clear();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        applyFilters(filterField.getText());
        // Επαναφορά των ρυθμίσεων ταξινόμησης
        accountantTable.getSortOrder().setAll(sortOrder);
    }

    private List<Accountant> fetchDataFromMySQL() throws SQLException {
        List<Accountant> accountants;
        accountants = DBHelper.getAccountantDao().getAccountants();
        return accountants;
    }

    private void setupFilter() {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });
        applyFilters(filterField.getText()); // Αρχική εφαρμογή φίλτρου
    }

    private void applyFilters(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            filteredData.setPredicate(accountant -> true);
            return;
        }

        String search1 = AppUtils.toGreek(filterValue);
        String search2 = AppUtils.toEnglish(filterValue);

        // Εφαρμογή φίλτρου
        filteredData.setPredicate(accountant -> (accountant.getName() != null && (accountant.getName().toUpperCase().contains(search1) || accountant.getName().toUpperCase().contains(search2))) || (accountant.getPhone() != null && (accountant.getPhone().contains(search1) || accountant.getPhone().contains(search2))) || (accountant.getMobile() != null && (accountant.getMobile().contains(search1) || accountant.getMobile().contains(search2))) || (accountant.getEmail() != null && (accountant.getEmail().toUpperCase().contains(search1) || accountant.getEmail().toUpperCase().contains(search2))));
    }

    public void accountantAddNew(ActionEvent actionEvent) throws IOException {
        try {
            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Νέος Λογιστής")) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newAccountant.fxml"));
            Parent accountantForm = loader.load();
            // Δημιουργία νέου tab για τη δημιουργία του πελάτη
            Tab accountantTab = new Tab("Νέος Λογιστής");
            accountantTab.setContent(accountantForm);

            AddAccountantController controller = loader.getController();
            controller.setMainTabPane(mainTabPane);
            controller.setAccountantsController(this); // Περνάμε το instance του CustomersController
            String filterValue = filterField.getText();

            // Προσθήκη του tab στο TabPane
            mainTabPane.getTabs().add(accountantTab);
            mainTabPane.getSelectionModel().select(accountantTab); // Επιλογή του νέου tab
            accountantTab.setOnClosed(event -> {
                refreshTableData(); // Ανανεώνει τη λίστα πελατών
                filteredData = new FilteredList<>(observableList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters(newValue));

                applyFilters(filterField.getText());

                SortedList<Accountant> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(accountantTable.comparatorProperty());
                accountantTable.setItems(sortedData);
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void accountantUpdate(ActionEvent actionEvent) throws IOException {
        Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();
        try {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals(selectedAccountant.getName().substring(0, Math.min(selectedAccountant.getName().length(), 18)))) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newAccountant.fxml"));
            Parent accountantForm = loader.load();

            // Δημιουργία νέου tab για την ενημέρωση του πελάτη
            Tab accountantTab = new Tab(selectedAccountant.getName().substring(0, Math.min(selectedAccountant.getName().length(), 18)));
            accountantTab.setContent(accountantForm);

            AddAccountantController controller = loader.getController();
            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setAccountantData(selectedAccountant);
            controller.setMainTabPane(mainTabPane);
            // Προσθήκη του tab στο TabPane
            mainTabPane.getTabs().add(accountantTab);
            mainTabPane.getSelectionModel().select(accountantTab); // Επιλογή του νέου tab

            accountantTab.setOnCloseRequest(event -> {
                refreshTableData();
                filteredData = new FilteredList<>(observableList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                    applyFilters(newValue);
                });
                applyFilters(filterField.getText());

                SortedList<Accountant> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(accountantTable.comparatorProperty());
                accountantTable.setItems(sortedData);
            });

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void openAccountantTab(int accountantId) {
        Accountant selectedAccountant = DBHelper.getAccountantDao().getSelectedAccountant(accountantId);
        if (selectedAccountant == null) {
            AlertDialogHelper.showErrorDialog("Σφάλμα", "Δεν βρέθηκε ο λογιστής με ID: " + accountantId);
            return;
        }

        try {
            // Check if a tab for this accountant is already open
            String tabTitle = selectedAccountant.getName().substring(0, Math.min(selectedAccountant.getName().length(), 18));
            for (Tab tab : mainTabPane.getTabs()) {
                if (tabTitle.equals(tab.getText())) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            // If not, create a new tab
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newAccountant.fxml"));
            Parent accountantForm = loader.load();

            Tab accountantTab = new Tab(tabTitle);
            accountantTab.setContent(accountantForm);

            AddAccountantController controller = loader.getController();
            controller.setAccountantData(selectedAccountant);
            controller.setMainTabPane(mainTabPane);

            mainTabPane.getTabs().add(accountantTab);
            mainTabPane.getSelectionModel().select(accountantTab);

            accountantTab.setOnClosed(event -> refreshTableData());

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα της καρτέλας του λογιστή.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditOption(ActionEvent event) throws IOException {
        Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();

        if (selectedAccountant == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Λογιστής!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        accountantUpdate(event);
    }


    public void viberOpen(ActionEvent event) {
        Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();
        AppUtils.viberComunicate(selectedAccountant.getMobile());
    }


    public void clean(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            filterField.setText("");
            accountantTable.getSelectionModel().clearSelection();
            filterField.requestFocus();
        } else if (event.getButton() == MouseButton.SECONDARY) {
            Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();
            DBHelper.getCustomerDao().customerUnlock(selectedAccountant.getId());
        }
    }

    public void refresh(MouseEvent mouseEvent) {
        refreshTableData();
    }
}