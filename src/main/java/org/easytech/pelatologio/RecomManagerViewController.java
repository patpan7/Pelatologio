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
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CustomNotification;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Recommendation;

import java.io.IOException;
import java.util.Optional;

public class RecomManagerViewController {


    @FXML
    private TableView<Recommendation> recomTable;

    @FXML
    private TableColumn<Recommendation, String> recomColumn;


    private ObservableList<Recommendation> recomList;


    @FXML
    public void initialize() {
        recomList = FXCollections.observableArrayList();

        // Ρύθμιση στήλης Τηλέφωνο
        recomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        recomTable.setItems(recomList);

        recomTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEdit(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadRecommendations() {
        recomList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        recomList.addAll(DBHelper.getRecommendationDao().getRecommendations());
        if (recomTable.getItems().size() == 1)
            recomTable.getSelectionModel().select(0);
    }


    public void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addRecom.fxml"));
            DialogPane dialogPane = loader.load();

            AddRecomController addRecomController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη σύστασης");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addRecomController.handleSave(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadRecommendations();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleDelete(ActionEvent event) {
        Recommendation selectedRecom = recomTable.getSelectionModel().getSelectedItem();
        if (selectedRecom == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κάποια σύσταση προς διαγραφή.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showWarning();
            });
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
            DBHelper.getRecommendationDao().deleteRecommendation(selectedRecom.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            recomTable.getItems().remove(selectedRecom);
        }
    }

    public void handleEdit(ActionEvent event) {
        Recommendation selectedRecom = recomTable.getSelectionModel().getSelectedItem();
        if (selectedRecom == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κατηγορία προς επεξεργασία.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showWarning();
            });
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editRecom.fxml"));
            DialogPane dialogPane = loader.load();

            EditRecomController editController = loader.getController();
            editController.setRecommendation(selectedRecom);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Σύστασης");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Recommendation updatedRecommendation = editController.getUpdatedRecommendation();

                // Ενημέρωση της βάσης
                DBHelper.getRecommendationDao().updateRecommendation(updatedRecommendation);

                // Ενημέρωση του πίνακα
                recomTable.refresh();
            }

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
