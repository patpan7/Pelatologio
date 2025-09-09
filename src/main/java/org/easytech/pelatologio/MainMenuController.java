package org.easytech.pelatologio;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
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
    public Button btnSubs, btnDashboard, btnAccountants, btnSuppliers, btnOffers, btnOrders, btnPartners, btnCallHistory, btnPartnerEarnings, btnCommissions, btnEdpsManagement;

    @FXML
    private StackPane stackPane;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab mainTab;
    @FXML
    private Label vesrion, lbAppUser, lbTasks, lbAppointments, lbSimply;
    @FXML
    private Label lbMyposTotal, lbMyposVerified, lbMyposUnverified, lbMyposActive, lbMyposBlocked, lbMyposClosed;
    @FXML
    private Label lblForOrder, lblWaitOrders, lblForDelivery;
    @FXML
    private ListView<Order> ordersList, pendingOrdersList, deliveryOrdersList;
    @FXML
    private Button btnCustomers, btnMyPOS, btnMyposAccounts, btnTasks, btnCalendar, btnD11, btnMyDataStatus, btnItems, btnDevices, btnSettings, btnSimplyStatus;
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

        if (!Features.isEnabled("suppliers")) {
            btnSuppliers.setVisible(false);
            btnSuppliers.setManaged(false);
        }

        if (!Features.isEnabled("tasks")) {
            btnTasks.setVisible(false);
            btnTasks.setManaged(false);
            lbTasks.setVisible(false);
            lbTasks.setManaged(false);
            lbAppointments.setVisible(false);
            lbAppointments.setManaged(false);
            btnCalendar.setVisible(false);
            btnCalendar.setManaged(false);
        }

        if (!Features.isEnabled("orders")) {
            btnOrders.setVisible(false);
            btnOrders.setManaged(false);
            lblForOrder.setVisible(false);
            lblForOrder.setManaged(false);
            ordersList.setVisible(false);
            ordersList.setManaged(false);
            lblWaitOrders.setVisible(false);
            lblWaitOrders.setManaged(false);
            pendingOrdersList.setVisible(false);
            pendingOrdersList.setManaged(false);
            lblForDelivery.setVisible(false);
            lblForDelivery.setManaged(false);
            deliveryOrdersList.setVisible(false);
            deliveryOrdersList.setManaged(false);
        }

        if (!Features.isEnabled("mypos")) {
            btnMyPOS.setVisible(false);
            btnMyPOS.setManaged(false);
            btnMyposAccounts.setVisible(false);
            btnMyposAccounts.setManaged(false);
            lbMyposTotal.setVisible(false);
            lbMyposTotal.setManaged(false);
            lbMyposVerified.setVisible(false);
            lbMyposVerified.setManaged(false);
            lbMyposUnverified.setVisible(false);
            lbMyposUnverified.setManaged(false);
            lbMyposActive.setVisible(false);
            lbMyposActive.setManaged(false);
            lbMyposBlocked.setVisible(false);
            lbMyposBlocked.setManaged(false);
            lbMyposClosed.setVisible(false);
            lbMyposClosed.setManaged(false);
        }

        if (!Features.isEnabled("devices")) {
            btnDevices.setVisible(false);
            btnDevices.setManaged(false);
            btnItems.setVisible(false);
            btnItems.setManaged(false);
        }

        if (!Features.isEnabled("subs")) {
            btnSubs.setVisible(false);
            btnSubs.setManaged(false);
        }

        if (!Features.isEnabled("d11")) {
            btnD11.setVisible(false);
            btnD11.setManaged(false);
        }

        if (!Features.isEnabled("simply")) {
            btnSimplyStatus.setVisible(false);
            lbSimply.setManaged(false);
        }

        if (!Features.isEnabled("calls")) {
            btnCallHistory.setVisible(false);
            btnCallHistory.setManaged(false);
            btnSimulateCall.setVisible(false);
            btnSimulateCall.setManaged(false);
        }

        if (!Features.isEnabled("partners")) {
            btnPartners.setVisible(false);
            btnPartners.setManaged(false);
            btnPartnerEarnings.setVisible(false);
            btnPartnerEarnings.setManaged(false);
            btnCommissions.setVisible(false);
            btnCommissions.setManaged(false);
        }


        if (!Features.isEnabled("offers")) {
            btnOffers.setVisible(false);
            btnOffers.setManaged(false);
        }

        if (!Features.isEnabled("edps")) {
            btnEdpsManagement.setVisible(false);
            btnEdpsManagement.setManaged(false);
        }
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

            // Add key combination for feature management
            final KeyCombination keyCombination = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
            stage.getScene().getAccelerators().put(keyCombination, this::openFeatureManagement);
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
            controller.setPopupPosition(PopupPosition.valueOf(AppSettings.loadSetting("callerPopupPosition")));
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
                final String tasksCount = Features.isEnabled("tasks") ? String.valueOf(DBHelper.getTaskDao().getTasksCount()) : "N/A";
                final String appointmentsCount = String.valueOf(DBHelper.getTaskDao().getAppointmentsCount());
                final String simplyCount = Features.isEnabled("simply") ? String.valueOf(DBHelper.getLoginDao().getLoginsCount(2)) : "N/A";

                // myPOS Stats
                if (Features.isEnabled("mypos")) {
                    final int myposTotal = DBHelper.getCustomerMyPosDetailsDao().getTotalCount();
                    final int myposVerified = DBHelper.getCustomerMyPosDetailsDao().countByVerificationStatus("Verified");
                    final int myposUnverified = DBHelper.getCustomerMyPosDetailsDao().countByVerificationStatus("Unverified");
                    final int myposActive = DBHelper.getCustomerMyPosDetailsDao().countByAccountStatus("Active");
                    final int myposBlocked = DBHelper.getCustomerMyPosDetailsDao().countByAccountStatus("Blocked");
                    final int myposClosed = DBHelper.getCustomerMyPosDetailsDao().countByAccountStatus("Closed");

                    Platform.runLater(() -> {
                        // Update myPOS Labels
                        lbMyposTotal.setText("Σύνολο: " + myposTotal);
                        lbMyposVerified.setText("Verified: " + myposVerified);
                        lbMyposUnverified.setText("Unverified: " + myposUnverified);
                        lbMyposActive.setText("Active: " + myposActive);
                        lbMyposBlocked.setText("Blocked: " + myposBlocked);
                        lbMyposClosed.setText("Closed: " + myposClosed);
                    });
                } else {
                    Platform.runLater(() -> {
                        lbMyposTotal.setText("Σύνολο: N/A");
                        lbMyposVerified.setText("Verified: N/A");
                        lbMyposUnverified.setText("Unverified: N/A");
                        lbMyposActive.setText("Active: N/A");
                        lbMyposBlocked.setText("Blocked: N/A");
                        lbMyposClosed.setText("Closed: N/A");
                    });
                }

                final List<Order> pendingOrders = DBHelper.getOrderDao().getPendingOrders();
                final List<Order> unreceivedOrders = DBHelper.getOrderDao().getUnreceivedOrders();
                final List<Order> undeliveredOrders = DBHelper.getOrderDao().getUndeliveredOrders();

                Platform.runLater(() -> {
                    ordersList.getItems().setAll(pendingOrders);
                    pendingOrdersList.getItems().setAll(unreceivedOrders);
                    deliveryOrdersList.getItems().setAll(undeliveredOrders);
                    lbTasks.setText("Εκκρεμείς εργασίες: " + tasksCount);
                    lbAppointments.setText("Ραντεβού ημέρας: " + appointmentsCount);
                    lbSimply.setText("Πελάτες Simply: " + simplyCount);
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
        if (Features.isEnabled("mypos")) {
            setTooltip(btnMyPOS, "1)Είσοδος στο DAS της myPOS\n2)Έλεγχος κατάστασης myPOS");
        }
        if (Features.isEnabled("simply")) {
            setTooltip(btnSimplyStatus, "1)Είσοδος στο Simply Status Page\n2)Άνοιγμα Simply Status Tab");
        }
        if (Features.isEnabled("tasks")) {
            setTooltip(btnTasks, "Διαχείριση εργασιών");
        }
        setTooltip(btnCalendar, "Διαχείριση ραντεβού");
        setTooltip(btnD11, "Καταχώρηση Δ11");
        setTooltip(btnMyDataStatus, "Έλεγχος κατάστασης myData");
        setTooltip(btnItems, "Διαχείριση ειδών");
        if (Features.isEnabled("devices")) {
            setTooltip(btnDevices, "Διαχείριση συσκευών");
        }
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

        SuppliersViewController suppliersViewController = fxmlLoader.getController();
        suppliersViewController.setMainTabPane(mainTabPane);

        Tab newTab = new Tab("Προμηθευτές");
        newTab.setContent(suppliersContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void tasksClick(ActionEvent event) throws IOException {
        if (Features.isEnabled("tasks")) {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Εργασίες")) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tasksListView.fxml"));
            Parent tasksContent = fxmlLoader.load();

            Tab newTab = new Tab("Εργασίες");
            newTab.setContent(tasksContent);

            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab);
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module Εργασίες είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }
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
        if (Features.isEnabled("offers")) {
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
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module Προσφορές είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }
    }

    public void myposdasClick(MouseEvent event) {
        if (Features.isEnabled("mypos")) {
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
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module myPOS είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }
    }

    public void handleMyposAccountsClick(MouseEvent event) throws IOException {
        if (Features.isEnabled("mypos")) {
            if (event.getButton() == MouseButton.SECONDARY) {
                try {
                    LoginAutomator loginAutomation = new LoginAutomator(true);
                    loginAutomation.openPage("https://status.mypos.com/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                for (Tab tab : mainTabPane.getTabs()) {
                    if (tab.getText().equals("MyPOS")) {
                        mainTabPane.getSelectionModel().select(tab);
                        return;
                    }
                }
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("myposAccountsView.fxml"));
                Parent myposContent = fxmlLoader.load();


                MyposAccountsController myposAccountsController = fxmlLoader.getController();
                myposAccountsController.setMainTabPane(mainTabPane);
                myposAccountsController.setOpenCustomerCallback(this::openCustomerDetailsTabSimple);
                Tab newTab = new Tab("MyPOS");
                newTab.setContent(myposContent);

                mainTabPane.getTabs().add(newTab);
                mainTabPane.getSelectionModel().select(newTab);
            }
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module myPOS είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
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

        SubsViewController subsController = fxmlLoader.getController();
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
        if (Features.isEnabled("simply")) {
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
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module Simply είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
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
                    if (tab.getContent().getUserData() instanceof AddCustomerController controller) {
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

    public void partnersClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Συνεργάτες")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("partnersView.fxml"));
        Parent customersContent = fxmlLoader.load();

        PartnersController partnersController = fxmlLoader.getController();
        partnersController.setMainTabPane(mainTabPane);
        partnersController.setOpenCustomerCallback(this::openCustomerDetailsTabSimple); // Set the callback

        Tab newTab = new Tab("Συνεργάτες");
        newTab.setContent(customersContent);
        newTab.setUserData(partnersController); // Store the controller here!
        newTab.setUserData(partnersController); // Store the controller here!

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void partnerEarningsClick(ActionEvent actionEvent) {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Οφειλές Συνεργατών")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("partnerEarningsView.fxml"));
            Parent earningsContent = fxmlLoader.load();

            Tab newTab = new Tab("Οφειλές Συνεργατών");
            newTab.setContent(earningsContent);

            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab);
        } catch (IOException e) {
            AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των οφειλών συνεργατών.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void commisionsClick(ActionEvent actionEvent) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Προμήθειες")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("commissionsView.fxml"));
        Parent commissionsContent = fxmlLoader.load();

        Tab newTab = new Tab("Προμήθειες");
        newTab.setContent(commissionsContent);

        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void handleEdpsManagement(ActionEvent actionEvent) {
        if (Features.isEnabled("edps")) {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Διαχείριση EDPS")) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("edpsManagementView.fxml"));
                Parent edpsManagementContent = fxmlLoader.load();

                EdpsManagementController controller = fxmlLoader.getController();
                controller.setMainTabPane(mainTabPane);
                Tab newTab = new Tab("Διαχείριση EDPS");
                newTab.setContent(edpsManagementContent);

                mainTabPane.getTabs().add(newTab);
                mainTabPane.getSelectionModel().select(newTab);
            } catch (IOException e) {
                AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα της διαχείρισης EDPS.", e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Το module EDPS είναι απενεργοποιημένο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(3))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }
    }

    private void openFeatureManagement() {
        // Create a custom dialog
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Είσοδος Διαχειριστή");
        passwordDialog.setHeaderText("Απαιτείται κωδικός πρόσβασης για τη διαχείριση των modules.");

        // Set the button types
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the password field and label
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Κωδικός");

        grid.add(new Label("Κωdικός:"), 0, 0);
        grid.add(passwordField, 1, 0);

        passwordDialog.getDialogPane().setContent(grid);

        // Request focus on the password field by default
        Platform.runLater(passwordField::requestFocus);

        // Convert the result to the password string when the login button is clicked
        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        Optional<String> result = passwordDialog.showAndWait();

        result.ifPresent(password -> {
            if (password.equals("054909468")) { // Hardcoded password
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("featureManagementDialog.fxml"));
                    DialogPane pane = loader.load();

                    Dialog<ButtonType> featureDialog = new Dialog<>();
                    featureDialog.setDialogPane(pane);
                    featureDialog.setTitle("Διαχείριση Modules");
                    featureDialog.showAndWait();

                } catch (IOException e) {
                    e.printStackTrace();
                    AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα της διαχείρισης modules.", e.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                AlertDialogHelper.showDialog("Σφάλμα", "Λανθασμένος κωδικός πρόσβασης.", "", Alert.AlertType.ERROR);
            }
        });
    }
}

