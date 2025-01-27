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

public class TaskCategoryManagerViewController {


    @FXML
    private TableView<TaskCategory> taskCategoryTable;

    @FXML
    private TableColumn<TaskCategory, String> taskCategoryColumn;


    private ObservableList<TaskCategory> categoriesList;

    @FXML
    public void initialize() {
        categoriesList = FXCollections.observableArrayList();

        // Ρύθμιση στήλης Τηλέφωνο
        taskCategoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        taskCategoryTable.setItems(categoriesList);

        taskCategoryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
                handleEdit(null);
            }
        });
    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadTaskCategories() {
        categoriesList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        DBHelper dbHelper = new DBHelper();
        categoriesList.addAll(dbHelper.getAllTaskCategory());
        if (taskCategoryTable.getItems().size() == 1)
            taskCategoryTable.getSelectionModel().select(0);
    }


    public void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTaskCategory.fxml"));
            DialogPane dialogPane = loader.load();

            AddTaskCategoryController addTaskCategoryController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Κατηγορίας");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addTaskCategoryController.handleSave(event);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadTaskCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDelete(ActionEvent event) {
        TaskCategory selectedCategory = taskCategoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε κάποια κατηγορία προς διαγραφή."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς διαγραφή.");
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
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteTaskCategory(selectedCategory.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            taskCategoryTable.getItems().remove(selectedCategory);
        }
    }

    public void handleEdit(ActionEvent event) {
        TaskCategory selectedCategory = taskCategoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε κάποια κατηγορία προς επεξεργασία."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editTaskCategory.fxml"));
            DialogPane dialogPane = loader.load();

            EditTaskCategoryController editController = loader.getController();
            editController.setAddress(selectedCategory);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία κατηγορίας");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                TaskCategory updatedTaskCategory = editController.getUpdatedTaskCategory();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                dbHelper.updateTaskCategory(updatedTaskCategory);

                // Ενημέρωση του πίνακα
                taskCategoryTable.refresh();
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
