package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CustomerFolderManager;
import org.easytech.pelatologio.helper.EmailSender;
import org.easytech.pelatologio.models.Customer;

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
    private JFXButton sendButton;
    @FXML
    private ProgressBar progressBar;

    private Boolean copy = true;


    private Stage dialogStage;
    private Customer customer;

    private final List<File> attachments = new ArrayList<>();
    public boolean isSended = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCopy(Boolean copy) {
        this.copy = copy;
    }


    public void setEmail(String text) {
        emailField.setText(text);
    }
    public void setSubject(String text) {
        subjectField.setText(text);
    }
    public void setBody(String text) {
        bodyArea.setText(text);
    }
    public void setAttachments(List<File> attachments) {
        this.attachments.addAll(attachments);
        attachmentList.getItems().addAll(attachments);
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

        // Δημιουργία του context menu για διαγραφή
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Διαγραφή");
        contextMenu.getItems().add(deleteItem);

        // Λειτουργία διαγραφής από το context menu
        deleteItem.setOnAction(event -> {
            File selectedFile = attachmentList.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                attachments.remove(selectedFile);
                attachmentList.getItems().remove(selectedFile);
                if (selectedFile.exists()) {
                    selectedFile.delete(); // Διαγραφή του αρχείου
                }
            }
        });

        // Εφαρμογή του context menu στην λίστα
        attachmentList.setContextMenu(contextMenu);

        // Κουμπί αποστολής
        sendButton.setOnAction(event -> sendEmail());
        progressBar.setVisible(false); // Αρχικά κρυφό
    }

    private void sendEmail() {
        progressBar.setVisible(true); // Εμφάνιση προόδου
        String email = emailField.getText().trim();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        if (email == null || email.isEmpty()) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Σφάλμα")
                        .text("Το πεδίο παραλήπτη δεν μπορεί να είναι κενό.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
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
                        Notifications notifications = Notifications.create()
                                .title("Επιτυχία")
                                .text("Το email στάλθηκε με επιτυχία.")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showConfirm();
                    subjectField.setText("");
                    bodyArea.setText("");
                    attachmentList.getItems().clear();
                    isSended = true;
                    progressBar.setVisible(false); // Απόκρυψη προόδου
                });

                // Αποθήκευση του email (εκτός UI thread αλλά με τελική ενημέρωση στο UI)
                if (copy)
                    saveEmailCopy(email, subject, body, attachments);

            } catch (Exception e) {
                // Ενημέρωση του UI για αποτυχία (στο JavaFX thread)
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Σφάλμα")
                            .text("Υπήρξε πρόβλημα κατά την αποστολή του email.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
                    progressBar.setVisible(false); // Απόκρυψη προόδου
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποστολή email.", e.getMessage(), Alert.AlertType.ERROR));
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση του email.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
