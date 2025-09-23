package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.CustomNotification;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.EmailHelper;
import org.easytech.pelatologio.models.Accountant;

import java.io.IOException;
import java.util.function.Consumer;

public class AddAccountantController {
    private TabPane mainTabPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane customersContainer;
    @FXML
    private TextField tfName, tfPhone, tfMobile, tfEmail, tfErganiEmail;
    @FXML
    private Tab tabCustomers;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button btnPhone, btnMobile;

    private CustomerAccViewController customerAccViewController;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Accountant accountant;

    private AccountantsController accountantsController;

    private Consumer<Accountant> callback; // Callback function
    private Stage stage;

    public void setCallback(Consumer<Accountant> callback) {
        this.callback = callback;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    public void setAccountantsController(AccountantsController controller) {
        this.accountantsController = controller;
    }


    public void initialize() {
        Platform.runLater(() -> tabPane.requestFocus());
        try {
            FXMLLoader loaderTaxis = new FXMLLoader(getClass().getResource("customersAccView.fxml"));
            Parent customerContent = loaderTaxis.load();
            customerAccViewController = loaderTaxis.getController(); // Πάρε τον controller
            customersContainer.getChildren().setAll(customerContent);
            AnchorPane.setTopAnchor(customerContent, 0.0);
            AnchorPane.setBottomAnchor(customerContent, 0.0);
            AnchorPane.setLeftAnchor(customerContent, 0.0);
            AnchorPane.setRightAnchor(customerContent, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tabCustomers.setDisable(true);


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
        setupTextFieldContextMenu(tfPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);

        // Ενέργειες για τα copy, paste, clear items στο βασικό contextMenu
        copyItem.setOnAction(event -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(event -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(event -> AppUtils.clearText(currentTextField));

        // Ενέργειες για τα phoneCopyItem, phonePasteItem, phoneClearItem στο phoneContextMenu
        phoneCopyItem.setOnAction(event -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        phonePasteItem.setOnAction(event -> AppUtils.pasteText(currentTextField));
        phoneClearItem.setOnAction(event -> AppUtils.clearText(currentTextField));
        // Ενέργεια "Viber" μόνο για τα τηλέφωνα
        viberItem.setOnAction(e -> {
            if (currentTextField == tfPhone || currentTextField == tfMobile) { // Εκτέλεση μόνο αν είναι στο tfEmail
                AppUtils.viberComunicate(currentTextField.getText());
            }
        });
        // Ενέργειες για τα emailCopyItem, emailPasteItem, emailClearItem στο emailContextMenu
        emailCopyItem.setOnAction(event -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        emailPasteItem.setOnAction(event -> AppUtils.pasteText(currentTextField));
        emailClearItem.setOnAction(event -> AppUtils.clearText(currentTextField));

        // Ενέργεια "Δοκιμή Email" μόνο για το tfEmail
        mailItem.setOnAction(e -> {
            if (currentTextField == tfEmail) { // Εκτέλεση μόνο αν είναι στο tfEmail
                AppUtils.sendTestEmail(tfEmail.getText(), progressIndicator);
            }
        });

        btnPhone.setUserData(tfPhone);
        AppUtils.setupPhoneButton(btnPhone, tfPhone);
        //btnPhone.setOnAction(PhoneCall::callHandle);
        btnMobile.setUserData(tfMobile);
        AppUtils.setupPhoneButton(btnMobile, tfMobile);
        //btnMobile.setOnAction(PhoneCall::callHandle);
    }


    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }


    public void setAccountantData(Accountant accountant) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(accountant.getName());
        tfPhone.setText(accountant.getPhone());
        tfMobile.setText(accountant.getMobile());
        tfEmail.setText(accountant.getEmail());
        tfErganiEmail.setText(accountant.getErganiEmail());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = accountant.getId();
        this.accountant = accountant;
        if (customerAccViewController != null) {
            customerAccViewController.setAccountnat(accountant);
        } else {
            System.out.println("customerAccViewController δεν είναι ακόμα έτοιμος.");
        }

        tabCustomers.setDisable(false);
    }

    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addAccountanat();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateAccountant();
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

    void addAccountanat() {
        String name = tfName.getText();
        String phone = (tfPhone.getText() != null ? tfPhone.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String erganiEmail = (tfErganiEmail.getText() != null ? tfErganiEmail.getText() : "");
        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        DBHelper dbHelper = new DBHelper();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        int accountantId;
        accountantId = DBHelper.getAccountantDao().insertAccountant(name, phone, mobile, email, erganiEmail);
        // Εμφάνιση επιτυχίας
        if (accountantId > 0) {
            Accountant newAccountant = new Accountant(accountantId, name, phone, mobile, email, erganiEmail);

            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Επιτυχία")
                        .text("Ο Λογιστής εισήχθη με επιτυχία στη βάση δεδομένων.")
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT)
                        .showConfirmation();
                if (callback != null) {
                    callback.accept(newAccountant);
                    stage.close();
                } else {
                    closeCurrentTab(); // Κλείσιμο του "Νέος Πελάτης"
                    openAccountantTab(accountantId); // Άνοιγμα καρτέλας με τον νέο πελάτη
                }
            });
        }
    }

    private void openAccountantTab(int accountantId) {
        if (accountantsController != null) {
            accountantsController.openAccountantTab(accountantId);
        }
    }


    void updateAccountant() {
        String name = tfName.getText();
        String phone = (tfPhone.getText() != null ? tfPhone.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String erganiEmail = (tfErganiEmail.getText() != null ? tfErganiEmail.getText() : "");

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        DBHelper.getAccountantDao().updateAccountant(code, name, phone, mobile, email, erganiEmail);
        //showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
        CustomNotification.create()
                .title("Επιτυχία")
                .text("Ο λογιστής ενημερώθηκε με επιτυχία στη βάση δεδομένων.")
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showConfirmation();
    }


    @FXML
    private void showEmailDialog(ActionEvent actionEvent) {
        if (accountant != null) {
            EmailHelper.EmailDialogOptions options = new EmailHelper.EmailDialogOptions(tfEmail.getText())
                    .subject("")
                    .body("")
                    .showCopyOption(false)
                    .saveCopy(true)
                    .onSuccess(null);

            EmailHelper.showEmailDialog(options);
        }
    }
}
