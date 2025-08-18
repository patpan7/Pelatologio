package org.easytech.pelatologio;

import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.easytech.pelatologio.models.Subscription;
import org.openqa.selenium.By;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class SimplyViewController {
    private static final String WARNING_TITLE = "Προσοχή";
    private static final String SELECT_LOGIN_MSG = "Παρακαλώ επιλέξτε ένα login.";

    @FXML
    public Button btnSimplyPOS, btnSimplyCash, btnSimplyRest, btnSimplyPOSRegister, btnSimplyCloudRegister;
    @FXML
    public VBox progressBox;
    @FXML
    public JFXCheckBox cbStock, cbRegister, cbAuth, cbAccept, cbMail, cbParam, cbMydata, cbDelivered, cbPaid;
    @FXML
    private ComboBox<String> cbContractDuration;
    @FXML
    private TableView<Logins> loginTable;
    @FXML
    private TableColumn<Logins, String> usernameColumn;
    @FXML
    private TableColumn<Logins, String> passwordColumn;
    @FXML
    private TableColumn<Logins, String> tagColumn;

    private Customer customer;
    private ObservableList<Logins> loginList;
    private boolean isInitializing = false;

    @FXML
    public void initialize() {
        isInitializing = true;

        setupTooltips();
        setupTable();
        setupCheckboxListeners();
        setupBidirectionalBinding();

        isInitializing = false;
    }

    private void setupTooltips() {
        setTooltip(btnSimplyPOS, "1) Είσοδος στο Simply POS με επιλεγμένο κωδικό\n2) Αντιγραφή στοιχείων για επιλεγμένο κωδικό");
        setTooltip(btnSimplyCash, "1) Είσοδος στο Simply Cash με επιλεγμένο κωδικό\n2α) Αποστολή στοιχείων για επιλεγμένο κωδικό σε Simply \n2β) Αντιγραφή στοιχείων για επιλεγμένου κωδικού");
        setTooltip(btnSimplyRest, "1) Είσοδος στο Simply Rest με επιλεγμένο κωδικό\n2α) Αποστολή στοιχείων για επιλεγμένο κωδικό σε Simply \n2β) Αντιγραφή στοιχείων για επιλεγμένου κωδικού");
        setTooltip(btnSimplyPOSRegister, "Εγγραφή στο Simply POS");
        setTooltip(btnSimplyCloudRegister, "Εγγραφή Simply Cash/Rest");
    }

    private void setupTable() {
        progressBox.setVisible(false);
        loginList = FXCollections.observableArrayList();

        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));

        loginTable.setItems(loginList);

        loginTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditLogin(null);
            }
            if (event.getClickCount() == 1) {
                updateUIWithSelectedLogin();
            }
        });
    }

    private void setupCheckboxListeners() {
        Map<JFXCheckBox, String> checkboxMap = Map.of(
                cbStock, "stock",
                cbRegister, "register",
                cbAuth, "auth",
                cbAccept, "accept",
                cbMail, "mail",
                cbParam, "param",
                cbMydata, "mydata",
                cbDelivered, "delivered",
                cbPaid, "paid"
        );

        checkboxMap.forEach((checkbox, columnName) -> {
            checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (isInitializing) return;

                Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
                if (selectedLogin != null) {
                    DBHelper.getSimplyStatusDao().updateSimplyStatus(selectedLogin.getId(), columnName, newVal);

                    // Special handling for register checkbox
                    if (checkbox == cbRegister && newVal && !isInitializing) {
                        registercloudOpen(new ActionEvent());
                    } // Special handling for mail checkbox
                    else if (checkbox == cbMail && newVal && !isInitializing) {
                        handleSendEmailForSelectedLogin();
                    }
                }
            });
        });

        cbContractDuration.setOnAction(event -> {
            Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
            String selectedYear = cbContractDuration.getValue();
            if (selectedLogin != null && selectedYear != null && !selectedYear.isEmpty()) {
                DBHelper.getSimplyStatusDao().updateSimplyStatusYears(selectedLogin.getId(), selectedYear);
            }
        });
    }

    private void handleSendEmailForSelectedLogin() {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) return;

        if (selectedLogin.getTag().contains("Cash")) {
            sendSimplyServiceEmail("Cash", selectedLogin);
        } else if (selectedLogin.getTag().contains("Rest")) {
            sendSimplyServiceEmail("Rest", selectedLogin);
        } else {
            sendSimplyPosEmail(selectedLogin);
        }
    }

    private void sendSimplyServiceEmail(String serviceType, Logins login) {
        String subject = "Νέος Πελάτης Simply " + serviceType;
        String msg = "<b>Νέος Πελάτης Simply " + serviceType + "</b>" +
                "<br><b>Επωνυμία:</b> " + customer.getName() +
                "<br><b>ΑΦΜ:</b> " + customer.getAfm() +
                "<br><b>E-mail:</b> " + login.getUsername() +
                "<br><b>Κωδικός:</b> " + login.getPassword() +
                "<br><b>Κινητό:</b> " + customer.getMobile() +
                "<br>Έχει κάνει αποδοχή σύμβασης και εξουσιοδότηση" +
                "<br>";
        sendEmail(subject, msg);
    }

    private void sendSimplyPosEmail(Logins login) {
        String subject = "Νέος Πελάτης Simply POS";
        String msg = "Νέος Πελάτης Simply POS" +
                "\nΕπωνυμία: " + customer.getName() +
                "\nΑΦΜ: " + customer.getAfm() +
                "\nEmail: " + login.getUsername() +
                "\nΚωδικός: " + login.getPassword() +
                "\nΚινητό: " + customer.getMobile() +
                "\n";
        sendEmail(subject, msg);
    }

    private void setupBidirectionalBinding() {
        // Bidirectional binding between register checkbox and register button
        btnSimplyCloudRegister.setOnAction(event -> {
            if (!isInitializing) {
                cbRegister.setSelected(true);
            }
        });
    }

    private void updateUIWithSelectedLogin() {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin != null) {
            progressBox.setVisible(selectedLogin.getTag().contains("Cash") || selectedLogin.getTag().contains("Rest"));

            isInitializing = true;
            try {
                cbStock.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "stock"));
                cbRegister.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "register"));
                cbAuth.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "auth"));
                cbAccept.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "accept"));
                cbMail.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "mail"));
                cbParam.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "param"));
                cbMydata.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "mydata"));
                cbDelivered.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "delivered"));
                cbPaid.setSelected(DBHelper.getSimplyStatusDao().getSimpyStatus(selectedLogin.getId(), "paid"));
                cbContractDuration.setValue(String.valueOf(DBHelper.getSimplyStatusDao().getSimplyYears(selectedLogin.getId())));
            } finally {
                isInitializing = false;
            }
        }
    }

    public void loadLoginsForCustomer(int customerId) {
        loginList.clear();
        loginList.addAll(DBHelper.getLoginDao().getLogins(customerId, 2));

        if (!loginList.isEmpty()) {
            loginTable.getSelectionModel().select(0);
            updateUIWithSelectedLogin();
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadLoginsForCustomer(customer.getCode());
    }

    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
            DialogPane dialogPane = loader.load();

            AddLoginController addLoginController = loader.getController();
            addLoginController.setCustomer(customer);
            addLoginController.setUsername(customer.getEmail());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέου Login");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, e -> {
                if (!addLoginController.validateInputs()) {
                    e.consume();
                }
            });

            dialog.setOnHidden(e -> {
                loadLoginsForCustomer(customer.getCode());
            });

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    addLoginController.handleSaveLogin(event, 2);
                }
                return null;
            });

            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα",
                    "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο login;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper.getLoginDao().deleteLogin(selectedLogin.getId());
            loginTable.getItems().remove(selectedLogin);
        }
    }

    public void handleEditLogin(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editLogin.fxml"));
            DialogPane dialogPane = loader.load();

            EditLoginController editController = loader.getController();
            editController.setLogin(selectedLogin);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Login");

            // Προσθήκη των παρακάτω 2 γραμμών
            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Αλλαγή σε show() και χρήση setOnHidden
            dialog.show();

            // Μετακίνηση της λογικής στο OnHidden
            dialog.setOnHidden(e -> {
                ButtonType result = dialog.getResult();
                if (result != null && result == ButtonType.OK) {
                    Logins updatedLogin = editController.getUpdatedLogin();
                    new DBHelper().getLoginDao().updateLogin(updatedLogin); // Χρήση νέου instance για thread safety
                    Platform.runLater(() -> loginTable.refresh());
                }
            });

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleLabel(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        LabelPrintHelper.printLoginLabel(selectedLogin, customer, "Στοιχεία Simply " + selectedLogin.getTag());
    }

    public void handleCopy(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        String msg = "Στοιχεία εισόδου " + selectedLogin.getTag() +
                "\nΕπωνυμία: " + customer.getName() +
                "\nΑΦΜ: " + customer.getAfm() +
                "\nEmail: " + selectedLogin.getUsername() +
                "\nΚωδικός: " + selectedLogin.getPassword() +
                "\nΚινητό: " + customer.getMobile() +
                "\n";
        copyTextToClipboard(msg);
    }

    public void handleAddTask(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");

            AddTaskController controller = loader.getController();
            controller.setTaskTitle("Simply " + selectedLogin.getTag() + ": " + customer.getName());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.handleSaveTask()) {
                    event.consume();
                }
            });
            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleAddSub(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");

            AddSubController controller = loader.getController();
            controller.setSubTitle("Simply " + selectedLogin.getTag());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());
            controller.setNote(selectedLogin.getUsername());
            controller.lock();

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!controller.handleSaveSub()) {
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void simplyposOpen(MouseEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        if (event.getButton() == MouseButton.SECONDARY) {
            String msg = "Νέος Πελάτης Simply POS" +
                    "\nΕπωνυμία: " + customer.getName() +
                    "\nΑΦΜ: " + customer.getAfm() +
                    "\nEmail: " + selectedLogin.getUsername() +
                    "\nΚωδικός: " + selectedLogin.getPassword() +
                    "\nΚινητό: " + customer.getMobile() +
                    "\n";
            copyTextToClipboard(msg);
            showInfoNotification("Αντιγραφή", "Οι πληροφορίες έχουν αντιγραφεί στο πρόχειρο.");
        } else {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm(
                        "https://app.simplypos.com/Account/Login",
                        selectedLogin.getUsername(),
                        selectedLogin.getPassword(),
                        By.id("keyboard"),
                        By.id("Password"),
                        By.id("btnSubmit")
                );
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα Simply POS.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void simplycashOpen(MouseEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        if (event.getButton() == MouseButton.SECONDARY) {
            handleSimplyServiceOptions("Cash", selectedLogin);
        } else {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm(
                        "https://app.simplycloud.gr/",
                        selectedLogin.getUsername(),
                        selectedLogin.getPassword(),
                        By.id("keyboard"),
                        By.id("Password"),
                        By.id("btnSubmit")
                );
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα Simply Cash.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void simplyrestOpen(MouseEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        if (event.getButton() == MouseButton.SECONDARY) {
            handleSimplyServiceOptions("Rest", selectedLogin);
        } else {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm(
                        "https://rest.simplypos.com/",
                        selectedLogin.getUsername(),
                        selectedLogin.getPassword(),
                        By.name("Email"),
                        By.name("Password"),
                        By.id("kt_sign_in_submit")
                );
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα Simply Rest.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    private void handleSimplyServiceOptions(String serviceName, Logins selectedLogin) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιλογή Ενέργειας");
        alert.setHeaderText("Επιλέξτε μια ενέργεια");
        alert.setContentText("Θέλετε να αποστείλετε email ή να αντιγράψετε;");

        ButtonType buttonEmail = new ButtonType("Αποστολή Εγγραφής");
        ButtonType buttonCopy = new ButtonType("Αντιγραφή");
        ButtonType buttonRenew = new ButtonType("Αποστολή Ανανέωσης");
        ButtonType buttonCancel = new ButtonType("Ακύρωση", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(buttonEmail, buttonCopy, buttonRenew, buttonCancel);

        alert.showAndWait().ifPresent(choice -> {
            if (choice == buttonEmail) {
                handleSendEmailForSelectedLogin();
                cbMail.setSelected(true);
            } else if (choice == buttonCopy) {
                String msg = "Νέος Πελάτης Simply " + serviceName +
                        "\nΕπωνυμία: " + customer.getName() +
                        "\nΑΦΜ: " + customer.getAfm() +
                        "\nE-mail: " + selectedLogin.getUsername() +
                        "\nΚωδικός: " + selectedLogin.getPassword() +
                        "\nΚινητό: " + customer.getMobile() +
                        "\nΈχει κάνει αποδοχή σύμβασης και εξουσιοδότηση\n";
                copyTextToClipboard(msg);
            } else if (choice == buttonRenew) {
                String msg = "<b>Ανανέωση Πελάτη Simply " + serviceName + "</b>" +
                        "<br><b>Επωνυμία:</b> " + customer.getName() +
                        "<br><b>ΑΦΜ:</b> " + customer.getAfm() +
                        "<br><b>E-mail:</b> " + selectedLogin.getUsername() +
                        "<br><b>Κωδικός:</b> " + selectedLogin.getPassword() +
                        "<br><b>Κινητό:</b> " + customer.getMobile() +
                        "<br>";
                sendEmail("Ανανέωση Πελάτη Simply " + serviceName, msg);
                cbMail.setSelected(true);
            }
        });
    }

    public void registerposOpen(ActionEvent actionEvent) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://app.simplypos.com/Partners/NewTrial",
                    AppSettings.loadSetting("simplyPosUser"),
                    AppSettings.loadSetting("simplyPosPass"),
                    By.name("UserName"),
                    By.id("Password"),
                    By.cssSelector("button.btn.green.pull-right[type='submit']")
            );
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα εγγραφής Simply POS.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void registercloudOpen(ActionEvent actionEvent) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginRegisterCloud(
                    "https://app.simplycloud.gr/Partners",
                    AppSettings.loadSetting("simplyCloudUser"),
                    AppSettings.loadSetting("simplyCloudPass"),
                    By.name("Email"),
                    By.id("Password"),
                    By.id("btnSubmit"),
                    customer,
                    selectedLogin
            );

            cbRegister.setSelected(true);
            String price = "200";
            int category = 1;
            if (cbContractDuration.getSelectionModel().getSelectedIndex() == 0 && selectedLogin.getTag().contains("Cash"))
                price = "130";
            else if (cbContractDuration.getSelectionModel().getSelectedIndex() == 0 && selectedLogin.getTag().contains("Rest")){
                price = "260";
                category = 2;
            }
            LocalDate date = LocalDate.now().plusYears(cbContractDuration.getSelectionModel().getSelectedIndex()+1);
            Subscription newSub = new Subscription(0, selectedLogin.getTag(), date, customer.getCode(),category, price, selectedLogin.getUsername(), "Όχι");
            DBHelper.getSubscriptionDao().saveSub(newSub);
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα Simply Cloud.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private Logins checkSelectedLogin() {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showErrorNotification(WARNING_TITLE, SELECT_LOGIN_MSG);
        }
        return selectedLogin;
    }

    private void sendEmail(String subject, String msg) {
        EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"),
                AppSettings.loadSetting("smtpport"),
                AppSettings.loadSetting("email"),
                AppSettings.loadSetting("emailPass"));
        emailSender.sendEmail(AppSettings.loadSetting("simplyRegisterMail"), subject, msg);
    }

    private void copyTextToClipboard(String msg) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);
        clipboard.setContent(content);
        showInfoNotification("Αντιγραφή", "Οι πληροφορίες έχουν αντιγραφεί στο πρόχειρο.");
    }

    private void showInfoNotification(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showInformation();
    }

    private void showErrorNotification(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showError();
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}