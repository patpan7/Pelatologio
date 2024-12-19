package org.easytech.pelatologio;

import com.calendarfx.model.Entry;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAppointmentController {

    @FXML
    private Label customerName;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> calendarComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private ComboBox<Integer> startHourComboBox;
    @FXML
    private ComboBox<Integer> startMinuteComboBox;
    @FXML
    private ComboBox<Integer> durationComboBox;

    private Appointment appointment; // Το αντικείμενο ραντεβού που επεξεργαζόμαστε
    private boolean isSaved = false; // Κατάσταση αποθήκευσης
    private Map<Integer, String> calendarMap; // ID -> Όνομα ημερολογίου

    @FXML
    public void initialize() {
        // Γέμισμα ComboBox με τιμές ωρών, λεπτών και διάρκειας
        populateTimeFields();

        // Προεπιλεγμένες τιμές για τη διάρκεια
        durationComboBox.getItems().addAll(15, 30, 45, 60, 90, 120);
    }

    private void populateTimeFields() {
        for (int hour = 0; hour < 24; hour++) {
            startHourComboBox.getItems().add(hour);
        }
        for (int minute = 0; minute < 60; minute += 5) { // Βήμα 5 λεπτών
            startMinuteComboBox.getItems().add(minute);
        }
    }

    // Μέθοδος για αρχικοποίηση του χάρτη ημερολογίων
    public void setCalendarMap(List<Calendars> calendars) {
        calendarMap = new HashMap<>();
        for (Calendars calendar : calendars) {
            calendarMap.put(calendar.getId(), calendar.getName());
        }
        // Γέμισμα του ComboBox με τα ονόματα των ημερολογίων
        calendarComboBox.getItems().addAll(calendarMap.values());
    }

    // Μέθοδος για φόρτωση δεδομένων ραντεβού
    public void loadAppointment(Appointment appointment) {
        this.appointment = appointment;

        // Ορισμός τιμών στα πεδία
        //customerName.setText(customer != null ? customer : "Άγνωστος Πελάτης");
        titleField.setText(appointment.getTitle());
        descriptionField.setText(appointment.getDescription());
        // Εύρεση του ονόματος ημερολογίου με βάση το ID
        String calendarName = calendarMap.get(appointment.getCalendarId());
        calendarComboBox.setValue(calendarName);

        LocalDateTime startTime = appointment.getStartTime();
        startDatePicker.setValue(startTime.toLocalDate());
        startHourComboBox.setValue(startTime.getHour());
        startMinuteComboBox.setValue(startTime.getMinute());

        // Προεπιλογή διάρκειας
        durationComboBox.setValue((int) java.time.Duration.between(startTime, appointment.getEndTime()).toMinutes());
    }

    // Μέθοδος για αποθήκευση των αλλαγών
    public boolean saveAppointment() {
        if (!validateInput()) {
            return false; // Επιστρέφει false αν δεν είναι έγκυρα τα δεδομένα
        }

        // Ορισμός νέων τιμών στο αντικείμενο
        appointment.setTitle(titleField.getText());
        appointment.setDescription(descriptionField.getText());
        // Εύρεση ID ημερολογίου από το όνομα
        String selectedCalendarName = calendarComboBox.getValue();
        int calendarId = getCalendarIdByName(selectedCalendarName);
        appointment.setCalendarId(calendarId);

        LocalDate date = startDatePicker.getValue();
        LocalTime startTime = LocalTime.of(startHourComboBox.getValue(), startMinuteComboBox.getValue());
        // Έλεγχος αν η τιμή του ComboBox δεν είναι null και δεν είναι κενή
        String durationString = String.valueOf(durationComboBox.getValue());
        int duration = 0; // Προεπιλεγμένη τιμή για τη διάρκεια

        if (durationString != null && !durationString.isEmpty()) {
            try {
                duration = Integer.parseInt(durationString); // Μετατροπή από String σε Integer
            } catch (NumberFormatException e) {
                System.out.println("Σφάλμα: Η διάρκεια δεν είναι έγκυρος αριθμός.");
                duration = 15; // Αν αποτύχει, θέτουμε μια προεπιλεγμένη τιμή
            }
        }

        appointment.setStartTime(LocalDateTime.of(date, startTime));
        appointment.setEndTime(appointment.getStartTime().plusMinutes(duration));
        // Ενημέρωση της βάσης δεδομένων μέσω DBHelper
        DBHelper dbHelper = new DBHelper();
        boolean success = dbHelper.updateAppointment(appointment);

        if (success) {
            isSaved = true; // Ενημερώνουμε το flag
            return true;
        } else {
            showAlert("Σφάλμα", "Η ενημέρωση του ραντεβού στη βάση απέτυχε.");
            return false;
        }
    }

    private int getCalendarIdByName(String name) {
        for (Map.Entry<Integer, String> entry : calendarMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return -1; // Επιστρέφει -1 αν δεν βρεθεί
    }

    private boolean validateInput() {
        if (titleField.getText() == null || titleField.getText().isEmpty()) {
            showAlert("Σφάλμα Εισαγωγής", "Ο τίτλος δεν μπορεί να είναι κενός.");
            return false;
        }
        if (calendarComboBox.getValue() == null || calendarComboBox.getValue().isEmpty()) {
            showAlert("Σφάλμα Εισαγωγής", "Επιλέξτε ένα ημερολόγιο.");
            return false;
        }
        if (startDatePicker.getValue() == null) {
            showAlert("Σφάλμα Εισαγωγής", "Επιλέξτε ημερομηνία έναρξης.");
            return false;
        }
        if (startHourComboBox.getValue() == null || startMinuteComboBox.getValue() == null) {
            showAlert("Σφάλμα Εισαγωγής", "Επιλέξτε ώρα και λεπτά έναρξης.");
            return false;
        }
        if (durationComboBox.getValue() == null) {
            showAlert("Σφάλμα Εισαγωγής", "Επιλέξτε έγκυρη διάρκεια.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return isSaved;
    }

    private void closeDialog() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    public boolean deleteAppointment(Entry<Appointment> entry) {
        try {
            Appointment appointment = entry.getUserObject();
            if (appointment != null) {
                DBHelper dbHelper = new DBHelper();
                dbHelper.deleteAppointment(appointment.getId()); // Υποθέτοντας ότι υπάρχει αυτή η μέθοδος
                entry.removeFromCalendar(); // Αφαίρεση από το ημερολόγιο
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
