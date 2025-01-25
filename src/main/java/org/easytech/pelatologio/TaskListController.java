package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class TaskListController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn, isCompletedColumn;
    @FXML
    private CheckBox showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox;
    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton;

    private DBHelper dbHelper;
    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dbHelper = new DBHelper();
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("isCompleted"));
        isCompletedColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

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
        allTasks.setAll(dbHelper.getAllTasks());
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
            filteredTasks.removeIf(task -> task.getCustomerId() == 0);
        }

        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredTasks.removeIf(task -> task.getCustomerId() != 0);
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

    public void mainMenuClick(ActionEvent actionEvent) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
