package org.easytech.pelatologio;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.util.HashMap;
import java.util.Map;

public class LabelPrintHelper {

    public static void printCustomerLabel(Customer customer) {
        try {
            // Φόρτωση του JasperReport
            JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/org/easytech/pelatologio/customer_receipt.jrxml");
            //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream("/org/easytech/pelatologio/customer_receipt.jasper"));

            // Δημιουργία dataset με δεδομένα πελάτη
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", customer.getName()== null ? "" : customer.getName());
            parameters.put("title", customer.getTitle() == null ? "" : customer.getTitle());
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
            e.printStackTrace();
        }
    }

    public static void printLoginLabel(Logins login, Customer customer, String title) {
        try {
            // Φόρτωση του JasperReport
            JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/org/easytech/pelatologio/login_receipt.jrxml");
            //JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream("/org/easytech/pelatologio/customer_receipt.jasper"));

            // Δημιουργία dataset με δεδομένα πελάτη
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("title", title);
            parameters.put("name", customer.getName() == null ? "" : customer.getName());
            parameters.put("username", login.getUsername() == null ? "" : login.getUsername());
            parameters.put("password", login.getPassword() == null ? "" : login.getPassword());
            parameters.put("phone", login.getPhone() == null ? "" : login.getPhone());

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
            e.printStackTrace();
        }
    }
}
