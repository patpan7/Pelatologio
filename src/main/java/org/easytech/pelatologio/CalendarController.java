package org.easytech.pelatologio;


import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class CalendarController {
    @FXML
    StackPane stackPane;
    @FXML
    private CalendarView calendarView;
    DBHelper dbHelper;

    @FXML
    public void initialize() {
        // Ανάκτηση ημερολογίων από τη βάση
        dbHelper = new DBHelper();
        List<Calendars> customCalendars = dbHelper.getAllCalendars();
        List<Appointment> appointments = dbHelper.getAllAppointments();

        CalendarSource calendarSource = new CalendarSource("Calendars");
        // Αφαίρεση όλων των προεπιλεγμένων ημερολογίων
        calendarSource.getCalendars().clear();

        // Μετατροπή των δεδομένων σε CalendarFX Calendar
        String[] styles = {"STYLE1", "STYLE2", "STYLE3", "STYLE4", "STYLE5", "STYLE6", "STYLE7", "STYLE8", "STYLE9", "STYLE10"};
        int styleIndex = 0;

        for (Calendars customCalendar : customCalendars) {
            Calendar fxCalendar = new Calendar(customCalendar.getName());

            // Ορισμός στυλ
            String style = styles[styleIndex % styles.length];
            fxCalendar.setStyle(Calendar.Style.valueOf(style)); // Εδώ χρησιμοποιείται η αντιστοιχία

            styleIndex++;
            // Προσθήκη των ραντεβού στο συγκεκριμένο ημερολόγιο
            for (Appointment appointment : appointments) {
                if (appointment.getCalendarId() == customCalendar.getId()) {
                    Entry<Appointment> entry = new Entry<>(appointment.getTitle());
                    entry.setId(String.valueOf(appointment.getId()));
                    entry.setInterval(appointment.getStartTime(), appointment.getEndTime());
                    entry.setUserObject(appointment);
                    entry.setCalendar(fxCalendar); // Αυτό είναι απαραίτητο
                    fxCalendar.addEntry(entry);
                    System.out.println("Προσθήκη ραντεβού: " + appointment.getTitle() +
                            " από " + appointment.getStartTime() +
                            " έως " + appointment.getEndTime() +
                            " στο ημερολόγιο: " + fxCalendar.getName());

                }
            }

            calendarSource.getCalendars().addAll(fxCalendar);
        }


        // Προσθήκη στο View
        calendarView.getCalendarSources().addAll(calendarSource);
        calendarView.setRequestedTime(LocalTime.now());
        // Ρυθμίσεις εμφάνισης
        calendarView.setShowSearchField(true);
        calendarView.setShowToolBar(true);

        calendarView.setEntryDetailsCallback(param -> {
            Entry<Appointment> entry = (Entry<Appointment>) param.getEntry();
            if (entry != null) {
                showEditAppointmentDialog(entry, entry.getCalendar());
            }
            return null;
        });
    }


    private void showEditAppointmentDialog(Entry<Appointment> entry, Calendar fxCalendar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editAppointment.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Διαχείριση Ραντεβού");

            EditAppointmentController controller = loader.getController();

            // Έλεγχος αν το Entry είναι νέο
            if (entry.getUserObject() == null) {
                // Δημιουργούμε νέο Appointment με προεπιλεγμένες τιμές
                Appointment newAppointment = new Appointment(
                        0, // ID = 0 για νέο ραντεβού
                        0,
                        "", // Προεπιλεγμένος τίτλος
                        "", // Κενή περιγραφή
                        -1, // Προεπιλεγμένο ημερολόγιο (ID = -1 για ένδειξη)
                        entry.getStartAsLocalDateTime(),
                        entry.getEndAsLocalDateTime()
                );
                controller.setCalendarMap(dbHelper.getAllCalendars());
                controller.loadAppointment(newAppointment);
                entry.setUserObject(newAppointment); // Συνδέουμε το νέο Appointment με το Entry
            } else {
                // Φορτώνουμε υπάρχον Appointment
                Appointment appointment = entry.getUserObject();
                controller.setCalendarMap(dbHelper.getAllCalendars());
                controller.loadAppointment(appointment);
            }

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.saveAppointment()) {
                    event.consume(); // Αποτρέπουμε το κλείσιμο αν η αποθήκευση αποτύχει
                } else {
                    Appointment updatedAppointment = entry.getUserObject();
                    entry.setTitle(updatedAppointment.getTitle());
                    entry.setInterval(updatedAppointment.getStartTime(), updatedAppointment.getEndTime());
                }
            });

            dialog.showAndWait();
            // Έλεγχος αν αποθηκεύτηκε
            if (controller.isSaved()) {
                System.out.println("Το ραντεβού αποθηκεύτηκε επιτυχώς!");
                updateCalendarEntry(fxCalendar, entry.getUserObject());
                // Ενημέρωση του ημερολογίου
            } else {
                System.out.println("Το ραντεβού δεν αποθηκεύτηκε.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateCalendarEntry(Calendar fxCalendar, Appointment updatedAppointment) {
        // Αναζήτηση του Entry με το ίδιο ID στο Calendar
        Entry<?> entryToUpdate = null;
        for (Entry<?> entry : fxCalendar.findEntries(updatedAppointment.getTitle())) {
            if (entry.getId().equals(String.valueOf(updatedAppointment.getId()))) {
                entryToUpdate = entry;
                break;
            }
        }

        if (entryToUpdate != null) {
            // Ενημέρωση του υπάρχοντος Entry
            entryToUpdate.setTitle(updatedAppointment.getTitle());
            entryToUpdate.changeStartDate(updatedAppointment.getStartTime().toLocalDate());
            entryToUpdate.changeStartTime(updatedAppointment.getStartTime().toLocalTime());
            entryToUpdate.changeEndDate(updatedAppointment.getEndTime().toLocalDate());
            entryToUpdate.changeEndTime(updatedAppointment.getEndTime().toLocalTime());
            entryToUpdate.setCalendar(fxCalendar);
            System.out.println("Το ραντεβού ενημερώθηκε στο ημερολόγιο.");
        } else {
            // Προσθήκη νέου Entry αν δεν βρεθεί υπάρχον
            Entry<Appointment> newEntry = new Entry<>(updatedAppointment.getTitle());
            newEntry.setId(String.valueOf(updatedAppointment.getId()));
            newEntry.setInterval(updatedAppointment.getStartTime(), updatedAppointment.getEndTime());
            newEntry.setUserObject(updatedAppointment);

            fxCalendar.addEntry(newEntry);
            System.out.println("Νέο ραντεβού προστέθηκε στο ημερολόγιο.");
        }
    }

    public void calendarManager(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("calendarManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            CalendarManagerViewController controller = loader.getController();
            controller.loadCalendars();


            dialog.setTitle("Ημερολόγια");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }
}