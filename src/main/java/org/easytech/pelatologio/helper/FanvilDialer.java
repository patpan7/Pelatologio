package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.AppSettings;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class FanvilDialer {

    private static final String FANVIL_IP = AppSettings.loadSetting("fanvilIp");
    private static final String FANVIL_USER = AppSettings.loadSetting("fanvil.user");
    private static final String FANVIL_PASS = AppSettings.loadSetting("fanvil.pass");

    public static void dial(String phoneNumber) throws IOException, InterruptedException {
        if (FANVIL_IP == null || FANVIL_IP.isEmpty()) {
            throw new IOException("Η IP της συσκευής Fanvil δεν έχει οριστεί στις ρυθμίσεις.");
        }

        String command = "HANDFREE;" + phoneNumber + ";F_OK";
        String url = "http://" + FANVIL_IP + "/cgi-bin/ConfigManApp.com?key=" + command;

        HttpClient client;
        if (FANVIL_USER != null && !FANVIL_USER.isEmpty() && FANVIL_PASS != null) {
            client = HttpClient.newBuilder()
                    .authenticator(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(FANVIL_USER, FANVIL_PASS.toCharArray());
                        }
                    })
                    .build();
        } else {
            client = HttpClient.newHttpClient();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Η συσκευή Fanvil απάντησε με σφάλμα: " + response.statusCode() + " " + response.body());
        }
    }
}
