package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
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

    ObservableList<Customer> observableList;
    FilteredList<Customer> filteredData;
    DBHelper dbHelper;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbHelper = new DBHelper();

        // Δημιουργία και αρχικοποίηση των στηλών
//        TableColumn<Customer, String> codeColumn = new TableColumn<>("Κωδικός");
//        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

        //TableColumn<Customer, String> nameColumn = new TableColumn<>("Όνομα");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        //TableColumn<Customer, String> titleColumn = new TableColumn<>("Τίτλος");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //TableColumn<Customer, String> afmColumn = new TableColumn<>("ΑΦΜ");
        afmColumn.setCellValueFactory(new PropertyValueFactory<>("afm"));

//        TableColumn<Customer, String> jobColumn = new TableColumn<>("Επάγγελμα");
//        jobColumn.setCellValueFactory(new PropertyValueFactory<>("job"));

        //TableColumn<Customer, String> phone1Column = new TableColumn<>("Τηλέφωνο 1");
        phone1Column.setCellValueFactory(new PropertyValueFactory<>("phone1"));

        //TableColumn<Customer, String> phone2Column = new TableColumn<>("Τηλέφωνο 2");
        phone2Column.setCellValueFactory(new PropertyValueFactory<>("phone2"));

        //TableColumn<Customer, String> mobileColumn = new TableColumn<>("Κινητό");
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));

//        TableColumn<Customer, String> addressColumn = new TableColumn<>("Διεύθυνση");
//        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        //TableColumn<Customer, String> townColumn = new TableColumn<>("Πόλη");
        townColumn.setCellValueFactory(new PropertyValueFactory<>("town"));

        //TableColumn<Customer, String> emailColumn = new TableColumn<>("E-mail");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

//        TableColumn<Customer, String> managerColumn = new TableColumn<>("Υπεύθυνος");
//        managerColumn.setCellValueFactory(new PropertyValueFactory<>("manager"));

//        TableColumn<Customer, String> managerPhoneColumn = new TableColumn<>("Τηλ υπευθύνου");
//        managerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("managerPhone"));

        // Προσθήκη των κολόνων στο TableView
        //customerTable.getColumns().addAll(nameColumn, titleColumn, afmColumn, phone1Column, phone2Column, mobileColumn, townColumn, emailColumn);

        tableInit(); // Υποθέτουμε ότι είναι μια μέθοδος για αρχικοποίηση του πίνακα

        // Ορισμός του TableView resize policy
        //customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Δημιουργία της FilteredList για την αναζήτηση
        filteredData = new FilteredList<>(observableList, b -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(Customer -> {
                // If filter text is empty, display all persons.

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filter = newValue.toUpperCase();
                char[] chars1 = filter.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) {
                        chars1[i] = repl;
                    } else return;
                });
                char[] chars2 = filter.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) {
                        chars2[i] = repl;
                    } else return;
                });
                String newValToSearch1 = new String(chars1);
                String newValToSearch2 = new String(chars2);

                if (Customer.getName().toUpperCase().indexOf(newValToSearch1) != -1 || Customer.getName().toUpperCase().indexOf(newValToSearch2) != -1)
                    return true; // Filter matches first name.
                else if (String.valueOf(Customer.getTitle()).indexOf(newValToSearch1) != -1 || String.valueOf(Customer.getTitle()).indexOf(newValToSearch2) != -1)
                    return true; // Filter matches last name.
                else if (String.valueOf(Customer.getCode()).indexOf(newValToSearch1) != -1 || String.valueOf(Customer.getCode()).indexOf(newValToSearch2) != -1)
                    return true; // Filter matches last name.
                else if (Customer.getPhone1().toLowerCase().indexOf(newValToSearch1) != -1 || Customer.getPhone1().toLowerCase().indexOf(newValToSearch2) != -1)
                    return true;
                else if (Customer.getPhone2().toLowerCase().indexOf(newValToSearch1) != -1 || Customer.getPhone2().toLowerCase().indexOf(newValToSearch2) != -1)
                    return true;
                else if (Customer.getMobile().toLowerCase().indexOf(newValToSearch1) != -1 || Customer.getMobile().toLowerCase().indexOf(newValToSearch2) != -1)
                    return true;
                else if (Customer.getAfm().toLowerCase().indexOf(newValToSearch1) != -1 || Customer.getAfm().toLowerCase().indexOf(newValToSearch2) != -1)
                    return true;
                else
                    return false; // Does not match.
            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<Customer> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        // 	  Otherwise, sorting the TableView would have no effect.
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        customerTable.setItems(sortedData);

        // Διπλό κλικ για επεξεργασία πελάτη
        customerTable.setOnMouseClicked(event -> {
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
    }


    private void tableInit() {
        List<Customer> items1 = null;
        try {
            items1 = fetchDataFromMySQL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Προσθήκη των προϊόντων στο ObservableList για την παρακολούθηση των αλλαγών
        observableList = FXCollections.observableArrayList(items1);
        customerTable.setItems(observableList);
        customerTable.setRowFactory(tv -> new TableRow<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (customer == null) {
                    ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setSaturation(-1.0); // Ανενεργό εφέ
                    setEffect(colorAdjust);
                } else {
                    setEffect(null);
                }
            }
        });
//        observableList.sort(Comparator.comparingInt(Customer::getEnable).reversed());
        customerTable.setItems(observableList);
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


    public void customerAddNew(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Εισαγωγή Νέου Πελάτη");

            AddNewCustomerController controller = loader.getController();

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setOnAction(event -> controller.handleOkButton());
            // Add a key listener to save when Enter is pressed
            dialog.getDialogPane().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    okButton.fire();  // Triggers the OK button action
                }
            });
            dialog.showAndWait();
            tableInit();
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

                AddNewCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerData(selectedCustomer);


                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.setOnAction(event -> {
                    controller.handleOkButton();
                    // Reinitialize the table and apply the search filter when OK is pressed
                    tableInit();
                    filteredData = new FilteredList<>(observableList, b -> true);

                    filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                        applyFiler(newValue);
                    });

                    applyFiler(filterField.getText());

                    SortedList<Customer> sortedData = new SortedList<>(filteredData);
                    sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
                    customerTable.setItems(sortedData);
                });
                // Add a key listener to save when Enter is pressed
                dialog.getDialogPane().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        okButton.fire();  // Triggers the OK button action
                    }
                });
                dialog.showAndWait();
                //customerTable.getSelectionModel().clearSelection();
                dbHelper.customerUnlock(selectedCustomer.getCode());
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

    private void applyFiler(String newValue) {
        filteredData.setPredicate(Customer -> {
            if (newValue == null || newValue.isEmpty()) return true;
            String filter = newValue.toUpperCase();
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

            return Customer.getName().toUpperCase().contains(search1) || Customer.getName().toUpperCase().contains(search2)
                    || Customer.getTitle().contains(search1) || Customer.getTitle().contains(search2)
                    || String.valueOf(Customer.getCode()).contains(search1) || String.valueOf(Customer.getCode()).contains(search2)
                    || Customer.getPhone1().contains(search1) || Customer.getPhone1().contains(search2)
                    || Customer.getPhone2().contains(search1) || Customer.getPhone2().contains(search2)
                    || Customer.getMobile().contains(search1) || Customer.getMobile().contains(search2)
                    || Customer.getAfm().contains(search1) || Customer.getAfm().contains(search2);
        });
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

            dialog.showAndWait();

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

            dialog.showAndWait();

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

            dialog.showAndWait();

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
}
