package org.easytech.pelatologio.helper;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CallNotesController {

    public Label lbCustomer;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private Button saveAndEndCallButton;

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
    }

    @FXML
    private void handleSaveAndEndCall() {
        try {
            callLog.setNotes(notesTextArea.getText());
            callLog.setEndTime(LocalDateTime.now());
            callLog.setDurationSeconds(ChronoUnit.SECONDS.between(callLog.getStartTime(), callLog.getEndTime()));
            callLogDao.updateCallLog(callLog);
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα Βάσης Δεδομένων", "Αδυναμία αποθήκευσης σημειώσεων και τερματισμού κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
