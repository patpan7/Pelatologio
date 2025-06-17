package org.easytech.pelatologio.helper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.AppSettings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private String signature;

    public EmailSender(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void sendEmail(String toAddress, String subject, String messageContent) {
        // Ρυθμίσεις για τον SMTP διακομιστή
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Για ασφαλή σύνδεση
        signature = AppSettings.loadSetting("signature");

        // Αυθεντικοποίηση του λογαριασμού
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // Δημιουργία συνεδρίας με βάση τις ρυθμίσεις
        Session session = Session.getInstance(properties, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);

            // 1. Δημιουργία του HTML μέρους
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String htmlContent = "<html><body>" + messageContent + "<br><br>" + signature + "</body></html>";
            messageBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");

            // 2. Επισύναψη εικόνας
            String currentDir = System.getProperty("user.dir");
            String fullPath = currentDir + File.separator + "images" + File.separator + "logo.png";
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.attachFile(fullPath);
            imagePart.setContentID("<logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);

            // 3. Σύνθεση του email
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Attention")
                        .text("Η αποστολή του email απέτυχε.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποστολή email.", e.getMessage(), Alert.AlertType.ERROR));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmailWithAttachments(String recipientEmail, String subject, String body, List<File> attachments) throws Exception {
        // Ρυθμίσεις για το SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Για ασφαλή σύνδεση
        signature = AppSettings.loadSetting("signature");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Δημιουργία μηνύματος
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // 1. Δημιουργία του HTML μέρους
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        String htmlContent = "<html><body>" + body + "<br><br>" + signature + "</body></html>";
        messageBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");

        // 2. Επισύναψη εικόνας
        String currentDir = System.getProperty("user.dir");
        String fullPath = currentDir + File.separator + "images" + File.separator + "logo.png";
        MimeBodyPart imagePart = new MimeBodyPart();
        imagePart.attachFile(fullPath);
        imagePart.setContentID("<logo>");
        imagePart.setDisposition(MimeBodyPart.INLINE);

        // 3. Σύνθεση του email
        //Multipart multipart = new MimeMultipart();
        Multipart multipart = new MimeMultipart("mixed"); // mixed -> Για attachments

        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(imagePart);

        // 4. Προσθήκη όλων των συνημμένων αρχείων
        if (attachments != null && !attachments.isEmpty()) {
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);

                // Ορισμός του σωστού τύπου MIME ανάλογα με την κατάληξη του αρχείου
                if (file.getName().endsWith(".txt")) {
                    attachmentPart.setHeader("Content-Type", "text/plain; charset=UTF-8");
                } else if (file.getName().endsWith(".pdf")) {
                    attachmentPart.setHeader("Content-Type", "application/pdf");
                } else if (file.getName().endsWith(".docx")) {
                    attachmentPart.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                } else if (file.getName().endsWith(".doc")) {
                    attachmentPart.setHeader("Content-Type", "application/msword");
                } else if (file.getName().endsWith(".xlsx")) {
                    attachmentPart.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                } else if (file.getName().endsWith(".xls")) {
                    attachmentPart.setHeader("Content-Type", "application/vnd.ms-excel");
                } else if (file.getName().endsWith(".pptx")) {
                    attachmentPart.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
                } else if (file.getName().endsWith(".ppt")) {
                    attachmentPart.setHeader("Content-Type", "application/vnd.ms-powerpoint");
                }

                attachmentPart.setDisposition(MimeBodyPart.ATTACHMENT); // <-- Εξασφαλίζει ότι είναι attachment
                multipart.addBodyPart(attachmentPart);
            }
        }

        // Ορισμός του περιεχομένου στο μήνυμα
        message.setContent(multipart);

        // Αποστολή του μηνύματος
        Transport.send(message);
    }
}

