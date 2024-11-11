package org.easytech.pelatologio;

import com.fasterxml.jackson.core.util.Instantiatable;
import eu.hansolo.tilesfx.tools.DoubleExponentialSmoothingForLinearSeries;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    Label vesrion;
    @FXML
    Label lbAppUser;

    public Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lbAppUser.setText("Xειριστή: "+AppSettings.loadSetting("appuser"));
    }

    public void mainMenuClick(StackPane stackPane) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    public void customersClick(ActionEvent e) throws IOException {
        DBHelper dbHelper = new DBHelper();
        dbHelper.customerUnlockAll(AppSettings.loadSetting("appuser"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customers.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }


    public void settingsClick(ActionEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    public void myposdasClick(ActionEvent event) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginFormDas("https://das.mypos.eu/en/login",
                    AppSettings.loadSetting("myposUser"),
                    AppSettings.loadSetting("myposPass"),
                    By.id("username"),
                    By.className("btn-primary"),
                    By.id("password"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void d11Click(ActionEvent event) {
        try {
        LoginAutomator loginAutomation = new LoginAutomator(true);
        loginAutomation.openAndFillLoginForm(
                "https://www1.aade.gr/taxisnet/kvs/protected/displayLiabilitiesForYear.htm?declarationType=kvsD11",
                AppSettings.loadSetting("taxisUser"),
                AppSettings.loadSetting("taxisPass"),
                By.id("username"),
                By.id("password"),
                By.name("btn_login")
        );
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public void synkClick(ActionEvent event) {
        DBHelper dbHelper = new DBHelper();
        dbHelper.syncMegasoft();
    }
}