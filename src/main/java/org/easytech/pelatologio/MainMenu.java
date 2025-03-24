package org.easytech.pelatologio;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.animation.PauseTransition;
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
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Ξεκίνημα του HTTP server για λήψη κλήσεων
        startCallReceiverServer();

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
            }
            else if (result.isPresent() && result.get() == postponeButton) {
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
        DBHelper dbHelper = new DBHelper();
        dbHelper.snoozeAppointment(appointment.getId());
        Notifications.create()
                .title("Αναβολή Ραντεβού")
                .text("Το ραντεβού '" + appointment.getTitle() + "' ενημερώθηκε.")
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showInformation();
    }

    private void startCallReceiverServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/incomingcall", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    URI requestURI = exchange.getRequestURI();
                    String query = requestURI.getQuery();
                    Map<String, String> params = parseQuery(query);

                    String callerNumber = params.get("num");
                    System.out.println("Incoming call from: " + callerNumber);

                    // Αντικατάσταση της Notifications με το FXML popup
                    Platform.runLater(() -> showCallerPopup(callerNumber));

                    // Απάντηση στο τηλέφωνο
                    String response = "Call received";
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Call receiver server started on port 8000");
        } catch (IOException e) {
            e.printStackTrace();
            Notifications.create()
                    .title("Σφάλμα")
                    .text("Αδυναμία εκκίνησης του server λήψης κλήσεων.")
                    .showError();
        }
    }

    private void showCallerPopup(String callerNumber) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/caller_popup.fxml"));
                Parent root = loader.load();

                CallerPopupController controller = loader.getController();
                controller.initData(callerNumber);

                Stage popupStage = new Stage();
                controller.setStage(popupStage); // Περνάμε το stage στον controller

                popupStage.initStyle(StageStyle.UTILITY); // Απλό παράθυρο χωρίς κουμπιά
                popupStage.initModality(Modality.NONE); // Να μην μπλοκάρει άλλα παράθυρα
                popupStage.setScene(new Scene(root));
                popupStage.setTitle("Εισερχόμενη Κλήση");
                popupStage.show();

                // Αυτόματο κλείσιμο μετά από 10 δευτερόλεπτα (προαιρετικό)
                PauseTransition delay = new PauseTransition(Duration.seconds(10));
                delay.setOnFinished(e -> popupStage.close());
                delay.play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }


    public static void main(String[] args) {
        // Εκκίνηση της καταγραφής κονσόλας
        Logger.initLogging();
        launch();
    }
}