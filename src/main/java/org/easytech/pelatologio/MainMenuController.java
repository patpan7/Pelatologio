package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
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
    private TabPane mainTabPane;
    @FXML
    private Tab mainTab;
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
        mainTab.setClosable(false); // Αποτρέπει το κλείσιμο του κεντρικού menu
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
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customersView.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Πελάτες")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customersView.fxml"));
        Parent customersContent = fxmlLoader.load();


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Πελάτες");
        newTab.setContent(customersContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }


    public void settingsClick(ActionEvent e) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Ρυθμίσεις")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
        Parent settingsContent = fxmlLoader.load();

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Ρυθμίσεις");
        newTab.setContent(settingsContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
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
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void tasksClick(ActionEvent event) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tasksView.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Εργασίες")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tasksView.fxml"));
        Parent tasksContent = fxmlLoader.load();

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Εργασίες");
        newTab.setContent(tasksContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    @FXML
    public void myDataStatusClick(ActionEvent event) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openPage("https://status.mydatacloud.gr/");
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void calendarClick(ActionEvent event) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calendarView.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        // Έλεγχος αν υπάρχει ήδη tab για το ημερολόγιο
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Ημερολόγιο")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calendarView.fxml"));
        Parent calendarContent = fxmlLoader.load();

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Ημερολόγιο");
        newTab.setContent(calendarContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    public void itemsClick(ActionEvent actionEvent) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemsView.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Είδη")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemsView.fxml"));
        Parent itemsContent = fxmlLoader.load();

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Είδη");
        newTab.setContent(itemsContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    public void devicesClick(ActionEvent actionEvent) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deviceView.fxml"));
//        root = fxmlLoader.load();
//        stackPane.getChildren().clear();
//        stackPane.getChildren().add(root);
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Συσκευές")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deviceView.fxml"));
        Parent devicesContent = fxmlLoader.load();

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Συσκευές");
        newTab.setContent(devicesContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}