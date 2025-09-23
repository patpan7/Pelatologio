package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.easytech.pelatologio.dao.CommissionDao;
import org.easytech.pelatologio.helper.CustomNotification;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Commission;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CommissionsController {

    @FXML
    private TableView<Commission> commissionsTable;
    @FXML
    private TableColumn<Commission, String> partnerColumn;
    @FXML
    private TableColumn<Commission, String> customerColumn;
    @FXML
    private TableColumn<Commission, String> supplierColumn;
    @FXML
    private TableColumn<Commission, Double> rateColumn;
    @FXML
    private TableColumn<Commission, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Commission, LocalDate> endDateColumn;

    private CommissionDao commissionDao;
    private ObservableList<Commission> commissionList;

    @FXML
    public void initialize() {
        this.commissionDao = DBHelper.getCommissionDao();
        setupTable();
        loadCommissions();

        // Add double-click event handler for editing
        commissionsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Commission selected = commissionsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openCommissionDialog(selected);
                }
            }
        });
    }

    private void setupTable() {
        partnerColumn.setCellValueFactory(new PropertyValueFactory<>("partnerName"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        commissionList = FXCollections.observableArrayList();
        commissionsTable.setItems(commissionList);
    }

    private void loadCommissions() {
        commissionList.clear();
        List<Commission> allCommissions = commissionDao.getAllCommissions();
        commissionList.addAll(allCommissions);
    }

    @FXML
    void handleAddCommission(ActionEvent event) {
        openCommissionDialog(null);
    }

    @FXML
    void handleEditCommission(ActionEvent event) {
        Commission selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openCommissionDialog(selected);
        } else {
            // Show alert: no selection
        }
    }

    @FXML
    void handleDeleteCommission(ActionEvent event) throws SQLException {
        Commission selected = commissionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Επιβεβαίωση Διαγραφής");
            alert.setHeaderText("Είστε σίγουροι ότι θέλετε να διαγράψετε αυτήν την προμήθεια;");
            alert.setContentText("Παρακαλώ επιβεβαιώστε τη διαγραφή.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                commissionDao.deleteCommission(selected.getId());
                CustomNotification.create()
                        .title("Επιτυχία")
                        .text("Η προμήθεια διαγράφηκε με επιτυχία.")
                        .position(Pos.TOP_RIGHT)
                        .hideAfter(Duration.seconds(5))
                        .showConfirmation();
                loadCommissions();
            }
        } else {
            CustomNotification.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε μια προμήθεια για διαγραφή.")
                    .position(Pos.TOP_RIGHT)
                    .hideAfter(Duration.seconds(5))
                    .showWarning();
        }
    }

    private void openCommissionDialog(Commission commission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editCommissionDialog.fxml"));
            DialogPane pane = loader.load();

            EditCommissionDialogController controller = loader.getController();
            controller.setCommission(commission);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(commission == null ? "Add New Commission" : "Edit Commission");

            // Get the OK button from the dialog pane
            final Button okButton = (Button) pane.lookupButton(pane.getButtonTypes().get(0));

            // Add an event filter to handle the OK action
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Call the save method in the dialog controller
                if (!controller.handleOk()) {
                    // If validation fails, consume the event to prevent the dialog from closing
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                loadCommissions();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
