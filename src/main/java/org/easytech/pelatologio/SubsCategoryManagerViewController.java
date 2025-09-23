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
import org.easytech.pelatologio.models.SubsCategory;

import java.io.IOException;
import java.util.Optional;

public class SubsCategoryManagerViewController {


    @FXML
    private TableView<SubsCategory> subsCategoryTable;

    @FXML
    private TableColumn<SubsCategory, String> subsCategoryColumn;


    private ObservableList<SubsCategory> categoriesList;

    @FXML
    public void initialize() {
        categoriesList = FXCollections.observableArrayList();

        // Ρύθμιση στήλης Τηλέφωνο
        subsCategoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        subsCategoryTable.setItems(categoriesList);

        subsCategoryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEdit(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadSubsCategories() {
        categoriesList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        categoriesList.addAll(DBHelper.getSubscriptionDao().getAllSubsCategory());
        if (subsCategoryTable.getItems().size() == 1)
            subsCategoryTable.getSelectionModel().select(0);
    }


    public void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSubsCategory.fxml"));
            DialogPane dialogPane = loader.load();

            AddSubsCategoryController addSubsCategoryController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Κατηγορίας");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addSubsCategoryController.handleSave(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadSubsCategories();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleDelete(ActionEvent event) {
        SubsCategory selectedCategory = subsCategoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε κάποια κατηγορία προς διαγραφή.")
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
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε την επιλεγμένη κατηγορία;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Διαγραφή από τη βάση
            DBHelper.getSubscriptionDao().deleteSubsCategory(selectedCategory.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            subsCategoryTable.getItems().remove(selectedCategory);
        }
    }

    public void handleEdit(ActionEvent event) {
        SubsCategory selectedCategory = subsCategoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editSubsCategory.fxml"));
            DialogPane dialogPane = loader.load();

            EditSubsCategoryController editController = loader.getController();
            editController.setSubsCategory(selectedCategory);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία κατηγορίας");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                SubsCategory updatedSubsCategory = editController.getUpdatedSubsCategory();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                DBHelper.getSubscriptionDao().updateSubsCategory(updatedSubsCategory);

                // Ενημέρωση του πίνακα
                subsCategoryTable.refresh();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
