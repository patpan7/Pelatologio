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
import org.controlsfx.control.Notifications;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class MyposViewController {
    private static final String WARNING_TITLE = "Προσοχή";
    private static final String SELECT_LOGIN_MSG = "Παρακαλώ επιλέξτε ένα login.";
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
    @FXML
    private TableColumn<Logins, String> phoneColumn;
    @FXML
    private Button btnLogin, btnRegister;


    Customer customer;

    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setTooltip(btnLogin, "Είσοδος myPOS με επιλεγμένο κωδικό");
        setTooltip(btnRegister, "1) Εγγραφή πελάτη στην myPOS\n2) Αντιγραφή στοιχείων και επιλεγμένου κωδικού");

        loginList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        // Ρύθμιση στήλης username
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        // Ρύθμιση στήλης tag
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));
        // Ρύθμιση στήλης Τηλέφωνο
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

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
        DBHelper dbHelper = new DBHelper();
        loginList.addAll(dbHelper.getLogins(customerId, 1));
        if (loginTable.getItems().size() == 1)
            loginTable.getSelectionModel().select(0);
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
                    addLoginController.handleSaveLogin(event, 1);
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
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteLogin(selectedLogin.getId());

            // Διαγραφή από τη λίστα και ενημέρωση του πίνακα
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
                    new DBHelper().updateLogin(updatedLogin); // Χρήση νέου instance για thread safety
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

        LabelPrintHelper.printLoginLabel(selectedLogin, customer, "Στοιχεία myPOS");
    }


    public void handleCopy(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        String msg = "Στοιχεία εισόδου" + selectedLogin.getTag() +
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
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle("myPOS: " + customer.getName());
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

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    public void myposloginOpen(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www.mypos.com/el/login",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("email"),
                    By.id("password"),
                    By.cssSelector("button[data-testid='mypos_login_btn']")
            );
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

        }

    }

    public void myposregisterOpen(MouseEvent event) throws IOException {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard
            if (selectedLogin != null) {
                String msg = "Στοιχεία myPOS" +
                        "\nΕπωνυμία: " + customer.getName() +
                        "\nΑΦΜ: " + customer.getAfm() +
                        "\nEmail: " + selectedLogin.getUsername() +
                        "\nΚωδικός: " + selectedLogin.getPassword() +
                        "\nΚινητό: " + selectedLogin.getPhone();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(msg);  // Replace with the desired text
                clipboard.setContent(content);
                Notifications notifications = Notifications.create()
                        .title("Αντιγραφή στο clipboard")
                        .text(msg)
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();
            } else {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            }
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Left-click for regular functionality
            LoginAutomator loginAutomation = new LoginAutomator(true);
            //loginAutomation.openPage(myposRegister);
            loginAutomation.openAndFillRegistermyPOSForm(
                    AppSettings.loadSetting("myposlink"),
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    selectedLogin.getPhone(),
                    By.cssSelector("[data-testid='enroll_credentials_email']"),
                    By.cssSelector("[data-testid='enroll_credentials_password']"),
                    By.cssSelector("[data-testid='enroll_credentials_phone_number']")
            );

        }
    }    // Μέθοδος αντιγραφής κειμένου στο πρόχειρο

    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        //showAlert("Copied to Clipboard", msg);
        Notifications notifications = Notifications.create()
                .title("Αντιγραφή στο πρόχειρο")
                .text(msg)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
        notifications.showInformation();
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
            showErrorNotification(WARNING_TITLE, SELECT_LOGIN_MSG);
        }
        return selectedLogin;
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
}
