package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class AddSupplierController {
    private TabPane mainTabPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane customersContainer;
    @FXML
    private TextField tfName, tfTitle, tfPhone, tfMobile, tfContact, tfEmail, tfSite;
    @FXML
    private ProgressIndicator progressIndicator;


    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Supplier supplier;

    private SuppliersController suppliersController;

    private Consumer<Supplier> callback; // Callback function
    private Stage stage;

    public void setCallback(Consumer<Supplier> callback) {
        this.callback = callback;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    public void setSuppliersController(SuppliersController controller) {
        this.suppliersController = controller;
    }


    public void initialize() {
        Platform.runLater(() -> tabPane.requestFocus());
        // Δημιουργία του βασικού ContextMenu χωρίς την επιλογή "Δοκιμή Email"
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);

        // Δημιουργία του ContextMenu για τα Τηλέφωνα με την επιλογή "Δοκιμή Email"
        ContextMenu phoneContextMenu = new ContextMenu();
        MenuItem phoneCopyItem = new MenuItem("Αντιγραφή");
        MenuItem phonePasteItem = new MenuItem("Επικόλληση");
        MenuItem phoneClearItem = new MenuItem("Εκκαθάριση");
        MenuItem viberItem = new MenuItem("Επικοινωνία Viber");
        phoneContextMenu.getItems().addAll(phoneCopyItem, phonePasteItem, phoneClearItem, viberItem);


        // Δημιουργία του ContextMenu για το tfEmail με την επιλογή "Δοκιμή Email"
        ContextMenu emailContextMenu = new ContextMenu();
        MenuItem emailCopyItem = new MenuItem("Αντιγραφή");
        MenuItem emailPasteItem = new MenuItem("Επικόλληση");
        MenuItem emailClearItem = new MenuItem("Εκκαθάριση");
        MenuItem mailItem = new MenuItem("Δοκιμή Email");
        emailContextMenu.getItems().addAll(emailCopyItem, emailPasteItem, emailClearItem, mailItem);

        // Προσθήκη του contextMenu στα υπόλοιπα TextFields
        setupTextFieldContextMenu(tfName, contextMenu);
        setupTextFieldContextMenu(tfTitle, contextMenu);
        setupTextFieldContextMenu(tfPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);
        setupTextFieldContextMenu(tfContact, contextMenu);
        setupTextFieldContextMenu(tfSite, contextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);

        // Ενέργειες για τα copy, paste, clear items στο βασικό contextMenu
        copyItem.setOnAction(e -> copyText());
        pasteItem.setOnAction(e -> pasteText());
        clearItem.setOnAction(e -> clearText());

        // Ενέργειες για τα phoneCopyItem, phonePasteItem, phoneClearItem στο phoneContextMenu
        phoneCopyItem.setOnAction(e -> copyText());
        phonePasteItem.setOnAction(e -> pasteText());
        phoneClearItem.setOnAction(e -> clearText());
        // Ενέργεια "Viber" μόνο για τα τηλέφωνα
        viberItem.setOnAction(e -> {
            if (currentTextField == tfPhone || currentTextField == tfMobile) { // Εκτέλεση μόνο αν είναι στο tfEmail
                viberComunicate(currentTextField);
            }
        });
        // Ενέργειες για τα emailCopyItem, emailPasteItem, emailClearItem στο emailContextMenu
        emailCopyItem.setOnAction(e -> copyText());
        emailPasteItem.setOnAction(e -> pasteText());
        emailClearItem.setOnAction(e -> clearText());

        // Ενέργεια "Δοκιμή Email" μόνο για το tfEmail
        mailItem.setOnAction(e -> {
            if (currentTextField == tfEmail) { // Εκτέλεση μόνο αν είναι στο tfEmail
                sendTestEmail(tfEmail);
            }
        });
    }


    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    // Μέθοδοι για τις ενέργειες
    private void copyText() {
        if (currentTextField != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentTextField.getText());  // Replace with the desired text
            clipboard.setContent(content);
        }
    }

    private void pasteText() {
        if (currentTextField != null) {
            currentTextField.paste();
        }
    }

    private void clearText() {
        if (currentTextField != null) {
            currentTextField.clear();
        }
    }

    // Μέθοδος αποστολής με viber
    private void viberComunicate(TextField phoneField) {
        String phone = phoneField.getText();
        System.out.println("Τηλέφωνο: " + phone);
        if (phone != null && !phone.isEmpty()) {
            try {
                File viberPath = new File(System.getenv("LOCALAPPDATA") + "\\Viber\\Viber.exe");
                Desktop.getDesktop().open(viberPath);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(phone);  // Replace with the desired text
                clipboard.setContent(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ εισάγετε ένα έγκυρο τηλέφωνο")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
        }
    }

    // Μέθοδος αποστολής δοκιμαστικού email
    private void sendTestEmail(TextField emailField) {
        String email = emailField.getText();
        if (email != null && !email.isEmpty()) {
            // Εμφάνιση του progress indicator
            progressIndicator.setVisible(true);

            // Δημιουργία και αποστολή email σε ξεχωριστό thread για να μην κολλήσει το UI
            new Thread(() -> {
                try {
                    String subject = "Δοκιμή Email";
                    String body = "Δοκιμή email.";
                    EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
                    emailSender.sendEmail(email, subject, body);

                    // Ενημερώνουμε το UI όταν ολοκληρωθεί η αποστολή του email
                    Platform.runLater(() -> {
                        Notifications notifications = Notifications.create()
                                .title("Επιτυχία")
                                .text("Το email στάλθηκε με επιτυχία.")
                                .graphic(null)
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT);
                        notifications.showConfirm();
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποστολή του email.", e.getMessage(), Alert.AlertType.ERROR);
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                    Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποστολή email.", e.getMessage(), Alert.AlertType.ERROR));
                }
            }).start(); // Ξεκινάμε το thread για την αποστολή του email
        } else {
            //showAlert("Προσοχή", "Παρακαλώ εισάγετε ένα έγκυρο email.");
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ εισάγετε ένα έγκυρο email.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
        }
    }


    public void setSupplierData(Supplier supplier) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(supplier.getName());
        tfTitle.setText(supplier.getTitle());
        tfPhone.setText(supplier.getPhone());
        tfMobile.setText(supplier.getMobile());
        tfContact.setText(supplier.getContact());
        tfEmail.setText(supplier.getEmail());
        tfSite.setText(supplier.getSite());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = supplier.getId();
        this.supplier = supplier;
    }

    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addSupplier();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateSupplier();
        }
    }

    private void closeCurrentTab() {
        Platform.runLater(() -> {
            Tab currentTab = mainTabPane.getSelectionModel().getSelectedItem();
            System.out.println("Τρέχον tab: " + currentTab.getText());
            TabPane parentTabPane = currentTab.getTabPane();
            parentTabPane.getTabs().remove(currentTab);
        });
    }

    void addSupplier() {
        String name = tfName.getText();
        String title = tfTitle.getText() != null ? tfTitle.getText() : "";
        String phone = (tfPhone.getText() != null ? tfPhone.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String contact = (tfContact.getText() != null ? tfContact.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String site = (tfSite.getText() != null ? tfSite.getText() : "");
        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        DBHelper dbHelper = new DBHelper();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        int supplierId;
        supplierId = dbHelper.insertSupplier(name, title, phone, mobile, contact, email, site);
        // Εμφάνιση επιτυχίας
        if (supplierId > 0) {
            Supplier newSupplier = new Supplier(supplierId, name, title, phone, mobile, contact, email, site);

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();
                if (callback != null) {
                    callback.accept(newSupplier);
                    stage.close();
                } else {
                    closeCurrentTab(); // Κλείσιμο του "Νέος Πελάτης"
                    openSupplierTab(supplierId); // Άνοιγμα καρτέλας με τον νέο πελάτη
                }
            });
        }
    }

    private void openSupplierTab(int supplierId) {
        if (suppliersController != null) {
            suppliersController.openSupplierTab(supplierId);
        }
    }


    void updateSupplier() {
        DBHelper dbHelper = new DBHelper();

        String name = tfName.getText();
        String title = tfTitle.getText() != null ? tfTitle.getText() : "";
        String phone = (tfPhone.getText() != null ? tfPhone.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String contact = (tfContact.getText() != null ? tfContact.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String site = (tfSite.getText() != null ? tfSite.getText() : "");

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        dbHelper.updateSupplier(code, name, title, phone, mobile, contact, email, site);
        //showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
        Notifications notifications = Notifications.create()
                .title("Επιτυχία")
                .text("Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
        notifications.showInformation();
    }


    @FXML
    private void showEmailDialog(ActionEvent actionEvent) {
        if (supplier != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Αποστολή Email");
                EmailDialogController controller = loader.getController();
                //controller.setCustomer(accountant);
                controller.setEmail(tfEmail.getText());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                dialog.show();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void openSite(ActionEvent actionEvent) {
//        if (supplier.getSite().isEmpty() || supplier.getSite().equals("")){
//            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Δεν υπάρχει site.","Ο προμηθευτής δεν έχει site!", Alert.AlertType.ERROR));
//            return;
//        }

        try {
            LoginAutomator loginAutomation = new LoginAutomator(false);
            String site = supplier.getSite().trim();
            if (site.contains("http:"))
                loginAutomation.openPage(site);
            else
                loginAutomation.openPage("http://"+site);

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }
}
