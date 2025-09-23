package org.easytech.pelatologio;

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
import javafx.stage.Modality;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class NineposViewController implements CustomerTabController {
    private static final String WARNING_TITLE = "Προσοχή";
    private static final String SELECT_LOGIN_MSG = "Παρακαλώ επιλέξτε ένα login.";

    @FXML
    public Button btnNinepos, btnNineposRegister, btnNineposNew;
    @FXML
    private Label customerLabel;

    @FXML
    private TableView<Logins> loginTable;

    @FXML
    private TableColumn<Logins, String> usernameColumn;
    @FXML
    private TableColumn<Logins, String> passwordColumn;

    @FXML
    private TableColumn<Logins, String> tagColumn;


    Customer customer;
    private Runnable onDataSaved;

    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setTooltip(btnNinepos, "1) Είσοδος στο NinePOS με επιλεγμένο κωδικό\n2) Αντιγραφή στοιχείων για επιλεγμένο κωδικό");
        setTooltip(btnNineposNew, "Είσοδος στο νέο Backoffice NinePOS με επιλεγμένο κωδικό");
        setTooltip(btnNineposRegister, "Εγγραφή πελάτη στο Πελατολόγιο");

        loginList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        // Ρύθμιση στήλης username
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        // Ρύθμιση στήλης tag
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));

        loginTable.setItems(loginList);

        loginTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditLogin(null);
            }
        });

    }

    // Μέθοδος για τη φόρτωση των logins από τη βάση
    public void loadLoginsForCustomer(int customerId) {
        loginList.clear();
        // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
        // Προσθήκη των logins στη λίστα
        loginList.addAll(DBHelper.getLoginDao().getLogins(customerId, 7));
        if (loginTable.getItems().size() == 1)
            loginTable.getSelectionModel().select(0);
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    @Override
    public void setOnDataSaved(Runnable callback) {
        this.onDataSaved = callback;
    }

    private void notifyDataSaved() {
        if (onDataSaved != null) {
            onDataSaved.run();
        }
    }


    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
            DialogPane dialogPane = loader.load();

            AddLoginController addLoginController = loader.getController();
            addLoginController.setCustomer(customer); // Ορίζει τον πελάτη
            addLoginController.setUsername(customer.getEmail());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέου Login");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, e -> {
                if (!addLoginController.validateInputs()) {
                    e.consume(); // Εμποδίζει το κλείσιμο του dialog
                } else {
                    // Εάν οι εισαγωγές είναι έγκυρες, συνεχίστε με την αποθήκευση
                    addLoginController.handleSaveLogin(event, 7);
                    notifyDataSaved();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadLoginsForCustomer(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        // Εμφάνιση παραθύρου επιβεβαίωσης
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο login;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Διαγραφή από τη βάση
            DBHelper.getLoginDao().deleteLogin(selectedLogin.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
            loginTable.getItems().remove(selectedLogin);
            notifyDataSaved();
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
                    DBHelper.getLoginDao().updateLogin(updatedLogin); // Χρήση νέου instance για thread safety
                    Platform.runLater(() -> loginTable.refresh());
                    notifyDataSaved();
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleLabel(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        LabelPrintHelper.printLoginLabel(selectedLogin, customer, "Στοιχεία " + selectedLogin.getTag());
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
        AppUtils.copyTextToClipboard(msg);
    }

    public void handleAddTask(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle("NinePOS " + selectedLogin.getTag() + ": " + customer.getName());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveTask();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public void handleAddSub(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddSubController controller = loader.getController();
            controller.setSubTitle(selectedLogin.getTag());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());
            controller.setNote(selectedLogin.getUsername());
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveSub();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void nineposOpen(MouseEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard

            String msg = "Νέος Πελάτης NinePOS" +
                    "\nΕπωνυμία: " + customer.getName() +
                    "\nΑΦΜ: " + customer.getAfm() +
                    "\nEmail: " + selectedLogin.getUsername() +
                    "\nΚωδικός: " + selectedLogin.getPassword() +
                    "\nΚινητό: " + customer.getMobile() +
                    "\n";
            AppUtils.copyTextToClipboard(msg);
        } else {
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openAndFillLoginForm(
                        "https://www.ninepos.com/admin",
                        selectedLogin.getUsername(),
                        selectedLogin.getPassword(),
                        By.name("UserName"),
                        By.name("Password"),
                        By.cssSelector("button.btn.primary.btn-block.p-x-md")
                );
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void nineposNewOpen(MouseEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://admin.ninepos.com/login",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("Username"),
                    By.id("Password"),
                    By.xpath("//button[text()='Σύνδεση']")
            );
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void registerNineposOpen(ActionEvent actionEvent) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openPage(
                    "https://www.ninepos.com/Admin/Account/SignUp");
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    // Μέθοδος αποστολής email
    private void sendEmail(String subject, String msg) {
        // Κώδικας για αποστολή email
        EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
        emailSender.sendEmail(AppSettings.loadSetting("emblemRegisterMail"), subject, msg);
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }


    private Logins checkSelectedLogin() {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            CustomNotification.create()
                    .title(WARNING_TITLE)
                    .text(SELECT_LOGIN_MSG)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }
        return selectedLogin;
    }
}
