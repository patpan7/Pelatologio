package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AddTaskController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;

    private Task task;
    private int customerId;
    private String customerName;

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

    public void setTaskForEdit(Task task) {
        this.task = task;
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        categoryComboBox.setValue(task.getCategory());

        // Αν υπάρχει πελάτης, προ-συμπλήρωσε την επιλογή
        if (task.getCustomerId() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCode() == task.getCustomerId()) {
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

    }

    public boolean handleSaveTask() {
        try {
            // Έλεγχος για κενά ή μη έγκυρα πεδία
            if (dueDatePicker.getValue() == null || titleField.getText() == null || descriptionField.getText() == null) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα απαραίτητα πεδία!");
                return false; // Αποτυχία
            }

            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate date = dueDatePicker.getValue();
            Customer selectedCustomer = null;
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getName().equals(customerComboBox.getValue())) {
                    selectedCustomer = customer;
                    break;
                }
            }
            if (selectedCustomer != null) {
                System.out.println("Επιλέχθηκε πελάτης: " + selectedCustomer.getName());
            } else {
                System.out.println("Δεν επιλέχθηκε πελάτης");
            }
            String category = categoryComboBox.getValue();

            DBHelper dbHelper = new DBHelper();

            if (task == null) {
                // Δημιουργία νέας εργασίας
                Task newTask = new Task(0, title, description, date, false, category, selectedCustomer != null ? selectedCustomer.getCode() : 0);
                dbHelper.saveTask(newTask);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                System.out.println("Ενημέρωση");
                task.setTitle(title);
                task.setDescription(description);
                task.setDueDate(date);
                task.setCategory(category);
                task.setCustomerId(selectedCustomer != null ? selectedCustomer.getCode() : 0);
                dbHelper.updateTask(task);
            }

            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Η εργασία αποθηκεύτηκε!");
            return true; // Επιτυχία

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της εργασίας!");
            return false; // Αποτυχία
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
