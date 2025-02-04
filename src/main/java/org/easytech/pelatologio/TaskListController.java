package org.easytech.pelatologio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;

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
    private TableView<Task> taskTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn, categoryColumn;

    @FXML
    private CheckBox showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox;

    @FXML
    private ComboBox <TaskCategory> categoryFilterComboBox;
    @FXML
    private Button addCategoryButton, addTaskButton, editTaskButton, deleteTaskButton, completeTaskButton, uncompletedTaskButton;

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

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
        // Αρχικό γέμισμα του πίνακα
        loadTasks();

        // RowFactory για διαφορετικά χρώματα
        taskTable.setRowFactory(tv -> new TableRow<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setStyle("");
                } else {
                    if (task.getCompleted()) {
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
                Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedTask != null) {
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
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί εργασία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (dbHelper.completeTask(selectedTask.getId(), complete)) {
            System.out.println("Task completion status updated.");
            loadTasks(); // Φορτώνει ξανά τις εργασίες
        } else {
            System.out.println("Failed to update task completion status.");
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
        ObservableList<Task> filteredTasks = FXCollections.observableArrayList(allTasks);

        // Φιλτράρισμα βάσει ολοκλήρωσης
        if (!showAllCheckbox.isSelected()) {
            if (showCompletedCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> !task.getCompleted());
            } else if (showPendingCheckbox.isSelected()) {
                filteredTasks.removeIf(Task::getCompleted);
            }
        }

        // Φιλτράρισμα βάσει πελάτη
        if (showWithCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() == 0);
        }
        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() != 0);
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
                e.printStackTrace();
            }
    }

    private void handleEditTask() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
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
            controller.setTaskForEdit(selectedTask);
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
            e.printStackTrace();
        }
    }

    private void handleDeleteTask() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί εργασία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την εργασία " + selectedTask.getTitle() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteTask(selectedTask.getId());
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
            e.printStackTrace();
        }
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

}
