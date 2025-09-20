package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import org.easytech.pelatologio.dao.SubscriptionDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.ReportManager;
import org.easytech.pelatologio.models.Subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SubscriptionReportController {

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    private SubscriptionDao subscriptionDao;

    @FXML
    public void initialize() {
        this.subscriptionDao = DBHelper.getSubscriptionDao();
        dateFrom.setValue(LocalDate.now().withDayOfMonth(1)); // First day of current month
        dateTo.setValue(LocalDate.now()); // Current date
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate fromDate = dateFrom.getValue();
        LocalDate toDate = dateTo.getValue();

        if (fromDate == null || toDate == null) {
            AlertDialogHelper.showDialog("Σφάλμα", "Παρακαλώ επιλέξτε ημερομηνίες.", "", Alert.AlertType.ERROR);
            return;
        }

        List<Subscription> subscriptions = subscriptionDao.getAllSubs(fromDate, toDate);
        String fromDateStr = fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String toDateStr = toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        ReportManager.generateSubscriptionReport(subscriptions, fromDateStr, toDateStr);
    }

    @FXML
    private void handleExpiredReport() {
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.now();
        List<Subscription> subscriptions = subscriptionDao.getAllSubs(fromDate, toDate);
        String fromDateStr = fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String toDateStr = toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        ReportManager.generateSubscriptionReport(subscriptions, fromDateStr, toDateStr);
    }
}