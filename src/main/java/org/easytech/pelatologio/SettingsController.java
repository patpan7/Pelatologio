package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.easytech.pelatologio.models.Offer;
import org.easytech.pelatologio.models.Subscription;
import org.easytech.pelatologio.models.Tasks;
import org.easytech.pelatologio.util.ThemeManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.StringConverter;

public class SettingsController implements Initializable {
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private StackPane stackPane;
    @FXML
    private TextField tfServer;
    @FXML
    private TextField tfUser;
    @FXML
    private TextField tfPass;
    @FXML
    private TextField tfDb;
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
    private TitledPane simplySettingsPane;
    @FXML
    private TextField tfEmblemUser;
    @FXML
    private TextField tfEmblemPass;
    @FXML
    private TextField tfEmblemRegisterMail;
    @FXML
    private TextField tfErganiRegisterMail;
    @FXML
    private TitledPane emblemSettingsPane;
    @FXML
    private TitledPane erganiSettingsPane;
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
    private HTMLEditor taSignature;
    @FXML
    private TextField tfLogoPath;
    @FXML
    private Button btnChooseLogo;
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
    @FXML
    private TextField tfSipUser;
    @FXML
    private TextField tfSipPassword;
    @FXML
    private TextField tfSipDomain;
    @FXML
    private TextField tfSipPort;
    @FXML
    private TextField tfLocalIpAddress;
    @FXML
    private TextField tfSipTransport;
    @FXML
    private ComboBox<String> templateComboBox;
    @FXML
    private TextField templateSubjectField;
    @FXML
    private HTMLEditor templateBodyEditor;
    @FXML
    private ComboBox<Class<?>> modelComboBox;
    @FXML
    private ComboBox<String> fieldComboBox;
    @FXML
    private Button insertPlaceholderButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setupBrowserToggleGroup();
        setupPositionToggleGroup();
        loadTextFieldSettings();
        loadBrowserSettings();
        loadPositionSettings();
        initializeThemeSettings();
        setupLogoControls(); // NEW
        setupTemplateEditor(); // NEW
        setupPlaceholderHelper(); // NEW

        if (!Features.isEnabled("simply")) {
            simplySettingsPane.setVisible(false);
            simplySettingsPane.setManaged(false);
        }

        if (!Features.isEnabled("emblem")) {
            emblemSettingsPane.setVisible(false);
            emblemSettingsPane.setManaged(false);
        }

        if (!Features.isEnabled("ergani")) {
            erganiSettingsPane.setVisible(false);
            erganiSettingsPane.setManaged(false);
        }
    }

    private void setupPlaceholderHelper() {
        modelComboBox.getItems().addAll(Customer.class, Logins.class, Offer.class, Subscription.class, Tasks.class);
        modelComboBox.setConverter(new StringConverter<Class<?>>() {
            @Override
            public String toString(Class<?> object) {
                return object != null ? object.getSimpleName() : "";
            }

            @Override
            public Class<?> fromString(String string) {
                return null; // Not needed
            }
        });

        modelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fieldComboBox.getItems().clear();
                for (java.lang.reflect.Field field : newVal.getDeclaredFields()) {
                    fieldComboBox.getItems().add(field.getName());
                }
            }
        });

        insertPlaceholderButton.setOnAction(event -> {
            Class<?> selectedModel = modelComboBox.getValue();
            String selectedField = fieldComboBox.getValue();
            if (selectedModel != null && selectedField != null) {
                String placeholder = "{" + selectedModel.getSimpleName().toLowerCase() + "." + selectedField + "}";
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(placeholder);
                clipboard.setContent(content);
                Notifications.create().title("Placeholder Copied").text(placeholder).position(Pos.TOP_RIGHT).showInformation();
            }
        });
    }

    private void setupTemplateEditor() {
        templateComboBox.getItems().addAll("simplyService", "simplyRenew");
        templateComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                templateSubjectField.setText(getSettingOrEmpty("email.template.subject." + newVal));
                templateBodyEditor.setHtmlText(getSettingOrEmpty("email.template.body." + newVal));
            }
        });
    }

    private void initializeThemeSettings() {
        try {
            // Initialize theme combo box
            themeComboBox.getItems().clear();
            themeComboBox.getItems().addAll(ThemeManager.getAvailableThemes());

            // Load saved theme
            String currentTheme = ThemeManager.getCurrentTheme();
            if (currentTheme != null && !currentTheme.isEmpty()) {
                themeComboBox.setValue(currentTheme);
            } else {
                themeComboBox.setValue("Nord Light"); // Default theme
            }

            // Add theme change listener
            themeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && stackPane != null && stackPane.getScene() != null) {
                    ThemeManager.applyTheme(newVal, stackPane.getScene());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void setupLogoControls() {
        btnChooseLogo.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Επιλογή Λογότυπου");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(stackPane.getScene().getWindow());
            if (selectedFile != null) {
                tfLogoPath.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void loadTextFieldSettings() {
        tfServer.setText(getSettingOrEmpty("server"));
        tfUser.setText(getSettingOrEmpty("dbUser"));
        tfPass.setText(getSettingOrEmpty("dbPass"));
        tfDb.setText(getSettingOrEmpty("db"));
        tfMyposLink.setText(getSettingOrEmpty("myposlink"));
        tfMyposUser.setText(getSettingOrEmpty("myposUser"));
        tfMyposPass.setText(getSettingOrEmpty("myposPass"));
        if (Features.isEnabled("simply")) {
            tfSimplyPosUser.setText(getSettingOrEmpty("simplyPosUser"));
            tfSimplyPosPass.setText(getSettingOrEmpty("simplyPosPass"));
            tfSimplyCloudUser.setText(getSettingOrEmpty("simplyCloudUser"));
            tfSimplyCloudPass.setText(getSettingOrEmpty("simplyCloudPass"));
            tfSimplyRegisterMail.setText(getSettingOrEmpty("simplyRegisterMail"));
            tfSimplyMail1.setText(getSettingOrEmpty("simplyMail1"));
            tfSimplyMail2.setText(getSettingOrEmpty("simplyMail2"));
        }
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
        if (Features.isEnabled("emblem")) {
            tfEmblemUser.setText(getSettingOrEmpty("emblemUser"));
            tfEmblemPass.setText(getSettingOrEmpty("emblemPass"));
            tfEmblemRegisterMail.setText(getSettingOrEmpty("emblemRegisterMail"));
        }

        taSignature.setHtmlText(getSettingOrEmpty("signature"));
        tfLogoPath.setText(getSettingOrEmpty("logoPath")); // NEW
        if (Features.isEnabled("ergani")) {
            tfErganiRegisterMail.setText(getSettingOrEmpty("erganiRegisterMail"));
        }

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

    @FXML
    private void saveSettings() {
        AppSettings.saveSetting("server", tfServer.getText());
        AppSettings.saveSetting("dbUser", tfUser.getText());
        AppSettings.saveSetting("dbPass", tfPass.getText());
        AppSettings.saveSetting("db", tfDb.getText());
        AppSettings.saveSetting("myposlink", tfMyposLink.getText());
        AppSettings.saveSetting("myposUser", tfMyposUser.getText());
        AppSettings.saveSetting("myposPass", tfMyposPass.getText());
        if (Features.isEnabled("simply")) {
            AppSettings.saveSetting("simplyPosUser", tfSimplyPosUser.getText());
            AppSettings.saveSetting("simplyPosPass", tfSimplyPosPass.getText());
            AppSettings.saveSetting("simplyCloudUser", tfSimplyCloudUser.getText());
            AppSettings.saveSetting("simplyCloudPass", tfSimplyCloudPass.getText());
            AppSettings.saveSetting("simplyRegisterMail", tfSimplyRegisterMail.getText());
            AppSettings.saveSetting("simplyMail1", tfSimplyMail1.getText());
            AppSettings.saveSetting("simplyMail2", tfSimplyMail2.getText());
        }
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
        AppSettings.saveSetting("signature", taSignature.getHtmlText());
        AppSettings.saveSetting("logoPath", tfLogoPath.getText()); // NEW
        if (Features.isEnabled("emblem")) {
            AppSettings.saveSetting("emblemUser", tfEmblemUser.getText());
            AppSettings.saveSetting("emblemPass", tfEmblemPass.getText());
            AppSettings.saveSetting("emblemRegisterMail", tfEmblemRegisterMail.getText());
        }
        if (Features.isEnabled("ergani")) {
            AppSettings.saveSetting("erganiRegisterMail", tfErganiRegisterMail.getText());
        }

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

        // Save the currently selected email template
        String selectedTemplate = templateComboBox.getValue();
        if (selectedTemplate != null && !selectedTemplate.isEmpty()) {
            AppSettings.saveSetting("email.template.subject." + selectedTemplate, templateSubjectField.getText());
            AppSettings.saveSetting("email.template.body." + selectedTemplate, templateBodyEditor.getHtmlText());
        }

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
            notifications.showInformation();
        });
    }

    public void syncClick() {
        DBHelper.getMegasoftDao().syncMegasoft();
    }

    public void deactivateCustomers() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
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
            taSignature.setHtmlText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
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
        // Check for double click
        if (event.getClickCount() == 2) {
            openNotesDialog(taSignature.getHtmlText());
        }
    }

    @FXML
    private void handleDatabaseSetup() {
        String dbName = tfDb.getText();
        if (dbName == null || dbName.trim().isEmpty()) {
            AlertDialogHelper.showDialog("Σφάλμα", "Το όνομα της βάσης δεδομένων δεν μπορεί να είναι κενό.", "", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (DatabaseSetup.checkDatabaseExists(dbName)) {
                AlertDialogHelper.showDialog("Ενημέρωση", "Η βάση δεδομένων '" + dbName + "' υπάρχει ήδη.", "", Alert.AlertType.INFORMATION);
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Επιβεβαίωση Δημιουργίας");
                alert.setHeaderText("Η βάση δεδομένων '" + dbName + "' δεν υπάρχει.");
                alert.setContentText("Θέλετε να τη δημιουργήσετε;");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    DatabaseSetup.runSetupScript(dbName);
                    AlertDialogHelper.showDialog("Επιτυχία", "Η βάση δεδομένων δημιουργήθηκε με επιτυχία.", "", Alert.AlertType.INFORMATION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά τον έλεγχο ή τη δημιουργία της βάσης δεδομένων.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
