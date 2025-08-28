package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.AppSettings;
import org.easytech.pelatologio.helper.CustomerFolderManager;
import org.easytech.pelatologio.helper.EmailSender;
import org.easytech.pelatologio.models.Customer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProposalDialogController {

    @FXML private TextField commissionField;
    @FXML private TextField posPriceField;
    @FXML private TextField monthlyFeeField;
    @FXML private ComboBox<String> integrationTypeComboBox;
    @FXML private Label erpLabel;
    @FXML private TextField erpNameField;
    @FXML private Label attachmentLabel;

    private Customer customer;
    private File selectedAttachment;
    private List<String> recipients; // New field for recipients

    @FXML
    public void initialize() {
        integrationTypeComboBox.getItems().addAll("Ταμειακή", "ERP");
        erpLabel.visibleProperty().bind(integrationTypeComboBox.valueProperty().isEqualTo("ERP"));
        erpNameField.visibleProperty().bind(integrationTypeComboBox.valueProperty().isEqualTo("ERP"));
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    @FXML
    private void handleChooseAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Επιλογή Υπογεγραμμένου Α1");

        // Set initial directory to customer's EDPS subfolder
        if (customer != null) {
            CustomerFolderManager folderManager = new CustomerFolderManager();
            File customerFolder = folderManager.customerFolder(customer.getName(), customer.getAfm());
            if (customerFolder != null && customerFolder.exists()) {
                File edpsFolder = new File(customerFolder, "EDPS");
                if (edpsFolder.exists() && edpsFolder.isDirectory()) {
                    fileChooser.setInitialDirectory(edpsFolder);
                }
            }
        }

        selectedAttachment = fileChooser.showOpenDialog(attachmentLabel.getScene().getWindow());
        if (selectedAttachment != null) {
            attachmentLabel.setText(selectedAttachment.getName());
        }
    }

    @FXML
    private void handleSendProposal() {
        // 1. Validation
        if (commissionField.getText().isEmpty() || posPriceField.getText().isEmpty() || monthlyFeeField.getText().isEmpty() || integrationTypeComboBox.getValue() == null) {
            AlertDialogHelper.showErrorDialog("Validation Error", "Παρακαλώ συμπληρώστε όλα τα πεδία.");
            return;
        }
        if (selectedAttachment == null) {
            AlertDialogHelper.showErrorDialog("Validation Error", "Παρακαλώ επισυνάψτε το υπογεγραμμένο έντυπο Α1.");
            return;
        }

        // 2. Construct email body
        StringBuilder body = new StringBuilder();
        body.append("Αγαπητή EDPS,<br><br>");
        body.append("Παρακαλώ βρείτε παρακάτω τα στοιχεία της πρότασης για τον πελάτη: ").append(customer.getName()).append(" (ΑΦΜ: ").append(customer.getAfm()).append(")<br><br>");
        body.append("<b>Προμήθεια:</b> ").append(commissionField.getText()).append("%<br>");
        body.append("<b>Τιμή POS:</b> ").append(posPriceField.getText()).append("€<br>");
        body.append("<b>Μηνιαία Συνδρομή:</b> ").append(monthlyFeeField.getText()).append("€<br>");
        body.append("<b>Τύπος Σύνδεσης:</b> ").append(integrationTypeComboBox.getValue()).append("<br>");
        if (integrationTypeComboBox.getValue().equals("ERP")) {
            body.append("<b>Όνομα ERP:</b> ").append(erpNameField.getText()).append("<br>");
        }
        body.append("<br>Συνημμένα θα βρείτε το υπογεγραμμένο έντυπο Α1.<br><br>");
        body.append("Με εκτίμηση.");

        // Save proposal text to a file
        try {
            CustomerFolderManager folderManager = new CustomerFolderManager();
            File customerFolder = folderManager.customerFolder(customer.getName(), customer.getAfm());
            File edpsFolder = new File(customerFolder, "EDPS");
            if (!edpsFolder.exists()) edpsFolder.mkdirs();
            String fileName = "Proposal_" + LocalDate.now().toString() + ".txt";
            // Replace <br> with newline for the text file
            String textContent = body.toString().replaceAll("<br>", "\n").replaceAll("<b>", "").replaceAll("</b>", "");
            Files.writeString(Paths.get(edpsFolder.getAbsolutePath(), fileName), textContent);
        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showErrorDialog("File Error", "Could not save the proposal text file.");
        }

        // 3. Prepare for sending
        String recipientsString = (this.recipients != null && !this.recipients.isEmpty()) 
                                ? String.join(",", this.recipients) 
                                : "";
        if (recipientsString.isEmpty()) {
            AlertDialogHelper.showErrorDialog("Configuration Error", "Δεν έχουν οριστεί παραλήπτες για αυτή την ενέργεια.");
            return;
        }
        
        String subject = "Νέα Πρόταση Σύμβασης - " + customer.getName();
        List<File> attachments = new ArrayList<>();
        attachments.add(selectedAttachment);

        // 4. Send email in a background thread
        new Thread(() -> {
            try {
                EmailSender emailSender = new EmailSender(
                    AppSettings.loadSetting("smtp"),
                    AppSettings.loadSetting("smtpport"),
                    AppSettings.loadSetting("email"),
                    AppSettings.loadSetting("emailPass")
                );
                emailSender.sendEmailWithAttachments(recipientsString, subject, body.toString(), attachments);

                Platform.runLater(() -> {
                    AlertDialogHelper.showInfoDialog("Επιτυχία", "Η πρόταση στάλθηκε με επιτυχία.");
                    handleCancel(); // Close the dialog on success
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> AlertDialogHelper.showErrorDialog("Email Error", "Η αποστολή της πρότασης απέτυχε: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        ((Stage) attachmentLabel.getScene().getWindow()).close();
    }
}
