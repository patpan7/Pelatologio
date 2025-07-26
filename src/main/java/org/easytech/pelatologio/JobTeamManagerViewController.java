package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.Recommendation;

import java.io.IOException;
import java.util.Optional;

public class JobTeamManagerViewController {


    @FXML
    private TableView<JobTeam> table;

    @FXML
    private TableColumn<JobTeam, String> column;


    private ObservableList<JobTeam> recomList;


    @FXML
    public void initialize() {
        recomList = FXCollections.observableArrayList();

       // Ρύθμιση στήλης Τηλέφωνο
        column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        table.setItems(recomList);

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                handleEdit(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadJobTeams() {
        recomList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        recomList.addAll(DBHelper.getJobTeamDao().getJobTeams());
        if (table.getItems().size() == 1)
            table.getSelectionModel().select(0);
    }


    public void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addJobTeam.fxml"));
            DialogPane dialogPane = loader.load();

            AddJobTeamController addJobTeamController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Ομάδας");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addJobTeamController.handleSave(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadJobTeams();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleDelete(ActionEvent event) {
        JobTeam selectedJobTeam = table.getSelectionModel().getSelectedItem();
        if (selectedJobTeam == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κάποια σύσταση προς διαγραφή.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }

        // Εμφάνιση παραθύρου επιβεβαίωσης
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε την επιλεγμένη σύσταση;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Διαγραφή από τη βάση
            DBHelper.getJobTeamDao().deleteJobTeam(selectedJobTeam.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            table.getItems().remove(selectedJobTeam);
        }
    }

    public void handleEdit(ActionEvent event) {
        JobTeam selectedJobTeam = table.getSelectionModel().getSelectedItem();
        if (selectedJobTeam == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κατηγορία προς επεξεργασία.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editJobTeam.fxml"));
            DialogPane dialogPane = loader.load();

            EditJobTeamController editController = loader.getController();
            editController.setJobTeam(selectedJobTeam);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Ομάδας");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                JobTeam updatedJobTeam = editController.getUpdatedJobTeam();

                // Ενημέρωση της βάσης
                DBHelper.getJobTeamDao().updateJobTeam(updatedJobTeam);

                // Ενημέρωση του πίνακα
                table.refresh();
            }

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
