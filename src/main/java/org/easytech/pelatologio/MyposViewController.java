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
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Optional;

public class MyposViewController {
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
        loginList.addAll(dbHelper.getLogins(customerId,1));
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
                }
                else {
                    // Εάν οι εισαγωγές είναι έγκυρες, συνεχίστε με την αποθήκευση
                    addLoginController.handleSaveLogin(event,1);
                }
            });

            dialog.showAndWait();
            // Ανανέωση του πίνακα logins
            loadLoginsForCustomer(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
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
        System.out.println(selectedLogin.getPhone());
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
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
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την ενημέρωση.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void handleLabel(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }
        LabelPrintHelper.printLoginLabel(selectedLogin,customer,"Στοιχεία myPOS");
    }

    public void handleAddTask(ActionEvent evt) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle("myPOS: "+ customer.getName());
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

            dialog.showAndWait();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    public void myposloginOpen(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα login.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }
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

    public void myposregisterOpen(MouseEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        String myposRegister = AppSettings.loadSetting("myposlink");

        if (event.getButton() == MouseButton.SECONDARY) { // Right-click for copying to clipboard
            if (selectedLogin != null) {
                String msg ="Στοιχεία myPOS" +
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
            try {
                LoginAutomator loginAutomation = new LoginAutomator(true);
                loginAutomation.openPage(myposRegister);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (selectedLogin == null) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Προσοχή")
                            .text("Παρακαλώ επιλέξτε ένα login.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();});
                return;
            }
        }
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}
