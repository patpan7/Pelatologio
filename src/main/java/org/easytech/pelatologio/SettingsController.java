package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AfmLookupService;
import org.easytech.pelatologio.helper.AfmResponseParser;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TextField tfServer;
    @FXML
    private TextField tfUser;
    @FXML
    private TextField tfPass;
    @FXML
    private TextField tfMyposLink;
    @FXML
    private TextField tfMyposUser;
    @FXML
    private TextField tfMyposPass;
    @FXML
    private TextField tfSimplyPosUser;
    @FXML
    private TextField tfSimplyPosPass;
    @FXML
    private TextField tfSimplyCloudUser;
    @FXML
    private TextField tfSimplyCloudPass;
    @FXML
    private TextField tfSimplyRegisterMail;
    @FXML
    private TextField tfSimplyMail1;
    @FXML
    private TextField tfSimplyMail2;
    @FXML
    private TextField tfEmblemUser;
    @FXML
    private TextField tfEmblemPass;
    @FXML
    private TextField tfEmblemRegisterMail;
    @FXML
    private TextField tfErganiRegisterMail;
    @FXML
    private TextField tfTaxisUser;
    @FXML
    private TextField tfTaxisPass;
    @FXML
    private TextField tfAfmUser;
    @FXML
    private TextField tfAfmPass;
    @FXML
    private RadioButton rbChrome;
    @FXML
    private RadioButton rbFirefox;
    @FXML
    private RadioButton rbEdge;
    @FXML
    private ToggleGroup browserToggleGroup;
    @FXML
    private TextField tfAppUser;
    @FXML
    private TextField tfDataFolder;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfEmailPassKey;
    @FXML
    private TextField tfSMTP;
    @FXML
    private TextField tfSMTPPort;
    @FXML
    private TextArea taSignature;
    @FXML
    private TextField tfFanvilIp;
    @FXML
    private TextField tfFanvilUser;
    @FXML
    private TextField tfFanvilPass;
    @FXML
    private ToggleGroup positionToggleGroup;
    @FXML
    private RadioButton rbPositionTopRight;
    @FXML
    private RadioButton rbPositionTopLeft;
    @FXML
    private RadioButton rbPositionBottomLeft;
    @FXML
    private RadioButton rbPositionBottomRight;

    // SIP Settings
    @FXML private TextField tfSipUser;
    @FXML private TextField tfSipPassword;
    @FXML private TextField tfSipDomain;
    @FXML private TextField tfSipPort;
    @FXML private TextField tfLocalIpAddress;
    @FXML private TextField tfSipTransport;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setupBrowserToggleGroup();
        setupPositionToggleGroup();
        loadTextFieldSettings();
        loadBrowserSettings();
        loadPositionSettings();
    }

    private void setupBrowserToggleGroup() {
        browserToggleGroup = new ToggleGroup();
        rbChrome.setToggleGroup(browserToggleGroup);
        rbEdge.setToggleGroup(browserToggleGroup);
        rbFirefox.setToggleGroup(browserToggleGroup);
    }

    private void setupPositionToggleGroup() {
        positionToggleGroup = new ToggleGroup();
        rbPositionTopRight.setToggleGroup(positionToggleGroup);
        rbPositionTopLeft.setToggleGroup(positionToggleGroup);
        rbPositionBottomLeft.setToggleGroup(positionToggleGroup);
        rbPositionBottomRight.setToggleGroup(positionToggleGroup);
    }

    private void loadTextFieldSettings() {
        tfServer.setText(getSettingOrEmpty("server"));
        tfUser.setText(getSettingOrEmpty("dbUser"));
        tfPass.setText(getSettingOrEmpty("dbPass"));
        tfMyposLink.setText(getSettingOrEmpty("myposlink"));
        tfMyposUser.setText(getSettingOrEmpty("myposUser"));
        tfMyposPass.setText(getSettingOrEmpty("myposPass"));
        tfSimplyPosUser.setText(getSettingOrEmpty("simplyPosUser"));
        tfSimplyPosPass.setText(getSettingOrEmpty("simplyPosPass"));
        tfSimplyCloudUser.setText(getSettingOrEmpty("simplyCloudUser"));
        tfSimplyCloudPass.setText(getSettingOrEmpty("simplyCloudPass"));
        tfSimplyRegisterMail.setText(getSettingOrEmpty("simplyRegisterMail"));
        tfSimplyMail1.setText(getSettingOrEmpty("simplyMail1"));
        tfSimplyMail2.setText(getSettingOrEmpty("simplyMail2"));
        tfTaxisUser.setText(getSettingOrEmpty("taxisUser"));
        tfTaxisPass.setText(getSettingOrEmpty("taxisPass"));
        tfAfmUser.setText(getSettingOrEmpty("afmUser"));
        tfAfmPass.setText(getSettingOrEmpty("afmPass"));
        tfAppUser.setText(getSettingOrEmpty("appuser"));
        tfDataFolder.setText(getSettingOrEmpty("datafolder"));
        tfEmail.setText(getSettingOrEmpty("email"));
        tfEmailPassKey.setText(getSettingOrEmpty("emailPass"));
        tfSMTP.setText(getSettingOrEmpty("smtp"));
        tfSMTPPort.setText(getSettingOrEmpty("smtpport"));
        tfEmblemUser.setText(getSettingOrEmpty("emblemUser"));
        tfEmblemPass.setText(getSettingOrEmpty("emblemPass"));
        tfEmblemRegisterMail.setText(getSettingOrEmpty("emblemRegisterMail"));
        taSignature.setText(getSettingOrEmpty("signature"));
        tfErganiRegisterMail.setText(getSettingOrEmpty("erganiRegisterMail"));

        // Load SIP settings
        tfSipUser.setText(getSettingOrEmpty("sipUser"));
        tfSipPassword.setText(getSettingOrEmpty("sipPassword"));
        tfSipDomain.setText(getSettingOrEmpty("sipDomain"));
        tfSipPort.setText(getSettingOrEmpty("sipPort"));
        tfLocalIpAddress.setText(getSettingOrEmpty("localIpAddress"));
        tfSipTransport.setText(getSettingOrEmpty("sipTransport"));
        tfFanvilIp.setText(getSettingOrEmpty("fanvilIp"));
        tfFanvilUser.setText(getSettingOrEmpty("fanvil.user"));
        tfFanvilPass.setText(getSettingOrEmpty("fanvil.pass"));
    }

    private String getSettingOrEmpty(String key) {
        String setting = AppSettings.loadSetting(key);
        return setting != null ? setting : "";
    }

    private void loadBrowserSettings() {
        String browser = getSettingOrEmpty("browser");
        switch (browser) {
            case "chrome" -> rbChrome.setSelected(true);
            case "firefox" -> rbFirefox.setSelected(true);
            case "edge" -> rbEdge.setSelected(true);
        }
    }

    private void loadPositionSettings() {
        String position = getSettingOrEmpty("callerPopupPosition");
        switch (position) {
            case "TOP_LEFT" -> rbPositionTopLeft.setSelected(true);
            case "TOP_RIGHT" -> rbPositionTopRight.setSelected(true);
            case "BOTTOM_LEFT" -> rbPositionBottomLeft.setSelected(true);
            case "BOTTOM_RIGHT" -> rbPositionBottomRight.setSelected(true);
        }
    }

    public void saveSettings(ActionEvent event) {

        AppSettings.saveSetting("server", tfServer.getText());
        AppSettings.saveSetting("dbUser", tfUser.getText());
        AppSettings.saveSetting("dbPass", tfPass.getText());
        AppSettings.saveSetting("myposlink", tfMyposLink.getText());
        AppSettings.saveSetting("myposUser",tfMyposUser.getText());
        AppSettings.saveSetting("myposPass", tfMyposPass.getText());
        AppSettings.saveSetting("simplyPosUser", tfSimplyPosUser.getText());
        AppSettings.saveSetting("simplyPosPass", tfSimplyPosPass.getText());
        AppSettings.saveSetting("simplyCloudUser", tfSimplyCloudUser.getText());
        AppSettings.saveSetting("simplyCloudPass", tfSimplyCloudPass.getText());
        AppSettings.saveSetting("simplyRegisterMail", tfSimplyRegisterMail.getText());
        AppSettings.saveSetting("simplyMail1", tfSimplyMail1.getText());
        AppSettings.saveSetting("simplyMail2", tfSimplyMail2.getText());
        AppSettings.saveSetting("taxisUser", tfTaxisUser.getText());
        AppSettings.saveSetting("taxisPass", tfTaxisPass.getText());
        AppSettings.saveSetting("afmUser", tfAfmUser.getText());
        AppSettings.saveSetting("afmPass", tfAfmPass.getText());
        AppSettings.saveSetting("appuser", tfAppUser.getText());
        AppSettings.saveSetting("datafolder", tfDataFolder.getText());
        AppSettings.saveSetting("email", tfEmail.getText());
        AppSettings.saveSetting("emailPass", tfEmailPassKey.getText());
        AppSettings.saveSetting("smtp", tfSMTP.getText());
        AppSettings.saveSetting("smtpport", tfSMTPPort.getText());
        AppSettings.saveSetting("signature", taSignature.getText());
        AppSettings.saveSetting("emblemUser", tfEmblemUser.getText());
        AppSettings.saveSetting("emblemPass", tfEmblemPass.getText());
        AppSettings.saveSetting("emblemRegisterMail", tfEmblemRegisterMail.getText());
        AppSettings.saveSetting("erganiRegisterMail", tfErganiRegisterMail.getText());

        // Save SIP settings
        AppSettings.saveSetting("sipUser", tfSipUser.getText());
        AppSettings.saveSetting("sipPassword", tfSipPassword.getText());
        AppSettings.saveSetting("sipDomain", tfSipDomain.getText());
        AppSettings.saveSetting("sipPort", tfSipPort.getText());
        AppSettings.saveSetting("localIpAddress", tfLocalIpAddress.getText());
        AppSettings.saveSetting("sipTransport", tfSipTransport.getText());
        AppSettings.saveSetting("fanvilIp", tfFanvilIp.getText());
        AppSettings.saveSetting("fanvil.user", tfFanvilUser.getText());
        AppSettings.saveSetting("fanvil.pass", tfFanvilPass.getText());

        if (rbChrome.isSelected()) {
            AppSettings.saveSetting("browser", "chrome");
        } else if (rbFirefox.isSelected()) {
            AppSettings.saveSetting("browser", "firefox");
        } else if (rbEdge.isSelected()) {
            AppSettings.saveSetting("browser", "edge");
        }

        if (rbPositionTopLeft.isSelected()) {
            AppSettings.saveSetting("callerPopupPosition", "TOP_LEFT");
        } else if (rbPositionTopRight.isSelected()) {
            AppSettings.saveSetting("callerPopupPosition", "TOP_RIGHT");
        } else if (rbPositionBottomLeft.isSelected()) {
            AppSettings.saveSetting("callerPopupPosition", "BOTTOM_LEFT");
        } else if (rbPositionBottomRight.isSelected()) {
            AppSettings.saveSetting("callerPopupPosition", "BOTTOM_RIGHT");
        }
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create()
                    .title("Επιτυχία")
                    .text("Οι ρυθμίσεις αποθηκεύτηκαν!")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showInformation();});
    }

    public void syncClick(ActionEvent event) {
        DBHelper dbHelper = new DBHelper();
        DBHelper.getMegasoftDao().syncMegasoft();
    }

    public void deactivateCustomers(ActionEvent event) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
                DBHelper dbHelper = new DBHelper();
                AfmLookupService lookupService = new AfmLookupService();
                List<Customer> customers = DBHelper.getCustomerDao().getCustomers(); // Υλοποίησέ τη με SQL ή DAO

                int total = customers.size();
                int count = 0;

                for (Customer customer : customers) {
                    try {
                        String responseXml = lookupService.callAadeService(customer.getAfm());

                        if (AfmResponseParser.isFormerProfessional(responseXml)) {
                            DBHelper.getCustomerDao().deactivateCustomer(customer); // Υλοποίησε SQL update: π.χ. SET active = 0
                            System.out.println("ΑΦΜ " + customer.getAfm() + " έγινε ανενεργό (ΠΡΩΗΝ ΕΠΑΓΓΕΛΜΑΤΙΑΣ)");
                        } else {
                            System.out.println("ΑΦΜ " + customer.getAfm() + " παραμένει ενεργό");
                        }

                        count++;
                        updateProgress(count, total);
                        Thread.sleep(200); // Προαιρετικό throttling

                    } catch (Exception ex) {
                        ex.printStackTrace(); // Ή καταγραφή σε log
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> AlertDialogHelper.showDialog(
                        "Ολοκληρώθηκε",
                        "Έλεγχος ΑΦΜ",
                        "Η ενημέρωση ολοκληρώθηκε για όλους τους πελάτες.",
                        Alert.AlertType.INFORMATION
                ));
            }
        };

        new Thread(task).start();
    }

    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Σημειώσεων");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(400, 300);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            taSignature.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);
        //vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(taSignature.getText());
        }
    }
}
