package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.easytech.pelatologio.helper.HardwareIdUtil;
import org.easytech.pelatologio.helper.LicenseManager;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.util.ResourceBundle;

public class ActivationController implements Initializable {

    @FXML
    private TextField tfHardwareId; // Changed from Label to TextField
    @FXML
    private TextField tfActivationCode;
    @FXML
    private TextField tfSecretKey;
    @FXML
    private Label lblStatus;

    private LicenseManager licenseManager;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        licenseManager = new LicenseManager();
        tfHardwareId.setText(HardwareIdUtil.getDisplayHardwareId()); // Use raw ID for internal logic
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleActivate() {
        String activationCode = tfActivationCode.getText();
        String secretKey = tfSecretKey.getText(); // Read secret key from new field

        if (activationCode == null || activationCode.trim().isEmpty()) {
            lblStatus.setTextFill(Color.RED);
            lblStatus.setText("Παρακαλώ εισάγετε κωδικό ενεργοποίησης.");
            return;
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            lblStatus.setTextFill(Color.RED);
            lblStatus.setText("Παρακαλώ εισάγετε το μυστικό κλειδί.");
            return;
        }

        boolean activated = licenseManager.activate(activationCode, secretKey);

        if (activated) {
            lblStatus.setTextFill(Color.GREEN);
            lblStatus.setText("Ενεργοποίηση επιτυχής! Επανεκκίνηση εφαρμογής...");
            // Close the activation window
            if (stage != null) {
                stage.close();
            }
            // Request application restart
            MainMenu.restartApplication();
        } else {
            lblStatus.setTextFill(Color.RED);
            lblStatus.setText("Λανθασμένος κωδικός ή σφάλμα.");
        }
    }

    @FXML
    private void handleCopyHardwareId() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(tfHardwareId.getText());
        clipboard.setContent(content);
        lblStatus.setTextFill(Color.BLACK);
        lblStatus.setText("Hardware ID αντιγράφηκε στο πρόχειρο.");
    }
}
