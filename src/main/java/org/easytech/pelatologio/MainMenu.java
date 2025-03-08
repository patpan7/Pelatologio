package org.easytech.pelatologio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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

        // ✅ Ξεκινά το polling αφού φορτωθεί η εφαρμογή
        startPolling();

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

    private static void updateUI(List<Offer> updatedOffers) {
        for (Offer offer : updatedOffers) {
            //System.out.println("Νέα ενημέρωση στην προσφορά #" + offer.getId());
            Platform.runLater(() -> {
                // Ενημέρωσε το TableView ή όποιο στοιχείο UI χρησιμοποιείς
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Νέα ενημέρωση στην προσφορά #" + offer.getId()+
                                "\nΚατάσταση: "+offer.getStatus()+
                                "\nΠελάτης: "+offer.getCustomerName())
                        .graphic(null)
                        .hideAfter(Duration.seconds(10))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();});
            DBHelper dbHelper = new DBHelper();
            dbHelper.updateOfferStatus(offer.getId(), offer.getStatus());
            // Ενημέρωσε το TableView ή όποιο στοιχείο UI χρησιμοποιείς
        }
    }
    private static LocalDateTime lastCheck = LocalDateTime.now();

    public static void startPolling() {
        ScheduledService<List<Offer>> pollingService = new ScheduledService<>() {
            @Override
            protected Task<List<Offer>> createTask() {
                return new Task<>() {
                    @Override
                    protected List<Offer> call() {
                        DBHelper dbHelper = new DBHelper();
                        //System.out.println("Έλεγχος για ενημερώσεις..." + lastCheck);
                        return dbHelper.getUpdatedOffers(lastCheck);
                    }
                };
            }
        };

        pollingService.setPeriod(Duration.seconds(30)); // Ορισμός χρόνου polling
        pollingService.setOnSucceeded(event -> {
            List<Offer> updatedOffers = pollingService.getValue();
            if (!updatedOffers.isEmpty()) {
                Platform.runLater(() -> updateUI(updatedOffers));
                lastCheck = LocalDateTime.now(); // Ενημερώνουμε το timestamp
            }
        });

        pollingService.start();
    }


    public static void main(String[] args) {
        // Εκκίνηση της καταγραφής κονσόλας
        Logger.initLogging();
        launch();
    }
}