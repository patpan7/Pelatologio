package org.easytech.pelatologio;

public class test {
    public static void main(String[] args) {
        // Ρυθμίσεις για SMTP διακομιστή (π.χ., Gmail)
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "patpan7@gmail.com";
        String password = "P@n0$.-7"; // Χρησιμοποίησε κωδικό εφαρμογής για Gmail

        EmailSender emailSender = new EmailSender(host, port, username, password);

        // Αποστολή του email
        String recipient = "recipient_email@example.com";
        String subject = "Test Email";
        String messageContent = "This is a test email sent from Java.";

        emailSender.sendEmail(recipient, subject, messageContent);
    }
}