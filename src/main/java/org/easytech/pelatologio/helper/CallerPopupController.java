package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CallerPopupController {
    @FXML private Label callerNumberLabel;
    @FXML private Button openCustomerButton;
    @FXML private Button closeButton;

    private String callerNumber;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        positionWindowBottomRight();
    }

    private void positionWindowBottomRight() {
        Platform.runLater(() -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            double x = screenBounds.getMaxX() - stage.getWidth() - 20; // 20px απόσταση από τη δεξιά άκρη
            double y = screenBounds.getMaxY() - stage.getHeight() - 20; // 20px απόσταση από την κάτω άκρη

            stage.setX(x);
            stage.setY(y);
        });
    }


    @FXML
    private void initialize() {
        openCustomerButton.setOnAction(e -> handleOpenCustomer());
        closeButton.setOnAction(e -> stage.close());
    }
    // Αρχικοποίηση δεδομένων από τον κύριο controller
    public void initData(String callerNumber) {
        this.callerNumber = callerNumber;
        callerNumberLabel.setText("Αριθμός: " + callerNumber);
    }

    @FXML
    public void handleOpenCustomer() {
        // Κλείσιμο του popup και επιστροφή επιλογής
        ((Stage) callerNumberLabel.getScene().getWindow()).close();
        openCustomerDetails(callerNumber);
    }

    private void openCustomerDetails(String phoneNumber) {
        // Σύνδεση με τη βάση δεδομένων ή άλλο controller
        System.out.println("Αναζήτηση πελάτη με τηλέφωνο: " + phoneNumber);
        // ... υλοποίηση ...
    }
}