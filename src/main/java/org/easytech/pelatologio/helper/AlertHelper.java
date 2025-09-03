package org.easytech.pelatologio.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertHelper {
    
    public static void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Make the alert modal
        alert.initModality(Modality.APPLICATION_MODAL);
        
        // Style the alert
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getStylesheets().add(
            AlertHelper.class.getResource("/org/easytech/pelatologio/custom-atlantafx.css").toExternalForm()
        );
        
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Make the alert modal
        alert.initModality(Modality.APPLICATION_MODAL);
        
        // Style the alert
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getStylesheets().add(
            AlertHelper.class.getResource("/org/easytech/pelatologio/custom-atlantafx.css").toExternalForm()
        );
        
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}
