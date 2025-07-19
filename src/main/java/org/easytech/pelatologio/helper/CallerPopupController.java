package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.easytech.pelatologio.AppSettings;
import org.easytech.pelatologio.models.CallLog;

import java.util.function.Consumer;

public class CallerPopupController {
    @FXML private Label callerNumberLabel;
    @FXML private Button openCustomerButton;
    @FXML private Button closeButton;

    private String callerNumber;
    private String customerName;
    private int customerId;
    private Stage stage;
    private Consumer<Integer> openCustomerCallback;

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
    public void initData(String callerNumber, String customerName, int customerId) {
        this.callerNumber = callerNumber;
        this.customerName = customerName;
        this.customerId = customerId;

        if (customerName != null && !customerName.isEmpty() && !customerName.equals("Άγνωστο")) {
            callerNumberLabel.setText("Κλήση από: " + customerName + " (" + callerNumber + ")");
        } else {
            callerNumberLabel.setText("Κλήση από: " + callerNumber);
        }

        openCustomerButton.setDisable(customerId == -1);
    }

    public void setOpenCustomerCallback(Consumer<Integer> callback) {
        this.openCustomerCallback = callback;
    }

    @FXML
    public void handleOpenCustomer() {
        // Κλείσιμο του popup
        if (stage != null) {
            stage.close();
        }
        // Επιστροφή επιλογής στον κύριο controller
        if (openCustomerCallback != null && customerId != -1) {
            openCustomerCallback.accept(customerId);
        }
    }
}