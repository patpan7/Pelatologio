package org.easytech.pelatologio;

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
import org.easytech.pelatologio.dao.SupplierPaymentDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Supplier;
import org.easytech.pelatologio.models.SupplierPayment;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SupplierPaymentsViewController {

    @FXML
    private TableView<SupplierPayment> paymentsTable;
    @FXML
    private TableColumn<SupplierPayment, String> customerColumn;
    @FXML
    private TableColumn<SupplierPayment, LocalDate> dateColumn;
    @FXML
    private TableColumn<SupplierPayment, BigDecimal> amountColumn;
    @FXML
    private TableColumn<SupplierPayment, String> descriptionColumn;
    @FXML
    private TableColumn<SupplierPayment, Boolean> calculatedColumn;

    private Supplier supplier;
    private ObservableList<SupplierPayment> paymentList;
    private SupplierPaymentDao paymentDao;

    @FXML
    public void initialize() {
        this.paymentDao = DBHelper.getSupplierPaymentDao();
        setupTable();
    }

    private void setupTable() {
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        calculatedColumn.setCellValueFactory(new PropertyValueFactory<>("calculated"));

        paymentList = FXCollections.observableArrayList();
        paymentsTable.setItems(paymentList);

        // Add double-click event handler for editing
        paymentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditPayment(null);
            }
        });
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        loadPayments();
    }

    private void loadPayments() {
        paymentList.clear();
        if (supplier != null) {
            List<SupplierPayment> payments = paymentDao.getPaymentsForSupplier(supplier.getId());
            paymentList.addAll(payments);
        }
    }

    @FXML
    void handleAddPayment(ActionEvent event) {
        openPaymentDialog(null);
    }

    @FXML
    void handleEditPayment(ActionEvent event) {
        SupplierPayment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openPaymentDialog(selected);
        }
    }

    @FXML
    void handleDeletePayment(ActionEvent event) {
        SupplierPayment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete this payment?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                paymentDao.deletePayment(selected.getId());
                loadPayments();
            }
        }
    }

    private void openPaymentDialog(SupplierPayment payment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editSupplierPaymentDialog.fxml"));
            DialogPane pane = loader.load();

            EditSupplierPaymentDialogController controller = loader.getController();
            controller.setSupplier(this.supplier);
            controller.setPayment(payment);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(payment == null ? "Add New Payment" : "Edit Payment");

            final Button okButton = (Button) pane.lookupButton(pane.getButtonTypes().get(0));
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.handleOk()) {
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Notifications.create()
                        .title("Success")
                        .text("Payment saved successfully.")
                        .position(Pos.TOP_RIGHT)
                        .hideAfter(Duration.seconds(5))
                        .showInformation();
                loadPayments();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}