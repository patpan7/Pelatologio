package org.easytech.pelatologio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;


public class MainMenu extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        String username = AppSettings.loadSetting("appuser") != null ? AppSettings.loadSetting("appuser") : "";
        if (username == null || username.isEmpty()) {
            // Prompt user for username if not set
            Optional<String> result = promptForUsername();
            if (result.isPresent()) {
                username = result.get();
                AppSettings.saveSetting("appuser", username);
            } else {
                // If no username is provided, close the app
                System.out.println("No username provided. Exiting.");
                System.exit(0);
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainMenu.class.getResource("main-menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Πελατολόγιο");
        //stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            // Save settings before closing
            DBHelper dbHelper = new DBHelper();
            dbHelper.customerUnlockAll(AppSettings.loadSetting("appuser"));
        });

    }

    private Optional<String> promptForUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Εισαγωγή χρήστη");
        dialog.setHeaderText("Παρακαλώ εισάγετε το όνομα του χειριστή :");
        dialog.setContentText("Username:");
        return dialog.showAndWait();
    }

    public static void main(String[] args) {
        // Εκκίνηση της καταγραφής κονσόλας
        Logger.initLogging();
        launch();
    }
}