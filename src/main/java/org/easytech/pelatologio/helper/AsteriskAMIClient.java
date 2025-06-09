package org.easytech.pelatologio.helper;

import org.asteriskjava.manager.*;
import org.asteriskjava.manager.event.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsteriskAMIClient {
    private ManagerConnection managerConnection;

    public AsteriskAMIClient(String host, String username, String password) {
        managerConnection = new DefaultManagerConnection(host, username, password);
    }

    public void connect() throws Exception {
        managerConnection.addEventListener(this::handleEvent);
        managerConnection.login();
        // Απενεργοποίηση όλων των WARN μηνυμάτων του Asterisk-Java
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);

        Logger.getLogger("org.asteriskjava.manager.util.EventAttributesHelper").setLevel(Level.SEVERE);
    }

    private void handleEvent(ManagerEvent event) {
        if (event instanceof NewChannelEvent) {
            NewChannelEvent newChannel = (NewChannelEvent) event;
            if ("Ringing".equals(newChannel.getChannelStateDesc())) {
                // Παίρνουμε το Caller ID και το όνομα
                String callerIdNumber = newChannel.getCallerIdNum(); // Αριθμός καλούντος
                String callerIdName = newChannel.getCallerIdName(); // Όνομα καλούντος

                if (callerIdNumber != null && !callerIdNumber.isEmpty()) {
                    System.out.println("Εισερχόμενη Κλήση: " + callerIdNumber);
                } else {
                    System.out.println("Εισερχόμενη Κλήση από άγνωστο αριθμό");
                }

                if (callerIdName != null && !callerIdName.isEmpty()) {
                    System.out.println("Όνομα Καλούντος: " + callerIdName);
                } else {
                    System.out.println("Όνομα Καλούντος: Άγνωστο");
                }

                // Εμφάνιση ειδοποίησης με το Caller ID και το όνομα
                //showNotification("Εισερχόμενη Κλήση", "Από: " + callerIdName + " (" + callerIdNumber + ")");
                System.out.println("Κλήση από: " + callerIdName + " (" + callerIdNumber + ")");
            }
        } else if (event instanceof DialEvent) {
            DialEvent dialEvent = (DialEvent) event;
            String destination = dialEvent.getDialString(); // Παίρνει τον αριθμό κλήσης
            if (destination == null || destination.isEmpty()) {
                destination = dialEvent.getDestination(); // Εναλλακτικά, δοκίμασε το destination
            }
            System.out.println("Εξερχόμενη Κλήση: " + destination);

            // Κάνε logging για το κανάλι
            System.out.println("Κανάλι: " + dialEvent.getChannel());
            //showNotification("Εξερχόμενη Κλήση", "Προς: " + dialEvent.getDestination());
        }
        else if (event instanceof HangupEvent) {
            HangupEvent hangupEvent = (HangupEvent) event;
            System.out.println("Η κλήση τερματίστηκε από το κανάλι: " + hangupEvent.getChannel());
            //showNotification("Κλήση Τερματίστηκε", "Κανάλι: " + hangupEvent.getChannel());
        }
    }


    private void showNotification(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Ειδοποίηση Κλήσης");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.show();
        });
    }

    public void disconnect() {
        if (managerConnection != null) {
            managerConnection.logoff();
        }
    }
}
