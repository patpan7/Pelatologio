package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;

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
    public TableColumn nameColumn, titleColumn, afmColumn, phone1Column, phone2Column, mobileColumn, townColumn, emailColumn;
    @FXML
    StackPane stackPane;
    @FXML
    TableView<Customer> customerTable;
    @FXML
    TextField filterField;
    @FXML
    Button openFileButton;
    @FXML
    Button btnTaxis, btnMypos, btnSimply;

    ObservableList<Customer> observableList;
    FilteredList<Customer> filteredData;
    DBHelper dbHelper;
    private ContextMenu contextMenu = new ContextMenu();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        // Αρχικοποίηση πίνακα
        initializeTable();

        // Διπλό κλικ για επεξεργασία πελάτη
        customerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1){
                btnTaxis.setStyle("-fx-border-color: #D6D8DE;");
                btnMypos.setStyle("-fx-border-color: #D6D8DE;");
                btnSimply.setStyle("-fx-border-color: #D6D8DE;");
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

                if(dbHelper.hasApp(selectedCustomer.getCode(),2)){
                    btnSimply.setStyle("-fx-border-color: #FF0000;");
                }
                if(dbHelper.hasApp(selectedCustomer.getCode(),1)){
                    btnMypos.setStyle("-fx-border-color: #FF0000;");
                }
                if(dbHelper.hasApp(selectedCustomer.getCode(),3)){
                    btnTaxis.setStyle("-fx-border-color: #FF0000;");
                }
            }
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

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
            File folder = new File(AppSettings.loadSetting("datafolder")+"\\Docs");

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
            openFolderItem.setOnAction(e -> openFolder(AppSettings.loadSetting("datafolder")+"\\Docs"));
            contextMenu.getItems().add(openFolderItem);

            // Εμφάνιση του ContextMenu πάνω από το κουμπί
            double buttonX = openFileButton.localToScene(openFileButton.getBoundsInLocal()).getMinX();
            double buttonY = openFileButton.localToScene(openFileButton.getBoundsInLocal()).getMinY() - 2*openFileButton.getHeight();
            contextMenu.show(openFileButton, openFileButton.getScene().getWindow().getX() + buttonX,
                    openFileButton.getScene().getWindow().getY() + buttonY);
        });
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
        observableList.clear();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        applyFilters(filterField.getText());
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

            // Εφαρμογή φίλτρου
            return (customer.getName() != null && (customer.getName().toUpperCase().contains(search1) || customer.getName().toUpperCase().contains(search2)))
                    || (customer.getTitle() != null && (customer.getTitle().toUpperCase().contains(search1) || customer.getTitle().toUpperCase().contains(search2)))
                    || (customer.getJob() != null && (customer.getJob().toUpperCase().contains(search1) || customer.getJob().toUpperCase().contains(search2)))
                    || (String.valueOf(customer.getCode()).contains(search1) || String.valueOf(customer.getCode()).contains(search2))
                    || (customer.getPhone1() != null && (customer.getPhone1().contains(search1) || customer.getPhone1().contains(search2)))
                    || (customer.getPhone2() != null && (customer.getPhone2().contains(search1) || customer.getPhone2().contains(search2)))
                    || (customer.getMobile() != null && (customer.getMobile().contains(search1) || customer.getMobile().contains(search2)))
                    || (customer.getAfm() != null && (customer.getAfm().contains(search1) || customer.getAfm().contains(search2)))
                    || (customer.getManager() != null && (customer.getManager().toUpperCase().contains(search1) || customer.getManager().toUpperCase().contains(search2)));
        });
    }


    public void customerAddNew(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Εισαγωγή Νέου Πελάτη");

            AddNewCustomerController controller = loader.getController();
            String filterValue = filterField.getText();
            if (filterValue != null && filterValue.matches("\\d{9}")) {
                controller.setInitialAFM(filterValue); // Προ-συμπλήρωση ΑΦΜ
            }
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setOnAction(event -> {controller.handleOkButton();
                refreshTableData();
            });
            // Add a key listener to save when Enter is pressed
            dialog.getDialogPane().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    okButton.fire();  // Triggers the OK button action
                    refreshTableData();
                }
            });
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void customerUpdate(ActionEvent actionEvent) throws IOException {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Ενημέρωση Πελάτη");
                dialog.initModality(Modality.WINDOW_MODAL);

                AddNewCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerData(selectedCustomer);

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setOnAction(event -> {
                    controller.handleOkButton();
                    // Reinitialize the table and apply the search filter when OK is pressed
                    //tableInit();
                    refreshTableData();
                    filteredData = new FilteredList<>(observableList, b -> true);

                    filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                        applyFilters(newValue);
                    });

                    applyFilters(filterField.getText());

                    SortedList<Customer> sortedData = new SortedList<>(filteredData);
                    sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
                    customerTable.setItems(sortedData);
                });

                // Προσθήκη listener για το κλείσιμο του παραθύρου
                dialog.setOnHidden(event -> {
                    dbHelper.customerUnlock(selectedCustomer.getCode());
                });

                // Add a key listener to save when Enter is pressed
                dialog.getDialogPane().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        okButton.fire();  // Triggers the OK button action
                    }
                });
                dialog.show();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε τον πελάτη " + selectedCustomer.getName() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dbHelper.customerDelete(selectedCustomer.getCode());
            refreshTableData();
        }
    }

    public void customerNewAppointment(ActionEvent actionEvent) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newAppointment.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Ραντεβού");

                AddAppointmentController controller = loader.getController();

                // Προ-συμπλήρωση πελάτη
                controller.setCustomerId(selectedCustomer.getCode());
                controller.setCustomerName(selectedCustomer.getName());

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveAppointment();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void customerInfo(ActionEvent actionEvent) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            String msg ="Στοιχεία πελάτη" +
                    "\nΕπωνυμία: "+selectedCustomer.getName()+
                    "\nΤίτλος: " + selectedCustomer.getTitle()+
                    "\nΕπλαγγελμα: " + selectedCustomer.getJob()+
                    "\nΔιεύθυνση: " +selectedCustomer.getAddress()+
                    "\nΠόλη: " + selectedCustomer.getTown()+
                    "\nΤ.Κ.: " + selectedCustomer.getPostcode()+
                    "\nΑΦΜ: "+selectedCustomer.getAfm()+
                    "\nEmail: "+selectedCustomer.getEmail()+
                    "\nΤηλέφωνο: " + selectedCustomer.getPhone1()+
                    "\nΚινητό: "+selectedCustomer.getMobile();
            copyTextToClipboard(msg);
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
            e.printStackTrace();
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

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

    public void taxisClick(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taxisView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            TaxisViewController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setCustomer(selectedCustomer);

            dialog.setTitle("Κωδικοί Taxis");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void myposClick(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("myposView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            MyposViewController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setCustomer(selectedCustomer);

            dialog.setTitle("Κωδικοί myPOS");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void simplyClick(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί Πελάτης!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simplyView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            SimplyViewController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setCustomer(selectedCustomer);

            dialog.setTitle("Κωδικοί Simply");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    // Μέθοδος για άνοιγμα του φακέλου
    private void openFolder(String folderPath) {
        try {
            Desktop.getDesktop().open(new File(folderPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        showAlert("Copied to Clipboard", msg);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
