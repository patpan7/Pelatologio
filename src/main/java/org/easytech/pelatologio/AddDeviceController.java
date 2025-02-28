package org.easytech.pelatologio;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddDeviceController {

    @FXML
    private TextField serialField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField rateField;
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
        rateField.setText(device.getRate());
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
        setupComboBoxFilter(customerComboBox, filteredCustomers);
        setupComboBoxFilter(itemComboBox, filteredItems);

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
    }

    private <T> void setupComboBoxFilter(ComboBox<T> comboBox, FilteredList<T> filteredList) {
        // Ακροατής για το TextField του ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toLowerCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Εμφάνιση όλων των στοιχείων αν δεν υπάρχει φίλτρο
                }
                // Ελέγχουμε αν το όνομα του αντικειμένου ταιριάζει με το φίλτρο
                return item.toString().toLowerCase().contains(filterText);
            });
        });

        // Ακροατής για την επιλογή ενός στοιχείου
        comboBox.setOnHidden(event -> {
            T selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });

        // Ακροατής για την αλλαγή της επιλογής
        comboBox.setOnAction(event -> {
            T selectedItem = comboBox.getSelectionModel().getSelectedItem();
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
        expandedTextArea.setPrefSize(400, 300);
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

            String description = descriptionField.getText();
            String rate = rateField.getText();
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
}
