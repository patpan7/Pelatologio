package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private final ObservableList<CallLog> masterData = FXCollections.observableArrayList();
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
                } else
                    return callLog.getCallerName() != null && callLog.getCallerName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        callLogTable.setItems(filteredData);

        // Single-click to open call notes
        callLogTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CallLog selectedCall = callLogTable.getSelectionModel().getSelectedItem();
                if (selectedCall != null) {
                    openCallNotes(selectedCall);
                }
            }
        });

        // Right-click context menu for deletion
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Διαγραφή");
        deleteItem.setOnAction(event -> {
            CallLog selectedCall = callLogTable.getSelectionModel().getSelectedItem();
            if (selectedCall != null) {
                AlertDialogHelper.showDialog("Επιβεβαίωση Διαγραφής", "Είστε σίγουροι ότι θέλετε να διαγράψετε αυτήν την κλήση;", "Αυτή η ενέργεια δεν μπορεί να αναιρεθεί.", Alert.AlertType.CONFIRMATION);
                var alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Επιβεβαίωση Διαγραφής");
                alert.setHeaderText(null);
                alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο login;");

                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        callLogDao.deleteCallLog(selectedCall.getId());
                        loadCallLogs(); // Refresh table after deletion
                        AlertDialogHelper.showDialog("Επιτυχία", "Η κλήση διαγράφηκε επιτυχώς.", null, Alert.AlertType.INFORMATION);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        AlertDialogHelper.showDialog("Σφάλμα Βάσης Δεδομένων", "Αδυναμία διαγραφής κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
        });
        contextMenu.getItems().add(deleteItem);
        callLogTable.setContextMenu(contextMenu);
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
                customer.setName("Άγνωστος Πελάτης");
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
            masterData.addAll(callLogDao.getCallLogs()); // Load all call logs for now
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
