package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class SimplyViewController {
    @FXML
    public Button btnSimplyPOS, btnSimplyCash, btnSimplyRest, btnSimplyPOSRegister, btnSimplyCloudRegister;
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

    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setTooltip(btnSimplyPOS, "Είσοδος στο Simply POS με επιλεγμένο κωδικό");
        setTooltip(btnSimplyCash, "1) Είσοδος στο Simply Cash με επιλεγμένο κωδικό\n2α) Αποστολή στοιχείων για επιλεγμένο κωδικό σε Simply \n2β) Aντιγραφή στοιχείων για επιλεγμένου κωδικού");
        setTooltip(btnSimplyRest, "1) Είσοδος στο Simply Rest με επιλεγμένο κωδικό\n2α) Αποστολή στοιχείων για επιλεγμένο κωδικό σε Simply \n2β) Aντιγραφή στοιχείων για επιλεγμένου κωδικού");
        setTooltip(btnSimplyPOSRegister, "Εγγραφή στο Simply POS");
        setTooltip(btnSimplyCloudRegister,"Εγγραφή Simply Cash/Rest");

        loginList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        // Ρύθμιση στήλης username
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        // Ρύθμιση στήλης tag
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));

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
        loginList.addAll(dbHelper.getLogins(customerId,2));
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
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addLoginController.handleSaveLogin(event,2);
                }
                return null;
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadLoginsForCustomer(customer.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login προς διαγραφή."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς διαγραφή.");
            return;
        }

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
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login προς επεξεργασία."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editLogin.fxml"));
            DialogPane dialogPane = loader.load();

            EditLoginController editController = loader.getController();
            editController.setLogin(selectedLogin);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Login");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Logins updatedLogin = editController.getUpdatedLogin();

                // Ενημέρωση της βάσης
                DBHelper dbHelper = new DBHelper();
                dbHelper.updateLogin(updatedLogin);

                // Ενημέρωση του πίνακα
                loginTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    public void simplyposOpen(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            //System.out.println("Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }
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
            e.printStackTrace();
        }

    }

    public void simplycashOpen(MouseEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard
            if (selectedLogin != null) {
                String msg ="Νέος Πελάτης Simply Cash" +
                        "\nΕπωνυμία: "+customer.getName()+
                        "\nΑΦΜ: "+customer.getAfm()+
                        "\nEmail: "+selectedLogin.getUsername()+
                        "\nΚινητό: "+customer.getMobile()+
                        "\nΈχει κάνει αποδοχή σύμβασης και εξουσιοδότηση\n";
                // Δημιουργία παραθύρου διαλόγου για επιλογή
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Επιλογή Ενέργειας");
                alert.setHeaderText("Επιλέξτε μια ενέργεια");
                alert.setContentText("Θέλετε να αποστείλετε email ή να αντιγράψετε;");

                // Προσθήκη κουμπιών επιλογής
                ButtonType buttonEmail = new ButtonType("Αποστολή Email");
                ButtonType buttonCopy = new ButtonType("Αντιγραφή");
                ButtonType buttonCancel = new ButtonType("Ακύρωση", ButtonType.CANCEL.getButtonData());

                alert.getButtonTypes().setAll(buttonEmail, buttonCopy, buttonCancel);

                // Λήψη επιλογής χρήστη
                alert.showAndWait().ifPresent(choice -> {
                    if (choice == buttonEmail) {
                        sendEmail("Νέος Πελάτης Simply Cash", msg);
                    } else if (choice == buttonCopy) {
                        copyTextToClipboard(msg);
                    }
                });
               } else {
                showAlert("Attention", "Please select a login to copy.");
            }
        } else if (event.getButton() == MouseButton.PRIMARY) { // Left-click for regular functionality
            if (selectedLogin == null) {
                Platform.runLater(() -> showAlert("Attention", "Please select a login."));
                return;
            }

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
                e.printStackTrace();
            }
        }
    }

    public void simplyrestOpen(MouseEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard
            if (selectedLogin != null) {
                String msg ="Νέος Πελάτης Simply Rest" +
                        "\nΕπωνυμία: "+customer.getName()+
                        "\nΑΦΜ: "+customer.getAfm()+
                        "\nEmail: "+selectedLogin.getUsername()+
                        "\nΚινητό: "+customer.getMobile()+
                        "\nΈχει κάνει αποδοχή σύμβασης και εξουσιοδότηση\n";
                // Δημιουργία παραθύρου διαλόγου για επιλογή
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Επιλογή Ενέργειας");
                alert.setHeaderText("Επιλέξτε μια ενέργεια");
                alert.setContentText("Θέλετε να αποστείλετε email ή να αντιγράψετε;");

                // Προσθήκη κουμπιών επιλογής
                ButtonType buttonEmail = new ButtonType("Αποστολή Email");
                ButtonType buttonCopy = new ButtonType("Αντιγραφή");
                ButtonType buttonCancel = new ButtonType("Ακύρωση", ButtonType.CANCEL.getButtonData());

                alert.getButtonTypes().setAll(buttonEmail, buttonCopy, buttonCancel);

                // Λήψη επιλογής χρήστη
                alert.showAndWait().ifPresent(choice -> {
                    if (choice == buttonEmail) {
                        sendEmail("Νέος Πελάτης Simply Rest", msg);
                    } else if (choice == buttonCopy) {
                        copyTextToClipboard(msg);
                    }
                });
            } else {
                showAlert("Attention", "Please select a login to copy.");
            }
        } else if (event.getButton() == MouseButton.PRIMARY) { // Left-click for regular functionality
            if (selectedLogin == null) {
                Platform.runLater(() -> showAlert("Attention", "Please select a login."));
                return;
            }

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
                e.printStackTrace();
            }
        }

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
            e.printStackTrace();
        }
    }

    public void registercloudOpen(ActionEvent actionEvent) {
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://app.simplycloud.gr/Partners",
                    AppSettings.loadSetting("simplyCloudUser"),
                    AppSettings.loadSetting("simplyCloudPass"),
                    By.name("Email"),
                    By.id("Password"),
                    By.id("btnSubmit")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Μέθοδος αποστολής email
    private void sendEmail(String subject, String msg) {
        // Κώδικας για αποστολή email
        EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
        emailSender.sendEmail(AppSettings.loadSetting("simplyRegisterMail"), subject, msg);
    }

    // Μέθοδος αντιγραφής κειμένου στο πρόχειρο
    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        showAlert("Copied to Clipboard", msg);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}
