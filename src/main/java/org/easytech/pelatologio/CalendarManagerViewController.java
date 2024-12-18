package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional;

public class CalendarManagerViewController {


    @FXML
    private TableView<Calendars> calendarTable;

    @FXML
    private TableColumn<Calendars, String> calendarColumn;


    private ObservableList<Calendars> calendarList;

    @FXML
    public void initialize() {
        calendarList = FXCollections.observableArrayList();

        // Ρύθμιση στήλης Τηλέφωνο
        calendarColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        calendarTable.setItems(calendarList);

        calendarTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                handleEdit(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadCalendars() {
        calendarList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        calendarList.addAll(dbHelper.getAllCalendars());
        if (calendarTable.getItems().size() == 1)
            calendarTable.getSelectionModel().select(0);
    }


    public void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addCalendar.fxml"));
            DialogPane dialogPane = loader.load();

            AddCalendarController addCalendarController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Ημερολογίου");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addCalendarController.handleSave(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadCalendars();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDelete(ActionEvent event) {
        Calendars selectedCalendar = calendarTable.getSelectionModel().getSelectedItem();
        if (selectedCalendar == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε κάποιο ημερολόγιο προς διαγραφή."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς διαγραφή.");
            return;
        }

        // Εμφάνιση παραθύρου επιβεβαίωσης
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο ημερολόγιο;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Διαγραφή από τη βάση
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteCalendar(selectedCalendar.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            calendarTable.getItems().remove(selectedCalendar);
        }
    }

    public void handleEdit(ActionEvent event) {
        Calendars selectedCalendar = calendarTable.getSelectionModel().getSelectedItem();
        if (selectedCalendar == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε κάποιο ημερολόγιο προς επεξεργασία."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editCalendar.fxml"));
            DialogPane dialogPane = loader.load();

            EditCalendarController editController = loader.getController();
            editController.setAddress(selectedCalendar);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία διεύθυνσης");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Calendars updatedCalendar = editController.getUpdatedCalendar();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                dbHelper.updateCalendra(updatedCalendar);

                // Ενημέρωση του πίνακα
                calendarTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
