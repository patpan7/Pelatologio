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
    TextField tfSimplyUser;
    @FXML
    TextField tfSimplyPass;

    String server;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        server = AppSettings.loadSetting("server");
        tfServer.setText(server);
        tfUser.setText(AppSettings.loadSetting("dbUser"));
        tfPass.setText(AppSettings.loadSetting("dbPass"));
        tfMyposLink.setText(AppSettings.loadSetting("myposlink"));
        tfSimplyUser.setText(AppSettings.loadSetting("simplyUser"));
        tfSimplyPass.setText(AppSettings.loadSetting("simplyPass"));
    }


    public void saveSettings(ActionEvent event) throws IOException {

        AppSettings.saveSetting("server", tfServer.getText());
        AppSettings.saveSetting("dbUser", tfUser.getText());
        AppSettings.saveSetting("dbPass", tfPass.getText());
        AppSettings.saveSetting("myposlink", tfMyposLink.getText());
        AppSettings.saveSetting("simplyUser", tfSimplyUser.getText());
        AppSettings.saveSetting("simplyPass", tfSimplyPass.getText());
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
