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
import org.easytech.pelatologio.accountants.AccountantsController;
import org.easytech.pelatologio.applications.SimplyStatusController;
import org.easytech.pelatologio.customers.CustomersController;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.LoginAutomator;
import org.easytech.pelatologio.models.Order;
import org.easytech.pelatologio.offers.OffersController;
import org.easytech.pelatologio.orders.AddOrderController;
import org.easytech.pelatologio.orders.OrdersListController;
import org.easytech.pelatologio.settings.AppSettings;
import org.easytech.pelatologio.subs.SubsController;
import org.easytech.pelatologio.suppliers.SuppliersController;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab mainTab;
    @FXML
    private Label vesrion, lbAppUser, lbTasks, lbAppointments, lbSimply, lbMypos;
    @FXML
    private ListView<Order> ordersList, pendingOrdersList, deliveryOrdersList;
    @FXML
    private Button btnCustomers, btnMyPOS, btnTasks, btnCalendar, btnD11, btnMyDataStatus, btnItems, btnDevices, btnSettings, btnSimplyStatus;

    private final DBHelper dbHelper = new DBHelper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainTab.setClosable(false);
        setTooltips();
        lbAppUser.setText("Χειριστής: " + AppSettings.loadSetting("appuser"));
        loadDashboardData();
        loadOrders();
    }

    private void loadDashboardData() {
        lbTasks.setText("Εκκρεμείς εργασίες: " + dbHelper.getTasksCount());
        lbAppointments.setText("Ραντεβού ημέρας: " + dbHelper.getAppointmentsCount());
        lbSimply.setText("Πελάτες Simply: " + dbHelper.getLoginsCount(2));
        lbMypos.setText("Πελάτες myPOS: " + dbHelper.getLoginsCount(1));
    }

    private void loadOrders() {
        ordersList.getItems().setAll(dbHelper.getPendingOrders());
        pendingOrdersList.getItems().setAll(dbHelper.getUnreceivedOrders());
        deliveryOrdersList.getItems().setAll(dbHelper.getUndeliveredOrders());
    }

    private void setTooltips() {
        setTooltip(btnCustomers, "Διαχείριση πελατών");
        setTooltip(btnMyPOS, "1)Είσοδος στο DAS της myPOS\n2)Έλεγχος κατάστασης myPOS");
        setTooltip(btnTasks, "Διαχείριση εργασιών");
        setTooltip(btnCalendar, "Διαχείριση ραντεβού");
        setTooltip(btnD11, "Καταχώρηση Δ11");
        setTooltip(btnMyDataStatus, "Έλεγχος κατάστασης myData");
        setTooltip(btnItems, "Διαχείριση ειδών");
        setTooltip(btnDevices, "Διαχείριση συσκευών");
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.seconds(0.3));
        button.setTooltip(tooltip);
    }


    public void customersClick(ActionEvent e) throws IOException {
        DBHelper dbHelper = new DBHelper();
        dbHelper.customerUnlockAll(AppSettings.loadSetting("appuser"));
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Πελάτες")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customersView.fxml"));
        Parent customersContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        CustomersController customersController = fxmlLoader.getController();
        customersController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση

        // Δημιουργία νέου tab
        Tab newTab = new Tab("Πελάτες");
        newTab.setContent(customersContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    public void accauntantsClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Λογιστές")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accountantsView.fxml"));
        Parent accountantsContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        AccountantsController accountantsController = fxmlLoader.getController();
        accountantsController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Λογιστές");
        newTab.setContent(accountantsContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }


    public void suppliersClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Προμηθευτές")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("suppliersView.fxml"));
        Parent suppliersContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        SuppliersController suppliersController = fxmlLoader.getController();
        suppliersController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Προμηθευτές");
        newTab.setContent(suppliersContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    public void tasksClick(ActionEvent event) throws IOException {
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

    public void calendarClick(ActionEvent event) throws IOException {
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

    public void offersClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Προσφορές")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }
        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("offersView.fxml"));
        Parent offerContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        OffersController offerController = fxmlLoader.getController();
        offerController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Προσφορές");
        newTab.setContent(offerContent);

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

    public void myDataStatusClick(ActionEvent event) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openPage("https://status.mydatacloud.gr/");
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void ordersClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Παραγγελίες")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }

        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ordersView.fxml"));
        Parent ordersContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        OrdersListController ordersListController = fxmlLoader.getController();


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Παραγγελίες");
        newTab.setContent(ordersContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
    }

    public void itemsClick(ActionEvent actionEvent) throws IOException {
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

    public void subsClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Συμβόλαια")) {
                mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                return;
            }
        }
        // Φόρτωση του FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("subsView.fxml"));
        Parent subsContent = fxmlLoader.load();

        // Περνάμε το mainTabPane στον CustomersController
        SubsController subsController = fxmlLoader.getController();
        subsController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση


        // Δημιουργία νέου tab
        Tab newTab = new Tab("Συμβόλαια");
        newTab.setContent(subsContent);

        // Προσθήκη του tab στο TabPane
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
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

    public void settingsClick(ActionEvent e) throws IOException {
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

    @FXML
    private void handleOrderSelection(MouseEvent event) {
        if (event.getClickCount() == 2) { // Διπλό κλικ για άνοιγμα παραθύρου
            Order selectedOrder = ordersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    @FXML
    private void handlePendingOrderSelection(MouseEvent event) {

        if (event.getClickCount() == 2) { // Διπλό κλικ για άνοιγμα παραθύρου
            Order selectedOrder = pendingOrdersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    @FXML
    private void handleDeliveryOrderSelection(MouseEvent event) {
        if (event.getClickCount() == 2) { // Διπλό κλικ για άνοιγμα παραθύρου
            Order selectedOrder = deliveryOrdersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    public void handleRefreshButton() {
        loadDashboardData();
        loadOrders();
    }

    private void openOrderDetails(Order selectedOrder, MouseEvent event) {
        if (selectedOrder == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί παραγγελία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία παραγγελίας");
            AddOrderController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setOrderForEdit(selectedOrder);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, evt -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOrder();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadOrders();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void simplyClick(MouseEvent event) throws IOException {

        if (event.getButton() == MouseButton.SECONDARY) {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openPage("https://simplypos.statuspage.io/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Simply")) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("simplyStatusView.fxml"));
            Parent simplyStatusContent = fxmlLoader.load();

            // Περνάμε το mainTabPane στον CustomersController
            SimplyStatusController simplyStatusController = fxmlLoader.getController();
            simplyStatusController.setMainTabPane(mainTabPane);  // Περίπου εδώ γίνεται η μετάβαση


            // Δημιουργία νέου tab
            Tab newTab = new Tab("Simply");
            newTab.setContent(simplyStatusContent);

            // Προσθήκη του tab στο TabPane
            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab); // Επιλογή του νέου tab
        }
    }

    private void openWebPage(String url) {
        try {
            new LoginAutomator(true).openPage(url);
        } catch (IOException e) {
            showErrorDialog("Σφάλμα κατά το άνοιγμα.", e.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", title, message, Alert.AlertType.ERROR));
    }
}
