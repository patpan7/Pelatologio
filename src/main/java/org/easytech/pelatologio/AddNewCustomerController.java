package org.easytech.pelatologio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddNewCustomerController {

    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfEmail;
    @FXML
    private Button btnAfmSearch;
    int code = 0;

    public void initialize() {
        // Εδώ μπορείς να αρχικοποιήσεις στοιχεία του pane αν χρειάζεται
        btnAfmSearch.setOnAction(event -> handleAfmSearch(tfAfm.getText()));
    }

    public void setCustomerData(Customer customer) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(customer.getName());
        tfTitle.setText(customer.getTitle());
        tfJob.setText(customer.getJob());
        tfAfm.setText(customer.getAfm());
        tfPhone1.setText(customer.getPhone1());
        tfPhone2.setText(customer.getPhone2());
        tfMobile.setText(customer.getMobile());
        tfAddress.setText(customer.getAddress());
        tfTown.setText(customer.getTown());
        tfEmail.setText(customer.getEmail());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = customer.getCode();
    }


    private void handleAfmSearch(String afm) {
        if (afm.isEmpty()) {
            showAlert("Error", "Please enter a valid ΑΦΜ.");
            return;
        }

        try {
            // Call the API
            String apiUrl = "https://publicity.businessportal.gr/api/autocomplete/" + afm;
            System.out.println("API URL: " + apiUrl);
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            // Προσθήκη του User-Agent header
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Check the response code
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.toString());
                JsonNode autocomplete = rootNode.path("payload").path("autocomplete");

                if (autocomplete.size() > 0) {
                    // Get the first result
                    JsonNode firstResult = autocomplete.get(0);
                    String name = firstResult.path("co_name").asText();
                    String title = firstResult.path("title").asText();

                    // Set values to the text fields
                    tfName.setText(name);
                    tfTitle.setText(title);
                } else {
                    showAlert("No Results", "No results found for the given ΑΦΜ.");
                }
            } else {
                showAlert("Error", "Failed to fetch data from API. Response Code: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while fetching data: " + e.getMessage());
        }
    }


    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addCustomer();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateCustomer();
        }
    }

    void addCustomer(){
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText();
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String email = tfEmail.getText();
        DBHelper dbHelper = new DBHelper();
        if (dbHelper.isAfmExists(afm)) {
            Platform.runLater(() -> showAlert("Προσοχή","Ο πελάτης με ΑΦΜ " + afm + " υπάρχει ήδη."));

        } else {
            dbHelper.insertCustomer(name, title, job, afm, phone1, phone2, mobile, address, town, email);
            Platform.runLater(() -> showAlert("Επιτυχία", "Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων."));
        }
    }

    void updateCustomer(){
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText();
        String afm = tfAfm.getText();
        String phone1 = tfPhone1.getText();
        String phone2 = tfPhone2.getText();
        String mobile = tfMobile.getText();
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String email = tfEmail.getText();
        DBHelper dbHelper = new DBHelper();
        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, email);
        showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
