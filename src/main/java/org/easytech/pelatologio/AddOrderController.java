package org.easytech.pelatologio;

import atlantafx.base.controls.ToggleSwitch;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Order;
import org.easytech.pelatologio.models.Supplier;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
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
    private Button btnCustomer, btnSupplier;
    @FXML
    private ToggleSwitch is_completed, is_ergent, is_wait, is_received, is_delivered;

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
        ToggleSwitch[] checkBoxes = {
                is_ergent,
                is_wait
        };
        configureSingleSelectionCheckBoxes(checkBoxes);
    }

    public boolean handleSaveOrder() {
        try {
            if (dueDatePicker.getValue() == null || titleField.getText() == null || descriptionField.getText() == null) {
                Platform.runLater(() -> {
                    CustomNotification.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT)
                            .showWarning();
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
                CustomNotification.create()
                        .title("Επιτυχία")
                        .text("Η παραγγελία αποθηκεύτηκε!")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showConfirmation();
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

    private void configureSingleSelectionCheckBoxes(ToggleSwitch[] checkBoxes) {
        for (ToggleSwitch checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (ToggleSwitch otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }
}
