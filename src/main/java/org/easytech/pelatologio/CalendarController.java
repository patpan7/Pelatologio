package org.easytech.pelatologio;


import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
//        // Δημιουργία Calendar για τα ραντεβού
//        appointmentCalendar = new Calendar();
//        appointmentCalendar.setStyle(Calendar.Style.STYLE1);
//        appointmentCalendar.setName("Ραντεβού");
//
//
//        // Σύνδεση του Calendar με το CalendarView
//        CalendarSource calendarSource = new CalendarSource("Calendars");
//        com.calendarfx.model.Calendar calendar = new com.calendarfx.model.Calendar();
//        calendarSource.getCalendars().add(appointmentCalendar);
//        calendarSource.setName("Rantebou");

        // Ανάκτηση ημερολογίων από τη βάση
        DBHelper dbHelper = new DBHelper();
        List<Calendars> customCalendars = dbHelper.getAllCalendars();
        List<Appointment> appointments = dbHelper.getAllAppointments();

        CalendarSource calendarSource = new CalendarSource("Calendars");
        // Αφαίρεση όλων των προεπιλεγμένων ημερολογίων
        calendarSource.getCalendars().clear();

        // Μετατροπή των δεδομένων σε CalendarFX Calendar
        String[] styles = { "STYLE1", "STYLE2", "STYLE3", "STYLE4", "STYLE5", "STYLE6", "STYLE7", "STYLE8", "STYLE9", "STYLE10" };
        int styleIndex = 0;

        for (Calendars customCalendar : customCalendars) {
            com.calendarfx.model.Calendar fxCalendar = new com.calendarfx.model.Calendar(customCalendar.getName());
            fxCalendar.setStyle(Calendar.Style.valueOf(styles[styleIndex % styles.length]));
            styleIndex++;
            // Προσθήκη των ραντεβού στο συγκεκριμένο ημερολόγιο
            for (Appointment appointment : appointments) {
                if (appointment.getCalendarId() == customCalendar.getId()) {
                    Entry<Appointment> entry = new Entry<>(appointment.getTitle());
                    entry.setInterval(appointment.getStartTime(), appointment.getEndTime());
                    entry.setUserObject(appointment);
                    System.out.println(appointment.getDescription());
                    fxCalendar.addEntry(entry);
                }
            }

            calendarSource.getCalendars().add(fxCalendar);
        }


// Προσθήκη στο View
        calendarView.getCalendarSources().add(calendarSource);

        // Ρυθμίσεις εμφάνισης
        calendarView.setShowSearchField(true);
        calendarView.setShowToolBar(true);
        calendarView.setShowAddCalendarButton(false);


        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry != null) {
                showEditAppointmentDialog(entry);
            }
            return null;
        });

    }

    private void showEditAppointmentDialog(Entry<?> entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newAppointment.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Ραντεβού");

            NewAppointmentController controller = loader.getController();
            Appointment appointment = (Appointment) entry.getUserObject();
            // Προ-συμπλήρωση δεδομένων από το επιλεγμένο Entry
            controller.setAppointmentDetails(
                    appointment.getId(), // ID του ραντεβού
                    appointment.getTitle(),
                    appointment.getDescription(),
                    appointment.getCalendarId(),
                    entry.getStartAsLocalDateTime(),
                    entry.getEndAsLocalDateTime()
            );

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Λογική για το "OK" κουμπί
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.handleSaveOrUpdateAppointment(Integer.parseInt(entry.getId()))) {
                    event.consume(); // Αποτρέπουμε το κλείσιμο αν υπάρχει σφάλμα
                } else {
                    // Ενημέρωση του Entry στο CalendarView
                    updateCalendarEntry(entry, controller.getUpdatedStart(), controller.getUpdatedEnd(), controller.getUpdatedTitle());
                }
            });


            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCalendarEntry(Entry<?> entry, LocalDateTime updatedStart, LocalDateTime updatedEnd, String updatedTitle) {
        if (updatedStart != null) {
            entry.changeStartTime(updatedStart.toLocalTime());
            entry.changeStartDate(updatedStart.toLocalDate());
        }
        if (updatedEnd != null) {
            entry.changeEndTime(updatedEnd.toLocalTime());
            entry.changeEndDate(updatedEnd.toLocalDate());
        }
        if (updatedTitle != null && !updatedTitle.isEmpty()) {
            entry.setTitle(updatedTitle);
        }
    }




    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }
}