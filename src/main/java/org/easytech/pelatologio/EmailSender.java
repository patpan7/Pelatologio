package org.easytech.pelatologio;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    private final String host;
    private final String port;
    private final String username;
    private final String password;

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
            // Δημιουργία του email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.setSubject(subject);
            message.setText(messageContent);

            // Αποστολή του email
            Transport.send(message);
            //Platform.runLater(() -> showAlert("Attention", "Το email στάλθηκε επιτυχώς."));
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Attention")
                        .text("Το email στάλθηκε επιτυχώς.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();});
            System.out.println("Το email στάλθηκε επιτυχώς!");
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

        }
    }

    public void sendEmailWithAttachments(String recipientEmail, String subject, String body, List<File> attachments) throws Exception {
        // Ρυθμίσεις για το SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Για ασφαλή σύνδεση

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Δημιουργία μηνύματος
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Δημιουργία του περιεχομένου με συνημμένα
        Multipart multipart = new MimeMultipart();

        // Κείμενο μηνύματος
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);
        multipart.addBodyPart(textPart);

        // Προσθήκη συνημμένων
        if (attachments != null) {
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
            }
        }

        // Ορισμός του περιεχομένου στο μήνυμα
        message.setContent(multipart);

        // Αποστολή του μηνύματος
        Transport.send(message);
    }
}

