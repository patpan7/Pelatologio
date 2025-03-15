package org.easytech.pelatologio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
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

        // Ξεκινά το polling αφού φορτωθεί η εφαρμογή
        startPolling();
        startAppointmentReminder();

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

    // ΝΕΟ: Υπενθύμιση για ραντεβού
    public static void startAppointmentReminder() {
        ScheduledService<List<Tasks>> appointmentService = new ScheduledService<>() {
            @Override
            protected Task<List<Tasks>> createTask() {
                return new Task<>() {
                    @Override
                    protected List<Tasks> call() {
                        DBHelper dbHelper = new DBHelper();

                        return dbHelper.getUpcomingAppointments(LocalDateTime.now());
                    }
                };
            }
        };

        appointmentService.setPeriod(Duration.seconds(60)); // Έλεγχος κάθε λεπτό
        appointmentService.setOnSucceeded(event -> {
            List<Tasks> upcomingAppointments = appointmentService.getValue();
            if (!upcomingAppointments.isEmpty()) {
                Platform.runLater(() -> showAppointmentReminder(upcomingAppointments));
            }
        });

        appointmentService.start();
    }

    private static void showAppointmentReminder(List<Tasks> appointments) {
        for (Tasks appointment : appointments) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Υπενθύμιση Ραντεβού");
            alert.setHeaderText("Σε 30 λεπτά έχεις ραντεβού!");
            alert.setContentText("Τίτλος: " + appointment.getTitle() + "\nΏρα: " + appointment.getStartTime());

            ButtonType postponeButton = new ButtonType("Αναβολή");
            alert.getButtonTypes().add(postponeButton);

            ButtonType okButton = new ButtonType("OK");
            alert.getButtonTypes().add(okButton);


            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == okButton) {
                snoozeAppointment(appointment);
            }
            else if (result.isPresent() && result.get() == postponeButton) {
                showCustomer(appointment.getCustomerId());
                DBHelper dbHelper = new DBHelper();
                dbHelper.getSelectedCustomer(appointment.getCustomerId());
            }
        }
    }

    private static void showCustomer(int customerId) {
        DBHelper dbHelper = new DBHelper();

        Customer selectedCustomer = dbHelper.getSelectedCustomer(customerId);
        if (selectedCustomer.getCode() == 0) {
            return;
        }
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerData(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    dbHelper.customerUnlock(selectedCustomer.getCode());
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private static void snoozeAppointment(Tasks appointment) {
        DBHelper dbHelper = new DBHelper();
        dbHelper.snoozeAppointment(appointment.getId());
        Notifications.create()
                .title("Αναβολή Ραντεβού")
                .text("Το ραντεβού '" + appointment.getTitle() + "' ενημερώθηκε.")
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showInformation();
    }


    public static void main(String[] args) {
        // Εκκίνηση της καταγραφής κονσόλας
        Logger.initLogging();
        launch();
    }
}