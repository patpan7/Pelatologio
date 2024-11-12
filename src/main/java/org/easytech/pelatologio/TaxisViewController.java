package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class TaxisViewController {
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
        loginList.addAll(dbHelper.getLogins(customerId,3));
        if (loginTable.getItems().size() == 1)
            loginTable.getSelectionModel().select(0);
    }
    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
            DialogPane dialogPane = loader.load();

            AddLoginController addLoginController = loader.getController();
            addLoginController.setCustomer(customer); // Ορίζει τον πελάτη

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέου Login");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    addLoginController.handleSaveLogin(event,3);
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

    public void taxisOpen(ActionEvent event) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/saadeapps3/comregistry/#!/arxiki",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void authorizationsOpen(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.gsis.gr/taxisnet/mytaxisnet/protected/authorizations.htm",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mydataOpen(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/saadeapps2/bookkeeper-web/bookkeeper/#!/",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void esendOpen(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.gsis.gr/tameiakes/myweb/esendN.php?FUNCTION=1",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("idEMAIL"),
                    By.name("PASSWD"),
                    By.cssSelector("input.btn.btn-primary[value='Σύνδεση']")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afm1Open(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/webtax/wspublicreg/faces/pages/wspublicreg/menu.xhtml",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afm2Open(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/sgsisapps/tokenservices/protected/displayConsole.htm",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tameiakesOpen(ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (selectedLogin == null) {
            // Εμφάνιση μηνύματος αν δεν υπάρχει επιλογή
            Platform.runLater(() -> showAlert("Προσοχή", "Παρακαλώ επιλέξτε ένα login."));
            return;
        }
        try {
            LoginAutomator loginAutomation = new LoginAutomator(true);
            loginAutomation.openAndFillLoginForm(
                    "https://www1.aade.gr/taxisnet/info/protected/displayTillInfo.htm",
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    By.id("username"),
                    By.id("password"),
                    By.name("btn_login")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
