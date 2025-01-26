package org.easytech.pelatologio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class TaskListController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn;

    @FXML
    private CheckBox showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox;

    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton, completeTaskButton, uncompletedTaskButton;

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
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
        showAllCheckbox.setOnAction(e -> updateTaskTable());
        showCompletedCheckbox.setOnAction(e -> updateTaskTable());
        showPendingCheckbox.setOnAction(e -> updateTaskTable());
        showWithCustomerCheckbox.setOnAction(e -> updateTaskTable());
        showWithoutCustomerCheckbox.setOnAction(e -> updateTaskTable());

        // Κουμπιά
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
        ObservableList<Task> filteredTasks = FXCollections.observableArrayList(allTasks);

        if (!showAllCheckbox.isSelected()) {
            if (showCompletedCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> !task.getCompleted());
            } else if (showPendingCheckbox.isSelected()) {
                filteredTasks.removeIf(Task::getCompleted);
            }
        }

        if (showWithCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() == 0);
        }

        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() != 0);
        }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddTask.fxml"));
            DialogPane dialogPane = loader.load();
            AddTaskController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setTaskForEdit(selectedTask);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Εργασίας");
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

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
