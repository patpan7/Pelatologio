package org.easytech.pelatologio;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AfmLookupService {

    public String callAadeService(String afmToSearch) {
        String username = AppSettings.loadSetting("afmUser");
        String password = AppSettings.loadSetting("afmPass");
        String url = "https://www1.gsis.gr/wsaade/RgWsPublic2/RgWsPublic2";
        String action = "POST";

        try {
            // Δημιουργία του SOAP αιτήματος
            String soapRequest = createSoapRequest(username, password, afmToSearch);

            // Ρύθμιση της σύνδεσης
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/soap+xml;charset=\"utf-8\"");
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("SOAPAction", action);
            connection.setDoOutput(true);

            // Αποστολή του αιτήματος
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(soapRequest.getBytes("UTF-8"));
            }

            // Λήψη της απάντησης
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            // Επιστροφή της απάντησης ως κείμενο
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Σφάλμα κατά την κλήση της υπηρεσίας: " + e.getMessage();
        }
    }

    private String createSoapRequest(String username, String password, String afmToSearch) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope" 
                              xmlns:ns2="http://rgwspublic2/RgWsPublic2Service" 
                              xmlns:ns3="http://rgwspublic2/RgWsPublic2" 
                              xmlns:ns1="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                    <env:Header>
                        <ns1:Security>
                            <ns1:UsernameToken>
                                <ns1:Username>%s</ns1:Username>
                                <ns1:Password>%s</ns1:Password>
                            </ns1:UsernameToken>
                        </ns1:Security>
                    </env:Header>
                    <env:Body>
                        <ns2:rgWsPublic2AfmMethod>
                            <ns2:INPUT_REC>
                                <ns3:afm_called_by/>
                                <ns3:afm_called_for>%s</ns3:afm_called_for>
                            </ns2:INPUT_REC>
                        </ns2:rgWsPublic2AfmMethod>
                    </env:Body>
                </env:Envelope>
                """.formatted(username, password, afmToSearch);
    }
}
