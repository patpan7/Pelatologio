package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddNewCustomerController {

    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfEmail,tfManager, tfManagerPhone;
    @FXML
    private Button btnAfmSearch;
    int code = 0;

    public void initialize() {
        // Εδώ μπορείς να αρχικοποιήσεις στοιχεία του pane αν χρειάζεται
        btnAfmSearch.setOnAction(event -> handleAfmSearch());
    }

    public void setCustomerData(Customer customer) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(customer.getName());
        tfTitle.setText(customer.getTitle());
        tfJob.setText(customer.getJob());
        tfAfm.setText(customer.getAfm());
        tfPhone1.setText(customer.getPhone1());
        tfPhone2.setText(customer.getPhone2());
        tfMobile.setText(customer.getMobile());
        tfAddress.setText(customer.getAddress());
        tfTown.setText(customer.getTown());
        tfEmail.setText(customer.getEmail());
        tfManager.setText(customer.getManager());
        tfManagerPhone.setText(customer.getManagerPhone());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = customer.getCode();
    }


    private void handleAfmSearch() {
        tfName.setText("");
        tfTitle.setText("");
        tfJob.setText("");
        tfPhone1.setText("");
        tfPhone2.setText("");
        tfMobile.setText("");
        tfAddress.setText("");
        tfTown.setText("");
        tfEmail.setText("");
        String afm = tfAfm.getText();
        if (afm == null || afm.isEmpty()) {
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ εισάγετε ένα έγκυρο ΑΦΜ."));
            return;
        }

        AfmLookupService service = new AfmLookupService();
        String responseXml = service.callAadeService(afm);

        // Έλεγχος για μήνυμα σφάλματος
        String errorDescr = AfmResponseParser.getXPathValue(responseXml, "//error_rec/error_descr");
        if (errorDescr != null && !errorDescr.isEmpty()) {
            Platform.runLater(() -> showAlert("Προσοχή", errorDescr));
            return;
        }

        Customer companyInfo = AfmResponseParser.parseResponse(responseXml);

        if (companyInfo != null) {
            tfName.setText(companyInfo.getName());
            tfTitle.setText(companyInfo.getTitle());
            tfJob.setText(companyInfo.getJob());
            tfAddress.setText(companyInfo.getAddress());
            tfTown.setText(companyInfo.getTown());
        } else {
            Platform.runLater(() -> showAlert("Προσοχή", "Σφάλμα κατά την ανάγνωση των δεδομένων."));
        }
    }


    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addCustomer();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateCustomer();
        }
    }

    void addCustomer() {
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText();
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String email = tfEmail.getText();
        String manager = tfManager.getText();
        String managerPhone = tfManagerPhone.getText();
        DBHelper dbHelper = new DBHelper();
        if (dbHelper.isAfmExists(afm)) {
            Platform.runLater(() -> showAlert("Προσοχή", "Ο πελάτης με ΑΦΜ " + afm + " υπάρχει ήδη."));

        } else {
            dbHelper.insertCustomer(name, title, job, afm, phone1, phone2, mobile, address, town, email, manager, managerPhone);
            Platform.runLater(() -> showAlert("Επιτυχία", "Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων."));
        }
    }

    void updateCustomer() {
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText();
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String email = tfEmail.getText();
        String manager = tfManager.getText();
        String managerPhone = tfManagerPhone.getText();
        DBHelper dbHelper = new DBHelper();
        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, email, manager, managerPhone);
        showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
