package org.easytech.pelatologio.helper;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.easytech.pelatologio.EmailDialogController;

import java.io.IOException;

public class EmailHelper {

    public static class EmailDialogOptions {
        public String recipientEmail;
        public String subject;
        public String body;
        public boolean showCopyOption;
        public boolean saveCopy;
        public java.util.List<java.io.File> attachments;
        public org.easytech.pelatologio.models.Customer customer;
        public Runnable onSuccess;

        public EmailDialogOptions(String recipientEmail) {
            this.recipientEmail = recipientEmail;
        }

        public EmailDialogOptions subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailDialogOptions body(String body) {
            this.body = body;
            return this;
        }

        public EmailDialogOptions showCopyOption(boolean showCopyOption) {
            this.showCopyOption = showCopyOption;
            return this;
        }

        public EmailDialogOptions saveCopy(boolean saveCopy) {
            this.saveCopy = saveCopy;
            return this;
        }

        public EmailDialogOptions attachments(java.util.List<java.io.File> attachments) {
            this.attachments = attachments;
            return this;
        }

        public EmailDialogOptions customer(org.easytech.pelatologio.models.Customer customer) {
            this.customer = customer;
            return this;
        }

        public EmailDialogOptions onSuccess(Runnable onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }
    }

    public static void showEmailDialog(EmailDialogOptions options) {
        try {
            FXMLLoader loader = new FXMLLoader(EmailHelper.class.getResource("/org/easytech/pelatologio/emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");

            EmailDialogController controller = loader.getController();
            controller.setEmail(options.recipientEmail);
            controller.setSubject(options.subject);
            controller.setBody(options.body);
            controller.setSaveCopy(options.saveCopy);
            controller.setAttachments(options.attachments);
            controller.setCustomer(options.customer);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setOnCloseRequest(evt -> {
                if (dialog.getResult() == ButtonType.OK && controller.isSended && options.onSuccess != null) {
                    options.onSuccess.run();
                }
            });

            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showErrorDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα του παραθύρου email."+ e.getMessage());
        }
    }
}
