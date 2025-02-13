package org.easytech.pelatologio;


import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CalendarController {
    @FXML
    StackPane stackPane;
    @FXML
    private CalendarView calendarView;
    @FXML
    private ButtonType deleteButton;
    DBHelper dbHelper;

    @FXML
    public void initialize() {
        Platform.runLater(() -> stackPane.requestFocus());
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

                    // Προσθήκη listener για drag-and-drop
                    addDragAndDropListener(entry);

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
                        0, // Προεπιλεγμένος πελάτης
                        "", // Κενός τίτλος
                        "", // Κενή περιγραφή
                        -1, // Προεπιλεγμένο ημερολόγιο (ID = -1)
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

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.FINISH);
            Button deleteButtonNode = (Button) dialog.getDialogPane().lookupButton(ButtonType.FINISH);
            deleteButtonNode.setText("Διαγραφή");
            if (deleteButtonNode != null) {
                System.out.println("Delete button found!");
                deleteButtonNode.addEventFilter(ActionEvent.ACTION, event -> {
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Επιβεβαίωση Διαγραφής");
                    confirmationAlert.setHeaderText("Θέλετε σίγουρα να διαγράψετε αυτό το ραντεβού;");
                    confirmationAlert.setContentText("Αυτή η ενέργεια δεν μπορεί να αναιρεθεί.");

                    if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        if (controller.deleteAppointment(entry)) {
                            dialog.close();
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Σφάλμα Διαγραφής");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Η διαγραφή του ραντεβού απέτυχε.");
                            errorAlert.showAndWait();
                        }
                    }

                    event.consume();
                });
            }

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
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Επιτυχία")
                            .text("Το ραντεβού αποθηκεύτηκε επιτυχώς.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showConfirm();});
                updateCalendarEntry(fxCalendar, entry.getUserObject());
                // Ενημέρωση του ημερολογίου
            } else {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Αποτυχία")
                            .text("Το ραντεβού δεν αποθηκεύτηκε.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
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
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Το ραντεβού ενημερώθηκε στο ημερολόγιο.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();});
        } else {
            // Προσθήκη νέου Entry αν δεν βρεθεί υπάρχον
            Entry<Appointment> newEntry = new Entry<>(updatedAppointment.getTitle());
            newEntry.setId(String.valueOf(updatedAppointment.getId()));
            newEntry.setInterval(updatedAppointment.getStartTime(), updatedAppointment.getEndTime());
            newEntry.setUserObject(updatedAppointment);

            fxCalendar.addEntry(newEntry);
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Νέο ραντεβού προστέθηκε στο ημερολόγιο.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
        }
    }

    private void addDragAndDropListener(Entry<Appointment> entry) {
        entry.intervalProperty().addListener((obs, oldInterval, newInterval) -> {

            if (entry.getUserObject() instanceof Appointment) {
                Appointment appointment = entry.getUserObject();
                System.out.println("move");
                appointment.setStartTime(newInterval.getStartDateTime());
                appointment.setEndTime(newInterval.getEndDateTime());
                // Ενημέρωση της βάσης
                boolean success = dbHelper.updateAppointment(appointment);

                if (!success) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Σφάλμα κατά την ενημέρωση του ραντεβού στη βάση.");
                    errorAlert.show();
                }
            }
        });
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }
}