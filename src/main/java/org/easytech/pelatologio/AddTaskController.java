package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AddTaskController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<Customer> customerComboBox;

    private Task task;
    private boolean isSaved = false;

    public void initialize() {
        // Φόρτωση πελατών
        DBHelper dbHelper = new DBHelper();
        try {
            customerComboBox.getItems().addAll(dbHelper.getCustomers());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadTask(Task task) {
        this.task = task;
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        dueDatePicker.setValue(task.getDueDate() != null ? task.getDueDate().toLocalDate() : null);
        if (task.getCustomerId() != 0) {
            //customerComboBox.getSelectionModel().select(DBHelper.getCustomerById(task.getCustomerId()));
        }
    }

    public boolean isSaved() {
        return isSaved;
    }
}
