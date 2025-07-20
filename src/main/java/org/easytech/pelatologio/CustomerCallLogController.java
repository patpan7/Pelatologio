package org.easytech.pelatologio;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Order;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerCallLogController {
    @FXML
    private TableView<CallLog> callLogTable;
    @FXML
    private TableColumn colStartTime, colEndTime, colDuration, colCallType, colCallerNumber, colNotes;

    private CallLogDao callLogDao;
    private ObservableList<CallLog> masterData = FXCollections.observableArrayList();
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
