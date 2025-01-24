package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;


public class TaskListController {
    @FXML
    StackPane stackPane;
    @FXML
    public TableColumn titleColumn, descriptionColumn, dueDateColumn, customerColumn, categoryColumn, statusColumn;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private ComboBox<String> filterComboBox;

    public void initialize() {
        DBHelper dbHelper = new DBHelper();

        // Δημιουργία και αρχικοποίηση των στηλών
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        filterComboBox.getItems().addAll("Γενικές", "Πελάτες");
        filterComboBox.getSelectionModel().select(0);
        loadTasks();
    }

    private void loadTasks() {
        taskTable.getItems().clear();
        DBHelper dbHelper = new DBHelper();
        taskTable.getItems().addAll(dbHelper.getTasks(null));
    }

    @FXML
    private void handleAddTask() {
        LocalDateTime localDate = LocalDateTime.now();
        Task newTask = new Task("", "", localDate, null, null, false);
        openTaskDialog(newTask);
        if (newTask.getId() != 0) {
            taskTable.getItems().add(newTask);
        }
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            openTaskDialog(selectedTask);
            taskTable.refresh();
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
//            if (DBHelper.deleteTask(selectedTask.getId())) {
//                taskTable.getItems().remove(selectedTask);
//            }
        }
    }

    private void openTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            AddTaskController controller = loader.getController();
            controller.loadTask(task);
            stage.showAndWait();
            if (controller.isSaved()) {
                loadTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilterCalls() {
        taskTable.getItems().clear();
//        taskTable.getItems().addAll(DBHelper.getTasksByCategory("Τηλεφωνική Κλήση"));
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
