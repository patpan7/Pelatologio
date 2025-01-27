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
    private ComboBox<TaskCategory> categoryComboBox;

    private Task task;
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

    public void setTaskForEdit(Task task) {
        this.task = task;
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate());
        for (TaskCategory taskCategory : categoryComboBox.getItems()) {
            if (taskCategory.getName().equals(task.getCategory())) {
                categoryComboBox.setValue(taskCategory);
                break;
            }
        }
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

        List<TaskCategory> categories = dbHelper.getAllTaskCategory();
        categoryComboBox.getItems().addAll(categories);
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TaskCategory taskCategory) {
                return taskCategory != null ? taskCategory.getName() : "";
            }

            @Override
            public TaskCategory fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        dueDatePicker.setValue(LocalDate.now());
    }

    public boolean handleSaveTask() {
        try {
            if (dueDatePicker.getValue() == null || titleField.getText() == null || descriptionField.getText() == null) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα απαραίτητα πεδία!");
                return false;
            }

            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate date = dueDatePicker.getValue();
            Customer selectedCustomer = customerComboBox.getValue(); // Απευθείας χρήση του ComboBox
            String category = categoryComboBox.getValue().getName();

            DBHelper dbHelper = new DBHelper();

            if (task == null) {
                // Δημιουργία νέας εργασίας
                Task newTask = new Task(0, title, description, date, false, category, selectedCustomer != null ? selectedCustomer.getCode() : 0);
                dbHelper.saveTask(newTask);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                task.setTitle(title);
                task.setDescription(description);
                task.setDueDate(date);
                task.setCategory(category);
                int customerId = selectedCustomer != null ? selectedCustomer.getCode() : 0;
                task.setCustomerId(customerId);
                dbHelper.updateTask(task);
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
