package org.easytech.pelatologio.devices;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Device;
import org.easytech.pelatologio.models.Item;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

public class AddDeviceController {

    @FXML
    private TextField serialField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> rateField;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<Item> itemComboBox;
    @FXML
    private ListView<String> serialListView;
    @FXML
    private Label serialListTitle;
    @FXML
    private Button addSerialButton;

    private ObservableList<String> serialNumbers = FXCollections.observableArrayList();
    private ObservableList<String> rateList = FXCollections.observableArrayList();

    private Device device;
    private int customerId;
    private String customerName;
    private Customer selectedCustomer;
    private FilteredList<Customer> filteredCustomers;
    private FilteredList<Item> filteredItems;

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String custName) {
        for (Customer customer : customerComboBox.getItems()) {
            if (customer.getName().equals(custName)) {
                customerComboBox.setValue(customer);
                break;
            }
        }
    }

    public void setDeviceForEdit(Device device) {
        this.device = device;
        serialField.setText(device.getSerial());
        descriptionField.setText(device.getDescription());
        for (String rate : rateList) {
            if (rate.equals(device.getRate())) {
                rateField.getSelectionModel().select(rate);
                break;
            }
        }
        for (Item item : itemComboBox.getItems()) {
            if (item.getName().equals(device.getItemName())) {
                itemComboBox.setValue(item);
                break;
            }
        }
        // Αν υπάρχει πελάτης, προ-συμπλήρωσε την επιλογή
        if (device.getCustomerId() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCode() == device.getCustomerId()) {
                    customerComboBox.setValue(customer);
                    break;
                }
            }
        }
        serialListTitle.setVisible(false);
        serialListView.setVisible(false);
        addSerialButton.setVisible(false);
    }

    public void lock() {
        serialField.setEditable(false);
        customerComboBox.setDisable(true);
    }

    public void lockCustomer() {
        customerComboBox.setDisable(true);
        itemComboBox.setDisable(true);
        descriptionField.setDisable(true);
    }

    public void initialize() throws SQLException {
        DBHelper dbHelper = new DBHelper();

        // Φόρτωση πελατών
        List<Customer> customers = dbHelper.getCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers));
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        // Φόρτωση ειδών
        List<Item> items = dbHelper.getItems();
        filteredItems = new FilteredList<>(FXCollections.observableArrayList(items));
        itemComboBox.setItems(filteredItems);
        itemComboBox.setEditable(true);

        // Προσθήκη ακροατών για φιλτράρισμα
        setupComboBoxFilterCust(customerComboBox, filteredCustomers);
        setupComboBoxFilterItem(itemComboBox, filteredItems);

        serialListView.setItems(serialNumbers);

        serialListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Διπλό κλικ για επεξεργασία
                String selectedSerial = serialListView.getSelectionModel().getSelectedItem();
                if (selectedSerial != null) {
                    TextInputDialog dialog = new TextInputDialog(selectedSerial);
                    dialog.setTitle("Επεξεργασία Σειριακού Αριθμού");
                    dialog.setHeaderText("Επεξεργαστείτε τον σειριακό αριθμό");
                    dialog.setContentText("Νέος σειριακός αριθμός:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(newSerial -> {
                        if (!newSerial.trim().isEmpty() && !serialNumbers.contains(newSerial)) {
                            serialNumbers.set(serialNumbers.indexOf(selectedSerial), newSerial);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Ο σειριακός αριθμός είναι κενός ή υπάρχει ήδη!");
                        }
                    });
                }
            }
        });

        serialListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Διαγραφή");
            deleteItem.setOnAction(e -> {
                serialNumbers.remove(cell.getItem());
            });
            contextMenu.getItems().add(deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                cell.setContextMenu(isNowEmpty ? null : contextMenu);
            });
            return cell;
        });

        serialField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                addSerialNumber();
                keyEvent.consume();
            }
        });

        rateList.clear();
        rateList.addAll(dbHelper.getRates());
        rateField.setItems(rateList);
    }

    private <T> void setupComboBoxFilterCust(ComboBox<Customer> comboBox, FilteredList<Customer> filteredList) {
        // Ακροατής για το TextField του ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toUpperCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Εμφάνιση όλων των στοιχείων αν δεν υπάρχει φίλτρο
                }
//                // Ελέγχουμε αν το όνομα του αντικειμένου ταιριάζει με το φίλτρο
//                return item.toString().toLowerCase().contains(filterText);
                // Υποστήριξη Ελληνικών/Αγγλικών
                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);

                // Αν δεν είναι επιλεγμένο κανένα φίλτρο, κάνε αναζήτηση σε όλα τα πεδία
                return (item.getName() != null && (item.getName().toUpperCase().contains(search1) || item.getName().toUpperCase().contains(search2)))
                        || (item.getTitle() != null && (item.getTitle().toUpperCase().contains(search1) || item.getTitle().toUpperCase().contains(search2)))
                        || (item.getJob() != null && (item.getJob().toUpperCase().contains(search1) || item.getJob().toUpperCase().contains(search2)))
                        || (String.valueOf(item.getCode()).contains(search1) || String.valueOf(item.getCode()).contains(search2))
                        || (item.getPhone1() != null && (item.getPhone1().contains(search1) || item.getPhone1().contains(search2)))
                        || (item.getPhone2() != null && (item.getPhone2().contains(search1) || item.getPhone2().contains(search2)))
                        || (item.getMobile() != null && (item.getMobile().contains(search1) || item.getMobile().contains(search2)))
                        || (item.getAfm() != null && (item.getAfm().contains(search1) || item.getAfm().contains(search2)))
                        || (item.getManager() != null && (item.getManager().toUpperCase().contains(search1) || item.getManager().toUpperCase().contains(search2)))
                        || (item.getTown() != null && (item.getTown().toUpperCase().contains(search1) || item.getTown().toUpperCase().contains(search2)));

            });
        });

        // Ακροατής για την επιλογή ενός στοιχείου
        comboBox.setOnHidden(event -> {
            Customer selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });

        // Ακροατής για την αλλαγή της επιλογής
        comboBox.setOnAction(event -> {
            Customer selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });
    }

    private <T> void setupComboBoxFilterItem(ComboBox<Item> comboBox, FilteredList<Item> filteredList) {
        // Ακροατής για το TextField του ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toUpperCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Εμφάνιση όλων των στοιχείων αν δεν υπάρχει φίλτρο
                }
//                // Ελέγχουμε αν το όνομα του αντικειμένου ταιριάζει με το φίλτρο
//                return item.toString().toLowerCase().contains(filterText);
                // Υποστήριξη Ελληνικών/Αγγλικών
                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);

                // Αν δεν είναι επιλεγμένο κανένα φίλτρο, κάνε αναζήτηση σε όλα τα πεδία
                return (item.getName() != null && (item.getName().toUpperCase().contains(search1) || item.getName().toUpperCase().contains(search2)))
                        || (item.getDescription() != null && (item.getDescription().toUpperCase().contains(search1) || item.getDescription().toUpperCase().contains(search2)))
                        || (String.valueOf(item.getId()).contains(search1) || String.valueOf(item.getId()).contains(search2));

            });
        });

        // Ακροατής για την επιλογή ενός στοιχείου
        comboBox.setOnHidden(event -> {
            Item selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });

        // Ακροατής για την αλλαγή της επιλογής
        comboBox.setOnAction(event -> {
            Item selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(descriptionField.getText());
        }
    }

    @FXML
    private void addSerialNumber() {
        String serial = serialField.getText().trim();
        if (serial.isEmpty()) {
            //showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Το πεδίο σειριακού αριθμού είναι κενό!");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Σφάλμα")
                        .text("Το πεδίο σειριακού αριθμού είναι κενό!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }

        if (serialNumbers.contains(serial)) {
            //showAlert(Alert.AlertType.WARNING, "Προειδοποίηση", "Ο σειριακός αριθμός έχει ήδη προστεθεί!");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προειδοποίηση")
                        .text("Ο σειριακός αριθμός έχει ήδη προστεθεί!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            serialField.setText("");
            return;
        }

        serialNumbers.add(serial);
        serialField.clear();
    }


    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Σημειώσεων");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(600, 500);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            descriptionField.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);
        //vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }


    public boolean handleSaveDevice() {
        try {
            if (serialField.getText() != null) {
                String serial = serialField.getText().trim();
                if (!serial.isEmpty()) {
                    if (!serialNumbers.contains(serial)){
                        serialNumbers.add(serial);
                        serialField.clear();
                    }
                }
            }
            String description;
            String rate = rateField.getEditor().getText().trim();
            if (rate.isEmpty())
                description = descriptionField.getText();
            else
                description = "TID: " + descriptionField.getText();

            // Επιλογή πελάτη
            Object value = customerComboBox.getValue();
            Customer selectedCustomer = null;

            if (value instanceof Customer) {
                selectedCustomer = (Customer) value;
            } else if (value instanceof String) {
                String customerName = (String) value;
                selectedCustomer = customerComboBox.getItems().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(customerName))
                        .findFirst()
                        .orElse(null);
            }

            // Επιλογή είδους
            Object itemValue = itemComboBox.getValue();
            Item selectedItem = null;

            if (itemValue instanceof Item) {
                selectedItem = (Item) itemValue;
            } else if (itemValue instanceof String) {
                String itemName = (String) itemValue;
                selectedItem = itemComboBox.getItems().stream()
                        .filter(i -> i.getName().equalsIgnoreCase(itemName))
                        .findFirst()
                        .orElse(null);
            }

            if (selectedItem == null) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Πρέπει να επιλέξετε ένα είδος!");
                return false;
            }

            int itemId = selectedItem.getId();
            DBHelper dbHelper = new DBHelper();


            if (device == null) {
                // Έλεγχος μοναδικότητας του σειριακού αριθμού
                List<String> invalidSerials = new ArrayList<>();
                for (String serial : serialNumbers) {
                    if (!dbHelper.isSerialUnique(serial)) {
                        Platform.runLater(() -> {
                            Notifications notifications = Notifications.create()
                                    .title("Σφάλμα")
                                    .text("Ο σειριακός αριθμός " + serial + " υπάρχει ήδη!")
                                    .graphic(null)
                                    .hideAfter(Duration.seconds(5))
                                    .position(Pos.TOP_RIGHT);
                            notifications.showError();
                        });
                        invalidSerials.add(serial);
                    }
                }
                // Αφαίρεση μη έγκυρων σειριακών αριθμών μετά το loop
                serialNumbers.removeAll(invalidSerials);
                // Αν μετά την αφαίρεση η λίστα είναι άδεια, σταματάμε
                if (serialNumbers.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Προειδοποίηση", "Δεν υπάρχουν έγκυροι σειριακοί αριθμοί για αποθήκευση.");
                    return false;
                }

                // Δημιουργία νέας συσκευής
                for (String serial : serialNumbers) {
                    Device newDevice = new Device(0, serial, description, rate, itemId, selectedCustomer != null ? selectedCustomer.getCode() : 0);
                    if (dbHelper.saveDevice(newDevice)) {
                        Platform.runLater(() -> {
                            Notifications notifications = Notifications.create()
                                    .title("Επιτυχία")
                                    .text("Η συσκευή με σειριακό " + serial + " προστέθηκε!")
                                    .graphic(null)
                                    .hideAfter(Duration.seconds(5))
                                    .position(Pos.TOP_RIGHT);
                            notifications.showInformation();
                        });
                    }
                }
            } else {
                // Ενημέρωση υπάρχουσας συσκευής
                device.setSerial(serialNumbers.get(0));
                System.out.println(serialNumbers.get(0));
                device.setDescription(description);
                device.setRate(rate);
                device.setItemId(itemId);
                int customerId = selectedCustomer != null ? selectedCustomer.getCode() : 0;
                device.setCustomerId(customerId);
                dbHelper.updateDevice(device);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της συσκευής!");
            return false;
        }
    }

    public boolean handleAssignDevice() {
        try {
            if (serialField.getText() != null) {
                String serial = serialField.getText().trim();
                if (!serial.isEmpty()) {
                    if (!serialNumbers.contains(serial)){
                        serialNumbers.add(serial);
                        serialField.clear();
                    }
                }
            }

            DBHelper dbHelper = new DBHelper();

            // Έλεγχος μοναδικότητας του σειριακού αριθμού
            List<String> invalidSerials = new ArrayList<>();
            for (String serial : serialNumbers) {
                if (dbHelper.isSerialAssigned(serial, customerId)) {
                    Platform.runLater(() -> {
                        Notifications notifications = Notifications.create()
                                .title("Σφάλμα")
                                .text("Ο σειριακός αριθμός " + serial + " ανήκει σε άλλον πελάτη!")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showError();
                    });
                    invalidSerials.add(serial);
                }
                if (dbHelper.isSerialUnique(serial)) {
                    Platform.runLater(() -> {
                        Notifications notifications = Notifications.create()
                                .title("Σφάλμα")
                                .text("Ο σειριακός αριθμός " + serial + " δεν υπάρχει!")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showError();
                    });
                    invalidSerials.add(serial);
                }
            }
            // Αφαίρεση μη έγκυρων σειριακών αριθμών μετά το loop
            serialNumbers.removeAll(invalidSerials);
            // Αν μετά την αφαίρεση η λίστα είναι άδεια, σταματάμε
            if (serialNumbers.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Προειδοποίηση", "Δεν υπάρχουν έγκυροι σειριακοί αριθμοί για αποθήκευση.");
                return false;
            }

            // Δημιουργία νέας συσκευής
            for (String serial : serialNumbers) {
                if (dbHelper.assignDevice(serial, customerId)) {
                    Platform.runLater(() -> {
                        Notifications notifications = Notifications.create()
                                .title("Επιτυχία")
                                .text("Η συσκευή με σειριακό " + serial + " προστέθηκε!")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showInformation();
                    });
                }
            }
            return true;

        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της συσκευής.", e.getMessage(), Alert.AlertType.ERROR));
            return false;
        }
    }


    // Μέθοδος για εμφάνιση Alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
