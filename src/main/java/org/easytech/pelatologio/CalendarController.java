package org.easytech.pelatologio;


import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.AppointmentImplLocal;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroupImpl;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarController {

    @FXML
    private Agenda agenda;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;
    @FXML
    private Label dateRangeLabel;

    private LocalDateTime currentDate;
    @FXML
    public void initialize() {
        // Ξεκινάμε την ημέρα από τις 7 το πρωί
        currentDate = LocalDateTime.now().withHour(7).withMinute(0).withSecond(0);

        // Φόρτωση ραντεβού από τη βάση
        loadAppointments();

        // Διαχείριση πλοήγησης
        previousButton.setOnAction(event -> navigatePrevious());
        nextButton.setOnAction(event -> navigateNext());

        // Ρύθμιση της ώρας για το ημερολόγιο να ξεκινά από τις 7 το πρωί
        //agenda.setStartHour(LocalTime.of(7, 0));  // Έναρξη από τις 7 το πρωί
    }

    private void loadAppointments() {
        // Διαγραφή όλων των ραντεβού πριν την προσθήκη νέων
        agenda.appointments().clear();

        // Ανάκτηση ραντεβού από τη βάση δεδομένων
        List<Appointment> appointments = getAppointmentsFromDatabase();

        // Προσθήκη ραντεβού στην agenda
        for (Appointment appointment : appointments) {
            agenda.appointments().add(new AppointmentImplLocal()
                    .withStartLocalDateTime(appointment.getStartTime())
                    .withEndLocalDateTime(appointment.getEndTime())
                    .withSummary(appointment.getTitle())
                    .withDescription(appointment.getDescription())
                    .withAppointmentGroup(new AppointmentGroupImpl().withStyleClass("group")));
        }

        // Ανάλογα με την επιλεγμένη προβολή, ορίζουμε την περίοδο
        updateCalendarView();
    }

    private List<Appointment> getAppointmentsFromDatabase() {
        // Χρησιμοποιούμε την DBHelper για να πάρουμε τα ραντεβού από τη βάση
        DBHelper dbHelper = new DBHelper();
        return dbHelper.getAllAppointments();
    }


    private void updateCalendarView() {
        // Ρύθμιση της ημερομηνίας εκκίνησης για την προβολή
        agenda.setDisplayedLocalDateTime(currentDate);

        // Ενημέρωση του layout της agenda
        agenda.layout();
        // Ενημέρωση του Label για την προβολή ημερομηνιών
        updateDateRangeLabel();
    }

    private void updateDateRangeLabel() {
        // Δημιουργία του DateTimeFormatter για την εμφάνιση των ημερομηνιών
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime endDate = currentDate.plusDays(7 - 1); // Ανάλογα με τις μέρες που προβάλλονται
        String startDateString = currentDate.format(formatter);
        String endDateString = endDate.format(formatter);

        // Ενημέρωση του Label με το εύρος των ημερομηνιών
        dateRangeLabel.setText("Εμφάνιση: " + startDateString + " - " + endDateString);
    }

    private void navigatePrevious() {
        // Αλλάζουμε την ημερομηνία για το προηγούμενο χρονικό διάστημα
        currentDate = currentDate.minusWeeks(1);
        loadAppointments();  // Επαναφορτώνουμε τα ραντεβού για την προηγούμενη περίοδο
    }

    private void navigateNext() {
        // Αλλάζουμε την ημερομηνία για το επόμενο χρονικό διάστημα
        currentDate = currentDate.plusWeeks(1);
        loadAppointments();  // Επαναφορτώνουμε τα ραντεβού για την επόμενη περίοδο
    }
}