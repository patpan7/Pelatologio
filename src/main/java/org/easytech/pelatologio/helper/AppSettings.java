package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.easytech.pelatologio.helper.EncryptionHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppSettings {
    private static final String FILE_NAME = "app.properties";

    public String server;
    public String dbUser;
    public String dbPass;
    public String db;
    public String myposLink;
    public String SimplyUser;
    public String SimplyPass;
    public String fanvilUser;
    public String fanvilPass;

    private AppSettings() {
        init();
    }

    public static synchronized AppSettings getInstance() {
        return new AppSettings();
    }

    private void init() {
        server = AppSettings.loadSetting("server");
        dbUser = AppSettings.loadSetting("dbUser");
        dbPass = AppSettings.loadSetting("dbPass");
        db = AppSettings.loadSetting("db");
        myposLink = AppSettings.loadSetting("myposlink");
        SimplyUser = AppSettings.loadSetting("simplyUser");
        SimplyPass = AppSettings.loadSetting("simplyPass");
        fanvilUser = AppSettings.loadSetting("fanvil.user");
        fanvilPass = AppSettings.loadSetting("fanvil.pass");

        // Add new email templates if they don't exist
        if (loadSetting("email.template.subject.simplyService") == null) {
            saveSetting("email.template.subject.simplyService", "Νέος Πελάτης Simply {serviceType}");
        }
        if (loadSetting("email.template.body.simplyService") == null) {
            saveSetting("email.template.body.simplyService", "<b>Νέος Πελάτης Simply {serviceType}</b><br><b>Επωνυμία:</b> {customer.name}<br><b>ΑΦΜ:</b> {customer.afm}<br><b>E-mail:</b> {login.username}<br><b>Κωδικός:</b> {login.password}<br><b>Κινητό:</b> {customer.mobile}<br>Έχει κάνει αποδοχή σύμβασης και εξουσιοδότηση<br>");
        }
        if (loadSetting("email.template.subject.simplyRenew") == null) {
            saveSetting("email.template.subject.simplyRenew", "Ανανέωση Πελάτη Simply {serviceType}");
        }
        if (loadSetting("email.template.body.simplyRenew") == null) {
            saveSetting("email.template.body.simplyRenew", "<b>Ανανέωση Πελάτη Simply {serviceType}</b><br><b>Επωνυμία:</b> {customer.name}<br><b>ΑΦΜ:</b> {customer.afm}<br><b>E-mail:</b> {login.username}<br><b>Κωδικός:</b> {login.password}<br><b>Κινητό:</b> {customer.mobile}<br>");
        }
        if (loadSetting("email.template.subject.simplyPos") == null) {
            saveSetting("email.template.subject.simplyPos", "Νέος Πελάτης Simply POS");
        }
        if (loadSetting("email.template.body.simplyPos") == null) {
            saveSetting("email.template.body.simplyPos", "Νέος Πελάτης Simply POS\nΕπωνυμία: {customer.name}\nΑΦΜ: {customer.afm}\nEmail: {login.username}\nΚωδικός: {login.password}\nΚινητό: {customer.mobile}\n");
        }
    }

    public static void saveSetting(String key, String value) {
        Properties prop = new Properties();
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                byte[] encryptedBytes = input.readAllBytes();
                if (encryptedBytes.length > 0) {
                    String encryptedSettings = new String(encryptedBytes, StandardCharsets.UTF_8);
                    String decryptedSettings = EncryptionHelper.decrypt(encryptedSettings);
                    prop.load(new StringReader(decryptedSettings));
                }
            } catch (Exception e) {
                System.err.println("Could not load or decrypt existing settings. A new file will be created.");
                e.printStackTrace();
            }
        }

        prop.setProperty(key, value);

        try (StringWriter stringWriter = new StringWriter()) {
            prop.store(stringWriter, null);
            String settingsString = stringWriter.toString();
            String encryptedSettings = EncryptionHelper.encrypt(settingsString);

            try (OutputStream output = new FileOutputStream(FILE_NAME)) {
                output.write(encryptedSettings.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public static String loadSetting(String key) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        try (InputStream input = new FileInputStream(file)) {
            Properties prop = new Properties();
            byte[] encryptedBytes = input.readAllBytes();
            if (encryptedBytes.length == 0) {
                return null; // Empty file
            }
            String encryptedSettings = new String(encryptedBytes, StandardCharsets.UTF_8);
            String decryptedSettings = EncryptionHelper.decrypt(encryptedSettings);
            prop.load(new StringReader(decryptedSettings));
            return prop.getProperty(key);
        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την ανάκτηση.", e.getMessage(), Alert.AlertType.ERROR));
            return null;
        }
    }
}
