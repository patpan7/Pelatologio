package org.easytech.pelatologio.helper;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.easytech.pelatologio.CustomNotification;
import org.easytech.pelatologio.models.Subscription;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ReportManager {

    public static void generateSubscriptionReport(List<Subscription> subscriptions, String fromDate, String toDate) {
        try {
            if (subscriptions.isEmpty()) {
                AlertDialogHelper.showDialog("Ενημέρωση", "Δεν βρέθηκαν δεδομένα για την αναφορά.", "", Alert.AlertType.INFORMATION);
                return;
            }

            List<SubscriptionReportWrapper> reportData = new ArrayList<>();
            for (Subscription sub : subscriptions) {
                reportData.add(new SubscriptionReportWrapper(sub));
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);

            String reportStream = getPath("subscription_report.jrxml");
            if (reportStream == null) {
                throw new JRException("Report template not found: /images/subscription_report.jrxml");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            JasperViewer.viewReport(jasperPrint, false);

        } catch (JRException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα JasperReports", "Προέκυψε σφάλμα κατά τη δημιουργία της αναφοράς.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private static String getPath(String name) {
        String currentDir = System.getProperty("user.dir");
        String fullPath = currentDir + File.separator + "images" + File.separator + name;

        File imageFile = new File(fullPath);
        if (!imageFile.exists()) {
            System.out.println("❌ Δεν βρέθηκε η εικόνα: " + fullPath);
            CustomNotification.create()
                    .title("Σφάλμα")
                    .text("❌ Δεν βρέθηκε: " + fullPath)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showError();
            return null;
        }
        return fullPath;
    }

    public static class SubscriptionReportWrapper {
        private final Subscription original;
        private final boolean isExpired;

        public SubscriptionReportWrapper(Subscription original) {
            this.original = original;
            if (original.getEndDate() != null) {
                this.isExpired = original.getEndDate().isBefore(LocalDate.now());
            } else {
                this.isExpired = false;
            }
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

        public boolean getIsExpired() {
            return isExpired;
        }
    }
}
