package org.easytech.pelatologio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TaskListController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Tasks> taskTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn, categoryColumn;

    @FXML
    private CheckBox showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox, showErgentCheckBox, showWaitCheckBox;

    @FXML
    private ComboBox <TaskCategory> categoryFilterComboBox;
    @FXML
    private Button addCategoryButton, addTaskButton, editTaskButton, deleteTaskButton, completeTaskButton, uncompletedTaskButton;

    private ObservableList<Tasks> allTasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addTaskButton, "Προσθήκη νέας εργασίας");
        setTooltip(editTaskButton, "Επεξεργασία εργασίας");
        setTooltip(deleteTaskButton, "Διαγραφή εργασίας");
        setTooltip(completeTaskButton, "Σημείωση εργασίας ως ολοκληρωμένη");
        setTooltip(uncompletedTaskButton,"Σημείωση εργασίας ως σε επεξεργασία");
        setTooltip(addCategoryButton, "Προσθήκη/Επεξεργασία κατηγοριών εργασιών");
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        showAllCheckbox.setSelected(false);
        showPendingCheckbox.setSelected(true);
        // Αρχικό γέμισμα του πίνακα
        loadTasks();

        // RowFactory για διαφορετικά χρώματα
        taskTable.setRowFactory(tv -> new TableRow<Tasks>() {
            @Override
            protected void updateItem(Tasks tasks, boolean empty) {
                super.updateItem(tasks, empty);
                if (empty || tasks == null) {
                    setStyle("");
                } else {
                    if (tasks.getCompleted()) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;"); // Πράσινο
                    } else {
                        setStyle(""); // Προεπιλογή
                    }
                }
            }
        });

        taskTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Tasks selectedTasks = taskTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedTasks != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditTask();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Φίλτρα
        CheckBox[] checkBoxes1 = {
                showAllCheckbox,
                showCompletedCheckbox,
                showPendingCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes1);

        CheckBox[] checkBoxes2 = {
                showWithCustomerCheckbox,
                showWithoutCustomerCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes2);

        CheckBox[] checkBoxes3 = {
                showErgentCheckBox,
                showWaitCheckBox
        };
        configureSingleSelectionCheckBoxes(checkBoxes3);

        DBHelper dbHelper = new DBHelper();
        List<TaskCategory> categories = dbHelper.getAllTaskCategory();
        categoryFilterComboBox.getItems().add(new TaskCategory(0,"Όλες"));
        categoryFilterComboBox.getItems().addAll(categories);
        categoryFilterComboBox.getSelectionModel().selectFirst();
        categoryFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TaskCategory taskCategory) {
                return taskCategory != null ? taskCategory.getName() : "";
            }

            @Override
            public TaskCategory fromString(String string) {
                return categoryFilterComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTaskTable());


        showAllCheckbox.setOnAction(e -> updateTaskTable());
        showCompletedCheckbox.setOnAction(e -> updateTaskTable());
        showPendingCheckbox.setOnAction(e -> updateTaskTable());
        showWithCustomerCheckbox.setOnAction(e -> updateTaskTable());
        showWithoutCustomerCheckbox.setOnAction(e -> updateTaskTable());
        showErgentCheckBox.setOnAction(e -> updateTaskTable());
        showWaitCheckBox.setOnAction(e -> updateTaskTable());

        // Κουμπιά
        addCategoryButton.setOnAction(e -> TaskCategoryManager());
        addTaskButton.setOnAction(e -> handleAddTask());
        editTaskButton.setOnAction(e -> {
            try {
                handleEditTask();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteTaskButton.setOnAction(e -> {
            try {
                handleDeleteTask();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        completeTaskButton.setOnAction(e -> toggleComplete(true));
        uncompletedTaskButton.setOnAction(e -> toggleComplete(false));
    }


    private void configureSingleSelectionCheckBoxes(CheckBox[] checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (CheckBox otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private void toggleComplete(boolean complete) {
        Tasks selectedTasks = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTasks == null) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί εργασία.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (dbHelper.completeTask(selectedTasks.getId(), complete)) {
            System.out.println("Task completion status updated.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση εργασίας επιτυχής.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
            loadTasks(); // Φορτώνει ξανά τις εργασίες
        } else {
            System.out.println("Failed to update task completion status.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Αποτυχία ενημέρωση εργασίας.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
        }
    }



    private void loadTasks() {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allTasks.setAll(dbHelper.getAllTasks());
        updateTaskTable();
    }

    private void updateTaskTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Tasks> filteredTasks = FXCollections.observableArrayList(allTasks);

        // Φιλτράρισμα βάσει ολοκλήρωσης
        if (!showAllCheckbox.isSelected()) {
            if (showCompletedCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> !task.getCompleted());
            } else if (showPendingCheckbox.isSelected()) {
                filteredTasks.removeIf(Tasks::getCompleted);
            }
        }

        // Φιλτράρισμα βάσει πελάτη
        if (showWithCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() == 0);
        }
        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() != 0);
        }

        if (showErgentCheckBox.isSelected()) {
            filteredTasks.removeIf(task -> !task.getErgent());
        }
        if (showWaitCheckBox.isSelected()) {
            filteredTasks.removeIf(task -> !task.getWait());
        } else {
            filteredTasks.removeIf(tasks -> tasks.getWait());
        }

        // Φιλτράρισμα βάσει κατηγορίας
        TaskCategory selectedCategory = categoryFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedCategory != null && selectedCategory.getId() != 0) { // Εξαιρείται η κατηγορία "Όλες"
            filteredTasks.removeIf(task -> !task.getCategory().equals(selectedCategory.getName()));
        }


        // Ανανεώνουμε τα δεδομένα του πίνακα
        taskTable.setItems(filteredTasks);
    }



    private void handleAddTask() {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Εργασίας");
                AddTaskController controller = loader.getController();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveTask();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
                loadTasks();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
            }
    }

    private void handleEditTask() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Tasks selectedTasks = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTasks == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί εργασία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Εργασίας");
            AddTaskController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setTaskForEdit(selectedTasks);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveTask();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadTasks();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleDeleteTask() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Tasks selectedTasks = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTasks == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί εργασία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την εργασία " + selectedTasks.getTitle() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteTask(selectedTasks.getId());
            loadTasks();
        }
    }

    public void TaskCategoryManager() {
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
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void refresh(MouseEvent mouseEvent) {
        loadTasks();
    }
}
