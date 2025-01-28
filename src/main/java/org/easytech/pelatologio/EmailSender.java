package org.easytech.pelatologio;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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
            Platform.runLater(() -> showAlert("Attention", "Το email στάλθηκε επιτυχώς."));
            System.out.println("Το email στάλθηκε επιτυχώς!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Η αποστολή του email απέτυχε.");
            Platform.runLater(() -> showAlert("Attention", "Η αποστολή του email απέτυχε."));
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

