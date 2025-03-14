package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.time.LocalDateTime;
import java.util.List;

public class AddAppointmentController {
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα.", e.getMessage(), Alert.AlertType.ERROR));
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
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
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
                appointment = new Appointment(0, customerId, title, description, selectedCalendar.getId(), startDateTime, endDateTime, false);


            DBHelper dbHelper = new DBHelper();
            dbHelper.saveAppointment(appointment);

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Το ραντεβού αποθηκεύτηκε.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
            return true; // Επιτυχία

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση του ραντεβού.", e.getMessage(), Alert.AlertType.ERROR));
            return false; // Αποτυχία
        }
    }

    public boolean handleSaveOrUpdateAppointment(int appointmentId) {
        try {
            // Έλεγχος για κενά ή μη έγκυρα πεδία
            if (startDatePicker.getValue() == null || startHourComboBox.getValue() == null ||
                    startMinuteComboBox.getValue() == null || durationComboBox.getValue() == null ||
                    calendarComboBox.getValue() == null) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Σφάλμα")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
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
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Σφάλμα")
                            .text("Δεν επιλέχθηκε έγκυρο ημερολόγιο.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
                return false;
            }

            // Δημιουργία αντικειμένου ραντεβού
            Appointment appointment = new Appointment(
                    appointmentId, // ID του ραντεβού
                    customerId, title, description,
                    selectedCalendar.getId(),
                    startDateTime, endDateTime,
                    false
            );

            DBHelper dbHelper = new DBHelper();

            if (appointmentId == 0) {
                // Νέο ραντεβού
                dbHelper.saveAppointment(appointment);
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Επιτυχία")
                            .text("Το ραντεβού αποθηκεύτηκε.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showConfirm();});
            } else {
                // Ενημέρωση υπάρχοντος ραντεβού
                dbHelper.updateAppointment(appointment);
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Επιτυχία")
                            .text("Το ραντεβού ενημερώθηκε.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showConfirm();});
            }

            return true; // Επιτυχία

        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση του ραντεβού.", e.getMessage(), Alert.AlertType.ERROR));
            return false; // Αποτυχία
        }
    }


    public LocalDateTime getUpdatedStart() { return updatedStart; }
    public LocalDateTime getUpdatedEnd() { return updatedEnd; }
    public String getUpdatedTitle() { return updatedTitle; }

}
