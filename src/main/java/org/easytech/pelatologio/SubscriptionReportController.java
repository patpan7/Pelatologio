package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.dao.SubscriptionDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Subscription;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionReportController {

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    private SubscriptionDao subscriptionDao;

    @FXML
    public void initialize() {
        this.subscriptionDao = DBHelper.getSubscriptionDao();
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate fromDate = dateFrom.getValue();
        LocalDate toDate = dateTo.getValue();

        if (fromDate == null || toDate == null) {
            AlertDialogHelper.showDialog("Σφάλμα", "Παρακαλώ επιλέξτε ημερομηνίες.", "", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 1. Fetch Data
            List<Subscription> subscriptions = subscriptionDao.getAllSubs(fromDate, toDate);
            if (subscriptions.isEmpty()) {
                AlertDialogHelper.showDialog("Ενημέρωση", "Δεν βρέθηκαν συνδρομές για το επιλεγμένο διάστημα.", "", Alert.AlertType.INFORMATION);
                return;
            }

            // 2. Convert to a list of wrapper objects with java.util.Date
            List<SubscriptionReportWrapper> reportData = new ArrayList<>();
            for (Subscription sub : subscriptions) {
                reportData.add(new SubscriptionReportWrapper(sub));
            }

            // 3. Create JRBeanCollectionDataSource with the new list
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            // 4. Set Report Parameters
            Map<String, Object> parameters = new HashMap<>();
            String fromDateStr = fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String toDateStr = toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            parameters.put("fromDate", fromDateStr);
            parameters.put("toDate", toDateStr);

            // 5. Load and Compile Report
            String reportStream = getPath("subscription_report.jrxml");
            if (reportStream == null) {
                throw new JRException("Report template not found: /images/subscription_report.jrxml");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 6. Fill Report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // 7. Print Report (with print dialog)
            JasperPrintManager.printReport(jasperPrint, true);

        } catch (JRException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα JasperReports", "Προέκυψε σφάλμα κατά τη δημιουργία της αναφοράς.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private static String getPath(String name) {
        // Παίρνει τον φάκελο που τρέχει η εφαρμογή (ο φάκελος του .exe)
        String currentDir = System.getProperty("user.dir");
        String fullPath = currentDir + File.separator + "images" + File.separator + name;

        // Έλεγχος αν υπάρχει το αρχείο
        File imageFile = new File(fullPath);
        if (!imageFile.exists()) {
            System.out.println("❌ Δεν βρέθηκε η εικόνα: " + fullPath);
            Notifications notifications = Notifications.create()
                    .title("Σφάλμα")
                    .text("❌ Δεν βρέθηκε η εικόνα: " + fullPath)
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return null;
        }
        return fullPath;
    }

    // Wrapper class for the report
    public static class SubscriptionReportWrapper {
        private final Subscription original;

        public SubscriptionReportWrapper(Subscription original) {
            this.original = original;
        }

        public String getCustomerName() {
            return original.getCustomerName();
        }

        public String getTitle() {
            return original.getTitle();
        }

        public Date getEndDate() {
            if (original.getEndDate() == null) {
                return null;
            }
            return Date.from(original.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        public String getPrice() {
            try {
                double priceValue = Double.parseDouble(original.getPrice().replace(",", "."));
                return String.format("%,.2f€", priceValue).replace(".", ",");
            } catch (NumberFormatException | NullPointerException e) {
                return original.getPrice(); // Return original value if parsing fails
            }
        }
    }
}