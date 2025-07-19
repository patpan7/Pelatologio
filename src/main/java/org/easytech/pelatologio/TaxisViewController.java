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
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.LoginAutomator;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Logins;
import org.openqa.selenium.By;

import java.io.IOException;

public class TaxisViewController {
    // Constants
    private static final String TAXISNET_URL = "https://www1.aade.gr/saadeapps3/comregistry/#!/arxiki";
    private static final String AUTHORIZATIONS_URL = "https://www1.gsis.gr/taxisnet/mytaxisnet/protected/authorizations.htm";
    private static final String MYDATA_URL = "https://www1.aade.gr/saadeapps2/bookkeeper-web/bookkeeper/#!/";
    private static final String ESEND_URL = "https://www1.gsis.gr/tameiakes/myweb/esendN.php?FUNCTION=1";
    private static final String AFM1_URL = "https://www1.aade.gr/webtax/wspublicreg/faces/pages/wspublicreg/menu.xhtml";
    private static final String AFM2_URL = "https://www1.aade.gr/sgsisapps/tokenservices/protected/displayConsole.htm";
    private static final String TAMEIAKES_URL = "https://www1.aade.gr/taxisnet/info/protected/displayTillInfo.htm";
    private static final String GEMI_URL = "https://publicity.businessportal.gr/";

    private static final By DEFAULT_USERNAME_LOCATOR = By.id("username");
    private static final By DEFAULT_PASSWORD_LOCATOR = By.id("password");
    private static final By DEFAULT_SUBMIT_LOCATOR = By.name("btn_login");

    @FXML private Button btnTaxis, btnAuthorizations, btnMyData, btnESend, btnAfm1, btnAfm2, btnTameiakes, btnGemi;
    @FXML private TableView<Logins> loginTable;
    @FXML private TableColumn<Logins, String> usernameColumn, passwordColumn, tagColumn;

    private Customer customer;
    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setupTooltips();
        initializeTable();
    }

    private void setupTooltips() {
        setTooltip(btnTaxis, "1) Είσοδος στο Taxis με επιλεγμένο κωδικό\n2) Αντιγραφή στοιχείων για επιλεγμένου κωδικού");
        setTooltip(btnAuthorizations, "Είσοδος στις εξουσιοδοτήσεις με επιλεγμένο κωδικό");
        setTooltip(btnMyData, "Είσοδος στο myData με επιλεγμένο κωδικό");
        setTooltip(btnESend, "Είσοδος στο e-send με επιλεγμένο κωδικό");
        setTooltip(btnAfm1, "Εγγραφή στην υπηρεσία ανεύρεσης ΑΦΜ με επιλεγμένο κωδικό");
        setTooltip(btnAfm2, "Είσοδος στη διαχείριση ειδικών κωδικών με επιλεγμένο κωδικό");
        setTooltip(btnTameiakes, "Είσοδος στις ταμειακές με επιλεγμένο κωδικό");
        setTooltip(btnGemi, "Αναζήτηση στο ΓΕΜΗ για τον πελάτη");
    }

    private void initializeTable() {
        loginList = FXCollections.observableArrayList();
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));
        loginTable.setItems(loginList);
        loginTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditLogin(null);
            }
        });
    }

    public void loadLoginsForCustomer(int customerId) {
        loginList.clear();
        DBHelper dbHelper = new DBHelper();
        loginList.addAll(DBHelper.getLoginDao().getLogins(customerId, 3));
        if (loginTable.getItems().size() == 1) {
            loginTable.getSelectionModel().select(0);
        }
    }

    public void handleAddLogin(ActionEvent event) {
        try {
            var loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
            var dialogPane = (DialogPane)loader.load();
            var controller = (AddLoginController)loader.getController();

            controller.setCustomer(customer);
            //controller.setUsername(customer.getEmail());

            var dialog = createDialog(dialogPane, "Προσθήκη Νέου Login Taxis");

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, e -> {
                if (!controller.validateInputs()) {
                    e.consume();
                }
            });

            dialog.setOnHidden(e -> loadLoginsForCustomer(customer.getCode()));

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    controller.handleSaveLogin(event, 3);
                }
                return null;
            });
            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();
        } catch (IOException e) {
            showErrorDialog("Προέκυψε σφάλμα κατά την προσθήκη του login.", e.getMessage());
        }
    }    public void handleDeleteLogin(ActionEvent event) {
        var selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showNotification("Προσοχή", "Παρακαλώ επιλέξτε ένα login προς διαγραφή.");
            return;
        }

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση Διαγραφής");
        alert.setHeaderText(null);
        alert.setContentText("Είστε σίγουροι ότι θέλετε να διαγράψετε το επιλεγμένο login;");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DBHelper.getLoginDao().deleteLogin(selectedLogin.getId());
            loginTable.getItems().remove(selectedLogin);
        }
    }

    public void handleEditLogin(ActionEvent event) {
        var selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showNotification("Προσοχή", "Παρακαλώ επιλέξτε ένα login προς επεξεργασία.");
            return;
        }

        try {
            var loader = new FXMLLoader(getClass().getResource("editLogin.fxml"));
            var dialogPane = (DialogPane)loader.load();
            var controller = (EditLoginController)loader.getController();

            controller.setLogin(selectedLogin);
            var dialog = createDialog(dialogPane, "Επεξεργασία Login");

            dialog.setOnHidden(e -> loginTable.refresh());

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        if (!new DBHelper().getLoginDao().updateLogin(controller.getUpdatedLogin())) {
                            showErrorDialog("Αποτυχία ενημέρωσης", "Η ενημέρωση του login απέτυχε.");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Προέκυψε σφάλμα", e.getMessage());
                    }
                }
                return null;
            });
            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();
        } catch (IOException e) {
            showErrorDialog("Προέκυψε σφάλμα κατά την ενημέρωση.", e.getMessage());
        }
    }    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadLoginsForCustomer(customer.getCode());
    }

    // Button handlers
    public void taxisOpen(MouseEvent event) {
        var selectedLogin = loginTable.getSelectionModel().getSelectedItem();

        if (event.getButton() == MouseButton.SECONDARY) {
            if (selectedLogin != null) {
                copyCredentialsToClipboard(selectedLogin);
            } else {
                showNotification("Προσοχή", "Παρακαλώ επιλέξτε ένα login προς αντιγραφή.");
            }
        } else if (event.getButton() == MouseButton.PRIMARY) {
            openLoginPage(TAXISNET_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
        }
    }

    public void authorizationsOpen(ActionEvent actionEvent) {
        openLoginPageAuth(AUTHORIZATIONS_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
    }

    public void mydataOpen(ActionEvent actionEvent) {
        openLoginPage(MYDATA_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
    }

    public void esendOpen(ActionEvent actionEvent) {
        openLoginPage(ESEND_URL, By.id("idEMAIL"), By.name("PASSWD"),
                By.cssSelector("input.btn.btn-primary[value='Σύνδεση']"));
    }

    public void afm1Open(ActionEvent actionEvent) {
        openLoginPage(AFM1_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
    }

    public void afm2Open(ActionEvent actionEvent) {
        openLoginPage(AFM2_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
    }

    public void tameiakesOpen(ActionEvent actionEvent) {
        openLoginPage(TAMEIAKES_URL, DEFAULT_USERNAME_LOCATOR, DEFAULT_PASSWORD_LOCATOR, DEFAULT_SUBMIT_LOCATOR);
    }

    public void gemiSearch(ActionEvent actionEvent) {
        if (customer != null) {
            try {
                new LoginAutomator(true).openGemi(GEMI_URL, customer.getAfm());
            } catch (IOException e) {
                showErrorDialog("Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage());
            }
        }
    }

    // Helper methods
    private Dialog<ButtonType> createDialog(DialogPane dialogPane, String title) {
        var dialog = new Dialog<ButtonType>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.initModality(Modality.NONE);
        return dialog;
    }

    private void openLoginPage(String url, By usernameLocator, By passwordLocator, By submitLocator) {
        var selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showNotification("Προσοχή", "Παρακαλώ επιλέξτε ένα login.");
            return;
        }
        try {
            new LoginAutomator(true).openAndFillLoginForm(
                    url,
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    usernameLocator,
                    passwordLocator,
                    submitLocator
            );
        } catch (IOException e) {
            showErrorDialog("Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage());
        }
    }

    private void openLoginPageAuth(String url, By usernameLocator, By passwordLocator, By submitLocator) {
        var selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showNotification("Προσοχή", "Παρακαλώ επιλέξτε ένα login.");
            return;
        }
        try {
            new LoginAutomator(true).openAndFillLoginFormAuthorizations(
                    url,
                    selectedLogin.getUsername(),
                    selectedLogin.getPassword(),
                    usernameLocator,
                    passwordLocator,
                    submitLocator
            );
        } catch (IOException e) {
            showErrorDialog("Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage());
        }
    }

    private void copyCredentialsToClipboard(Logins login) {
        var content = new ClipboardContent();
        content.putString(login.getTag() + "\nUsername: " + login.getUsername() + "\nPassword: " + login.getPassword());
        Clipboard.getSystemClipboard().setContent(content);

        Notifications.create()
                .title("Αντιγραφή στο πρόχειρο")
                .text("Τα στοιχεία του login αντιγράφηκαν")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showInformation();
    }

    private void showNotification(String title, String message) {
        Platform.runLater(() -> Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showError());
    }

    private void showErrorDialog(String header, String content) {
        Platform.runLater(() -> AlertDialogHelper.showDialog(
                "Σφάλμα", header, content, Alert.AlertType.ERROR));
    }

    private void setTooltip(Button button, String text) {
        var tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.seconds(0.3));
        button.setTooltip(tooltip);
    }

}