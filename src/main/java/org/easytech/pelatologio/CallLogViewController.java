package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CallLog;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CallLogViewController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<CallLog> callLogTable;
    @FXML
    private TableColumn<CallLog, LocalDateTime> colStartTime;
    @FXML
    private TableColumn<CallLog, LocalDateTime> colEndTime;
    @FXML
    private TableColumn<CallLog, Long> colDuration;
    @FXML
    private TableColumn<CallLog, String> colCallType;
    @FXML
    private TableColumn<CallLog, String> colCallerNumber;
    @FXML
    private TableColumn<CallLog, String> colCallerName;
    @FXML
    private TableColumn<CallLog, String> colNotes;

    private CallLogDao callLogDao;
    private ObservableList<CallLog> masterData = FXCollections.observableArrayList();
    private FilteredList<CallLog> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        callLogDao = DBHelper.getCallLogDao();

        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        colCallType.setCellValueFactory(new PropertyValueFactory<>("callType"));
        colCallerNumber.setCellValueFactory(new PropertyValueFactory<>("callerNumber"));
        colCallerName.setCellValueFactory(new PropertyValueFactory<>("callerName"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        loadCallLogs();

        filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(callLog -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (callLog.getCallerNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (callLog.getCallerName() != null && callLog.getCallerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        callLogTable.setItems(filteredData);
    }

    private void loadCallLogs() {
        try {
            masterData.clear();
            masterData.addAll(callLogDao.getCallLogsByCustomerId(0)); // Load all call logs for now
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    @FXML
    private void handleRefresh() {
        loadCallLogs();
    }
}
