package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewCustomerController {

    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfPostCode, tfEmail,tfManager, tfManagerPhone;
    @FXML
    private Button btnAfmSearch;
    @FXML
    Button btnAddressAdd;
    @FXML
    private ProgressIndicator progressIndicator;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    public void initialize() {
        btnAfmSearch.setOnAction(event -> handleAfmSearch());
        btnAddressAdd.setDisable(true);
        // Δημιουργία του βασικού ContextMenu χωρίς την επιλογή "Δοκιμή Email"
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);

        // Δημιουργία του ContextMenu για τα Τηλέφωνα με την επιλογή "Δοκιμή Email"
        ContextMenu phoneContextMenu = new ContextMenu();
        MenuItem phoneCopyItem = new MenuItem("Αντιγραφή");
        MenuItem phonePasteItem = new MenuItem("Επικόλληση");
        MenuItem phoneClearItem = new MenuItem("Εκκαθάριση");
        MenuItem viberItem = new MenuItem("Επικοινωνία Viber");
        phoneContextMenu.getItems().addAll(phoneCopyItem, phonePasteItem, phoneClearItem, viberItem);


        // Δημιουργία του ContextMenu για το tfEmail με την επιλογή "Δοκιμή Email"
        ContextMenu emailContextMenu = new ContextMenu();
        MenuItem emailCopyItem = new MenuItem("Αντιγραφή");
        MenuItem emailPasteItem = new MenuItem("Επικόλληση");
        MenuItem emailClearItem = new MenuItem("Εκκαθάριση");
        MenuItem mailItem = new MenuItem("Δοκιμή Email");
        emailContextMenu.getItems().addAll(emailCopyItem, emailPasteItem, emailClearItem, mailItem);

        // Προσθήκη του contextMenu στα υπόλοιπα TextFields
        setupTextFieldContextMenu(tfName, contextMenu);
        setupTextFieldContextMenu(tfTitle, contextMenu);
        setupTextFieldContextMenu(tfJob, contextMenu);
        setupTextFieldContextMenu(tfAfm, contextMenu);
        setupTextFieldContextMenu(tfPhone1, phoneContextMenu);
        setupTextFieldContextMenu(tfPhone2, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);
        setupTextFieldContextMenu(tfAddress, contextMenu);
        setupTextFieldContextMenu(tfTown, contextMenu);
        setupTextFieldContextMenu(tfPostCode,contextMenu);
        setupTextFieldContextMenu(tfManager, contextMenu);
        setupTextFieldContextMenu(tfManagerPhone, phoneContextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);

        // Ενέργειες για τα copy, paste, clear items στο βασικό contextMenu
        copyItem.setOnAction(e -> copyText());
        pasteItem.setOnAction(e -> pasteText());
        clearItem.setOnAction(e -> clearText());

        // Ενέργειες για τα phoneCopyItem, phonePasteItem, phoneClearItem στο phoneContextMenu
        phoneCopyItem.setOnAction(e -> copyText());
        phonePasteItem.setOnAction(e -> pasteText());
        phoneClearItem.setOnAction(e -> clearText());
        // Ενέργεια "Viber" μόνο για τα τηλέφωνα
        viberItem.setOnAction(e -> {
            if (currentTextField == tfPhone1 || currentTextField == tfPhone2 || currentTextField == tfMobile || currentTextField == tfManagerPhone) { // Εκτέλεση μόνο αν είναι στο tfEmail
                viberComunicate(currentTextField);
            }
        });
        // Ενέργειες για τα emailCopyItem, emailPasteItem, emailClearItem στο emailContextMenu
        emailCopyItem.setOnAction(e -> copyText());
        emailPasteItem.setOnAction(e -> pasteText());
        emailClearItem.setOnAction(e -> clearText());

        // Ενέργεια "Δοκιμή Email" μόνο για το tfEmail
        mailItem.setOnAction(e -> {
            if (currentTextField == tfEmail) { // Εκτέλεση μόνο αν είναι στο tfEmail
                sendTestEmail(tfEmail);
            }
        });
    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    // Μέθοδοι για τις ενέργειες
    private void copyText() {
        if (currentTextField != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentTextField.getText());  // Replace with the desired text
            clipboard.setContent(content);
        }
    }

    private void pasteText() {
        if (currentTextField != null) {
            currentTextField.paste();
        }
    }

    private void clearText() {
        if (currentTextField != null) {
            currentTextField.clear();
        }
    }

    // Μέθοδος αποστολής με viber
    private void viberComunicate(TextField phoneField) {
        String phone = phoneField.getText();
        System.out.println("Τηλέφωνο: " + phone);
        if (phone != null && !phone.isEmpty()) {
            try {
                File viberPath = new File(System.getenv("LOCALAPPDATA") + "\\Viber\\Viber.exe");
                Desktop.getDesktop().open(viberPath);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(phone);  // Replace with the desired text
                clipboard.setContent(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Προσοχή", "Παρακαλώ εισάγετε ένα έγκυρο τηλέφωνο.");
        }
    }

    // Μέθοδος αποστολής δοκιμαστικού email
    private void sendTestEmail(TextField emailField) {
        String email = emailField.getText();
        if (email != null && !email.isEmpty()) {
            // Εμφάνιση του progress indicator
            progressIndicator.setVisible(true);

            // Δημιουργία και αποστολή email σε ξεχωριστό thread για να μην κολλήσει το UI
            new Thread(() -> {
                try {
                    String subject = "Δοκιμή Email";
                    String body = "Δοκιμή email.";
                    EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
                    emailSender.sendEmail(email, subject, body);

                    // Ενημερώνουμε το UI όταν ολοκληρωθεί η αποστολή του email
                    Platform.runLater(() -> {
//                        showAlert("Επιτυχία", "Το email στάλθηκε με επιτυχία.");
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
//                        showAlert("Προσοχή", "Υπήρξε πρόβλημα με την αποστολή του email.");
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                    e.printStackTrace();
                }
            }).start(); // Ξεκινάμε το thread για την αποστολή του email
        } else {
            showAlert("Προσοχή", "Παρακαλώ εισάγετε ένα έγκυρο email.");
        }
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
        tfPostCode.setText(customer.getPostcode());
        tfEmail.setText(customer.getEmail());
        tfManager.setText(customer.getManager());
        tfManagerPhone.setText(customer.getManagerPhone());
        btnAddressAdd.setDisable(false);
        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = customer.getCode();
    }


    private void handleAfmSearch() {
        tfName.setText("");
        tfTitle.setText("");
        tfJob.setText("");
        tfAddress.setText("");
        tfTown.setText("");
        tfPostCode.setText("");
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
            tfPostCode.setText(companyInfo.getPostcode());
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
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String primaryAddress = tfAddress.getText();  // Πρώτη διεύθυνση
        String town = tfTown.getText();
        String postcode = tfPostCode.getText();
        String email = tfEmail.getText();
        String manager = tfManager.getText();
        String managerPhone = tfManagerPhone.getText();

        DBHelper dbHelper = new DBHelper();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        if (dbHelper.isAfmExists(afm)) {
            Platform.runLater(() -> showAlert("Προσοχή", "Ο πελάτης με ΑΦΜ " + afm + " υπάρχει ήδη."));
        } else {
            // Εισαγωγή του πελάτη στον κύριο πίνακα με την πρώτη διεύθυνση
            int customerId = dbHelper.insertCustomer(name, title, job, afm, phone1, phone2, mobile, primaryAddress, town, postcode, email, manager, managerPhone);
            // Εμφάνιση επιτυχίας
            Platform.runLater(() -> showAlert("Επιτυχία", "Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων."));
        }
    }

    void updateCustomer() {
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String posCode = tfPostCode.getText();
        String email = tfEmail.getText();
        String manager = tfManager.getText();
        String managerPhone = tfManagerPhone.getText();
        DBHelper dbHelper = new DBHelper();
        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, posCode, email, manager, managerPhone);
        showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
