package org.easytech.pelatologio;

import atlantafx.base.controls.ToggleSwitch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Supplier;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class AddSupplierController {
    private TabPane mainTabPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabOrders, tabPayments;
    @FXML
    private AnchorPane ordersContainer, paymentsContainer;
    @FXML
    private TextField tfName, tfTitle, tfAfm, tfPhone, tfMobile, tfContact, tfEmail, tfEmail2, tfSite;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button btnPhone, btnMobile;
    @FXML
    private Button btnAfmSearch;
    @FXML
    private TextArea taNotes;
    @FXML
    private ToggleSwitch hasCommissionsCheck;

    private OrdersSupViewController orderSupViewController;
    private SupplierPaymentsViewController supplierPaymentsController;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Supplier supplier;

    private SuppliersViewController suppliersViewController;


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

    public void setSuppliersController(SuppliersViewController controller) {
        this.suppliersViewController = controller;
    }


    public void initialize() {
        Platform.runLater(() -> tabPane.requestFocus());

        try {
            FXMLLoader loaderOrders = new FXMLLoader(getClass().getResource("ordersSupView.fxml"));
            Parent ordersContent = loaderOrders.load();
            orderSupViewController = loaderOrders.getController();
            ordersContainer.getChildren().setAll(ordersContent);
            AnchorPane.setTopAnchor(ordersContent, 0.0);
            AnchorPane.setBottomAnchor(ordersContent, 0.0);
            AnchorPane.setLeftAnchor(ordersContent, 0.0);
            AnchorPane.setRightAnchor(ordersContent, 0.0);

            FXMLLoader loaderPayments = new FXMLLoader(getClass().getResource("supplierPaymentsView.fxml"));
            Parent paymentsContent = loaderPayments.load();
            supplierPaymentsController = loaderPayments.getController();
            paymentsContainer.getChildren().setAll(paymentsContent);
            AnchorPane.setTopAnchor(paymentsContent, 0.0);
            AnchorPane.setBottomAnchor(paymentsContent, 0.0);
            AnchorPane.setLeftAnchor(paymentsContent, 0.0);
            AnchorPane.setRightAnchor(paymentsContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        tabOrders.setDisable(true);

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
        setupTextFieldContextMenu(tfAfm, contextMenu);
        setupTextFieldContextMenu(tfPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);
        setupTextFieldContextMenu(tfContact, contextMenu);
        setupTextFieldContextMenu(tfSite, contextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);
        tfEmail2.setContextMenu(emailContextMenu);
        tfEmail2.setOnContextMenuRequested(e -> currentTextField = tfEmail2);

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

        btnPhone.setUserData(tfPhone);
        //btnPhone.setOnAction(PhoneCall::callHandle);
        setupPhoneButton(btnPhone, tfPhone);
        btnMobile.setUserData(tfMobile);
        //btnMobile.setOnAction(PhoneCall::callHandle);
        setupPhoneButton(btnMobile, tfMobile);
        btnAfmSearch.setOnAction(event -> handleAfmSearch());
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
        tfAfm.setText(supplier.getAfm());
        tfPhone.setText(supplier.getPhone());
        tfMobile.setText(supplier.getMobile());
        tfContact.setText(supplier.getContact());
        tfEmail.setText(supplier.getEmail());
        tfEmail2.setText(supplier.getEmail2());
        tfSite.setText(supplier.getSite());
        taNotes.setText(supplier.getNotes());
        hasCommissionsCheck.setSelected(supplier.hasCommissions());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = supplier.getId();
        this.supplier = supplier;
        if (orderSupViewController != null) {
            orderSupViewController.setSupplier(supplier);
        } else {
            System.out.println("orderSupViewController δεν είναι ακόμα έτοιμος.");
        }

        if (supplierPaymentsController != null) {
            supplierPaymentsController.setSupplier(supplier);
        } else {
            System.out.println("supplierPaymentsController δεν είναι ακόμα έτοιμος.");
        }

        tabOrders.setDisable(false);
        tabPayments.setDisable(false);
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
        String afm = (tfAfm.getText() != null ? tfAfm.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String contact = (tfContact.getText() != null ? tfContact.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String email2 = (tfEmail2.getText() != null ? tfEmail2.getText() : "");
        String site = (tfSite.getText() != null ? tfSite.getText() : "");
        String notes = (taNotes.getText() != null ? taNotes.getText() : "");
        boolean hasCommissions = hasCommissionsCheck.isSelected();

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        DBHelper dbHelper = new DBHelper();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        int supplierId;
        supplierId = DBHelper.getSupplierDao().insertSupplier(name, title, afm, phone, mobile, contact, email, email2, site, notes, hasCommissions);
        // Εμφάνιση επιτυχίας
        if (supplierId > 0) {
            Supplier newSupplier = new Supplier(supplierId, name, title, afm, phone, mobile, contact, email, email2, site, notes, hasCommissions);

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
        if (suppliersViewController != null) {
            suppliersViewController.openSupplierTab(supplierId);
        }
    }


    void updateSupplier() {
        DBHelper dbHelper = new DBHelper();

        String name = tfName.getText();
        String title = tfTitle.getText() != null ? tfTitle.getText() : "";
        String afm = (tfAfm.getText() != null ? tfAfm.getText() : "");
        String phone = (tfPhone.getText() != null ? tfPhone.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String contact = (tfContact.getText() != null ? tfContact.getText() : "");
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String email2 = (tfEmail2.getText() != null ? tfEmail2.getText() : "");
        String site = (tfSite.getText() != null ? tfSite.getText() : "");
        String notes = (taNotes.getText() != null ? taNotes.getText() : "");
        boolean hasCommissions = hasCommissionsCheck.isSelected();

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone.startsWith("+30"))
            phone = phone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone = phone.replaceAll("\\s+", "");

        DBHelper.getSupplierDao().updateSupplier(code, name, title, afm, phone, mobile, contact, email, email2, site, notes, hasCommissions);
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
                loginAutomation.openPage("http://" + site);

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    private void handleAfmSearch() {
        tfName.setText("");
        tfTitle.setText("");

        String afm = tfAfm.getText();
        if (afm == null || afm.isEmpty()) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ εισάγετε ένα έγκυρο ΑΦΜ.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }

        AfmLookupService service = new AfmLookupService();
        String responseXml = service.callAadeService(afm);

        // Έλεγχος για μήνυμα σφάλματος
        String errorDescr = AfmResponseParser.getXPathValue(responseXml, "//error_rec/error_descr");
        if (errorDescr != null && !errorDescr.isEmpty()) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text(errorDescr)
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }

        Customer companyInfo = AfmResponseParser.parseResponse(responseXml);

        if (companyInfo != null) {
            tfName.setText(companyInfo.getName());
            tfTitle.setText(companyInfo.getTitle());
        } else {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Σφάλμα κατά την ανάγνωση των δεδομένων")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(taNotes.getText());
        }
    }

    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Περιγραφής");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(400, 300);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            taNotes.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);
        //vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void setupPhoneButton(Button button, TextField textField) {
        button.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                PhoneCall.callHandle(textField.getText());
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                PhoneCall.callHandle2(textField.getText());
            }
        });
    }
}
