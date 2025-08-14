package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.PartnerEarning;

import java.time.LocalDate;

public class EditPartnerEarningStatusDialogController {

    @FXML private ComboBox<String> invoiceStatusComboBox;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField invoiceRefField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private PartnerEarning partnerEarning;

    @FXML
    public void initialize() {
        invoiceStatusComboBox.getItems().addAll("Pending", "Received", "Checked");
        paymentStatusComboBox.getItems().addAll("Unpaid", "Partially Paid", "Paid");

        // Listener for paymentStatusComboBox to enable/disable paymentDatePicker
        paymentStatusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            paymentDatePicker.setDisable(!"Paid".equals(newVal));
            if ("Paid".equals(newVal) && paymentDatePicker.getValue() == null) {
                paymentDatePicker.setValue(LocalDate.now());
            }
        });
    }

    public void setPartnerEarning(PartnerEarning earning) {
        this.partnerEarning = earning;
        if (earning != null) {
            invoiceStatusComboBox.setValue(earning.getPartnerInvoiceStatus());
            paymentStatusComboBox.setValue(earning.getPaymentToPartnerStatus());
            paymentDatePicker.setValue(earning.getPaymentToPartnerDate());
            invoiceRefField.setText(earning.getPartnerInvoiceRef());
        }
    }

    @FXML
    private void handleOkAction(ActionEvent event) {
        // Basic validation
        if (invoiceStatusComboBox.getValue() == null || paymentStatusComboBox.getValue() == null) {
            AlertDialogHelper.showDialog("Validation Error", "Missing Information", "Please select both Invoice Status and Payment Status.", Alert.AlertType.ERROR);
            return;
        }

        // Update the earning object
        partnerEarning.setPartnerInvoiceStatus(invoiceStatusComboBox.getValue());
        partnerEarning.setPaymentToPartnerStatus(paymentStatusComboBox.getValue());
        partnerEarning.setPaymentToPartnerDate(paymentDatePicker.getValue());
        partnerEarning.setPartnerInvoiceRef(invoiceRefField.getText());

        // Save to DB
        DBHelper.getPartnerEarningDao().updateEarningStatus(
            partnerEarning.getId(),
            partnerEarning.getPartnerInvoiceStatus(),
            partnerEarning.getPaymentToPartnerStatus(),
            partnerEarning.getPaymentToPartnerDate()
        );

        // Close the dialog
        ((Stage) okButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        // Close the dialog without saving
        ((Stage) cancelButton.getScene().getWindow()).close();
    }
}
