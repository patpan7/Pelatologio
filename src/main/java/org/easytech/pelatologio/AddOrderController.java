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

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private JFXButton btnCustomer;
    @FXML
    private JFXCheckBox is_completed, is_ergent, is_wait;

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
    }


    public void initialize() throws SQLException {
        // Φόρτωση πελατών
        DBHelper dbHelper = new DBHelper();
        List<Customer> customers = dbHelper.getCustomers();
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

        List<Supplier> suppliers = dbHelper.getSuppliers();
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

        setupComboBoxFilter(customerComboBox, filteredCustomers);
        setupComboBoxFilter(supplierComboBox, filteredSuppliers);

        dueDatePicker.setValue(LocalDate.now());
        CheckBox[] checkBoxes = {
                is_ergent,
                is_wait
        };
        configureSingleSelectionCheckBoxes(checkBoxes);
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
                    notifications.showError();});
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

            DBHelper dbHelper = new DBHelper();

            if (order == null) {
                // Δημιουργία νέας εργασίας
                Order newOrder = new Order();
                newOrder.setId(0);
                newOrder.setTitle(title);
                newOrder.setTitle(title);
                newOrder.setDescription(description);
                newOrder.setDueDate(date);
                newOrder.setCustomerId(selectedCustomer != null ? selectedCustomer.getCode() : 0);
                newOrder.setSupplierId(selectedSupplier != null ? selectedSupplier.getId() : 0);
                newOrder.setErgent(isErgent);
                newOrder.setWait(isWait);

                //Tasks newTasks = new Tasks(0, title, description, date, false, category, selectedCustomer != null ? selectedCustomer.getCode() : 0, isErgent, isWait);
                dbHelper.saveOrder(newOrder);
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
                dbHelper.updateOrder(order);
            }

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Η παραγγελία αποθηκεύτηκε!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
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

        Customer selectedCustomer = dbHelper.getSelectedCustomer(order.getCustomerId());
        if (selectedCustomer.getCode() == 0) {
            return;
        }
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerData(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    dbHelper.customerUnlock(selectedCustomer.getCode());
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
}
