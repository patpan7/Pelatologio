package org.easytech.pelatologio;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPopup;
import javafx.application.Platform;
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
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
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
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.openqa.selenium.By;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

public class CustomersController implements Initializable {
    @FXML
    public TableColumn nameColumn, titleColumn, afmColumn, phone1Column, phone2Column, mobileColumn, townColumn, emailColumn, balanceColumn;
    @FXML
    StackPane stackPane;
    @FXML
    TableView<Customer> customerTable;
    @FXML
    Button filterButton;
    @FXML
    TextField filterField;
    @FXML
    Button btnTaxis, btnMypos, btnSimply, btnEmblem, btnErgani, btnData, openFileButton;

    ObservableList<Customer> observableList;
    FilteredList<Customer> filteredData;
    DBHelper dbHelper;
    List<CheckBox> checkBoxes = new ArrayList<>();
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
        VBox filterBox = new VBox();
        filterBox.setSpacing(5);

        // ** Δημιουργία των CheckBox φίλτρων **
        String[] filters = {"Όνομα", "Τίτλος", "ΑΦΜ", "Αριθμοί επικοινωνίας", "Πόλη", "Υπεύθυνος", "Σύσταση"};

        for (String filter : filters) {
            JFXCheckBox checkBox = new JFXCheckBox(filter);
            //checkBox.applyCss();
            checkBox.getStyleClass().add("normal-label");
            checkBoxes.add(checkBox);
            filterBox.getChildren().add(checkBox);
        }


        // ** Δημιουργία του popup **
        JFXPopup popup = new JFXPopup(filterBox);

        // ** Όταν πατάμε το κουμπί, εμφανίζεται το popup **
        filterButton.setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) {  // Έλεγχος αν έγινε δεξί κλικ
                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setSelected(false);  // Αποεπιλογή όλων των CheckBox
                }
                // Ενημέρωση των φίλτρων αν χρειάζεται
                applyFilters("");  // Άδειασμα του φίλτρου
                filterField.setText("");

            } else
                popup.show(filterButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0,50);
        });


        dbHelper = new DBHelper();

        // Δημιουργία και αρχικοποίηση των στηλών
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        afmColumn.setCellValueFactory(new PropertyValueFactory<>("afm"));
        phone1Column.setCellValueFactory(new PropertyValueFactory<>("phone1"));
        phone2Column.setCellValueFactory(new PropertyValueFactory<>("phone2"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        townColumn.setCellValueFactory(new PropertyValueFactory<>("town"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Αρχικοποίηση πίνακα
        initializeTable();

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

                if (dbHelper.hasApp(selectedCustomer.getCode(), 1)) {
                    btnMypos.setStyle("-fx-border-color: #FF0000;");
                }
                if (dbHelper.hasApp(selectedCustomer.getCode(), 2)) {
                    btnSimply.setStyle("-fx-border-color: #FF0000;");
                }
                if (dbHelper.hasApp(selectedCustomer.getCode(), 3)) {
                    btnTaxis.setStyle("-fx-border-color: #FF0000;");
                }
                if (dbHelper.hasApp(selectedCustomer.getCode(), 4)) {
                    btnEmblem.setStyle("-fx-border-color: #FF0000;");
                }
                if (dbHelper.hasApp(selectedCustomer.getCode(), 5)) {
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
        DBHelper dbHelper = new DBHelper();
        List<Customer> customers;
        try {
            customers = dbHelper.getCustomers();
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
        filteredData.setPredicate(customer -> {
            // ✅ Αγνόησε τους μη ενεργούς πελάτες
            if (!customer.getActive()) {
                return false;
            }

            if (filterValue == null || filterValue.isEmpty()) {
                return true;
            }

            String filter = filterValue.toUpperCase();

            // Υποστήριξη Ελληνικών/Αγγλικών
            char[] chars1 = filter.toCharArray();
            IntStream.range(0, chars1.length).forEach(i -> {
                Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                if (repl != null) chars1[i] = repl;
            });
            char[] chars2 = filter.toCharArray();
            IntStream.range(0, chars2.length).forEach(i -> {
                Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                if (repl != null) chars2[i] = repl;
            });
            String search1 = new String(chars1);
            String search2 = new String(chars2);

            Set<String> selectedFilters = getSelectedFilters(checkBoxes);

            if (selectedFilters.isEmpty()) {
                return (customer.getName() != null && (customer.getName().toUpperCase().contains(search1) || customer.getName().toUpperCase().contains(search2)))
                        || (customer.getTitle() != null && (customer.getTitle().toUpperCase().contains(search1) || customer.getTitle().toUpperCase().contains(search2)))
                        || (customer.getJob() != null && (customer.getJob().toUpperCase().contains(search1) || customer.getJob().toUpperCase().contains(search2)))
                        || (String.valueOf(customer.getCode()).contains(search1) || String.valueOf(customer.getCode()).contains(search2))
                        || (customer.getPhone1() != null && (customer.getPhone1().contains(search1) || customer.getPhone1().contains(search2)))
                        || (customer.getPhone2() != null && (customer.getPhone2().contains(search1) || customer.getPhone2().contains(search2)))
                        || (customer.getMobile() != null && (customer.getMobile().contains(search1) || customer.getMobile().contains(search2)))
                        || (customer.getAfm() != null && (customer.getAfm().contains(search1) || customer.getAfm().contains(search2)))
                        || (customer.getManager() != null && (customer.getManager().toUpperCase().contains(search1) || customer.getManager().toUpperCase().contains(search2)))
                        || (customer.getManagerPhone() != null && (customer.getManagerPhone().toUpperCase().contains(search1) || customer.getManagerPhone().toUpperCase().contains(search2)))
                        || (customer.getEmail() != null && (customer.getEmail().toUpperCase().contains(search1) || customer.getEmail().toUpperCase().contains(search2)))
                        || (customer.getEmail2() != null && (customer.getEmail2().toUpperCase().contains(search1) || customer.getEmail2().toUpperCase().contains(search2)))
                        || (customer.getTown() != null && (customer.getTown().toUpperCase().contains(search1) || customer.getTown().toUpperCase().contains(search2)))
                        || (customer.getAddress() != null && (customer.getAddress().toUpperCase().contains(search1) || customer.getAddress().toUpperCase().contains(search2)));
            }

            if (selectedFilters.contains("Όνομα") && customer.getName() != null && (customer.getName().toUpperCase().contains(search1) || customer.getName().toUpperCase().contains(search2))) {
                return true;
            }
            if (selectedFilters.contains("Τίτλος") && customer.getTitle() != null && (customer.getTitle().toUpperCase().contains(search1) || customer.getTitle().toUpperCase().contains(search2))) {
                return true;
            }
            if (selectedFilters.contains("ΑΦΜ") && customer.getAfm() != null && (customer.getAfm().contains(search1) || customer.getAfm().contains(search2))) {
                return true;
            }
            if (selectedFilters.contains("Αριθμοί επικοινωνίας") && (customer.getPhone1() != null && (customer.getPhone1().contains(search1) || customer.getPhone1().contains(search2))
                    || customer.getPhone2() != null && (customer.getPhone2().contains(search1) || customer.getPhone2().contains(search2))
                    || customer.getMobile() != null && (customer.getMobile().contains(search1) || customer.getMobile().contains(search2)))) {
                return true;
            }
            if (selectedFilters.contains("Πόλη") && customer.getTown() != null && (customer.getTown().toUpperCase().contains(search1) || customer.getTown().toUpperCase().contains(search2))) {
                return true;
            }
            if (selectedFilters.contains("Υπεύθυνος") && customer.getManager() != null && (customer.getManager().toUpperCase().contains(search1) || customer.getManager().toUpperCase().contains(search2))) {
                return true;
            }
            if (selectedFilters.contains("Σύσταση") && customer.getRecommendation() != null && (customer.getRecommendation().toUpperCase().contains(search1) || customer.getRecommendation().toUpperCase().contains(search2))) {
                return true;
            }

            return false;
        });
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
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                for (Tab tab : mainTabPane.getTabs()) {
                    if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                        mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                        return;
                    }
                }

                // Δημιουργία νέου tab
                Tab customerTab = new Tab(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)));
                // Φόρτωση του FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent customerForm = loader.load();

                // Δημιουργία νέου tab για την ενημέρωση του πελάτη
                //Tab customerTab = new Tab(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)));
                customerTab.setContent(customerForm);

                AddCustomerController controller = loader.getController();
                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setMainTabPane(mainTabPane, customerTab);
                controller.setCustomerData(selectedCustomer);

                // Προσθήκη του tab στο TabPane
                mainTabPane.getTabs().add(customerTab);
                mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab

                customerTab.setOnCloseRequest(event -> {
                    dbHelper.customerUnlock(selectedCustomer.getCode());
                    controller.handleTabCloseRequest();
                });

                customerTab.setOnClosed(event -> {
                    refreshTableData(); // Ανανεώνει τη λίστα πελατών
                    filteredData = new FilteredList<>(observableList, b -> true);

                    filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                        applyFilters(newValue);
                    });

                    applyFilters(filterField.getText());

                    SortedList<Customer> sortedData = new SortedList<>(filteredData);
                    sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
                    customerTable.setItems(sortedData);
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
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
        Customer selectedCustomer = dbHelper.getSelectedCustomer(customerId);
        System.out.println("selectedCustomer: " + selectedCustomer);
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
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
                controller.setCustomerData(selectedCustomer);
                controller.setMainTabPane(mainTabPane, customerTab);
                // Προσθήκη του tab στο TabPane
                Platform.runLater(() -> {
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab);
                    System.out.println("Tab added successfully: " + customerTab.getText());
                });

                customerTab.setOnCloseRequest(event -> {
                    dbHelper.customerUnlock(selectedCustomer.getCode());

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
            dbHelper.customerDelete(selectedCustomer.getCode());
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
            if (selectedCustomer == null) {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί Πελάτης!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
                return;
            }
            try {
                String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                if (res.equals("unlocked")) {
                    dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                    // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                    for (Tab tab : mainTabPane.getTabs()) {
                        if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                            mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                            // Πάρε τον controller και άλλαξε tab στο "Taxis"
                            AddCustomerController controller = (AddCustomerController) tab.getUserData();
                            Platform.runLater(() -> controller.selectTaxisTab());
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
                    controller.setCustomerData(selectedCustomer);

                    // Προσθήκη του tab στο TabPane
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                    Platform.runLater(() -> controller.selectTaxisTab());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Προσοχή");
                    alert.setContentText(res);
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
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
        } else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί Πελάτης!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
                return;
            }
            try {
                String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                if (res.equals("unlocked")) {
                    dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                    // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                    for (Tab tab : mainTabPane.getTabs()) {
                        if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                            mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                            // Πάρε τον controller και άλλαξε tab στο "myPOS"
                            AddCustomerController controller = (AddCustomerController) tab.getUserData();
                            Platform.runLater(() -> controller.selectMyPOSTab());
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
                    controller.setCustomerData(selectedCustomer);

                    // Προσθήκη του tab στο TabPane
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                    Platform.runLater(() -> controller.selectMyPOSTab());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Προσοχή");
                    alert.setContentText(res);
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
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
            if (selectedCustomer == null) {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί Πελάτης!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
                return;
            }
            try {
                String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                if (res.equals("unlocked")) {
                    dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                    // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                    for (Tab tab : mainTabPane.getTabs()) {
                        if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                            mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                            // Πάρε τον controller και άλλαξε tab στο "Simply"
                            AddCustomerController controller = (AddCustomerController) tab.getUserData();
                            Platform.runLater(() -> controller.selectSimplyTab());
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
                    controller.setCustomerData(selectedCustomer);

                    // Προσθήκη του tab στο TabPane
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                    Platform.runLater(() -> controller.selectSimplyTab());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Προσοχή");
                    alert.setContentText(res);
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
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
        } else {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί Πελάτης!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
                return;
            }
            try {
                String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                if (res.equals("unlocked")) {
                    dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                    // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                    for (Tab tab : mainTabPane.getTabs()) {
                        if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                            mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                            // Πάρε τον controller και άλλαξε tab στο "Emblem"
                            AddCustomerController controller = (AddCustomerController) tab.getUserData();
                            Platform.runLater(() -> controller.selectEmbelmTab());
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
                    controller.setCustomerData(selectedCustomer);

                    // Προσθήκη του tab στο TabPane
                    mainTabPane.getTabs().add(customerTab);
                    mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                    Platform.runLater(() -> controller.selectEmbelmTab());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Προσοχή");
                    alert.setContentText(res);
                    alert.showAndWait();
                }

            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void erganiClick(MouseEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Δεν έχει επιλεγεί Πελάτης!")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                for (Tab tab : mainTabPane.getTabs()) {
                    if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                        mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                        // Πάρε τον controller και άλλαξε tab στο "Emblem"
                        AddCustomerController controller = (AddCustomerController) tab.getUserData();
                        Platform.runLater(() -> controller.selectErganiTab());
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
                controller.setCustomerData(selectedCustomer);

                // Προσθήκη του tab στο TabPane
                mainTabPane.getTabs().add(customerTab);
                mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                Platform.runLater(() -> controller.selectErganiTab());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
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
            dbHelper.customerUnlock(selectedCustomer.getCode());
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
        dbHelper.customerUnlock(selectedCustomer.getCode());
    }
}
