package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CallMonitor implements Runnable {

    private SipTapiHandler sipTapiHandler;

    public CallMonitor(SipTapiHandler sipTapiHandler) {
        this.sipTapiHandler = sipTapiHandler;
    }

    @Override
    public void run() {
        while (true) {
            String callerId = sipTapiHandler.getCallerId();
            if (callerId != null && !callerId.isEmpty()) {
                Platform.runLater(() -> showPopup(callerId));
            }
            try {
                Thread.sleep(1000);  // Ελέγχει για νέες κλήσεις κάθε 1 δευτερόλεπτο
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPopup(String callerId) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Εισερχόμενη Κλήση");
        alert.setHeaderText("Εισερχόμενη κλήση από:");
        alert.setContentText(callerId);
        alert.showAndWait();
    }
}
