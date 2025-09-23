package org.easytech.pelatologio.helper;

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
    private String signature;

    public EmailSender(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void sendEmail(String toAddress, String subject, String messageContent) {
        try {
            sendEmailWithAttachments(toAddress, subject, messageContent, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        // Ρυθμίσεις για τον SMTP διακομιστή
//        Properties properties = new Properties();
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port", port);
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true"); // Για ασφαλή σύνδεση
//        signature = AppSettings.loadSetting("signature");
//
//        // Αυθεντικοποίηση του λογαριασμού
//        Authenticator auth = new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(username, password);
//            }
//        };
//
//        // Δημιουργία συνεδρίας με βάση τις ρυθμίσεις
//        Session session = Session.getInstance(properties, auth);
//
//        try {
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
//            message.setSubject(subject);
//
//            // 1. Δημιουργία του HTML μέρους
//            MimeBodyPart messageBodyPart = new MimeBodyPart();
//            String htmlContent = "<html><body>" + messageContent + "<br><br>" + signature + "</body></html>";
//            messageBodyPart.setContent(htmlContent, "text/html; charset=UTF-8");
//
//            // 2. Επισύναψη εικόνας
//            String currentDir = System.getProperty("user.dir");
//            String fullPath = currentDir + File.separator + "images" + File.separator + "logo.png";
//            MimeBodyPart imagePart = new MimeBodyPart();
//            imagePart.attachFile(fullPath);
//            imagePart.setContentID("<logo>");
//            imagePart.setDisposition(MimeBodyPart.INLINE);
//
//            // 3. Σύνθεση του email
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(messageBodyPart);
//            multipart.addBodyPart(imagePart);
//
//            message.setContent(multipart);
//
//            Transport.send(message);
//        } catch (MessagingException e) {
//            Platform.runLater(() -> {
//                Notifications notifications = Notifications.create()
//                        .title("Attention")
//                        .text("Η αποστολή του email απέτυχε.")
//                        
//                        .hideAfter(Duration.seconds(5))
//                        .position(Pos.TOP_RIGHT);
//                notifications.showError();
//            });
//            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποστολή email.", e.getMessage(), Alert.AlertType.ERROR));
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void sendEmailWithAttachments(String recipientEmail, String subject, String body, List<File> attachments) throws Exception {
        // SMTP ρυθμίσεις
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // ασφαλής σύνδεση

        signature = AppSettings.loadSetting("signature");
        String logoPath = AppSettings.loadSetting("logoPath");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Create a multipart/related for the HTML and inline image
        MimeMultipart multipartRelated = new MimeMultipart("related");

        // HTML part
        MimeBodyPart htmlPart = new MimeBodyPart();
        String htmlContent = "<html><body>" + body + "<br><br><img src=\"cid:logoImage\" width=\"200\">" + signature + "</body></html>";
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
        multipartRelated.addBodyPart(htmlPart);

        // Image part
        if (logoPath != null && !logoPath.isEmpty()) {
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.attachFile(logoFile);
                imagePart.setContentID("<logoImage>");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                multipartRelated.addBodyPart(imagePart);
            }
        }

        // If there are no attachments, the multipart/related is the content
        if (attachments == null || attachments.isEmpty()) {
            message.setContent(multipartRelated);
        } else {
            // Create a multipart/mixed for the main content and attachments
            MimeMultipart multipartMixed = new MimeMultipart("mixed");

            // Add the multipart/related as the first part of the mixed multipart
            MimeBodyPart relatedBodyPart = new MimeBodyPart();
            relatedBodyPart.setContent(multipartRelated);
            multipartMixed.addBodyPart(relatedBodyPart);

            // Add attachments
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipartMixed.addBodyPart(attachmentPart);
            }

            message.setContent(multipartMixed);
        }

        Transport.send(message);
    }

    private void debugMultipart(Multipart multipart, String indent) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            System.out.println(indent + "Part " + i + ":");
            System.out.println(indent + "  Class: " + part.getClass().getSimpleName());
            System.out.println(indent + "  Content-Type: " + part.getContentType());
            System.out.println(indent + "  Disposition: " + part.getDisposition());
            System.out.println(indent + "  Filename: " + part.getFileName());

            Object content = part.getContent();
            if (content instanceof Multipart) {
                System.out.println(indent + "  Contains nested Multipart:");
                debugMultipart((Multipart) content, indent + "    ");
            } else if (content instanceof String) {
                String text = ((String) content).trim();
                if (text.length() > 100) text = text.substring(0, 100) + "...";
                System.out.println(indent + "  Content snippet: " + text);
            } else {
                System.out.println(indent + "  Content class: " + content.getClass().getSimpleName());
            }
        }
    }

}

