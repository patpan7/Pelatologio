package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.LocalTime;
import jfxtras.scene.control.LocalTimePicker;
public class NewAppointmentController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML
    private ComboBox<String> startHourComboBox;

    @FXML
    private ComboBox<String> startMinuteComboBox;
    @FXML
    private ComboBox<String> endHourComboBox;

    @FXML
    private ComboBox<String> endMinuteComboBox;

    private int customerId;
    private String customerName;

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @FXML
    public void initialize() {
        for (int hour = 0; hour < 24; hour++) {
            startHourComboBox.getItems().add(String.format("%02d", hour));
        }
        for (int minute = 0; minute < 60; minute += 5) {
            startMinuteComboBox.getItems().add(String.format("%02d", minute));
        }
        for (int hour = 0; hour < 24; hour++) {
            endHourComboBox.getItems().add(String.format("%02d", hour));
        }
        for (int minute = 0; minute < 60; minute += 5) {
            endMinuteComboBox.getItems().add(String.format("%02d", minute));
        }

    }


    public void handleSaveAppointment() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        int startHour = Integer.parseInt(startHourComboBox.getValue());
        int startMinute = Integer.parseInt(startMinuteComboBox.getValue());
        LocalDateTime startDateTime = LocalDateTime.from(startDatePicker.getValue().atTime(startHour, startMinute));
        int endHour = Integer.parseInt(endHourComboBox.getValue());
        int endMinute = Integer.parseInt(endMinuteComboBox.getValue());
        LocalDateTime endDateTime = LocalDateTime.from(startDatePicker.getValue().atTime(endHour, endMinute));

        Appointment appointment = new Appointment(0, customerId, title, description, startDateTime, endDateTime);

        DBHelper dbHelper = new DBHelper();
        dbHelper.saveAppointment(appointment);

        System.out.println("Ραντεβού αποθηκεύτηκε για τον πελάτη: " + customerName);
    }
}
