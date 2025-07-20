package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
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

    private static final List<Stage> openPopups = new ArrayList<>();
    private static final double POPUP_SPACING = 10.0;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnHidden(event -> {
            openPopups.remove(this.stage);
            repositionPopups();
        });
    }

    private void positionWindow() {
        Platform.runLater(() -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double popupHeight = stage.getHeight();
            double yPosition = screenBounds.getMaxY() - popupHeight - openPopups.size() * (popupHeight + POPUP_SPACING);

            stage.setX(screenBounds.getMaxX() - stage.getWidth() - POPUP_SPACING);
            stage.setY(yPosition);
            stage.show();
            openPopups.add(stage);
        });
    }

    private static void repositionPopups() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        for (int i = 0; i < openPopups.size(); i++) {
            Stage s = openPopups.get(i);
            double newY = screenBounds.getMaxY() - s.getHeight() - i * (s.getHeight() + POPUP_SPACING);
            s.setY(newY);
        }
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
        positionWindow();
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