package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CallerPopupController {
    @FXML private Label callerNameLabel;
    @FXML private Label callerNumberLabel;
    @FXML private Button openCustomerButton;
    @FXML private Button closeButton;

    private String callerNumber;
    private String customerName;
    private int customerId;
    private Stage stage;
    private Consumer<Integer> openCustomerCallback;

    private static final List<CallerPopupController> openPopups = new ArrayList<>();

    private static final double POPUP_SPACING = 10.0;
    private PopupPosition popupPosition = PopupPosition.BOTTOM_RIGHT;

    public void setPopupPosition(PopupPosition position) {
        this.popupPosition = position;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnHidden(event -> {
            openPopups.remove(this);
            repositionPopups();
        });
    }

    private void positionWindow() {
        Platform.runLater(() -> {
            stage.sizeToScene(); // Calculate size before showing
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double popupHeight = stage.getHeight();
            double popupWidth = stage.getWidth();

            double xPosition = 0;
            double yPosition = 0;
            int index = openPopups.size();
            double spacingOffset = index * (popupHeight + POPUP_SPACING);
            double safeMargin = 35.0; // Απόσταση από το επάνω άκρο, για να μη μπει πάνω στο X

            switch (popupPosition) {
                case TOP_LEFT:
                    xPosition = POPUP_SPACING;
                    yPosition = safeMargin + spacingOffset;
                    break;
                case TOP_RIGHT:
                    xPosition = screenBounds.getMaxX() - popupWidth - POPUP_SPACING;
                    yPosition = safeMargin + spacingOffset;
                    break;
                case BOTTOM_LEFT:
                    xPosition = POPUP_SPACING;
                    yPosition = screenBounds.getMaxY() - popupHeight - spacingOffset;
                    break;
                case BOTTOM_RIGHT:
                default:
                    xPosition = screenBounds.getMaxX() - popupWidth - POPUP_SPACING;
                    yPosition = screenBounds.getMaxY() - popupHeight - spacingOffset;
                    break;
            }

            stage.setX(xPosition);
            stage.setY(yPosition);
            stage.show();
            openPopups.add(this);
        });
    }

    private static void repositionPopups() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double safeMargin = 50.0;

        for (int i = 0; i < openPopups.size(); i++) {
            CallerPopupController ctrl = openPopups.get(i);
            Stage s = ctrl.stage;
            double popupHeight = s.getHeight();
            double popupWidth = s.getWidth();
            double spacingOffset = i * (popupHeight + POPUP_SPACING);

            double x = 0;
            double y = 0;

            switch (ctrl.popupPosition) {
                case TOP_LEFT:
                    x = POPUP_SPACING;
                    y = safeMargin + spacingOffset;
                    break;
                case TOP_RIGHT:
                    x = screenBounds.getMaxX() - popupWidth - POPUP_SPACING;
                    y = safeMargin + spacingOffset;
                    break;
                case BOTTOM_LEFT:
                    x = POPUP_SPACING;
                    y = screenBounds.getMaxY() - popupHeight - spacingOffset;
                    break;
                case BOTTOM_RIGHT:
                default:
                    x = screenBounds.getMaxX() - popupWidth - POPUP_SPACING;
                    y = screenBounds.getMaxY() - popupHeight - spacingOffset;
                    break;
            }

            double finalX = x;
            double finalY = y;
            Platform.runLater(() -> {
                s.setX(finalX);
                s.setY(finalY);
            });
        }
    }

    @FXML
    private void initialize() {
        openCustomerButton.setOnAction(e -> handleOpenCustomer());
        closeButton.setOnAction(e -> stage.close());
    }

    // Αρχικοποίηση δεδομένων από τον κύριο controller
    public void initData(String callerNumber, String customerName, int customerId, String customerTitle) {
        this.callerNumber = callerNumber;
        this.customerName = customerName;
        this.customerId = customerId;

        if (customerName != null && !customerName.isEmpty() && !customerName.equals("Άγνωστο")) {
            callerNameLabel.setText(customerName);
            callerNameLabel.setTooltip(new Tooltip(customerName));
        } else {
            callerNameLabel.setText("Άγνωστος");
        }
        callerNumberLabel.setText(callerNumber);

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