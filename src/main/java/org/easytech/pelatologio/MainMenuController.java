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
    private Button btnCustomers, btnMyPOS, btnTasks, btnCalendar, btnD11, btnMyDataStatus, btnItems, btnDevices, btnSettings;

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

    private void openTab(String title, String fxmlPath) throws IOException {
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = fxmlLoader.load();
        Tab newTab = new Tab(title, content);
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    public void customersClick(ActionEvent e) throws IOException {
        dbHelper.customerUnlockAll(AppSettings.loadSetting("appuser"));
        openTab("Πελάτες", "customersView.fxml");
    }

    public void accauntantsClick(ActionEvent e) throws IOException {
        openTab("Λογιστές", "accountantsView.fxml");
    }

    public void suppliersClick(ActionEvent e) throws IOException {
        openTab("Προμηθευτές", "suppliersView.fxml");
    }

    public void tasksClick(ActionEvent event) throws IOException {
        openTab("Εργασίες", "tasksView.fxml");
    }

    public void calendarClick(ActionEvent event) throws IOException {
        openTab("Ημερολόγιο", "calendarView.fxml");
    }

    public void offersClick(ActionEvent event) throws IOException {
        openTab("Προσφορές", "offersView.fxml");
    }

    public void myposdasClick(MouseEvent event) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            if (event.getButton() == MouseButton.SECONDARY) {
                loginAutomation.openPage("https://status.mypos.com/");
            } else {
                loginAutomation.openAndFillLoginFormDas("https://das.mypos.eu/en/login",
                        AppSettings.loadSetting("myposUser"),
                        AppSettings.loadSetting("myposPass"),
                        By.id("username"),
                        By.className("btn-primary"),
                        By.id("password"));
            }
        } catch (IOException e) {
            showErrorDialog("Σφάλμα κατά το άνοιγμα.", e.getMessage());
        }
    }

    public void myDataStatusClick(ActionEvent event) {
        openWebPage("https://status.mydatacloud.gr/");
    }

    public void ordersClick(ActionEvent event) throws IOException {
        openTab("Παραγγελίες", "ordersView.fxml");
    }

    public void itemsClick(ActionEvent actionEvent) throws IOException {
        openTab("Είδη", "itemsView.fxml");
    }

    public void devicesClick(ActionEvent actionEvent) throws IOException {
        openTab("Συσκευές", "deviceView.fxml");
    }

    public void subsClick(ActionEvent actionEvent) throws IOException {
        openTab("Συμβόλαια", "subsView.fxml");
    }

    public void d11Click(ActionEvent event) {
        try {
            new LoginAutomator(true).openAndFillLoginForm(
                    "https://www1.aade.gr/taxisnet/kvs/protected/displayLiabilitiesForYear.htm?declarationType=kvsD11",
                    AppSettings.loadSetting("taxisUser"),
                    AppSettings.loadSetting("taxisPass"),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login"));
        } catch (IOException e) {
            showErrorDialog("Σφάλμα κατά το άνοιγμα.", e.getMessage());
        }
    }

    public void settingsClick(ActionEvent e) throws IOException {
        openTab("Ρυθμίσεις", "settings.fxml");
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
