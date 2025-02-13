package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.Properties;

public class AppSettings {
    private static final String FILE_NAME = "app.properties";

    public String server;
    public String dbUser;
    public String dbPass;
    public String myposLink;
    public String SimplyUser;
    public String SimplyPass;
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
        myposLink = AppSettings.loadSetting("myposlink");
        SimplyUser = AppSettings.loadSetting("simplyUser");
        SimplyPass = AppSettings.loadSetting("simplyPass");
    }
    public static void saveSetting(String key, String value) {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILE_NAME)) {
            prop.load(input);
        } catch (IOException ignored) {}

        try (OutputStream output = new FileOutputStream(FILE_NAME)) {
            prop.setProperty(key, value);
            prop.store(output, null);
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public static String loadSetting(String key) {
        try (InputStream input = new FileInputStream(FILE_NAME)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(key);
        } catch (IOException | NullPointerException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την ανάκτηση.", e.getMessage(), Alert.AlertType.ERROR));
            return null;
        }
    }
}
