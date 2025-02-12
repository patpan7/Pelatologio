package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
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
    @FXML
    Label lbTasks;
    @FXML
    Label lbAppointments;
    @FXML
    Label lbSimply;
    @FXML
    Label lbMypos;
    @FXML
    Button btnCustomers, btnMyPOS, btnTasks, btnCalendar, btnD11, btnMyDataStatus, btnItems, btnDevices, btnSettings;


    public Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setTooltip(btnCustomers, "Διαχείριση πελατών");
        setTooltip(btnMyPOS, "1)Είσοδος στο DAS της myPOS\n2)Έλεγχος κατάστασης myPOS");
        setTooltip(btnTasks, "Διαχείριση εργασιών");
        setTooltip(btnCalendar, "Διαχείριση ραντεβού");
        setTooltip(btnD11, "Καταχώρηση Δ11");
        setTooltip(btnMyDataStatus, "Έλεγχος κατάστασης myData");
        setTooltip(btnItems, "Διαχείριση ειδών");
        setTooltip(btnDevices, "Διαχείριση συσκευών");
        Platform.runLater(() -> stackPane.requestFocus());
        lbAppUser.setText("Χειριστή: "+AppSettings.loadSetting("appuser"));
        DBHelper dbHelper = new DBHelper();
        int tasksCount = dbHelper.getTasksCount();
        lbTasks.setText("Εκκρεμής εργασίες: "+tasksCount);
        int appointmentsCount = dbHelper.getAppointmentsCount();
        lbAppointments.setText("Ραντεβού ημέρας: "+appointmentsCount);
        int simplyCount = dbHelper.getLoginsCount(2);
        lbSimply.setText("Πελάτες Simply: "+simplyCount);
        int myposCount = dbHelper.getLoginsCount(1);
        lbMypos.setText("Πελάτες myPOS: " + myposCount);
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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customersView.fxml"));
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

    public void myposdasClick(MouseEvent event) {

        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openPage("https://status.mypos.com/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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

    public void tasksClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tasksView.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    @FXML
    public void myDataStatusClick(ActionEvent event) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openPage("https://status.mydatacloud.gr/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calendarClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calendarView.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    public void itemsClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemsView.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    public void devicesClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deviceView.fxml"));
        root = fxmlLoader.load();
        stackPane.getChildren().clear();
        stackPane.getChildren().add(root);
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}