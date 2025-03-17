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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
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
    List<TaskCategory> customCalendars;
    List<Tasks> appointments;

    @FXML
    public void initialize() {
        Platform.runLater(() -> stackPane.requestFocus());
        // Ανάκτηση ημερολογίων από τη βάση
        loadEvents();

        // Ρυθμίσεις εμφάνισης
        calendarView.setShowSearchField(true);
        calendarView.setShowToolBar(true);

        calendarView.setEntryDetailsCallback(param -> {
            Entry<Tasks> entry = (Entry<Tasks>) param.getEntry();
            if (entry != null) {
                showEditAppointmentDialog(entry, entry.getCalendar());
            }
            return null;
        });

    }

    private void loadEvents() {
        // Φόρτωση όλων των συμβάντων από τη βάση
        dbHelper = new DBHelper();
        customCalendars = dbHelper.getAllTaskCategory();
        appointments = dbHelper.getAllTasks();
        updateCalendar();
    }

    private void updateCalendar() {
        // Αδειάζουμε το ημερολόγιο πριν την ανανέωση
        calendarView.getCalendarSources().clear();

        // Δημιουργούμε νέο CalendarSource για τα συμβάντα
        CalendarSource calendarSource = new CalendarSource("Calendars");
        // Αφαίρεση όλων των προεπιλεγμένων ημερολογίων
        calendarSource.getCalendars().clear();
        // Μετατροπή των δεδομένων σε CalendarFX Calendar
        String[] styles = {"STYLE1", "STYLE2", "STYLE3", "STYLE4", "STYLE5", "STYLE6", "STYLE7"};
        int styleIndex = 0;
        for (TaskCategory customCalendar : customCalendars) {
            Calendar fxCalendar = new Calendar(customCalendar.getName());

            // Ορισμός στυλ
            String style = styles[styleIndex % styles.length];
            fxCalendar.setStyle(Calendar.Style.valueOf(style)); // Εδώ χρησιμοποιείται η αντιστοιχία

            styleIndex++;
            // Προσθήκη των ραντεβού στο συγκεκριμένο ημερολόγιο
            for (Tasks appointment : appointments) {
                if (appointment.getIsCalendar()) {
                    if (appointment.getCategory().equals(customCalendar.getName())) {
                        Entry<Tasks> entry;
                        if (appointment.getCustomerId() != 0)
                            entry = new Entry<>(appointment.getCustomerName() + " " + appointment.getTitle());
                        else
                            entry = new Entry<>(appointment.getTitle());
                        entry.setId(String.valueOf(appointment.getId()));
                        entry.setInterval(appointment.getStartTime(), appointment.getEndTime());
                        entry.setUserObject(appointment);
                        entry.setCalendar(fxCalendar); // Αυτό είναι απαραίτητο
                        if (appointment.getCompleted()) {
                            //entry.setTitle("[✔] " + appointment.getTitle());
                            entry.setTitle("\u0336 [✔] " + appointment.getTitle().replaceAll(".", "$0\u0336")); // Διαγράμμιση κειμένου
                            entry.getStyleClass().add("completed-entry");
                        }
                        // Προσθήκη listener για drag-and-drop
                        addDragAndDropListener(entry);

                        fxCalendar.addEntry(entry);
                    }
                }
            }
            calendarSource.getCalendars().addAll(fxCalendar);
        }


        // Προσθήκη στο View
        calendarView.getCalendarSources().addAll(calendarSource);
        calendarView.setRequestedTime(LocalTime.now());
    }


    private void showEditAppointmentDialog(Entry<Tasks> entry, Calendar fxCalendar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Διαχείριση Ραντεβού");

            AddTaskController controller = loader.getController();

            // Έλεγχος αν το Entry είναι νέο
            if (entry.getUserObject() == null) {
                // Δημιουργούμε νέο Appointment με προεπιλεγμένες τιμές
                Tasks newAppointment = new Tasks();
                //controller.setCalendarMap(dbHelper.getAllCalendars());
                controller.setTaskForEdit(newAppointment);
                entry.setUserObject(newAppointment); // Συνδέουμε το νέο Appointment με το Entry
            } else {
                // Φορτώνουμε υπάρχον Appointment
                Tasks appointment = entry.getUserObject();
                controller.setTaskForEdit(appointment);
            }

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.handleSaveTask()) {
                    event.consume(); // Αποτρέπουμε το κλείσιμο αν η αποθήκευση αποτύχει
                } else {
                    Tasks updatedAppointment = entry.getUserObject();
                    entry.setTitle(updatedAppointment.getTitle());
                    entry.setInterval(updatedAppointment.getStartTime(), updatedAppointment.getEndTime());
                }
            });

            dialog.showAndWait();
            loadEvents();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }



    private void addDragAndDropListener(Entry<Tasks> entry) {
        entry.intervalProperty().addListener((obs, oldInterval, newInterval) -> {

            if (entry.getUserObject() instanceof Tasks) {
                Tasks appointment = entry.getUserObject();
                System.out.println("move");
                appointment.setStartTime(newInterval.getStartDateTime());
                appointment.setEndTime(newInterval.getEndDateTime());
                // Ενημέρωση της βάσης
                boolean success = dbHelper.updateTask(appointment);

                if (!success) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Σφάλμα κατά την ενημέρωση του ραντεβού στη βάση.");
                    errorAlert.show();
                }
                loadEvents();
            }
        });
    }


    public void calendarManager(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskCategoryManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            TaskCategoryManagerViewController controller = loader.getController();
            controller.loadTaskCategories();


            dialog.setTitle("Κατηγορίες Εργασιών");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.showAndWait();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void addAppointment(ActionEvent event) {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Ραντεβού");

            AddTaskController controller = loader.getController();
            controller.checkCalendar();

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, evt -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveTask();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    evt.consume();
                }
            });

            dialog.showAndWait();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void refresh(MouseEvent mouseEvent) {
        loadEvents();
    }
}