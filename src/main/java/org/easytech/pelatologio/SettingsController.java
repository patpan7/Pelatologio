package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    TextField tfServer;
    @FXML
    TextField tfUser;
    @FXML
    TextField tfPass;
    @FXML
    TextField tfMyposLink;
    @FXML
    TextField tfMyposUser;
    @FXML
    TextField tfMyposPass;
    @FXML
    TextField tfSimplyPosUser;
    @FXML
    TextField tfSimplyPosPass;
    @FXML
    TextField tfSimplyCloudUser;
    @FXML
    TextField tfSimplyCloudPass;
    @FXML
    TextField tfSimplyRegisterMail;
    @FXML
    TextField tfSimplyMail1;
    @FXML
    TextField tfSimplyMail2;
    @FXML
    TextField tfEmblemUser;
    @FXML
    TextField tfEmblemPass;
    @FXML
    TextField tfEmblemRegisterMail;
    @FXML
    TextField tfTaxisUser;
    @FXML
    TextField tfTaxisPass;
    @FXML
    TextField tfAfmUser;
    @FXML
    TextField tfAfmPass;
    @FXML
    RadioButton rbChrome;
    @FXML
    RadioButton rbFirefox;
    @FXML
    RadioButton rbEdge;
    @FXML
    ToggleGroup browserToggleGroup;
    @FXML
    TextField tfAppUser;
    @FXML
    TextField tfDataFolder;
    @FXML
    TextField tfEmail;
    @FXML
    TextField tfEmailPassKey;
    @FXML
    TextField tfSMTP;
    @FXML
    TextField tfSMTPPort;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        browserToggleGroup = new ToggleGroup();
        rbChrome.setToggleGroup(browserToggleGroup);
        rbEdge.setToggleGroup(browserToggleGroup);
        rbFirefox.setToggleGroup(browserToggleGroup);
        tfServer.setText(AppSettings.loadSetting("server") != null ? AppSettings.loadSetting("server") : "");
        tfUser.setText(AppSettings.loadSetting("dbUser") != null ? AppSettings.loadSetting("dbUser") : "");
        tfPass.setText(AppSettings.loadSetting("dbPass") != null ? AppSettings.loadSetting("dbPass") : "");
        tfMyposLink.setText(AppSettings.loadSetting("myposlink") != null ? AppSettings.loadSetting("myposlink") : "");
        tfMyposUser.setText(AppSettings.loadSetting("myposUser") != null ? AppSettings.loadSetting("myposUser") : "");
        tfMyposPass.setText(AppSettings.loadSetting("myposPass") != null ? AppSettings.loadSetting("myposPass") : "");
        tfSimplyPosUser.setText(AppSettings.loadSetting("simplyPosUser") != null ? AppSettings.loadSetting("simplyPosUser") : "");
        tfSimplyPosPass.setText(AppSettings.loadSetting("simplyPosPass") != null ? AppSettings.loadSetting("simplyPosPass") : "");
        tfSimplyCloudUser.setText(AppSettings.loadSetting("simplyCloudUser") != null ? AppSettings.loadSetting("simplyCloudUser") : "");
        tfSimplyCloudPass.setText(AppSettings.loadSetting("simplyCloudPass") != null ? AppSettings.loadSetting("simplyCloudPass") : "");
        tfSimplyRegisterMail.setText(AppSettings.loadSetting("simplyRegisterMail") != null ? AppSettings.loadSetting("simplyRegisterMail") : "");
        tfSimplyMail1.setText(AppSettings.loadSetting("simplyMail1") != null ? AppSettings.loadSetting("simplyMail1") : "");
        tfSimplyMail2.setText(AppSettings.loadSetting("simplyMail2") != null ? AppSettings.loadSetting("simplyMail2") : "");
        tfTaxisUser.setText(AppSettings.loadSetting("taxisUser") != null ? AppSettings.loadSetting("taxisUser") : "");
        tfTaxisPass.setText(AppSettings.loadSetting("taxisPass") != null ? AppSettings.loadSetting("taxisPass") : "");
        tfAfmUser.setText(AppSettings.loadSetting("afmUser") != null ? AppSettings.loadSetting("afmUser") : "");
        tfAfmPass.setText(AppSettings.loadSetting("afmPass") != null ? AppSettings.loadSetting("afmPass") : "");
        tfAppUser.setText(AppSettings.loadSetting("appuser") != null ? AppSettings.loadSetting("appuser") : "");
        tfDataFolder.setText(AppSettings.loadSetting("datafolder") != null ? AppSettings.loadSetting("datafolder") : "");
        tfEmail.setText(AppSettings.loadSetting("email") != null ? AppSettings.loadSetting("email") : "");
        tfEmailPassKey.setText(AppSettings.loadSetting("emailPass") != null ? AppSettings.loadSetting("emailPass") : "");
        tfSMTP.setText(AppSettings.loadSetting("smtp") != null ? AppSettings.loadSetting("smtp") : "");
        tfSMTPPort.setText(AppSettings.loadSetting("smtpport") != null ? AppSettings.loadSetting("smtpport") : "");

        tfEmblemUser.setText(AppSettings.loadSetting("emblemUser") != null ? AppSettings.loadSetting("emblemUser") : "");
        tfEmblemPass.setText(AppSettings.loadSetting("emblemPass") != null ? AppSettings.loadSetting("emblemPass") : "");
        tfEmblemRegisterMail.setText(AppSettings.loadSetting("emblemRegisterMail") != null ? AppSettings.loadSetting("emblemRegisterMail") : "");

        String browser = AppSettings.loadSetting("browser") != null ? AppSettings.loadSetting("browser") : "";
        switch (browser) {
            case "chrome" -> rbChrome.setSelected(true);
            case "firefox" -> rbFirefox.setSelected(true);
            case "edge" -> rbEdge.setSelected(true);
        }
    }


    public void saveSettings(ActionEvent event) throws IOException {

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
        AppSettings.saveSetting("emblemUser", tfEmblemUser.getText());
        AppSettings.saveSetting("emblemPass", tfEmblemPass.getText());
        AppSettings.saveSetting("emblemRegisterMail", tfEmblemRegisterMail.getText());
        if (rbChrome.isSelected()) {
            AppSettings.saveSetting("browser", "chrome");
        } else if (rbFirefox.isSelected()) {
            AppSettings.saveSetting("browser", "firefox");
        } else if (rbEdge.isSelected()) {
            AppSettings.saveSetting("browser", "edge");
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
        dbHelper.syncMegasoft();
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
