package org.easytech.pelatologio;

import com.microsoft.playwright.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.controlsfx.control.Notifications;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class ErganiViewController {
    private static final String WARNING_TITLE = "Προσοχή";
    private static final String SELECT_LOGIN_MSG = "Παρακαλώ επιλέξτε ένα login.";

    @FXML
    public Button btnErganiRegister;
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

    Customer customer;

    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setTooltip(btnErganiRegister, "Εγγραφή πελάτη στο Ergani");

        loginList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        // Ρύθμιση στήλης username
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        // Ρύθμιση στήλης tag
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));
        // Ρύθμιση στήλης phone
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

        loginTable.setItems(loginList);

        loginTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2){
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
        loginList.addAll(dbHelper.getLogins(customerId,5));
        if (loginTable.getItems().size() == 1)
            loginTable.getSelectionModel().select(0);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
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
                }
                else {
                    // Εάν οι εισαγωγές είναι έγκυρες, συνεχίστε με την αποθήκευση
                    addLoginController.handleSaveLogin(event,5);
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

        LabelPrintHelper.printLoginLabel(selectedLogin,customer,"Στοιχεία "+selectedLogin.getTag());
    }

    public void handleCopy(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        String msg ="Στοιχεία εισόδου " + selectedLogin.getTag() +
                "\nΕπωνυμία: "+customer.getName()+
                "\nΑΦΜ: "+customer.getAfm()+
                "\nEmail: "+selectedLogin.getUsername()+
                "\nΚωδικός: "+selectedLogin.getPassword()+
                "\nΚινητό: "+customer.getMobile()+
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
            controller.setTaskTitle(selectedLogin.getTag() +": "+ customer.getName());
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

    public void registerErgani(ActionEvent actionEvent) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        Dialog<ErganiRegistration> dialog = new Dialog<>();
        dialog.setTitle("Εγγραφή στο Εργάνη");

        // Δημιουργία των στοιχείων εισαγωγής
        ComboBox<String> comboProgram = new ComboBox<>();
        comboProgram.getItems().addAll("1-2 Υπάλληλοι", "3-5 Υπάλληλοι", "6-20 Υπάλληλοι", "21-50 Υπάλληλοι"); // Σταθερές επιλογές
        //comboProgram.setValue("Πρόγραμμα 1"); // Προεπιλογή

        TextField yearsField = new TextField();
        yearsField.setPromptText("Αριθμός Ετών");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Λογιστή");
        DBHelper dbHelper = new DBHelper();
        String erganiEmail = dbHelper.getErganiEmail(selectedLogin.getCustomerId());
        emailField.setText(erganiEmail);

        TextField entranceField = new TextField();
        entranceField.setPromptText("Είσοδος");
        entranceField.setText("Όχι");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Επιλογή Προγράμματος:"), 0, 0);
        grid.add(comboProgram, 1, 0);
        grid.add(new Label("Σύνολο Ετών:"), 0, 1);
        grid.add(yearsField, 1, 1);
        grid.add(new Label("Email Λογιστή:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Extra Είσοδος:"), 0, 3);
        grid.add(entranceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Ενεργοποίηση του κουμπιού OK μόνο αν έχουν συμπληρωθεί όλα τα πεδία
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        yearsField.textProperty().addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.trim().isEmpty() || emailField.getText().trim().isEmpty()));

        emailField.textProperty().addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.trim().isEmpty() || yearsField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new ErganiRegistration(
                        comboProgram.getValue(),
                        yearsField.getText().trim(),
                        emailField.getText().trim(),
                        entranceField.getText().trim()
                );
            }
            return null; // Αν ο χρήστης πατήσει "Άκυρο"
        });


        Optional<ErganiRegistration> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String program = data.getProgram();
            String years = data.getYears();
            String emailAcc = data.getEmail();
            String entrance = data.getEntrance();
            String subject = "Νέος πελάτης Εργάνη";
            String msg = "<b>Νέος πελάτης Εργάνη</b>" +
                    "<br><b>Επωνυμία:</b> " + customer.getName() +
                    "<br><b>ΑΦΜ:</b> " + customer.getAfm() +
                    "<br><b>E-mai:</b> " + selectedLogin.getUsername() +
                    "<br><b>Κινητό:</b> " + selectedLogin.getPhone() +
                    "<br><b>E-mail Λογιστή:</b> " + emailAcc +
                    "<br><b>Προγράμματα:</b> " + program +
                    "<br><b>Σύνολο Ετών:</b> " + years +
                    "<br><b>Extra Είσοδος:</b> " + entrance;
            sendEmail(subject, msg);
            if (dbHelper.hasAccountant(selectedLogin.getCustomerId())) {
                dbHelper.updateErganiEmail(selectedLogin.getCustomerId(), emailAcc);
            }
        });


    }

    public void loginErgani (ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα login.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        try {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate("https://myaccount.epsilonnet.gr/Identity/Account/Login?product=8fd59003-5af4-4ca7-6fbd-08dace2c8999");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Μέθοδος αποστολής email
    private void sendEmail(String subject, String msg) {
        // Κώδικας για αποστολή email
        EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
        emailSender.sendEmail(AppSettings.loadSetting("erganiRegisterMail"), subject, msg);
    }

    // Μέθοδος αντιγραφής κειμένου στο πρόχειρο
    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
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
