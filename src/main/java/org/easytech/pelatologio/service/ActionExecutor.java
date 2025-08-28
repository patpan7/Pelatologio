package org.easytech.pelatologio.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.FileChooser;
import org.easytech.pelatologio.ProposalDialogController;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.ProjectStepProgress;
import org.easytech.pelatologio.models.actions.EmailActionConfig;
import org.easytech.pelatologio.models.actions.ProposalActionConfig;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActionExecutor {

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public void execute(ProjectStepProgress progress, Customer customer) {
        if (progress.getActionType() == null || progress.getActionType().isEmpty()) {
            return; // No action to execute
        }

        switch (progress.getActionType()) {
            case "SEND_EMAIL_CUSTOMER_TEMPLATE":
                sendEmailCustomerTemplate(progress, customer);
                break;

            case "FILL_AND_SEND_A1":
                fillAndSendA1(progress, customer);
                break;

            case "OPEN_PROPOSAL_DIALOG":
                openProposalDialog(progress, customer);
                break;

            case "SEND_ATTACHMENT_EMAIL":
                sendAttachmentEmail(progress, customer);
                break;

            default:
                System.err.println("Unknown action type: " + progress.getActionType());
        }
    }

    private void sendEmailCustomerTemplate(ProjectStepProgress progress, Customer customer) {
        // This is a generic template sender, can be implemented fully later if needed
        AlertDialogHelper.showInfoDialog("Info", "Action type 'SEND_EMAIL_CUSTOMER_TEMPLATE' is not fully implemented yet.");
    }

    private void fillAndSendA1(ProjectStepProgress progress, Customer customer) {
        try {
            String jsonConfig = progress.getActionConfigJson();
            if (jsonConfig == null || jsonConfig.isEmpty()) {
                AlertDialogHelper.showErrorDialog("Configuration Error", "No JSON configuration found for this A1 action.");
                return;
            }
            EmailActionConfig config = gson.fromJson(jsonConfig, EmailActionConfig.class);

            if (config.getAttachments() == null || config.getAttachments().isEmpty()) {
                AlertDialogHelper.showErrorDialog("Configuration Error", "The JSON config for this step is missing the 'attachments' list with the template PDF name.");
                return;
            }

            // 1. Fetch DOY from AADE
            AfmLookupService service = new AfmLookupService();
            String responseXml = service.callAadeService(customer.getAfm());
            String doy = AfmResponseParser.getDoyFromResponse(responseXml);
            if (doy == null || doy.isEmpty()) {
                throw new IOException("Could not retrieve DOY from AADE for AFM: " + customer.getAfm());
            }

            // 2. Get paths
            String templatePath = AppSettings.loadSetting("datafolder") + "/Templates/" +
                    config.getAttachments().get(0);
            CustomerFolderManager folderManager = new CustomerFolderManager();
            File customerFolder = folderManager.customerFolder(customer.getName(), customer.getAfm());
            if (customerFolder == null) {
                throw new IOException("Could not create or find customer folder.");
            }

            // 3. Create the "EDPS" subfolder
            File edpsFolder = new File(customerFolder, "EDPS");
            if (!edpsFolder.exists()) {
                if (!edpsFolder.mkdirs()) {
                    throw new IOException("Could not create EDPS subfolder.");
                }
            }

            String outputPdfPath = edpsFolder.getAbsolutePath() + "/A1_EDPS_" + customer.getAfm() + ".pdf";

            // 4. Fill the PDF
            PdfFormFiller filler = new PdfFormFiller();
            File filledPdf = filler.fillA1Form(customer, doy, templatePath, outputPdfPath);

            // 5. Send the email with the filled PDF as an attachment
            String recipient = customer.getEmail();
            String subject = config.getSubject();
            String body = "";
            if (config.getBodyTemplate() != null) {
                body = config.getBodyTemplate().replace("{customerName}", customer.getName());
            }

            List<File> attachments = new ArrayList<>();
            attachments.add(filledPdf);

            // 6. Send email in a background thread
            final String finalBody = body;
            new Thread(() -> {
                try {
                    EmailSender emailSender = new EmailSender(
                            AppSettings.loadSetting("smtp"),
                            AppSettings.loadSetting("smtpport"),
                            AppSettings.loadSetting("email"),
                            AppSettings.loadSetting("emailPass")
                    );
                    emailSender.sendEmailWithAttachments(recipient, subject, finalBody, attachments);

                    Platform.runLater(() -> {
                        AlertDialogHelper.showInfoDialog("Επιτυχία", "Το email στάλθηκε με επιτυχία.");
                        // Mark step as complete only after successful sending
                        progress.setCompleted(true);
                        progress.setCompletionDate(LocalDate.now());
                        DBHelper.getCustomerProjectTaskDao().updateProgress(progress);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> AlertDialogHelper.showErrorDialog("Email Error", "Η αποστολή του email απέτυχε: " + e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogHelper.showErrorDialog("A1 Form Error", "Could not create or send the A1 form: " +
                    e.getMessage());
        }
    }

    private void openProposalDialog(ProjectStepProgress progress, Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/easytech/pelatologio/proposalDialog.fxml"));
            DialogPane pane = loader.load();

            ProposalDialogController controller = loader.getController();
            controller.setCustomer(customer);

            String jsonConfig = progress.getActionConfigJson();
            if (jsonConfig != null && !jsonConfig.isEmpty()) {
                ProposalActionConfig config = gson.fromJson(jsonConfig, ProposalActionConfig.class);
                controller.setRecipients(config.getRecipients());
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Νέα Πρόταση Σύμβασης");

            pane.getButtonTypes().clear();

            dialog.showAndWait();

            // Assuming the dialog's controller handles the sending and the user wants to mark as complete upon closing.
            progress.setCompleted(true);
            progress.setCompletionDate(LocalDate.now());
            DBHelper.getCustomerProjectTaskDao().updateProgress(progress);

        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showErrorDialog("Dialog Error", "Could not open the proposal dialog: " +
                    e.getMessage());
        }
    }

    private void sendAttachmentEmail(ProjectStepProgress progress, Customer customer) {
        String jsonConfig = progress.getActionConfigJson();
        if (jsonConfig == null || jsonConfig.isEmpty()) {
            AlertDialogHelper.showErrorDialog("Configuration Error", "No JSON configuration found for this email action.");
            return;
        }

        EmailActionConfig config = gson.fromJson(jsonConfig, EmailActionConfig.class);

        // 1. Open FileChooser in the correct customer/edps folder
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(config.getAttachmentDialogTitle() != null ?
                config.getAttachmentDialogTitle() : "Επιλογή Αρχείου");

        CustomerFolderManager folderManager = new CustomerFolderManager();
        File customerFolder = folderManager.customerFolder(customer.getName(),
                customer.getAfm());
        if (customerFolder != null && customerFolder.exists()) {
            File edpsFolder = new File(customerFolder, "EDPS");
            if (edpsFolder.exists() && edpsFolder.isDirectory()) {
                fileChooser.setInitialDirectory(edpsFolder);
            }
        }

        File selectedAttachment = fileChooser.showOpenDialog(null);

        if (selectedAttachment == null) {
            AlertDialogHelper.showInfoDialog("Info", "Η αποστολή ακυρώθηκε επειδή δεν επιλέχθηκε αρχείο.");
            return;
        }

        // 2. Determine recipient
        String recipient = "";
        System.out.println("Configured recipient type: " + config.getRecipientType());
        if ("customer".equalsIgnoreCase(config.getRecipientType())) {
            recipient = customer.getEmail();
            System.out.println("Using customer email: " + recipient);
        } else if ("custom".equalsIgnoreCase(config.getRecipientType())) { // Handle custom recipient
            recipient = config.getCustomRecipient();
            System.out.println("Using custom recipient: " + recipient);
        }

        if (recipient == null || recipient.isEmpty()) {
            AlertDialogHelper.showErrorDialog("Missing Data", "Δεν βρέθηκε το email του παραλήπτη. Ελέγξτε τη διαμόρφωση JSON (recipient_type, custom_recipient).");
            return;
        }

        // 3. Construct and send email
        String subject = config.getSubject() != null ? config.getSubject().replace(
                "{customerName}", customer.getName()) : "";
        String body = "";
        if (config.getBodyTemplate() != null) {
            body = config.getBodyTemplate()
                    .replace("{customerName}", customer.getName())
                    .replace("{customerAfm}", customer.getAfm());
        }

        List<File> attachments = new ArrayList<>();
        attachments.add(selectedAttachment);

        final String finalRecipient = recipient;
        final String finalBody = body;
        new Thread(() -> {
            try {
                EmailSender emailSender = new EmailSender(
                        AppSettings.loadSetting("smtp"),
                        AppSettings.loadSetting("smtpport"),
                        AppSettings.loadSetting("email"),
                        AppSettings.loadSetting("emailPass")
                );
                emailSender.sendEmailWithAttachments(finalRecipient, subject, finalBody,
                        attachments);

                Platform.runLater(() -> {
                    AlertDialogHelper.showInfoDialog("Επιτυχία", "Το email στάλθηκε με επιτυχία.");
                    progress.setCompleted(true);
                    progress.setCompletionDate(LocalDate.now());
                    DBHelper.getCustomerProjectTaskDao().updateProgress(progress);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> AlertDialogHelper.showErrorDialog("Email Error", "Η αποστολή του email απέτυχε: " + e.getMessage()));
            }
        }).start();
    }
}