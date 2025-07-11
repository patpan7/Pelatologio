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
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Accountant;

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
        DBHelper dbHelper = new DBHelper();
        List<Accountant> accountants;
        accountants = dbHelper.getAccountants();
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

        String filter = filterValue.toUpperCase();

        // Υποστήριξη Ελληνικών/Αγγλικών
        char[] chars1 = filter.toCharArray();
        for (int i = 0; i < chars1.length; i++) {
            Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
            if (repl != null) {
                chars1[i] = repl;
            }
        }
        char[] chars2 = filter.toCharArray();
        for (int i = 0; i < chars2.length; i++) {
            Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
            if (repl != null) {
                chars2[i] = repl;
            }
        }
        String search1 = new String(chars1);
        String search2 = new String(chars2);

        // Εφαρμογή φίλτρου
        filteredData.setPredicate(accountant ->
                (accountant.getName() != null && (accountant.getName().toUpperCase().contains(search1) || accountant.getName().toUpperCase().contains(search2)))
                        || (accountant.getPhone() != null && (accountant.getPhone().contains(search1) || accountant.getPhone().contains(search2)))
                        || (accountant.getMobile() != null && (accountant.getMobile().contains(search1) || accountant.getMobile().contains(search2)))
                        || (accountant.getEmail() != null && (accountant.getEmail().toUpperCase().contains(search1) || accountant.getEmail().toUpperCase().contains(search2)))
        );
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

                filterField.textProperty().addListener((observable, oldValue, newValue) ->
                        applyFilters(newValue)
                );

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
                dbHelper.customerUnlock(selectedAccountant.getId());
            });

            accountantTab.setOnClosed(event -> {
                refreshTableData(); // Ανανεώνει τη λίστα πελατών
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
        refreshTableData(); // Ανανεώνει τη λίστα πελατών
        filteredData = new FilteredList<>(observableList, b -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });

        applyFilters(filterField.getText());

        SortedList<Accountant> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(accountantTable.comparatorProperty());
        accountantTable.setItems(sortedData);
        // Έλεγχος αν υπάρχει ήδη ανοικτό tab για τον συγκεκριμένο πελάτη
        Accountant selectedAccountant = dbHelper.getSelectedAccountant(accountantId);
        System.out.println("selectedCustomer: " + selectedAccountant);
        try {
            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
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
            Platform.runLater(() -> {
                mainTabPane.getTabs().add(accountantTab);
                mainTabPane.getSelectionModel().select(accountantTab);
                System.out.println("Tab added successfully: " + accountantTab.getText());
            });

            accountantTab.setOnCloseRequest(event -> {
                dbHelper.customerUnlock(selectedAccountant.getId());

            });

            accountantTab.setOnClosed(event -> {
                refreshTableData(); // Ανανεώνει τη λίστα πελατών
                filteredData = new FilteredList<>(observableList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                    applyFilters(newValue);
                });

                applyFilters(filterField.getText());

                SortedList<Accountant> sortedData1 = new SortedList<>(filteredData);
                sortedData1.comparatorProperty().bind(accountantTable.comparatorProperty());
                accountantTable.setItems(sortedData1);
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
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
        try {
            File viberPath = new File(System.getenv("LOCALAPPDATA") + "\\Viber\\Viber.exe");
            Desktop.getDesktop().open(viberPath);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedAccountant.getMobile());  // Replace with the desired text
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

    public void clean(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            filterField.setText("");
            accountantTable.getSelectionModel().clearSelection();
            filterField.requestFocus();
        } else if (event.getButton() == MouseButton.SECONDARY) {
            Accountant selectedAccountant = accountantTable.getSelectionModel().getSelectedItem();
            dbHelper.customerUnlock(selectedAccountant.getId());
        }
    }

    public void refresh(MouseEvent mouseEvent) {
        refreshTableData();
    }
}