package org.easytech.pelatologio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.util.ThemeManager;
import org.easytech.pelatologio.models.Offer;
import org.easytech.pelatologio.models.Tasks;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MainMenu extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // License Check
        LicenseManager licenseManager = new LicenseManager();
        if (!licenseManager.isLicenseValid()) {
            showActivationWindow(stage);
            // Re-check after the window is closed. If still invalid, exit.
            if (!licenseManager.isLicenseValid()) {
                Platform.exit();
                return;
            }
        }

        // Database initialization loop
        while (!DBHelper.initializeDatabase()) {
            // Show dialog to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Could not connect to the database.");
            alert.setContentText("Please check your settings.");

            ButtonType settingsButton = new ButtonType("Settings");
            ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(settingsButton, exitButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == settingsButton) {
                // Open settings window
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("settings.fxml"));
                    Parent root = loader.load();
                    Stage settingsStage = new Stage();
                    settingsStage.setTitle("Settings");
                    settingsStage.setScene(new Scene(root));
                    settingsStage.initModality(Modality.APPLICATION_MODAL);
                    settingsStage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Exit application
                Platform.exit();
                return;
            }
        }

        Features.loadFeatureFlags();
        //Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
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
        
        // Apply saved theme
        ThemeManager.applySavedTheme(scene);
        
        // Apply ControlsFX CSS fix
        scene.getStylesheets().add(getClass().getResource("fix-controlsfx-styles.css").toExternalForm());

        stage.setTitle("Πελατολόγιο");
        //stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // Ξεκινά το polling αφού φορτωθεί η εφαρμογή
        if (!Features.isEnabled("offers"))
            startPolling();
        if (Features.isEnabled("tasks"))
            startAppointmentReminder();

        checkExpiredSubscriptions();
    }

    private void checkExpiredSubscriptions() {
        List<org.easytech.pelatologio.models.Subscription> expiredSubscriptions = DBHelper.getSubscriptionDao().getAllSubs(LocalDate.of(2000, 1, 1), LocalDate.now());
        List<org.easytech.pelatologio.models.Subscription> activeExpiredSubscriptions = new java.util.ArrayList<>();
        for (org.easytech.pelatologio.models.Subscription sub : expiredSubscriptions) {
            if (sub.isActive()) {
                activeExpiredSubscriptions.add(sub);
            }
        }

        if (!activeExpiredSubscriptions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ειδοποίηση Ληγμένων Συνδρομών");
            alert.setHeaderText("Βρέθηκαν οι παρακάτω ληγμένες συνδρομές:");

            TableView<org.easytech.pelatologio.models.Subscription> tableView = new TableView<>();
            TableColumn<org.easytech.pelatologio.models.Subscription, String> customerCol = new TableColumn<>("Πελάτης");
            customerCol.setPrefWidth(270);
            customerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));

            TableColumn<org.easytech.pelatologio.models.Subscription, String> titleCol = new TableColumn<>("Συνδρομή");
            titleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

            TableColumn<org.easytech.pelatologio.models.Subscription, String> endDateCol = new TableColumn<>("Ημ/νία Λήξης");
            endDateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            tableView.getColumns().addAll(customerCol, titleCol, endDateCol);
            tableView.setItems(javafx.collections.FXCollections.observableArrayList(activeExpiredSubscriptions));

            alert.getDialogPane().setContent(tableView);
            alert.getDialogPane().setPrefWidth(1000);
            alert.showAndWait();
        }
    }

    private void showActivationWindow(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ActivationView.fxml"));
        Parent root = loader.load();
        ActivationController controller = loader.getController();
        
        Stage activationStage = new Stage();
        activationStage.initModality(Modality.APPLICATION_MODAL);
        activationStage.initOwner(primaryStage);
        activationStage.setTitle("Ενεργοποίηση Εφαρμογής");
        activationStage.setScene(new Scene(root));
        controller.setStage(activationStage);
        activationStage.showAndWait();

        // After activation window is closed, re-check license
        if (!new LicenseManager().isLicenseValid()) {
            Platform.exit(); // Exit if license is still not valid
        }
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
                        .text("Νέα ενημέρωση στην προσφορά #" + offer.getId() +
                                "\nΚατάσταση: " + offer.getStatus() +
                                "\nΠελάτης: " + offer.getCustomerName())
                        .graphic(null)
                        .hideAfter(Duration.seconds(10))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();
            });
            DBHelper.getOfferDao().updateOfferStatus(offer.getId(), offer.getStatus());
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
                        //System.out.println("Έλεγχος για ενημερώσεις..." + lastCheck);
                        return DBHelper.getOfferDao().getUpdatedOffers(lastCheck);
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
                        return DBHelper.getTaskDao().getUpcomingAppointments(LocalDateTime.now());
                    }
                };
            }
        };

        appointmentService.setPeriod(Duration.minutes(15)); // Έλεγχος κάθε λεπτό
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

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                snoozeAppointment(appointment);
            } else if (result.isPresent() && result.get() == postponeButton) {
                try {
                    FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("calendarView.fxml"));
                    Parent root = loader.load();

                    Stage stage = new Stage();
                    stage.setTitle("Ημερολόγιο");
                    stage.setScene(new Scene(root));

                    // Προαιρετικά: Κλείδωμα του MainMenu αν χρειάζεται
                    stage.initModality(Modality.APPLICATION_MODAL);

                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void snoozeAppointment(Tasks appointment) {
        DBHelper.getTaskDao().snoozeAppointment(appointment.getId());
        Notifications.create()
                .title("Αναβολή Ραντεβού")
                .text("Το ραντεβού '" + appointment.getTitle() + "' ενημερώθηκε.")
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showInformation();
    }

    public static void main(String[] args) {
        Logger.initLogging();
        launch(args);
    }

    public static void restartApplication() {
        Platform.runLater(() -> {
            try {
                String java = System.getProperty("java.home") + "/bin/java";
                String jar = new java.io.File(MainMenu.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder(java, "-jar", jar);
                pb.start();
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogHelper.showDialog("Σφάλμα Επανεκκίνησης", "Δεν ήταν δυνατή η επανεκκίνηση της εφαρμογής.", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
