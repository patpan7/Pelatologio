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
    TextField tfSimplyPosUser;
    @FXML
    TextField tfSimplyPosPass;
    @FXML
    TextField tfSimplyCloudUser;
    @FXML
    TextField tfSimplyCloudPass;
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

    String server;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        browserToggleGroup = new ToggleGroup();
        rbChrome.setToggleGroup(browserToggleGroup);
        rbEdge.setToggleGroup(browserToggleGroup);
        rbFirefox.setToggleGroup(browserToggleGroup);
        server = AppSettings.loadSetting("server");
        tfServer.setText(server);
        tfUser.setText(AppSettings.loadSetting("dbUser") != null ? AppSettings.loadSetting("dbUser") : "");
        tfPass.setText(AppSettings.loadSetting("dbPass") != null ? AppSettings.loadSetting("dbPass") : "");
        tfMyposLink.setText(AppSettings.loadSetting("myposlink") != null ? AppSettings.loadSetting("myposlink") : "");
        tfMyposUser.setText(AppSettings.loadSetting("myposUser") != null ? AppSettings.loadSetting("myposUser") : "");
        tfMyposPass.setText(AppSettings.loadSetting("myposPass") != null ? AppSettings.loadSetting("myposPass") : "");
        tfSimplyPosUser.setText(AppSettings.loadSetting("simplyPosUser") != null ? AppSettings.loadSetting("simplyPosUser") : "");
        tfSimplyPosPass.setText(AppSettings.loadSetting("simplyPosPass") != null ? AppSettings.loadSetting("simplyPosPass") : "");
        tfSimplyPosUser.setText(AppSettings.loadSetting("simplyCloudUser") != null ? AppSettings.loadSetting("simplyCloudUser") : "");
        tfSimplyPosPass.setText(AppSettings.loadSetting("simplyCloudPass") != null ? AppSettings.loadSetting("simplyCloudPass") : "");
        tfTaxisUser.setText(AppSettings.loadSetting("taxisUser") != null ? AppSettings.loadSetting("taxisUser") : "");
        tfTaxisPass.setText(AppSettings.loadSetting("taxisPass") != null ? AppSettings.loadSetting("taxisPass") : "");
        tfAfmUser.setText(AppSettings.loadSetting("afmUser") != null ? AppSettings.loadSetting("afmUser") : "");
        tfAfmPass.setText(AppSettings.loadSetting("afmPass") != null ? AppSettings.loadSetting("afmPass") : "");

        if (AppSettings.loadSetting("browser").equals("chrome")) {
            rbChrome.setSelected(true);
        } else if (AppSettings.loadSetting("browser").equals("firefox")) {
            rbFirefox.setSelected(true);
        } else if (AppSettings.loadSetting("browser").equals("edge")) {
            rbEdge.setSelected(true);
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
        AppSettings.saveSetting("taxisUser", tfTaxisUser.getText());
        AppSettings.saveSetting("taxisPass", tfTaxisPass.getText());
        AppSettings.saveSetting("afmUser", tfAfmUser.getText());
        AppSettings.saveSetting("afmPass", tfAfmPass.getText());
        if (rbChrome.isSelected()) {
            AppSettings.saveSetting("browser", "chrome");
        } else if (rbFirefox.isSelected()) {
            AppSettings.saveSetting("browser", "firefox");
        } else if (rbEdge.isSelected()) {
            AppSettings.saveSetting("browser", "edge");
        }
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
