package org.easytech.pelatologio.batches;

import org.easytech.pelatologio.helper.AfmLookupService;
import org.easytech.pelatologio.helper.AfmResponseParser;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BatchCustomers {

    public static void main(String[] args) {
        String csvFile = "import_mypos.csv"; // The CSV file with the 200 customers to import
        System.out.println("Starting batch customer creation from: " + csvFile);


        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip header line

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 5) continue; // Skip malformed lines

                String companyNameFromCsv = values[0].replace("\"", "").trim();
                String afm = values[1].replace("\"", "").trim();
                String myposClientId = values[2].replace("\"", "").trim();
                String phone = values[3].replace("\"", "").trim();
                String email = values[4].replace("\"", "").trim();

                System.out.println("\nProcessing AFM: " + afm);

                // 1. Check if customer already exists
                if (DBHelper.getCustomerDao().isAfmExists(afm)) {
                    System.out.println("  -> Customer with AFM " + afm + " already exists. Skipping.");
                    continue;
                }

                // 2. Call AADE to get official data
                System.out.println("  -> Fetching data from AADE...");
                AfmLookupService aadeService = new AfmLookupService();
                String responseXml = aadeService.callAadeService(afm);
                String errorDescr = AfmResponseParser.getXPathValue(responseXml, "//error_rec/error_descr");

                if (errorDescr != null && !errorDescr.isEmpty()) {
                    System.err.println("  -> AADE Error for AFM " + afm + ": " + errorDescr);
                    continue;
                }

                Customer customerFromAade = AfmResponseParser.parseResponse(responseXml);
                if (customerFromAade == null) {
                    System.err.println("  -> Could not parse AADE response for AFM " + afm);
                    continue;
                }

                // 3. Create new customer
                System.out.println("  -> Creating new customer...");
                int newCustomerId = DBHelper.getCustomerDao().insertCustomer(
                        customerFromAade.getName(),
                        customerFromAade.getTitle(),
                        customerFromAade.getJob(),
                        afm,
                        phone, // phone1 from CSV
                        "",      // phone2 is empty
                        phone, // mobile from CSV
                        customerFromAade.getAddress(),
                        customerFromAade.getTown(),
                        customerFromAade.getPostcode(),
                        email,   // email from CSV
                        "",      // email2 is empty
                        "",      // manager is empty
                        "",      // managerPhone is empty
                        "Αυτόματη δημιουργία από myPOS export.", // notes
                        0,       // accId
                        "",      // accName1
                        "",      // accEmail1
                        0,       // recommendation id
                        "0",     // balance
                        "",      // balanceReason
                        0        // subJobTeam id (for now)
                );

                if (newCustomerId == -1) {
                    System.err.println("  -> Failed to insert customer with AFM " + afm + " into the database.");
                    continue;
                }

                // 4. Update mypos_client_id for the new customer
                DBHelper.getCustomerDao().updateMyPosClientId(newCustomerId, myposClientId);

                // 5. Add myPOS login
                System.out.println("  -> Adding myPOS login...");
                Logins logins = new Logins();
                logins.setUsername(email);
                logins.setPassword("");
                logins.setPhone(phone);
                logins.setTag("myPOS");
                DBHelper.getLoginDao().addLogin(newCustomerId, logins,1);

                System.out.println("  -> Successfully created customer '" + customerFromAade.getName() + "' with ID: " + newCustomerId);
            }

            System.out.println("\nBatch customer creation process finished.");

        } catch (IOException e) {
            System.err.println("A critical error occurred during the batch process:");
            e.printStackTrace();
        }
    }
}
