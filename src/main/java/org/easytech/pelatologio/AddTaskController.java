package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.ComboBoxHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.TaskCategory;
import org.easytech.pelatologio.models.Tasks;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AddTaskController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<String> startHourComboBox;

    @FXML
    private ComboBox<String> startMinuteComboBox;
    @FXML
    private ComboBox<Integer> durationComboBox;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<TaskCategory> categoryComboBox;
    @FXML
    private JFXButton btnCustomer;
    @FXML
    private JFXCheckBox is_completed, is_ergent, is_wait, is_calendar;

    private Tasks tasks;
    private int customerId;
    private String customerName;
    private Customer selectedCustomer;
    private FilteredList<Customer> filteredCustomers;

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String custName) {
        for (Customer customer : customerComboBox.getItems()) {
            if (customer.getName().equals(custName)) {
                customerComboBox.setValue(customer);
                break;
            }
        }
    }

    public void setTaskTitle(String title) {
        titleField.setText(title);
    }

    public void setTaskForEdit(Tasks tasks) {
        this.tasks = tasks;
        titleField.setText(tasks.getTitle());
        descriptionField.setText(tasks.getDescription());
        dueDatePicker.setValue(tasks.getDueDate());
        startHourComboBox.setValue(String.format("%02d", tasks.getStartTime() != null ? tasks.getStartTime().getHour() : 0));
        startMinuteComboBox.setValue(String.format("%02d", tasks.getStartTime() != null ? tasks.getStartTime().getMinute() : 0));
        long duration = 0;
        if (tasks.getStartTime() != null && tasks.getEndTime() != null)
            duration = java.time.Duration.between(tasks.getStartTime(), tasks.getEndTime()).toMinutes();
        durationComboBox.setValue((int) duration);
        for (TaskCategory tasksCategory : categoryComboBox.getItems()) {
            if (tasksCategory.getName().equals(tasks.getCategory())) {
                categoryComboBox.setValue(tasksCategory);
                break;
            }
        }
        // Αν υπάρχει πελάτης, προ-συμπλήρωσε την επιλογή
        if (tasks.getCustomerId() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCode() == tasks.getCustomerId()) {
                    customerComboBox.setValue(customer);
                    break;
                }
            }
        }

        is_completed.setSelected(tasks.getCompleted());
        is_ergent.setSelected(tasks.getErgent());
        is_wait.setSelected(tasks.getWait());
        is_calendar.setSelected(tasks.getIsCalendar());

    }


    public void initialize() throws SQLException {
        // Φόρτωση πελατών
        DBHelper dbHelper = new DBHelper();
        List<Customer> customers = DBHelper.getCustomerDao().getCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers));
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        // StringConverter για σωστή διαχείριση αντικειμένων
        customerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return customers.stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        ComboBoxHelper.setupFilter(customerComboBox, filteredCustomers);
        customerComboBox.setVisibleRowCount(5);
        List<TaskCategory> categories = DBHelper.getTaskDao().getAllTaskCategory();
        categoryComboBox.getItems().addAll(categories);
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TaskCategory tasksCategory) {
                return tasksCategory != null ? tasksCategory.getName() : "";
            }

            @Override
            public TaskCategory fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(tasksCategory -> tasksCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        dueDatePicker.setValue(LocalDate.now());
        CheckBox[] checkBoxes = {
                is_ergent,
                is_wait
        };
        configureSingleSelectionCheckBoxes(checkBoxes);
        for (int hour = 7; hour < 23; hour++) {
            startHourComboBox.getItems().add(String.format("%02d", hour));
        }
        for (int minute = 0; minute < 60; minute += 5) {
            startMinuteComboBox.getItems().add(String.format("%02d", minute));
        }

        durationComboBox.getItems().addAll(15, 30, 45, 60, 90, 120, 0);
        durationComboBox.getSelectionModel().select(1); // Προεπιλογή 30 λεπτά
    }


    public boolean handleSaveTask() {
        try {
            if (dueDatePicker.getValue() == null || titleField.getText() == null || descriptionField.getText() == null) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();
                });
                return false;
            }
            if (is_calendar.isSelected()) {
                if (startHourComboBox.getValue() == null || startHourComboBox.getValue() == null ||
                        startMinuteComboBox.getValue() == null || durationComboBox.getValue() == null ||
                        categoryComboBox.getValue() == null) {
                    Platform.runLater(() -> {
                        Notifications notifications = Notifications.create()
                                .title("Σφάλμα")
                                .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showError();
                    });
                    return false; // Αποτυχία
                }
            }

            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate date = dueDatePicker.getValue();
            int startHour = Integer.parseInt(startHourComboBox.getValue());
            int startMinute = Integer.parseInt(startMinuteComboBox.getValue());
            LocalDateTime startDateTime = dueDatePicker.getValue().atTime(startHour, startMinute);
            int duration = 0; // Προεπιλεγμένη τιμή για τη διάρκεια
            String durationString = String.valueOf(durationComboBox.getValue());
            if (durationString != null && !durationString.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationString); // Μετατροπή από String σε Integer
                } catch (NumberFormatException e) {
                    System.out.println("Σφάλμα: Η διάρκεια δεν είναι έγκυρος αριθμός.");
                    duration = 15; // Αν αποτύχει, θέτουμε μια προεπιλεγμένη τιμή
                }
            }

            LocalDateTime endDateTime = startDateTime.plusMinutes(duration);
            Customer selectedCustomer = customerComboBox.getValue(); // Απευθείας χρήση του ComboBox
            String category = categoryComboBox.getValue().getName();
            Boolean isCompleted = is_completed.isSelected();
            Boolean isErgent = is_ergent.isSelected();
            Boolean isWait = is_wait.isSelected();
            Boolean isCalendar = is_calendar.isSelected();

            DBHelper dbHelper = new DBHelper();

            if (tasks == null) {
                // Δημιουργία νέας εργασίας
                Tasks newTask = new Tasks();
                newTask.setId(0);
                newTask.setTitle(title);
                newTask.setTitle(title);
                newTask.setDescription(description);
                newTask.setDueDate(date);
                newTask.setCategory(category);
                newTask.setCustomerId(selectedCustomer != null ? selectedCustomer.getCode() : 0);
                newTask.setErgent(isErgent);
                newTask.setWait(isWait);
                newTask.setCalendar(isCalendar);
                newTask.setStartTime(startDateTime);
                newTask.setEndTime(endDateTime);

                //Tasks newTasks = new Tasks(0, title, description, date, false, category, selectedCustomer != null ? selectedCustomer.getCode() : 0, isErgent, isWait);
                DBHelper.getTaskDao().saveTask(newTask);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                tasks.setTitle(title);
                tasks.setDescription(description);
                tasks.setDueDate(date);
                tasks.setCategory(category);
                int customerId = selectedCustomer != null ? selectedCustomer.getCode() : 0;
                tasks.setCustomerId(customerId);
                tasks.setErgent(isErgent);
                tasks.setWait(isWait);
                tasks.setCalendar(isCalendar);
                tasks.setStartTime(startDateTime);
                tasks.setEndTime(endDateTime);
                tasks.setCompleted(isCompleted);
                DBHelper.getTaskDao().updateTask(tasks);
            }

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Η εργασία αποθηκεύτηκε!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();
            });
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
            return false;
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(descriptionField.getText());
        }
    }

    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Σημειώσεων");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(600, 500);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            descriptionField.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void showCustomer(ActionEvent evt) {
        DBHelper dbHelper = new DBHelper();

        Customer selectedCustomer = DBHelper.getCustomerDao().getSelectedCustomer(tasks.getCustomerId());
        if (selectedCustomer.getCode() == 0) {
            System.out.println("No customer selected.");
            return;
        }
        try {
            String res = DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerForEdit(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void lock() {
        customerComboBox.setDisable(true);
        btnCustomer.setDisable(true);
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

    public void checkCalendar() {
        is_calendar.setSelected(true);
    }


}
