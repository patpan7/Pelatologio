package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerSubsController {
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn idColumn, titleColumn, endDateColumn, categoryColumn, priceColumn;
    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton, completeTaskButton, uncompletedTaskButton;

    private ObservableList<Subscription> allSubs;

    Customer customer;

    @FXML
    public void initialize() {
        setTooltip(addTaskButton, "Προσθήκη νέας εργασίας");
        setTooltip(editTaskButton, "Επεξεργασία εργασίας");
        setTooltip(deleteTaskButton, "Διαγραφή εργασίας");
        setTooltip(completeTaskButton, "Σημείωση εργασίας ως ολοκληρωμένη");
        setTooltip(uncompletedTaskButton,"Σημείωση εργασίας ως σε επεξεργασία");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        allSubs = FXCollections.observableArrayList();
        subsTable.setItems(allSubs);


        subsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedSub != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditSub(selectedSub);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

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


    private void loadTasks(int customerCode) {
        allSubs.clear();
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allSubs.setAll(dbHelper.getAllCustomerSubs(customerCode));
    }


    @FXML
    private void handleAddTask() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setCustomerId(customer.getCode());
            controller.setCustomerName(customer.getName());
            controller.lock();
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
            loadTasks(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditTask() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
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
            controller.lock();
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
            loadTasks(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleDeleteTask() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
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
            loadTasks(customer.getCode());
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadTasks(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void toggleComplete(ActionEvent event) {
        toggleComplete(true);
    }

    public void toggleRecall(ActionEvent event) {
        toggleComplete(false);
    }
}
