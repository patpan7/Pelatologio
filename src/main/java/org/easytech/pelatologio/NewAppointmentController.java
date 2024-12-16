package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import jfxtras.scene.control.LocalTimePicker;

public class NewAppointmentController {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private ComboBox<String> calendarComboBox;
    @FXML
    private ComboBox<String> startHourComboBox;

    @FXML
    private ComboBox<String> startMinuteComboBox;
    @FXML
    private ComboBox<Integer> durationComboBox;

    private int customerId;
    @FXML
    private Label customerName;
    List<Calendars> calendars;
    private int appointmentId;
    private LocalDateTime updatedStart;
    private LocalDateTime updatedEnd;
    private String updatedTitle;


    public void setAppointmentDetails(int id, String title,String description, int calendar_id, LocalDateTime start, LocalDateTime end) {
        this.appointmentId = id;
        titleField.setText(title);
        descriptionField.setText(description);
        calendarComboBox.setValue("calendar_id");
        startDatePicker.setValue(start.toLocalDate());
        startHourComboBox.setValue(String.format("%02d", start.getHour()));
        startMinuteComboBox.setValue(String.format("%02d", start.getMinute()));

        // Υπολογίζουμε τη διάρκεια
        long duration = java.time.Duration.between(start, end).toMinutes();
        durationComboBox.setValue((int) duration);
    }


    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String custName) {
        customerName.setText(custName);
    }

    @FXML
    public void initialize() {
        calendarComboBox.getItems().clear();
        try {
            DBHelper dbHelper = new DBHelper();
            calendars = dbHelper.getAllCalendars();

            for (Calendars calendar : calendars) {
                calendarComboBox.getItems().add(calendar.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int hour = 7; hour < 23; hour++) {
            startHourComboBox.getItems().add(String.format("%02d", hour));
        }
        for (int minute = 0; minute < 60; minute += 5) {
            startMinuteComboBox.getItems().add(String.format("%02d", minute));
        }

        durationComboBox.getItems().addAll(15, 30, 45, 60, 90, 120);
        durationComboBox.getSelectionModel().select(1); // Προεπιλογή 30 λεπτά

    }


    public boolean handleSaveAppointment() {
        try {
            // Έλεγχος για κενά ή μη έγκυρα πεδία
            if (startDatePicker.getValue() == null || startHourComboBox.getValue() == null ||
                    startMinuteComboBox.getValue() == null || durationComboBox.getValue() == null ||
                    calendarComboBox.getValue() == null) {

                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα απαραίτητα πεδία!");
                return false; // Αποτυχία
            }

            String title = titleField.getText() + " " + customerName.getText();
            String description = descriptionField.getText();
            int startHour = Integer.parseInt(startHourComboBox.getValue());
            int startMinute = Integer.parseInt(startMinuteComboBox.getValue());
            LocalDateTime startDateTime = LocalDateTime.from(startDatePicker.getValue().atTime(startHour, startMinute));
            int durationMinutes = durationComboBox.getValue();
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
            String selectedCalendarName = calendarComboBox.getValue();
            Calendars selectedCalendar = calendars.stream()
                    .filter(c -> c.getName().equals(selectedCalendarName))
                    .findFirst()
                    .orElse(null);
            Appointment appointment = null;
            if (selectedCalendar != null)
                appointment = new Appointment(0, customerId, title, description, selectedCalendar.getId(), startDateTime, endDateTime);


            DBHelper dbHelper = new DBHelper();
            dbHelper.saveAppointment(appointment);

            showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Το ραντεβού αποθηκεύτηκε!");
            return true; // Επιτυχία

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση του ραντεβού!");
            return false; // Αποτυχία
        }
    }

    public boolean handleSaveOrUpdateAppointment(int appointmentId) {
        try {
            // Έλεγχος για κενά ή μη έγκυρα πεδία
            if (startDatePicker.getValue() == null || startHourComboBox.getValue() == null ||
                    startMinuteComboBox.getValue() == null || durationComboBox.getValue() == null ||
                    calendarComboBox.getValue() == null) {

                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Συμπληρώστε όλα τα απαραίτητα πεδία!");
                return false; // Αποτυχία
            }

            String title = titleField.getText() + " " + customerName.getText();
            String description = descriptionField.getText();
            int startHour = Integer.parseInt(startHourComboBox.getValue());
            int startMinute = Integer.parseInt(startMinuteComboBox.getValue());
            LocalDateTime startDateTime = LocalDateTime.from(startDatePicker.getValue().atTime(startHour, startMinute));
            int durationMinutes = durationComboBox.getValue();
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
            String selectedCalendarName = calendarComboBox.getValue();

            Calendars selectedCalendar = calendars.stream()
                    .filter(c -> c.getName().equals(selectedCalendarName))
                    .findFirst()
                    .orElse(null);

            if (selectedCalendar == null) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Δεν επιλέχθηκε έγκυρο ημερολόγιο!");
                return false;
            }

            // Δημιουργία αντικειμένου ραντεβού
            Appointment appointment = new Appointment(
                    appointmentId, // ID του ραντεβού
                    customerId, title, description,
                    selectedCalendar.getId(),
                    startDateTime, endDateTime
            );

            DBHelper dbHelper = new DBHelper();

            if (appointmentId == 0) {
                // Νέο ραντεβού
                dbHelper.saveAppointment(appointment);
                showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Το ραντεβού αποθηκεύτηκε!");
            } else {
                // Ενημέρωση υπάρχοντος ραντεβού
                dbHelper.updateAppointment(appointment);
                showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Το ραντεβού ενημερώθηκε!");
            }

            return true; // Επιτυχία

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση του ραντεβού!");
            return false; // Αποτυχία
        }
    }


    public LocalDateTime getUpdatedStart() { return updatedStart; }
    public LocalDateTime getUpdatedEnd() { return updatedEnd; }
    public String getUpdatedTitle() { return updatedTitle; }



    // Μέθοδος για εμφάνιση Alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
