package org.easytech.pelatologio;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CallNotesController;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.models.Customer;

import java.io.IOException;
import java.sql.SQLException;

public class CustomerCallLogController {
    @FXML
    private TableView<CallLog> callLogTable;
    @FXML
    private TableColumn colStartTime, colEndTime, colDuration, colCallType, colCallerNumber, colNotes;

    private CallLogDao callLogDao;
    private final ObservableList<CallLog> masterData = FXCollections.observableArrayList();
    private FilteredList<CallLog> filteredData;
    private Customer customer;

    @FXML
    public void initialize() {
        callLogDao = DBHelper.getCallLogDao();

        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        colCallType.setCellValueFactory(new PropertyValueFactory<>("callType"));
        colCallerNumber.setCellValueFactory(new PropertyValueFactory<>("callerNumber"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        filteredData = new FilteredList<>(masterData, p -> true);

        callLogTable.setItems(filteredData);

        callLogTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CallLog selectedCall = callLogTable.getSelectionModel().getSelectedItem();
                if (selectedCall != null) {
                    openCallNotes(selectedCall);
                }
            }
        });
    }

    private void openCallNotes(CallLog selectedCall) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/call_notes.fxml"));
            Parent root = loader.load();
            CallNotesController controller = loader.getController();

            // Fetch the customer for the selected call log
            Customer customer = null;
            if (selectedCall.getCustomerId() > 0) {
                customer = DBHelper.getCustomerDao().getSelectedCustomer(selectedCall.getCustomerId());
            } else {
                // If no customerId, create a dummy customer for display purposes
                customer = new Customer();
                customer.setName("");
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Σημειώσεις Κλήσης");
            stage.setScene(new Scene(root));
            controller.initialize(stage, selectedCall, customer);
            controller.setData();
            stage.showAndWait();
            loadCallLogs(); // Refresh table after notes are saved
        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Αδυναμία φόρτωσης σημειώσεων κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadCallLogs() {
        try {
            masterData.clear();
            masterData.addAll(callLogDao.getCallLogsByCustomerId(customer.getCode())); // Load all call logs for now
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadCallLogs();
    }

}
