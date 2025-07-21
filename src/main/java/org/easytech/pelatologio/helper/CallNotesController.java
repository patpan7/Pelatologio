package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.easytech.pelatologio.AddOfferController;
import org.easytech.pelatologio.AddOrderController;
import org.easytech.pelatologio.AddTaskController;
import org.easytech.pelatologio.MainMenuController;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.scene.layout.HBox;

public class CallNotesController {

    public Label lbCustomer;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private Button saveAndEndCallButton;
    @FXML
    private HBox manualCallTypeBox;
    @FXML
    private RadioButton incomingRadio;
    @FXML
    private RadioButton outgoingRadio;
    @FXML
    private CheckBox createTaskCheckBox;
    @FXML
    private CheckBox createOfferCheckBox;
    @FXML
    private CheckBox createOrderCheckBox;

    private ToggleGroup callTypeGroup;
    private Stage stage;
    private CallLog callLog;
    private CallLogDao callLogDao;
    private Customer customer;

    public void initialize(Stage stage, CallLog callLog, Customer customer) {
        this.stage = stage;
        this.callLog = callLog;
        this.callLogDao = DBHelper.getCallLogDao();
        this.customer = customer;
        lbCustomer.setText(customer.getName());

        callTypeGroup = new ToggleGroup();
        incomingRadio.setToggleGroup(callTypeGroup);
        outgoingRadio.setToggleGroup(callTypeGroup);

        if ("MANUAL".equals(callLog.getType())) {
            manualCallTypeBox.setVisible(true);
            incomingRadio.setSelected(true); // Default selection
        } else {
            manualCallTypeBox.setVisible(false);
        }
    }

    public void setData() {
        lbCustomer.setText(customer.getName());
        notesTextArea.setText(callLog.getNotes());
        manualCallTypeBox.setVisible(false);
    }

    @FXML
    private void handleSaveAndEndCall() {
        try {
            if (manualCallTypeBox.isVisible()) {
                if (incomingRadio.isSelected()) {
                    callLog.setType("MANUAL_IN");
                } else if (outgoingRadio.isSelected()) {
                    callLog.setType("MANUAL_OUT");
                }
            }

            callLog.setNotes(notesTextArea.getText());
            callLog.setEndTime(LocalDateTime.now());
            callLog.setDurationSeconds(ChronoUnit.SECONDS.between(callLog.getStartTime(), callLog.getEndTime()));
            callLogDao.updateCallLog(callLog);

            if (createTaskCheckBox.isSelected()) {
                newTask();
            }
            if (createOfferCheckBox.isSelected()) {
                addOffer();
            }
            if (createOrderCheckBox.isSelected()) {
                addOrder();
            }

            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα Βάσης Δεδομένων", "Αδυναμία αποθήκευσης σημειώσεων και τερματισμού κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void newTask() {
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/addTask.fxml"));
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
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    private void addOffer() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/addOffer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Προσφοράς");
            AddOfferController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setCustomerName(customer.getName());
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOffer();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void addOrder() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Παραγγελίας");
            AddOrderController controller = loader.getController();
            controller.setCustomerName(customer.getName());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOrder();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
