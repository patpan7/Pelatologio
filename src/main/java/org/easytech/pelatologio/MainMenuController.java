package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.easytech.pelatologio.dao.CustomerDao;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Order;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
    @FXML
    private ProgressIndicator loadingIndicator;

    private CustomerDao customerDao;
    private SipClient sipClient;
    @FXML
    private Button btnSimulateCall;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainTab.setClosable(false);
        setTooltips();
        lbAppUser.setText("Χειριστής: " + AppSettings.loadSetting("appuser"));
        loadAllDashboardData();

        // Initialize CustomerDao
        customerDao = DBHelper.getCustomerDao();

        // Initialize SIP Client
        try {
            sipClient = new SipClient(callerId -> {
                Platform.runLater(() -> {
                    try {
                        Customer customer = customerDao.getCustomerByPhoneNumber(callerId);
                        int customerId = (customer != null) ? customer.getCode() : -1;
                        String customerName = (customer != null) ? customer.getName() : "Άγνωστος";
                        String customerTitle = (customer != null) ? customer.getTitle() : "";
                        showCallerPopup(callerId, customerName, customerId, customerTitle);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            });
            sipClient.start();
        } catch (Exception e) {
            System.err.println("Failed to start SIP client: " + e.getMessage());
            AlertDialogHelper.showDialog("Σφάλμα SIP", "Αδυναμία σύνδεσης με τον SIP server.", e.getMessage(), Alert.AlertType.ERROR);
        }

        // Add shutdown hook to stop SIP client when application closes
        Platform.runLater(() -> {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (sipClient != null) {
                    try {
                        sipClient.stop();
                        DBHelper.getCustomerDao().customerUnlockAll(AppSettings.loadSetting("appuser"));
                        DBHelper.closeDataSource();
                        System.out.println("SIP Client stopped.");
                        System.exit(0);
                    } catch (Exception e) {
                        System.err.println("Error stopping SIP client: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void showCallerPopup(String callerNumber, String customerName, int customerId, String customerTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/caller_popup.fxml"));
            Parent root = loader.load();
            CallerPopupController controller = loader.getController();

            controller.initData(callerNumber, customerName, customerId, customerTitle);
            controller.setOpenCustomerCallback(this::openCustomerDetailsTabSimple);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Αδυναμία φόρτωσης ειδοποίησης κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void startCallLogging(CallLog callLog) {
        try {
            DBHelper.getCallLogDao().insertCallLog(callLog);
            showActiveCallBar(callLog);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα Βάσης Δεδομένων", "Αδυναμία έναρξης καταγραφής κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showActiveCallBar(CallLog callLog) {
        // Create the active call bar
        HBox activeCallBar = new HBox(10);
        activeCallBar.setAlignment(Pos.CENTER_LEFT);
        activeCallBar.setStyle("-fx-background-color: #ffb84d; -fx-padding: 5;");

        Label callInfoLabel = new Label("Σε κλήση με: " + callLog.getCallerName());
        Button stopLoggingButton = new Button("Τερματισμός Καταγραφής");
        stopLoggingButton.setOnAction(e -> stopCallLogging(callLog, activeCallBar));

        activeCallBar.getChildren().addAll(callInfoLabel, stopLoggingButton);
        stackPane.getChildren().add(activeCallBar);
        StackPane.setAlignment(activeCallBar, Pos.BOTTOM_CENTER);
    }

    private void stopCallLogging(CallLog callLog, HBox activeCallBar) {
        try {
            callLog.setEndTime(java.time.LocalDateTime.now());
            callLog.setDurationSeconds(java.time.temporal.ChronoUnit.SECONDS.between(callLog.getStartTime(), callLog.getEndTime()));
            DBHelper.getCallLogDao().updateCallLog(callLog); // You need to implement this method in CallLogDaoImpl
            stackPane.getChildren().remove(activeCallBar);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα Βάσης Δεδομένων", "Αδυναμία τερματισμού καταγραφής κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllDashboardData() {
        loadingIndicator.setVisible(true);
        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                final String tasksCount = String.valueOf(DBHelper.getTaskDao().getTasksCount());
                final String appointmentsCount = String.valueOf(DBHelper.getTaskDao().getAppointmentsCount());
                final String simplyCount = String.valueOf(DBHelper.getLoginDao().getLoginsCount(2));
                final String myposCount = String.valueOf(DBHelper.getLoginDao().getLoginsCount(1));

                Platform.runLater(() -> {
                    lbTasks.setText("Εκκρεμείς εργασίες: " + tasksCount);
                    lbAppointments.setText("Ραντεβού ημέρας: " + appointmentsCount);
                    lbSimply.setText("Πελάτες Simply: " + simplyCount);
                    lbMypos.setText("Πελάτες myPOS: " + myposCount);
                });

                final List<Order> pendingOrders = DBHelper.getOrderDao().getPendingOrders();
                final List<Order> unreceivedOrders = DBHelper.getOrderDao().getUnreceivedOrders();
                final List<Order> undeliveredOrders = DBHelper.getOrderDao().getUndeliveredOrders();

                Platform.runLater(() -> {
                    ordersList.getItems().setAll(pendingOrders);
                    pendingOrdersList.getItems().setAll(unreceivedOrders);
                    deliveryOrdersList.getItems().setAll(undeliveredOrders);
                });
                return null;
            }
        };

        loadDataTask.setOnSucceeded(event -> loadingIndicator.setVisible(false));
        loadDataTask.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            AlertDialogHelper.showDialog("Σφάλμα Φόρτωσης", "Προέκυψε σφάλμα κατά τη φόρτωση των δεδομένων.", loadDataTask.getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(loadDataTask).start();
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
        DBHelper.getCustomerDao().customerUnlockAll(AppSettings.loadSetting("appuser"));
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Πελάτες")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customersView.fxml"));
        Parent customersContent = fxmlLoader.load();

        CustomersController customersController = fxmlLoader.getController();
        customersController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Πελάτες");
        newTab.setContent(customersContent);
        newTab.setUserData(customersController); // Store the controller here!
        newTab.setUserData(customersController); // Store the controller here!

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void accauntantsClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Λογιστές")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accountantsView.fxml"));
        Parent accountantsContent = fxmlLoader.load();

        AccountantsController accountantsController = fxmlLoader.getController();
        accountantsController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Λογιστές");
        newTab.setContent(accountantsContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }


    public void suppliersClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Προμηθευτές")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("suppliersView.fxml"));
        Parent suppliersContent = fxmlLoader.load();

        SuppliersController suppliersController = fxmlLoader.getController();
        suppliersController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Προμηθευτές");
        newTab.setContent(suppliersContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void tasksClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Εργασίες")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tasksView.fxml"));
        Parent tasksContent = fxmlLoader.load();

        Tab newTab = new Tab("Εργασίες");
        newTab.setContent(tasksContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void calendarClick(ActionEvent event) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Ημερολόγιο")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("calendarView.fxml"));
        Parent calendarContent = fxmlLoader.load();

        Tab newTab = new Tab("Ημερολόγιο");
        newTab.setContent(calendarContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void offersClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Προσφορές")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("offersView.fxml"));
        Parent offerContent = fxmlLoader.load();

        OffersController offerController = fxmlLoader.getController();
        offerController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Προσφορές");
        newTab.setContent(offerContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
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
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ordersView.fxml"));
        Parent ordersContent = fxmlLoader.load();

        Tab newTab = new Tab("Παραγγελίες");
        newTab.setContent(ordersContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void itemsClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Είδη")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("itemsView.fxml"));
        Parent itemsContent = fxmlLoader.load();

        Tab newTab = new Tab("Είδη");
        newTab.setContent(itemsContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void devicesClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Συσκευές")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deviceView.fxml"));
        Parent devicesContent = fxmlLoader.load();

        Tab newTab = new Tab("Συσκευές");
        newTab.setContent(devicesContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void subsClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Συμβόλαια")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("subsView.fxml"));
        Parent subsContent = fxmlLoader.load();

        SubsController subsController = fxmlLoader.getController();
        subsController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Συμβόλαια");
        newTab.setContent(subsContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
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
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
        Parent settingsContent = fxmlLoader.load();

        Tab newTab = new Tab("Ρυθμίσεις");
        newTab.setContent(settingsContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        try {
            // Check if Dashboard tab already exists
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Dashboard")) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setMainTabPane(mainTabPane); // Pass the mainTabPane
            dashboardController.setOpenCustomerCallback(this::openCustomerDetailsTabSimple); // Pass the callback

            Tab newTab = new Tab("Dashboard");
            newTab.setContent(dashboardRoot);

            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab);

        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα του Dashboard.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void callHistoryClick(ActionEvent e) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Ιστορικό Κλήσεων")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("callLogView.fxml"));
        Parent callLogContent = fxmlLoader.load();

        Tab newTab = new Tab("Ιστορικό Κλήσεων");
        newTab.setContent(callLogContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    @FXML
    private void handleOrderSelection(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Order selectedOrder = ordersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    @FXML
    private void handlePendingOrderSelection(MouseEvent event) {

        if (event.getClickCount() == 2) {
            Order selectedOrder = pendingOrdersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    @FXML
    private void handleDeliveryOrderSelection(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Order selectedOrder = deliveryOrdersList.getSelectionModel().getSelectedItem();
            openOrderDetails(selectedOrder, event);
        }
    }

    public void handleRefreshButton() {
        loadAllDashboardData();
    }

    private void openOrderDetails(Order selectedOrder, MouseEvent event) {
        if (selectedOrder == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί παραγγελία!");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία παραγγελίας");
            AddOrderController controller = loader.getController();

            controller.setOrderForEdit(selectedOrder);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, evt -> {
                boolean success = controller.handleSaveOrder();
                if (!success) {
                    evt.consume();
                }
            });
            dialog.showAndWait();
            loadAllDashboardData();
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
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("simplyStatusView.fxml"));
            Parent simplyStatusContent = fxmlLoader.load();

            SimplyStatusController simplyStatusController = fxmlLoader.getController();
            simplyStatusController.setMainTabPane(mainTabPane);

            Tab newTab = new Tab("Simply");
            newTab.setContent(simplyStatusContent);

            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab);
        }
    }

    private void openWebPage(String url) {
        LoginAutomator loginAutomator = null;
        try {
            loginAutomator = new LoginAutomator(true);
            loginAutomator.openPage(url);
        } catch (IOException e) {
            showErrorDialog("Σφάλμα κατά το άνοιγμα.", e.getMessage());
        } finally {
            if (loginAutomator != null) {
                loginAutomator.close();
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", title, message, Alert.AlertType.ERROR));
    }

    private void openCustomerDetailsTabSimple(int customerId) {
        Customer customer = DBHelper.getCustomerDao().getCustomerByCode(customerId);
        try {
            String lockResult = DBHelper.getCustomerDao().checkCustomerLock(customerId, AppSettings.loadSetting("appuser"));
            if (!lockResult.equals("unlocked")) {
                AlertDialogHelper.showDialog("Προσοχή", lockResult, "", Alert.AlertType.ERROR);
                return;
            }

            DBHelper.getCustomerDao().customerLock(customerId, AppSettings.loadSetting("appuser"));

// Find existing tab by checking UserData for the customer's unique ID
            for (Tab tab : mainTabPane.getTabs()) {
                if (Integer.valueOf(customer.getCode()).equals(tab.getUserData())) {
                    mainTabPane.getSelectionModel().select(tab);
                    // If a specific sub-tab needs to be selected, find the controller and call the method
                    if (tab.getContent().getUserData() instanceof AddCustomerController) {
                        AddCustomerController controller = (AddCustomerController) tab.getContent().getUserData();
                    }
                    return; // Tab found and selected, exit the method
                }
            }
// If no tab found, create a new one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Parent customerForm = loader.load();
            AddCustomerController controller = loader.getController();

            String tabTitle = customer.getName().substring(0, Math.min(customer.getName().length(), 18));
            Tab customerTab = new Tab(tabTitle);
            customerTab.setUserData(customer.getCode()); // <-- Set the unique customer ID as user data
            customerTab.setContent(customerForm);
            customerForm.setUserData(controller); // Store controller for later access

            controller.setMainTabPane(mainTabPane, customerTab);
            controller.setCustomerForEdit(customer);

            mainTabPane.getTabs().add(customerTab);
            mainTabPane.getSelectionModel().select(customerTab);


            customerTab.setOnCloseRequest(event -> {
                DBHelper.getCustomerDao().customerUnlock(customer.getCode());
                if (!controller.handleTabCloseRequest()) {
                    event.consume();
                }
            });

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των στοιχείων πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    // Method to initiate an outgoing call using SIP
    public void originateCall(String phoneNumber) {
        try {
            FanvilDialer.dial(phoneNumber);
            AlertDialogHelper.showDialog("Κλήση", "Εκκίνηση κλήσης προς: " + phoneNumber, "", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            AlertDialogHelper.showDialog("Σφάλμα Κλήσης", "Αδυναμία εκκίνησης κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

//    public void originateCall(String phoneNumber) {
//        if (sipClient != null) {
//            try {
//                sipClient.makeCall(phoneNumber);
//                AlertDialogHelper.showDialog("Κλήση", "Εκκίνηση κλήσης προς: " + phoneNumber, "", Alert.AlertType.INFORMATION);
//            } catch (Exception e) {
//                AlertDialogHelper.showDialog("Σφάλμα Κλήσης", "Αδυναμία εκκίνησης κλήσης μέσω SIP.", e.getMessage(), Alert.AlertType.ERROR);
//            }
//        } else {
//            AlertDialogHelper.showDialog("Σφάλμα", "Το SIP Client δεν έχει αρχικοποιηθεί.", "", Alert.AlertType.WARNING);
//        }
//    }

    @FXML
    private void simulateCall() {
        TextInputDialog dialog = new TextInputDialog("2101234567");
        dialog.setTitle("Simulate Incoming Call");
        dialog.setHeaderText("Enter a phone number to simulate an incoming call from.");
        dialog.setContentText("Phone Number:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(phoneNumber -> {
            if (sipClient != null) {
                sipClient.simulateIncomingCall(phoneNumber);
            }
        });
    }
}

