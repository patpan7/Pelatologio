package org.easytech.pelatologio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

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
    TextField tfSimplyUser;
    @FXML
    TextField tfSimplyPass;
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
    RadioButton rbBrave;

    String server;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        server = AppSettings.loadSetting("server");
        tfServer.setText(server);
        tfUser.setText(AppSettings.loadSetting("dbUser"));
        tfPass.setText(AppSettings.loadSetting("dbPass"));
        tfMyposLink.setText(AppSettings.loadSetting("myposlink"));
        tfMyposUser.setText(AppSettings.loadSetting("myposUser"));
        tfMyposPass.setText(AppSettings.loadSetting("myposPass"));
        tfSimplyUser.setText(AppSettings.loadSetting("simplyUser"));
        tfSimplyPass.setText(AppSettings.loadSetting("simplyPass"));
        tfTaxisUser.setText(AppSettings.loadSetting("taxisUser"));
        tfTaxisPass.setText(AppSettings.loadSetting("taxisPass"));
        tfAfmUser.setText(AppSettings.loadSetting("afmUser"));
        tfAfmPass.setText(AppSettings.loadSetting("afmPass"));
        if (AppSettings.loadSetting("browser").equals("chrome")) {
            rbChrome.setSelected(true);
        } else if (AppSettings.loadSetting("browser").equals("firefox")) {
            rbFirefox.setSelected(true);
        } else if (AppSettings.loadSetting("browser").equals("brave")) {
            rbBrave.setSelected(true);
        }
    }


    public void saveSettings(ActionEvent event) throws IOException {

        AppSettings.saveSetting("server", tfServer.getText());
        AppSettings.saveSetting("dbUser", tfUser.getText());
        AppSettings.saveSetting("dbPass", tfPass.getText());
        AppSettings.saveSetting("myposlink", tfMyposLink.getText());
        AppSettings.saveSetting("myposUser",tfMyposUser.getText());
        AppSettings.saveSetting("myposPass", tfMyposPass.getText());
        AppSettings.saveSetting("simplyUser", tfSimplyUser.getText());
        AppSettings.saveSetting("simplyPass", tfSimplyPass.getText());
        AppSettings.saveSetting("taxisUser", tfTaxisUser.getText());
        AppSettings.saveSetting("taxisPass", tfTaxisPass.getText());
        AppSettings.saveSetting("afmUser", tfAfmUser.getText());
        AppSettings.saveSetting("afmPass", tfAfmPass.getText());
        if (rbChrome.isSelected()) {
            AppSettings.saveSetting("browser", "chrome");
        } else if (rbFirefox.isSelected()) {
            AppSettings.saveSetting("browser", "firefox");
        } else if (rbBrave.isSelected()) {
            AppSettings.saveSetting("browser", "brave");
        }
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
