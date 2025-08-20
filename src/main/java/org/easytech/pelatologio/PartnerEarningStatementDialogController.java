package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.easytech.pelatologio.models.Partner;
import org.easytech.pelatologio.models.PartnerEarning;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PartnerEarningStatementDialogController {

    @FXML
    private TextArea statementTextArea;

    public void setStatementData(List<PartnerEarning> earnings, Partner selectedPartner) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (selectedPartner != null && selectedPartner.getId() != 0) {
            sb.append("Καρτέλα Οφειλών για Συνεργάτη: ").append(selectedPartner.getName()).append("\n");
        } else {
            sb.append("Συνολική Καρτέλα Οφειλών Συνεργατών\n");
        }
        sb.append("----------------------------------------------------");

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PartnerEarning earning : earnings) {
            sb.append(String.format("Ημ/νία: %s, Πελάτης: %s, Προμηθευτής: %s, Ποσό: %.2f€, Τιμολόγιο: %s (%s), Πληρωμή: %s (%s)\n",
                    earning.getEarningDate().format(dateFormatter),
                    earning.getCustomerName(),
                    earning.getSupplierName(),
                    earning.getEarningAmount(),
                    earning.getPartnerInvoiceStatus(),
                    earning.getPartnerInvoiceRef() != null ? earning.getPartnerInvoiceRef() : "-",
                    earning.getPaymentToPartnerStatus(),
                    earning.getPaymentToPartnerDate() != null ? earning.getPaymentToPartnerDate().format(dateFormatter) : "-"
            ));
            totalAmount = totalAmount.add(earning.getEarningAmount());
            if (earning.getPaymentToPartnerStatus().equals("Unpaid")) {
                totalUnpaid = totalUnpaid.add(earning.getEarningAmount());
            } else if (earning.getPaymentToPartnerStatus().equals("Paid")) {
                totalPaid = totalPaid.add(earning.getEarningAmount());
            }
        }

        sb.append("----------------------------------------------------");
        sb.append(String.format("Σύνολο Οφειλών: %.2f€\n", totalAmount));
        sb.append(String.format("Σύνολο Ανεξόφλητων: %.2f€\n", totalUnpaid));
        sb.append(String.format("Σύνολο Πληρωμένων: %.2f€\n", totalPaid));

        statementTextArea.setText(sb.toString());
    }

    @FXML
    private void handleCopy() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(statementTextArea.getText());
        clipboard.setContent(content);
    }

    @FXML
    private void handleClose() {
        // Get the current stage and close it
        Stage stage = (Stage) statementTextArea.getScene().getWindow();
        stage.close();
    }
}
