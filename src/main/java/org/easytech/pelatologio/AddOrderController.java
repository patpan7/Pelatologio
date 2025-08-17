package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.ComboBoxHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Order;
import org.easytech.pelatologio.models.Supplier;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class AddOrderController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<Supplier> supplierComboBox;
    @FXML
    private JFXButton btnCustomer, btnSupplier;
    @FXML
    private JFXCheckBox is_completed, is_ergent, is_wait, is_received, is_delivered;

    private Order order;
    private int customerId;
    private FilteredList<Customer> filteredCustomers;
    private FilteredList<Supplier> filteredSuppliers;

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

    public void setSupplierName(String supName) {
        for (Supplier supplier : supplierComboBox.getItems()) {
            if (supplier.getName().equals(supName)) {
                supplierComboBox.setValue(supplier);
                break;
            }
        }
    }

    public void setOrderForEdit(Order order) {
        this.order = order;
        titleField.setText(order.getTitle());
        descriptionField.setText(order.getDescription());
        dueDatePicker.setValue(order.getDueDate());

        // Αν υπάρχει πελάτης, προ-συμπλήρωσε την επιλογή
        if (order.getCustomerId() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCode() == order.getCustomerId()) {
                    customerComboBox.setValue(customer);
                    break;
                }
            }
        }

        if (order.getSupplierId() != null) {
            for (Supplier supplier : supplierComboBox.getItems()) {
                if (supplier.getId() == order.getSupplierId()) {
                    supplierComboBox.setValue(supplier);
                    break;
                }
            }
        }

        is_completed.setSelected(order.getCompleted());
        is_ergent.setSelected(order.getErgent());
        is_wait.setSelected(order.getWait());
        is_received.setSelected(order.getReceived());
        is_delivered.setSelected(order.getDelivered());
    }


    public void initialize() throws SQLException {
        // Φόρτωση πελατών
        List<Customer> customers = DBHelper.getCustomerDao().getCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers));
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        // StringConverter για σωστή διαχείριση αντικειμένων
        customerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return customers.stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        List<Supplier> suppliers = DBHelper.getSupplierDao().getSuppliers();
        filteredSuppliers = new FilteredList<>(FXCollections.observableArrayList(suppliers));
        supplierComboBox.setItems(filteredSuppliers);
        supplierComboBox.setEditable(true);
        // StringConverter για σωστή διαχείριση αντικειμένων
        supplierComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                return suppliers.stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        ComboBoxHelper.setupFilter(customerComboBox, filteredCustomers);
        customerComboBox.setVisibleRowCount(5);
        ComboBoxHelper.setupFilter(supplierComboBox, filteredSuppliers);
        supplierComboBox.setVisibleRowCount(5);

        dueDatePicker.setValue(LocalDate.now());
        CheckBox[] checkBoxes = {
                is_ergent,
                is_wait
        };
        configureSingleSelectionCheckBoxes(checkBoxes);
    }

    public boolean handleSaveOrder() {
        try {
            if (dueDatePicker.getValue() == null || titleField.getText() == null || descriptionField.getText() == null) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();
                });
                return false;
            }

            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate date = dueDatePicker.getValue();
            Customer selectedCustomer = customerComboBox.getValue(); // Απευθείας χρήση του ComboBox
            Supplier selectedSupplier = supplierComboBox.getValue(); // Απευθείας χρήση του ComboBox
            Boolean isCompleted = is_completed.isSelected();
            Boolean isErgent = is_ergent.isSelected();
            Boolean isWait = is_wait.isSelected();
            Boolean isReceived = is_received.isSelected();
            Boolean isDelivered = is_delivered.isSelected();

            if (order == null) {
                // Δημιουργία νέας εργασίας
                Order newOrder = new Order();
                newOrder.setId(0);
                newOrder.setTitle(title);
                newOrder.setTitle(title);
                newOrder.setDescription(description);
                newOrder.setDueDate(date);
                newOrder.setCompleted(false);
                newOrder.setCustomerId(selectedCustomer != null ? selectedCustomer.getCode() : 0);
                newOrder.setSupplierId(selectedSupplier != null ? selectedSupplier.getId() : 0);
                newOrder.setErgent(isErgent);
                newOrder.setWait(isWait);

                //Tasks newTasks = new Tasks(0, title, description, date, false, category, selectedCustomer != null ? selectedCustomer.getCode() : 0, isErgent, isWait);
                DBHelper.getOrderDao().saveOrder(newOrder);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                order.setTitle(title);
                order.setDescription(description);
                order.setDueDate(date);
                int customerId = selectedCustomer != null ? selectedCustomer.getCode() : 0;
                order.setCustomerId(customerId);
                int supplierId = selectedSupplier != null ? selectedSupplier.getId() : 0;
                order.setSupplierId(supplierId);
                order.setErgent(isErgent);
                order.setWait(isWait);
                order.setCompleted(isCompleted);
                order.setReceived(isReceived);
                order.setDelivered(isDelivered);
                DBHelper.getOrderDao().updateOrder(order);
            }

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Η παραγγελία αποθηκεύτηκε!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();
            });
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
            return false;
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(descriptionField.getText());
        }
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

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void showCustomer(ActionEvent evt) {
        DBHelper dbHelper = new DBHelper();

        Customer selectedCustomer = DBHelper.getCustomerDao().getSelectedCustomer(order.getCustomerId());
        if (selectedCustomer.getCode() == 0) {
            return;
        }
        try {
            String res = DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerForEdit(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void showSupplier(ActionEvent evt) {
        DBHelper dbHelper = new DBHelper();

        Supplier selectedSupplier = DBHelper.getSupplierDao().getSelectedSupplier(order.getSupplierId());
        if (selectedSupplier.getId() == 0) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newSupplier.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Λεπτομέρειες Προμηθευτή");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

            AddSupplierController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setSupplierData(selectedSupplier);

            stage.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void lock() {
        customerComboBox.setDisable(true);
        btnCustomer.setDisable(true);
    }

    private void configureSingleSelectionCheckBoxes(CheckBox[] checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (CheckBox otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
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
