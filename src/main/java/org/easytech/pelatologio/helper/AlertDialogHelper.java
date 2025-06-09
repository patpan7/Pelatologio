package org.easytech.pelatologio.helper;

import javafx.scene.control.Alert;


public class AlertDialogHelper {

    public static void showDialog(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
