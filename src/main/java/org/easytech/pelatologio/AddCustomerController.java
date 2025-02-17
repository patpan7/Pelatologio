package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class AddCustomerController {
    private TabPane mainTabPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane taxisContainer, myposContainer, simplyContainer, emblemContainer, devicesContainer, tasksContainer;
    @FXML
    private Tab tabTaxis, tabMypos, tabSimply, tabEmblem, tabDevices, tabTasks, tabAccountant;
    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfPostCode, tfEmail, tfEmail2, tfManager, tfManagerPhone;
    @FXML
    private TextField tfAccName, tfAccPhone, tfAccMobile, tfAccEmail;
    @FXML
    private Button btnAfmSearch;
    @FXML
    private Button btnAddressAdd;
    @FXML
    private TextArea taNotes;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    Button btnAddToMegasoft, btnShowToMegasoft, btnData, btnLabel, btnAppointment,btnTask;

    private TaxisViewController taxisViewController;
    private MyposViewController myposViewController;
    private SimplyViewController simplyViewController;
    private EmblemViewController emblemViewController;
    private CustomerDevicesController customerDevicesController;
    private CustomerTasksController customerTasksController;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Customer customer;

    private CustomersController customersController;

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    public void setCustomersController(CustomersController controller) {
        this.customersController = controller;
    }


    public void initialize() {
        Platform.runLater(() -> tabPane.requestFocus());
        try {
            FXMLLoader loaderTaxis = new FXMLLoader(getClass().getResource("taxisView.fxml"));
            Parent taxisContent = loaderTaxis.load();
            taxisViewController = loaderTaxis.getController(); // Πάρε τον controller
            taxisContainer.getChildren().setAll(taxisContent);
            AnchorPane.setTopAnchor(taxisContent, 0.0);
            AnchorPane.setBottomAnchor(taxisContent, 0.0);
            AnchorPane.setLeftAnchor(taxisContent, 0.0);
            AnchorPane.setRightAnchor(taxisContent, 0.0);

            FXMLLoader loaderMypos = new FXMLLoader(getClass().getResource("myposView.fxml"));
            Parent myposContent = loaderMypos.load();
            myposViewController = loaderMypos.getController(); // Πάρε τον controller
            myposContainer.getChildren().setAll(myposContent);
            AnchorPane.setTopAnchor(myposContent, 0.0);
            AnchorPane.setBottomAnchor(myposContent, 0.0);
            AnchorPane.setLeftAnchor(myposContent, 0.0);
            AnchorPane.setRightAnchor(myposContent, 0.0);

            FXMLLoader loaderSimply = new FXMLLoader(getClass().getResource("simplyView.fxml"));
            Parent simplyContent = loaderSimply.load();
            simplyViewController = loaderSimply.getController(); // Πάρε τον controller
            simplyContainer.getChildren().setAll(simplyContent);
            AnchorPane.setTopAnchor(simplyContent, 0.0);
            AnchorPane.setBottomAnchor(simplyContent, 0.0);
            AnchorPane.setLeftAnchor(simplyContent, 0.0);
            AnchorPane.setRightAnchor(simplyContent, 0.0);

            FXMLLoader loaderEmblem = new FXMLLoader(getClass().getResource("emblemView.fxml"));
            Parent emblemContent = loaderEmblem.load();
            emblemViewController = loaderEmblem.getController(); // Πάρε τον controller
            emblemContainer.getChildren().setAll(emblemContent);
            AnchorPane.setTopAnchor(emblemContent, 0.0);
            AnchorPane.setBottomAnchor(emblemContent, 0.0);
            AnchorPane.setLeftAnchor(emblemContent, 0.0);
            AnchorPane.setRightAnchor(emblemContent, 0.0);

            FXMLLoader loaderDevices = new FXMLLoader(getClass().getResource("customerDevicesView.fxml"));
            Parent devicesContent = loaderDevices.load();
            customerDevicesController = loaderDevices.getController(); // Πάρε τον controller
            devicesContainer.getChildren().setAll(devicesContent);
            AnchorPane.setTopAnchor(devicesContent, 0.0);
            AnchorPane.setBottomAnchor(devicesContent, 0.0);
            AnchorPane.setLeftAnchor(devicesContent, 0.0);
            AnchorPane.setRightAnchor(devicesContent, 0.0);

            FXMLLoader loaderTasks = new FXMLLoader(getClass().getResource("customerTasksView.fxml"));
            Parent tasksContent = loaderTasks.load();
            customerTasksController = loaderTasks.getController();// Πάρε τον controller
            tasksContainer.getChildren().setAll(tasksContent);
            AnchorPane.setTopAnchor(tasksContent, 0.0);
            AnchorPane.setBottomAnchor(tasksContent, 0.0);
            AnchorPane.setLeftAnchor(tasksContent, 0.0);
            AnchorPane.setRightAnchor(tasksContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        setTooltip(btnData, "Άνοιγμα φακέλου με δεδομένα πελάτη");
        setTooltip(btnLabel, "Εκτύπωση ετικέτας πελάτη");
        setTooltip(btnAppointment,"Προσθήκη νέου ραντεβού");
        setTooltip(btnTask,"Προσθήκη νέας εργασίας");
        setTooltip(btnAddToMegasoft, "Προσθήκη πελάτη στο Megasoft");


        btnAfmSearch.setOnAction(event -> handleAfmSearch());
        btnAddressAdd.setDisable(true);
        btnAddToMegasoft.setDisable(true);
        btnAddToMegasoft.setVisible(false);
        btnShowToMegasoft.setDisable(true);
        btnShowToMegasoft.setVisible(false);
        btnData.setDisable(true);
        btnData.setVisible(false);
        btnLabel.setDisable(true);
        btnLabel.setVisible(false);
        btnAppointment.setDisable(true);
        btnAppointment.setVisible(false);
        btnTask.setDisable(true);
        btnTask.setVisible(false);
        tabTaxis.setDisable(true);
        tabMypos.setDisable(true);
        tabSimply.setDisable(true);
        tabEmblem.setDisable(true);
        tabDevices.setDisable(true);
        tabTasks.setDisable(true);

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
        setupTextFieldContextMenu(tfJob, contextMenu);
        setupTextFieldContextMenu(tfAfm, contextMenu);
        setupTextFieldContextMenu(tfPhone1, phoneContextMenu);
        setupTextFieldContextMenu(tfPhone2, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);
        setupTextFieldContextMenu(tfAddress, contextMenu);
        setupTextFieldContextMenu(tfTown, contextMenu);
        setupTextFieldContextMenu(tfPostCode,contextMenu);
        setupTextFieldContextMenu(tfManager, contextMenu);
        setupTextFieldContextMenu(tfManagerPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfAccName, emailContextMenu);
        setupTextFieldContextMenu(tfAccPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfAccMobile, phoneContextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);
        tfEmail2.setContextMenu(emailContextMenu);
        tfEmail2.setOnContextMenuRequested(e -> currentTextField = tfEmail2);
        tfAccEmail.setContextMenu(emailContextMenu);
        tfAccEmail.setOnContextMenuRequested(e -> currentTextField = tfAccEmail);

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
            if (currentTextField == tfPhone1 || currentTextField == tfPhone2 || currentTextField == tfMobile || currentTextField == tfManagerPhone) { // Εκτέλεση μόνο αν είναι στο tfEmail
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
            } else if (currentTextField == tfEmail2) { // Εκτέλεση μόνο αν είναι στο tfEmail2
                sendTestEmail(tfEmail2);
            }
            else if (currentTextField == tfAccEmail) { // Εκτέλεση μόνο αν είναι στο tfEmail2
                sendTestEmail(tfAccEmail);
            }
        });
    }


    public void setInitialAFM(String afm) {
        tfAfm.setText(afm); // Ορισμός αρχικής τιμής στο πεδίο ΑΦΜ
        btnAfmSearch.fire();
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


    public void setCustomerData(Customer customer) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(customer.getName());
        tfTitle.setText(customer.getTitle());
        tfJob.setText(customer.getJob());
        tfAfm.setText(customer.getAfm());
        tfPhone1.setText(customer.getPhone1());
        tfPhone2.setText(customer.getPhone2());
        tfMobile.setText(customer.getMobile());
        tfAddress.setText(customer.getAddress());
        tfTown.setText(customer.getTown());
        tfPostCode.setText(customer.getPostcode());
        tfEmail.setText(customer.getEmail());
        tfEmail2.setText(customer.getEmail2());
        tfManager.setText(customer.getManager());
        tfManagerPhone.setText(customer.getManagerPhone());
        taNotes.setText(customer.getNotes());
        tfAccName.setText(customer.getAccName());
        tfAccPhone.setText(customer.getAccPhone());
        tfAccMobile.setText(customer.getAccMobile());
        tfAccEmail.setText(customer.getAccEmail());


        btnAddressAdd.setDisable(false);
        DBHelper dbHelper = new DBHelper();
        if (dbHelper.hasSubAddress(customer.getCode())) {
            System.out.println("Έχει υποδιεύθυνση");
            btnAddressAdd.setStyle("-fx-border-color: #FF0000;");
        }
        if(dbHelper.hasApp(customer.getCode(),1)){
            tabMypos.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasApp(customer.getCode(),2)){
            tabSimply.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasApp(customer.getCode(),3)){
            tabTaxis.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasApp(customer.getCode(),4)){
            tabEmblem.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasDevice(customer.getCode())){
            tabDevices.getStyleClass().add("tabHas");
        }

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = customer.getCode();
        this.customer = customer;
        if (taxisViewController != null) {
            taxisViewController.setCustomer(customer);
        } else {
            System.out.println("TaxisViewController δεν είναι ακόμα έτοιμος.");
        }
        if (myposViewController != null) {
            myposViewController.setCustomer(customer);
        } else {
            System.out.println("myposViewController δεν είναι ακόμα έτοιμος.");
        }
        if (simplyViewController != null) {
            simplyViewController.setCustomer(customer);
        } else {
            System.out.println("simplyViewController δεν είναι ακόμα έτοιμος.");
        }
        if (emblemViewController != null) {
            emblemViewController.setCustomer(customer);
        } else {
            System.out.println("emblemViewController δεν είναι ακόμα έτοιμος.");
        }
        if (customerDevicesController != null) {
            customerDevicesController.setCustomer(customer);
        } else {
            System.out.println("customerDevicesController δεν είναι ακόμα έτοιμος.");
        }
        if (customerTasksController != null) {
            customerTasksController.setCustomer(customer);
        } else {
            System.out.println("customerTasksController δεν είναι ακόμα έτοιμος.");
        }

        btnAddToMegasoft.setDisable(false);
        btnAddToMegasoft.setVisible(true);
        btnShowToMegasoft.setDisable(false);
        btnShowToMegasoft.setVisible(true);
        btnData.setDisable(false);
        btnData.setVisible(true);
        btnLabel.setDisable(false);
        btnLabel.setVisible(true);
        btnAppointment.setDisable(false);
        btnAppointment.setVisible(true);
        btnTask.setDisable(false);
        btnTask.setVisible(true);
        tabTaxis.setDisable(false);
        tabMypos.setDisable(false);
        tabSimply.setDisable(false);
        tabEmblem.setDisable(false);
        tabDevices.setDisable(false);
        tabTasks.setDisable(false);
    }


    private void handleAfmSearch() {
        tfName.setText("");
        tfTitle.setText("");
        tfJob.setText("");
        tfAddress.setText("");
        tfTown.setText("");
        tfPostCode.setText("");
        String afm = tfAfm.getText();
        if (afm == null || afm.isEmpty()) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ εισάγετε ένα έγκυρο ΑΦΜ.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
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
                notifications.showError();});
            return;
        }

        Customer companyInfo = AfmResponseParser.parseResponse(responseXml);

        if (companyInfo != null) {
            tfName.setText(companyInfo.getName());
            tfTitle.setText(companyInfo.getTitle());
            tfJob.setText(companyInfo.getJob());
            tfAddress.setText(companyInfo.getAddress());
            tfTown.setText(companyInfo.getTown());
            tfPostCode.setText(companyInfo.getPostcode());
        } else {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Σφάλμα κατά την ανάγνωση των δεδομένων")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
        }
    }


    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addCustomer();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateCustomer();
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

    void addCustomer() {
        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        String afm = tfAfm.getText();
        String phone1 = (tfPhone1.getText() != null ? tfPhone1.getText() : "");
        String phone2 = (tfPhone2.getText() != null ? tfPhone2.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String primaryAddress = tfAddress.getText();  // Πρώτη διεύθυνση
        String town = tfTown.getText();
        String postcode = tfPostCode.getText();
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String email2 = (tfEmail2.getText() != null ? tfEmail2.getText() : "");
        String manager = (tfManager.getText() != null ? tfManager.getText() : "");
        String managerPhone = (tfManagerPhone.getText() != null ? tfManagerPhone.getText() : "");
        String notes = taNotes.getText();
        String accName = (tfAccName.getText() != null ? tfAccName.getText() : "");
        String accPhone = (tfAccPhone.getText() != null ? tfAccPhone.getText() : "");
        String accMobile = (tfAccMobile.getText() != null ? tfAccMobile.getText() : "");
        String accEmail = (tfAccEmail.getText() != null ? tfAccEmail.getText() : "");

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone1.startsWith("+30"))
            phone1 = phone1.substring(3);
        if (phone2.startsWith("+30"))
            phone2 = phone2.substring(3);
        if (managerPhone.startsWith("+30"))
            managerPhone = managerPhone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone1 = phone1.replaceAll("\\s+", "");
        phone2 = phone2.replaceAll("\\s+", "");
        managerPhone = managerPhone.replaceAll("\\s+", "");

        DBHelper dbHelper = new DBHelper();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        int customerId;
        if (dbHelper.isAfmExists(afm)) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Ο πελάτης με ΑΦΜ " + afm + " υπάρχει ήδη.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
        } else {
            // Εισαγωγή του πελάτη στον κύριο πίνακα με την πρώτη διεύθυνση
            customerId = dbHelper.insertCustomer(name, title, job, afm, phone1, phone2, mobile, primaryAddress, town, postcode, email, email2, manager, managerPhone, notes, accName, accPhone, accMobile, accEmail);
            // Εμφάνιση επιτυχίας
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();
                closeCurrentTab(); // Κλείσιμο του "Νέος Πελάτης"
                openCustomerTab(customerId); // Άνοιγμα καρτέλας με τον νέο πελάτη
            });
        }

    }
    private void openCustomerTab(int customerId) {
        if (customersController != null) {
            System.out.println(customerId);
            customersController.openCustomerTab(customerId);
        }
    }


    void updateCustomer() {
        DBHelper dbHelper = new DBHelper();

        String name = tfName.getText();
        String title = tfTitle.getText();
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        String afm = tfAfm.getText();
        String phone1 = (tfPhone1.getText() != null ? tfPhone1.getText() : "");
        String phone2 = (tfPhone2.getText() != null ? tfPhone2.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String address = tfAddress.getText();
        String town = tfTown.getText();
        String posCode = tfPostCode.getText();
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        String email2 = (tfEmail2.getText() != null ? tfEmail2.getText() : "");
        String manager = (tfManager.getText() != null ? tfManager.getText() : "");
        String managerPhone = (tfManagerPhone.getText() != null ? tfManagerPhone.getText() : "");
        String notes = taNotes.getText();
        String accName = (tfAccName.getText() != null ? tfAccName.getText() : "");
        String accPhone = (tfAccPhone.getText() != null ? tfAccPhone.getText() : "");
        String accMobile = (tfAccMobile.getText() != null ? tfAccMobile.getText() : "");
        String accEmail = (tfAccEmail.getText() != null ? tfAccEmail.getText() : "");

        if (mobile.startsWith("+30"))
            mobile = mobile.substring(3);
        if (phone1.startsWith("+30"))
            phone1 = phone1.substring(3);
        if (phone2.startsWith("+30"))
            phone2 = phone2.substring(3);
        if (managerPhone.startsWith("+30"))
            managerPhone = managerPhone.substring(3);
        mobile = mobile.replaceAll("\\s+", "");
        phone1 = phone1.replaceAll("\\s+", "");
        phone2 = phone2.replaceAll("\\s+", "");
        managerPhone = managerPhone.replaceAll("\\s+", "");

        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, posCode, email, email2, manager, managerPhone, notes, accName, accPhone, accMobile, accEmail);
        //showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
        Notifications notifications = Notifications.create()
                .title("Επιτυχία")
                .text("Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
        notifications.showInformation();
    }

    public void addAddress(ActionEvent event) {
        if (tfAddress.getText() == null || tfAddress.getText().isEmpty()) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Δεν υπάρχει κεντρική διεύθυνση!")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addressView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            AddressViewController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setCustomer(customer);

            dialog.setTitle("Διευθύνσεις πελάτη");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public void folderClick(ActionEvent event) {
        CustomerFolderManager folderManager = new CustomerFolderManager();

        // Κλήση της μεθόδου για δημιουργία ή άνοιγμα του φακέλου
        folderManager.createOrOpenCustomerFolder(customer.getName(), customer.getAfm());
    }

    public void labelClick(ActionEvent event) {
        LabelPrintHelper.printCustomerLabel(customer);
    }

    public void newAppointment(ActionEvent actionEvent) {
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addAppointment.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Ραντεβού");

                AddAppointmentController controller = loader.getController();

                // Προ-συμπλήρωση πελάτη
                controller.setCustomerId(customer.getCode());
                controller.setCustomerName(customer.getName());

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveAppointment();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη ραντεβού.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void newTask(ActionEvent actionEvent){
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Εργασίας");
                AddTaskController controller = loader.getController();
                controller.setCustomerId(customer.getCode());
                controller.setCustomerName(customer.getName());
                controller.lock();
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
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    @FXML
    private void showEmailDialog(ActionEvent actionEvent) {
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Αποστολή Email");
                EmailDialogController controller = loader.getController();
                controller.setCustomer(customer);
                controller.setEmail(tfEmail.getText());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                dialog.show();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    @FXML
    private void showEmailDialog2(ActionEvent actionEvent) {
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Αποστολή Email");
                EmailDialogController controller = loader.getController();
                controller.setCustomer(customer);
                controller.setEmail(tfEmail2.getText());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                dialog.show();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    @FXML
    private void showEmailDialogAcc(ActionEvent actionEvent) {
        if (customer != null) {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Αποστολή Email");
                EmailDialogController controller = loader.getController();
                controller.setCustomer(customer);
                controller.setEmail(tfAccEmail.getText());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                dialog.show();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }

    public void addMegasoft(ActionEvent event) {
        PrismaWinAutomation.addCustomer(customer);
    }

    public void showMegasoft(ActionEvent event) {
        PrismaWinAutomation.showCustomer(customer);
    }

    public void selectTaxisTab() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(tabTaxis));
    }

    public void selectMyPOSTab() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(tabMypos));
    }

    public void selectSimplyTab() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(tabSimply));
    }

    public void selectEmbelmTab() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(tabEmblem));
    }
}
