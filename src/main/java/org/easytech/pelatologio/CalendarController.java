package org.easytech.pelatologio;


import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarController {
    @FXML
    StackPane stackPane;
    @FXML
    private CalendarView calendarView;

    private Calendar appointmentCalendar;

    private LocalDateTime currentDate;
    @FXML
    public void initialize() {
        // Δημιουργία Calendar για τα ραντεβού
        appointmentCalendar = new Calendar();
        appointmentCalendar.setStyle(Calendar.Style.STYLE2);


        // Σύνδεση του Calendar με το CalendarView
        CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(appointmentCalendar);
        calendarView.getCalendarSources().add(calendarSource);

        // Ρυθμίσεις εμφάνισης
        calendarView.setShowSearchField(true);
        calendarView.setShowToolBar(true);

        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry.getUserObject() instanceof Appointment) {
                Appointment appointment = (Appointment) entry.getUserObject();

                // Δημιουργία Popup
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Λεπτομέρειες Ραντεβού");
                alert.setHeaderText("Τίτλος: " + appointment.getTitle());
                alert.setContentText("Περιγραφή: " + appointment.getDescription() +
                        "\nΠελάτης ID: " + appointment.getCustomerId() +
                        "\nΈναρξη: " + appointment.getStartTime() +
                        "\nΛήξη: " + appointment.getEndTime());
                alert.showAndWait();
            }
            return null;
        });


        // Φόρτωση των ραντεβού από τη βάση δεδομένων
        loadAppointments();
    }

    private void loadAppointments() {
        // Ανάκτηση ραντεβού από τη βάση δεδομένων
        List<Appointment> appointments = getAppointmentsFromDatabase();

        // Προσθήκη ραντεβού στην agenda
        for (Appointment appointment : appointments) {
            Entry<Appointment> entry = new Entry<>(appointment.getTitle());
            entry.setInterval(appointment.getStartTime(), appointment.getEndTime());
            // Συσχέτιση του αντικειμένου Appointment με το Entry
            entry.setUserObject(appointment);
            appointmentCalendar.addEntry(entry);
        }
    }

    private List<Appointment> getAppointmentsFromDatabase() {
        // Χρησιμοποιούμε την DBHelper για να πάρουμε τα ραντεβού από τη βάση
        DBHelper dbHelper = new DBHelper();
        return dbHelper.getAllAppointments();
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }
}