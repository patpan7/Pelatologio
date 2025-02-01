package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AddDeviceController {

    @FXML
    private TextField serialField;
    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<Item> itemComboBox;

    private Device device;
    private int customerId;
    private String customerName;
    private Customer selectedCustomer;

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
    }


    public void initialize() throws SQLException {
        // Φόρτωση πελατών
        DBHelper dbHelper = new DBHelper();
        List<Customer> customers = dbHelper.getCustomers();
        customerComboBox.getItems().addAll(customers); // Προσθήκη αντικειμένων Customer
        customerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return customerComboBox.getItems().stream()
                        .filter(customer -> customer.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        List<Item> items = dbHelper.getItems();
        itemComboBox.getItems().addAll(items);
        itemComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Item item) {
                return item != null ? item.getName() : "";
            }

            @Override
            public Item fromString(String string) {
                return itemComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    public boolean handleSaveTask() {
        try {
            if (serialField.getText() == null || descriptionField.getText() == null) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα απαραίτητα πεδία!");
                return false;
            }

            String serial = serialField.getText();
            String description = descriptionField.getText();
            Customer selectedCustomer = customerComboBox.getValue(); // Απευθείας χρήση του ComboBox
            int itemId = itemComboBox.getValue().getId();

            DBHelper dbHelper = new DBHelper();

            if (device == null) {
                // Δημιουργία νέας εργασίας
                Device newDevice = new Device(0, serial, description, itemId, selectedCustomer != null ? selectedCustomer.getCode() : 0);
                dbHelper.saveDevice(newDevice);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                device.setSerial(serial);
                device.setDescription(description);
                device.setItemId(itemId);
                int customerId = selectedCustomer != null ? selectedCustomer.getCode() : 0;
                device.setCustomerId(customerId);
                dbHelper.updateDevice(device);
            }

            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η εργασία αποθηκεύτηκε!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της εργασίας!");
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
