package org.easytech.pelatologio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class TaskListController {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, Integer> idColumn;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    @FXML
    private TableColumn<Task, String> dueDateColumn;

    @FXML
    private CheckBox showAllCheckbox;
    @FXML
    private CheckBox showCompletedCheckbox;
    @FXML
    private CheckBox showPendingCheckbox;
    @FXML
    private CheckBox showWithCustomerCheckbox;
    @FXML
    private CheckBox showWithoutCustomerCheckbox;

    @FXML
    private Button addTaskButton;
    @FXML
    private Button editTaskButton;
    @FXML
    private Button deleteTaskButton;

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    public void initialize() {
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(data -> data.getValue().getId());
        titleColumn.setCellValueFactory(data -> data.getValue().getTitle());
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());

        // Αρχικό γέμισμα του πίνακα
        loadTasks();

        // Φίλτρα
        showAllCheckbox.setOnAction(e -> updateTaskTable());
        showCompletedCheckbox.setOnAction(e -> updateTaskTable());
        showPendingCheckbox.setOnAction(e -> updateTaskTable());
        showWithCustomerCheckbox.setOnAction(e -> updateTaskTable());
        showWithoutCustomerCheckbox.setOnAction(e -> updateTaskTable());

        // Κουμπιά
        addTaskButton.setOnAction(e -> handleAddTask());
        editTaskButton.setOnAction(e -> handleEditTask());
        deleteTaskButton.setOnAction(e -> handleDeleteTask());
    }

    private void loadTasks() {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allTasks.setAll(dbHelper.getTasks());
        updateTaskTable();
    }

    private void updateTaskTable() {
        ObservableList<Task> filteredTasks = FXCollections.observableArrayList(allTasks);

        if (!showAllCheckbox.isSelected()) {
            if (showCompletedCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> !task.isCompleted());
            } else if (showPendingCheckbox.isSelected()) {
                filteredTasks.removeIf(Task::isCompleted);
            }
        }

        if (showWithCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() == null);
        }

        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() != null);
        }

        taskTable.setItems(filteredTasks);
    }

    private void handleAddTask() {
//        // Άνοιγμα παραθύρου προσθήκης εργασίας
//        Task newTask = TaskDialog.showAddTaskDialog();
//        if (newTask != null) {
//            DBHelper.saveTask(newTask);
//            loadTasks();
//        }
    }

    private void handleEditTask() {
//        // Επεξεργασία επιλεγμένης εργασίας
//        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
//        if (selectedTask != null) {
//            Task updatedTask = TaskDialog.showEditTaskDialog(selectedTask);
//            if (updatedTask != null) {
//                DBHelper.updateTask(updatedTask);
//                loadTasks();
//            }
//        }
    }

    private void handleDeleteTask() {
//        // Διαγραφή επιλεγμένης εργασίας
//        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
//        if (selectedTask != null) {
//            DBHelper.deleteTask(selectedTask.getId());
//            loadTasks();
//        }
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
