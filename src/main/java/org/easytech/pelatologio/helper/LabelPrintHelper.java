package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LabelPrintHelper {

    public static void printCustomerLabel(Customer customer) {
        try {
            // Φόρτωση του JasperReport
            String fullPath = getPath("customer_receipt.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(fullPath);
            //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream("/org/easytech/pelatologio/customer_receipt.jasper"));

            // Δημιουργία dataset με δεδομένα πελάτη
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", customer.getName()== null ? "" : customer.getName());
            //parameters.put("title", customer.getTitle() == null ? "" : customer.getTitle());
            parameters.put("job", customer.getJob() == null ? "" : customer.getJob());
            parameters.put("address", customer.getAddress() == null ? "" : customer.getAddress());
            parameters.put("town", customer.getTown() == null ? "" : customer.getTown());
            parameters.put("postcode", "ΤΚ: " + (customer.getPostcode() == null ? "" : customer.getPostcode()));
            parameters.put("afm", "ΑΦΜ: " + (customer.getAfm() == null ? "" : customer.getAfm()));
            parameters.put("phone", "Τηλ: " + (customer.getPhone1() == null ? "" : customer.getPhone1()));
            parameters.put("mobile", "Κιν: " + (customer.getMobile() == null ? "" : customer.getMobile()));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            //JasperViewer.viewReport(jasperPrint, false);

            // Επιλογή εκτυπωτή από τα Windows
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            if (printServices.length == 0) {
                System.out.println("Δεν βρέθηκαν εκτυπωτές.");
                return;
            }

            PrintService selectedPrinter = printServices[0]; // Προεπιλογή πρώτου εκτυπωτή
            for (PrintService printer : printServices) {
                if (printer.getName().contains("Zebra")) { // Αντικατάστησε το με το όνομα του εκτυπωτή που θέλεις
                    selectedPrinter = printer;
                    break;
                }
            }

            //PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            //printRequestAttributeSet.add(new PrinterName(selectedPrinter.getName(), null));
            PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
            printServiceAttributeSet.add(new PrinterName(selectedPrinter.getName(), null));

            JasperPrintManager.printReport(jasperPrint, true); // Εκτύπωση χωρίς προεπισκόπηση

        } catch (JRException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εκτύπωση.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public static void printLoginLabel(Logins login, Customer customer, String title) {
        try {
            // Φόρτωση του JasperReport
            String fullPath = getPath("login_receipt.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(fullPath);

            // Δημιουργία dataset με δεδομένα πελάτη
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("title", title);
            parameters.put("name", customer.getName() == null ? "" : customer.getName());
            parameters.put("username", login.getUsername() == null ? "" : login.getUsername());
            parameters.put("password", login.getPassword() == null ? "" : login.getPassword());
            parameters.put("phone", login.getPhone() == null ? "" : login.getPhone());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // Επιλογή εκτυπωτή από τα Windows
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            if (printServices.length == 0) {
                System.out.println("Δεν βρέθηκαν εκτυπωτές.");
                return;
            }

            PrintService selectedPrinter = printServices[0]; // Προεπιλογή πρώτου εκτυπωτή
            for (PrintService printer : printServices) {
                if (printer.getName().contains("Zebra")) { // Αντικατάστησε το με το όνομα του εκτυπωτή που θέλεις
                    selectedPrinter = printer;
                    break;
                }
            }

            PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
            printServiceAttributeSet.add(new PrinterName(selectedPrinter.getName(), null));

            JasperPrintManager.printReport(jasperPrint, true); // Εκτύπωση χωρίς προεπισκόπηση

        } catch (JRException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εκτύπωση.", e.getMessage(), Alert.AlertType.ERROR));

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
}
