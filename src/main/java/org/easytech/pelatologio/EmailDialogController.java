package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class EmailDialogController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea bodyArea;

    @FXML
    private ListView<File> attachmentList;

    @FXML
    private Button attachButton;

    @FXML
    private Button sendButton;
    @FXML
    private ProgressBar progressBar;


    private Stage dialogStage;
    private Customer customer;

    private final List<File> attachments = new ArrayList<>();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        emailField.setText(customer.getEmail());

    }

    @FXML
    private void initialize() {
        // Κουμπί για προσθήκη συνημμένων
        attachButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Επιλογή αρχείων");
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dialogStage);
            if (selectedFiles != null) {
                attachments.addAll(selectedFiles);
                attachmentList.getItems().addAll(selectedFiles);
            }
        });

        // Κουμπί αποστολής
        sendButton.setOnAction(event -> sendEmail());
        progressBar.setVisible(false); // Αρχικά κρυφό
    }

    private void sendEmail() {
        progressBar.setVisible(true); // Εμφάνιση προόδου
        String email = emailField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        if (email == null || email.isEmpty()) {
            showAlert("Σφάλμα", "Το πεδίο παραλήπτη δεν μπορεί να είναι κενό!");
            progressBar.setVisible(false); // Απόκρυψη προόδου σε περίπτωση σφάλματος
            return;
        }

        // Εκτέλεση αποστολής σε νέο νήμα
        new Thread(() -> {
            try {
                EmailSender emailSender = new EmailSender(
                        AppSettings.loadSetting("smtp"),
                        AppSettings.loadSetting("smtpport"),
                        AppSettings.loadSetting("email"),
                        AppSettings.loadSetting("emailPass")
                );

                // Αποστολή email
                emailSender.sendEmailWithAttachments(email, subject, body, attachments);

                // Ενημέρωση του UI με επιτυχία (πρέπει να γίνει στο JavaFX thread)
                Platform.runLater(() -> {
                    showAlert("Επιτυχία", "Το email στάλθηκε με επιτυχία!");
                    subjectField.setText("");
                    bodyArea.setText("");
                    attachmentList.getItems().clear();
                    progressBar.setVisible(false); // Απόκρυψη προόδου
                });

                // Αποθήκευση του email (εκτός UI thread αλλά με τελική ενημέρωση στο UI)
                saveEmailCopy(email, subject, body, attachments);

            } catch (Exception e) {
                // Ενημέρωση του UI για αποτυχία (στο JavaFX thread)
                Platform.runLater(() -> {
                    showAlert("Σφάλμα", "Υπήρξε πρόβλημα κατά την αποστολή του email.");
                    progressBar.setVisible(false); // Απόκρυψη προόδου
                });
                e.printStackTrace();
            }
        }).start();
    }


    private void saveEmailCopy(String recipient, String subject, String body, List<File> attachments) {
        if (customer == null) {
            System.err.println("Customer is null. Cannot save email copy.");
            return;
        }

        try {
            // Δημιουργία ή άνοιγμα του φακέλου του πελάτη
            CustomerFolderManager folderManager = new CustomerFolderManager();
            File customerFolder = folderManager.customerFolder(customer.getName(), customer.getAfm());
            if (customerFolder == null) {
                System.err.println("Αποτυχία πρόσβασης στον φάκελο του πελάτη.");
                return;
            }

            // Δημιουργία υποφακέλου "Emails" αν δεν υπάρχει
            File emailsFolder = new File(customerFolder, "Emails");
            if (!emailsFolder.exists() && !emailsFolder.mkdirs()) {
                System.err.println("Αποτυχία δημιουργίας του φακέλου 'Emails'.");
                return;
            }

            // Δημιουργία αρχείου για το email
            String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
            File emailFile = new File(emailsFolder, "Email_" + timestamp + ".txt");

            try (PrintWriter writer = new PrintWriter(emailFile)) {
                writer.println("Παραλήπτης: " + recipient);
                writer.println("Θέμα: " + subject);
                writer.println("Ημερομηνία: " + java.time.LocalDateTime.now());
                writer.println("Κείμενο:");
                writer.println(body);
                writer.println();

                if (!attachments.isEmpty()) {
                    writer.println("Συνημμένα:");
                    for (File attachment : attachments) {
                        writer.println(" - " + attachment.getName());
                    }

                    // Αντιγραφή συνημμένων στον φάκελο του πελάτη
                    File attachmentsFolder = new File(emailsFolder, "Attachments_" + timestamp);
                    if (attachmentsFolder.mkdirs()) {
                        for (File attachment : attachments) {
                            File destFile = new File(attachmentsFolder, attachment.getName());
                            java.nio.file.Files.copy(
                                    attachment.toPath(),
                                    destFile.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );
                        }
                    } else {
                        System.err.println("Αποτυχία δημιουργίας του φακέλου για τα συνημμένα.");
                    }
                }
            }

            System.out.println("Το email αποθηκεύτηκε στον φάκελο: " + emailFile.getAbsolutePath());
            progressBar.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Σφάλμα κατά την αποθήκευση του email.");
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
