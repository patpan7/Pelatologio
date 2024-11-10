package org.easytech.pelatologio;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

