package org.easytech.pelatologio;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class test {
//    public static void main(String[] args) {
//        // Ρυθμίσεις για SMTP διακομιστή (π.χ., Gmail)
//        String host = "smtp.gmail.com";
//        String port = "587";
//        String username = "patelos942@gmail.com";
//        String password = "dhgv bawk wqlw szsj"; // Χρησιμοποίησε κωδικό εφαρμογής για Gmail
//
//        EmailSender emailSender = new EmailSender(host, port, username, password);
//
//        // Αποστολή του email
//        String recipient = "patpan7@gmail.com";
//        String subject = "Test Email";
//        String messageContent = "This is a test email sent from Java.";
//
//        emailSender.sendEmail(recipient, subject, messageContent);
//    }


    public static void dial(String phoneIp, String number, String username, String password) throws IOException {
        String url = "http://" + phoneIp + "/cgi-bin/ConfigManApp.com?key=PHONE&number=" + number;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        // Προσθήκη authentication header
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

        int responseCode = connection.getResponseCode();
        System.out.println("Dial Response Code: " + responseCode);

        if (responseCode == 200) {
            System.out.println("Κλήση προς " + number + " ξεκίνησε επιτυχώς!");
        } else {
            System.out.println("Σφάλμα κατά την έναρξη της κλήσης. Κωδικός: " + responseCode);
        }
    }

    public static void main(String[] args) {
        try {
            dial("192.168.1.22", "02101234567", "admin", "admin"); // Βάλε το σωστό password
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

