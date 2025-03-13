package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddCustomerController {
    private TabPane mainTabPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane taxisContainer, myposContainer, simplyContainer, emblemContainer, erganiContainer, devicesContainer, tasksContainer, subsContainer, offersContainer;
    @FXML
    private Tab tabTaxis, tabMypos, tabSimply, tabEmblem, tabErgani, tabDevices, tabTasks, tabAccountant, tabSubs, tabOffers, tabNotes;
    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfPostCode, tfEmail, tfEmail2, tfManager, tfManagerPhone, tfBalance;
    @FXML
    private ComboBox<String> tfRecommendation;
    @FXML
    private TextField tfAccPhone, tfAccMobile, tfAccEmail, tfAccName1, tfAccEmail1, tfAccErganiEmail;
    @FXML
    private ComboBox<Accountant> tfAccName;
    @FXML
    private Button btnAfmSearch;
    @FXML
    private Button btnAddressAdd;
    @FXML
    private TextArea taNotes, taBalanceReason;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button btnEmail, btnEmail2, btnAccEmail, btnAccEmail1;
    @FXML
    Button btnAddToMegasoft, btnData, btnLabel,btnCopy, btnAppointment,btnTask;
    @FXML
    private Label lblBlance;

    private TaxisViewController taxisViewController;
    private MyposViewController myposViewController;
    private SimplyViewController simplyViewController;
    private EmblemViewController emblemViewController;
    private ErganiViewController erganiViewController;
    private CustomerDevicesController customerDevicesController;
    private CustomerTasksController customerTasksController;
    private CustomerSubsController customerSubsController;
    private CustomerOffersController customerOffersController;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Customer customer;
    private FilteredList<Accountant> filteredAccountants;
    private ObservableList<String> recommendationList = FXCollections.observableArrayList();

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

            FXMLLoader loaderErgani = new FXMLLoader(getClass().getResource("erganiView.fxml"));
            Parent erganiContent = loaderErgani.load();
            erganiViewController = loaderErgani.getController(); // Πάρε τον controller
            erganiContainer.getChildren().setAll(erganiContent);
            AnchorPane.setTopAnchor(erganiContent, 0.0);
            AnchorPane.setBottomAnchor(erganiContent, 0.0);
            AnchorPane.setLeftAnchor(erganiContent, 0.0);
            AnchorPane.setRightAnchor(erganiContent, 0.0);

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

            FXMLLoader loaderSubs = new FXMLLoader(getClass().getResource("customerSubsView.fxml"));
            Parent subsContent = loaderSubs.load();
            customerSubsController = loaderSubs.getController();// Πάρε τον controller
            subsContainer.getChildren().setAll(subsContent);
            AnchorPane.setTopAnchor(subsContent, 0.0);
            AnchorPane.setBottomAnchor(subsContent, 0.0);
            AnchorPane.setLeftAnchor(subsContent, 0.0);
            AnchorPane.setRightAnchor(subsContent, 0.0);

            FXMLLoader loaderOffers = new FXMLLoader(getClass().getResource("customerOffersView.fxml"));
            Parent offersContent = loaderOffers.load();
            customerOffersController = loaderOffers.getController();// Πάρε τον controller
            offersContainer.getChildren().setAll(offersContent);
            AnchorPane.setTopAnchor(offersContent, 0.0);
            AnchorPane.setBottomAnchor(offersContent, 0.0);
            AnchorPane.setLeftAnchor(offersContent, 0.0);
            AnchorPane.setRightAnchor(offersContent, 0.0);

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
        btnData.setDisable(true);
        btnData.setVisible(false);
        btnLabel.setDisable(true);
        btnLabel.setVisible(false);
        btnCopy.setDisable(true);
        btnCopy.setVisible(false);
        btnAppointment.setDisable(true);
        btnAppointment.setVisible(false);
        btnTask.setDisable(true);
        tfBalance.setDisable(true);
        tfBalance.setVisible(false);
        lblBlance.setVisible(false);
        btnTask.setVisible(false);
        tabTaxis.setDisable(true);
        tabMypos.setDisable(true);
        tabSimply.setDisable(true);
        tabEmblem.setDisable(true);
        tabErgani.setDisable(true);
        tabDevices.setDisable(true);
        tabTasks.setDisable(true);
        tabSubs.setDisable(true);
        tabOffers.setDisable(true);

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
        setupTextFieldContextMenu(tfAccPhone, phoneContextMenu);
        setupTextFieldContextMenu(tfAccMobile, phoneContextMenu);

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);
        tfEmail2.setContextMenu(emailContextMenu);
        tfEmail2.setOnContextMenuRequested(e -> currentTextField = tfEmail2);
        tfAccEmail.setContextMenu(emailContextMenu);
        tfAccEmail.setOnContextMenuRequested(e -> currentTextField = tfAccEmail);
        tfAccEmail1.setContextMenu(emailContextMenu);
        tfAccEmail1.setOnContextMenuRequested(e -> currentTextField = tfAccEmail1);
        tfAccErganiEmail.setContextMenu(emailContextMenu);
        tfAccErganiEmail.setOnContextMenuRequested(e -> currentTextField = tfAccErganiEmail);

        btnEmail.setUserData(tfEmail);
        btnEmail2.setUserData(tfEmail2);
        btnAccEmail.setUserData(tfAccEmail);
        btnAccEmail1.setUserData(tfAccEmail1);

        btnEmail.setOnAction(this::showEmailDialog);
        btnEmail2.setOnAction(this::showEmailDialog);
        btnAccEmail.setOnAction(this::showEmailDialog);
        btnAccEmail1.setOnAction(this::showEmailDialog);



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
        DBHelper dbHelper = new DBHelper();
        List<Accountant> accountants = dbHelper.getAccountants();
        accountants.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        filteredAccountants = new FilteredList<>(FXCollections.observableArrayList(accountants));
        //accountantsList.clear();
        //accountantsList.addAll(dbHelper.getAccountants());
        //filteredAccountants.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        tfAccName.setItems(filteredAccountants);
        tfAccName.setConverter(new StringConverter<Accountant>() {
            @Override
            public String toString(Accountant accountant) {
                return accountant != null ? accountant.getName() : "";
            }

            @Override
            public Accountant fromString(String string) {
                return accountants.stream()
                        .filter(accountant -> accountant.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        tfAccName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tfAccPhone.setText(newValue.getPhone());
                tfAccMobile.setText(newValue.getMobile());
                tfAccEmail.setText(newValue.getEmail());
                tfAccErganiEmail.setText(newValue.getErganiEmail());
            } else {
                tfAccPhone.clear();
                tfAccMobile.clear();
                tfAccEmail.clear();
                tfAccErganiEmail.clear();
            }
        });
        setupComboBoxFilter(tfAccName,filteredAccountants);
        recommendationList.clear();
        recommendationList.addAll(dbHelper.getRecomedations());
        tfRecommendation.setItems(recommendationList);
    }

    private <T> void setupComboBoxFilter(ComboBox<T> comboBox, FilteredList<T> filteredList) {
        // Ακροατής για το TextField του ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toLowerCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Εμφάνιση όλων των στοιχείων αν δεν υπάρχει φίλτρο
                }
                // Ελέγχουμε αν το όνομα του αντικειμένου ταιριάζει με το φίλτρο
                return item.toString().toLowerCase().contains(filterText);
            });
        });

        // Ακροατής για την επιλογή ενός στοιχείου
        comboBox.setOnHidden(event -> {
            T selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });

        // Ακροατής για την αλλαγή της επιλογής
        comboBox.setOnAction(event -> {
            T selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
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
        tfAccName1.setText(customer.getAccName1());
        tfAccEmail1.setText(customer.getAccEmail1());
        tfBalance.setText(customer.getBalance());
        taBalanceReason.setText(customer.getBalanceReason());

        btnAddressAdd.setDisable(false);

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
        if (erganiViewController != null) {
            erganiViewController.setCustomer(customer);
        } else {
            System.out.println("erganiViewController δεν είναι ακόμα έτοιμος.");
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
        if (customerSubsController != null) {
            customerSubsController.setCustomer(customer);
        } else {
            System.out.println("customerSubsController δεν είναι ακόμα έτοιμος.");
        }
        if (customerOffersController != null) {
            customerOffersController.setCustomer(customer);
        } else {
            System.out.println("customerOffersController δεν είναι ακόμα έτοιμος.");
        }

        btnAddToMegasoft.setDisable(false);
        btnAddToMegasoft.setVisible(true);
        btnData.setDisable(false);
        btnData.setVisible(true);
        btnLabel.setDisable(false);
        btnLabel.setVisible(true);
        btnCopy.setDisable(false);
        btnCopy.setVisible(true);
        btnAppointment.setDisable(false);
        btnAppointment.setVisible(true);
        btnTask.setDisable(false);
        btnTask.setVisible(true);
        tfBalance.setDisable(false);
        tfBalance.setVisible(true);
        lblBlance.setVisible(true);
        tabTaxis.setDisable(false);
        tabMypos.setDisable(false);
        tabSimply.setDisable(false);
        tabEmblem.setDisable(false);
        tabErgani.setDisable(false);
        tabDevices.setDisable(false);
        tabTasks.setDisable(false);
        tabSubs.setDisable(false);
        tabOffers.setDisable(false);

        hasTabs();
        setAccountant();
        setRecommendation();
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

    public void handleRefreshButton() {
        hasTabs();
        setAccountant();
        setRecommendation();
    }

    private void setRecommendation() {
        DBHelper dbHelper = new DBHelper();
        recommendationList.clear();
        recommendationList.addAll(dbHelper.getRecomedations());
        tfRecommendation.setItems(recommendationList);

        for (String rec : recommendationList) {
            if (rec.equals(customer.getRecommendation())) {
                tfRecommendation.getSelectionModel().select(rec);
                break;
            }
        }
    }

    private void setAccountant() {
        DBHelper dbHelper = new DBHelper();
        List<Accountant> accountants = dbHelper.getAccountants();
        accountants.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        filteredAccountants = new FilteredList<>(FXCollections.observableArrayList(accountants));
        tfAccName.setItems(filteredAccountants);
        tfAccName.setConverter(new StringConverter<Accountant>() {
            @Override
            public String toString(Accountant accountant) {
                return accountant != null ? accountant.getName() : "";
            }

            @Override
            public Accountant fromString(String string) {
                return accountants.stream()
                        .filter(accountant -> accountant.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        tfAccName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tfAccPhone.setText(newValue.getPhone());
                tfAccMobile.setText(newValue.getMobile());
                tfAccEmail.setText(newValue.getEmail());
                tfAccErganiEmail.setText(newValue.getErganiEmail());
            } else {
                tfAccPhone.clear();
                tfAccMobile.clear();
                tfAccEmail.clear();
                tfAccErganiEmail.clear();
            }
        });

        for (Accountant accountant : filteredAccountants) {
            if (accountant.getId() == customer.getAccId()) {
                tfAccName.getSelectionModel().select(accountant);
                tfAccPhone.setText(accountant.getPhone());
                tfAccMobile.setText(accountant.getMobile());
                tfAccEmail.setText(accountant.getEmail());
                tfAccErganiEmail.setText(accountant.getErganiEmail());
                break;
            }
        }
        setupComboBoxFilter(tfAccName,filteredAccountants);
    }

    private void hasTabs() {
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
        if(dbHelper.hasApp(customer.getCode(),5)){
            tabErgani.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasDevice(customer.getCode())){
            tabDevices.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasTask(customer.getCode())){
            tabTasks.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasSub(customer.getCode())){
            tabSubs.getStyleClass().add("tabHas");
        }
        if(dbHelper.hasOffer(customer.getCode())){
            tabOffers.getStyleClass().add("tabHas");
        }
        if (customer.getAccId() != 0){
            tabAccountant.getStyleClass().add("tabHas");
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
        String accName1 = (tfAccName1.getText() != null ? tfAccName1.getText() : "");
        String accEmail1 = (tfAccEmail1.getText() != null ? tfAccEmail1.getText() : "");
        String selectedRecommendation = tfRecommendation.getEditor().getText().trim();
        Accountant selectedAccountant = tfAccName.getSelectionModel().getSelectedItem();
        int accId = (selectedAccountant != null) ? selectedAccountant.getId() : 0;
        String balance = tfBalance.getText();
        String balanceReason = taBalanceReason.getText();


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
            customerId = dbHelper.insertCustomer(name, title, job, afm, phone1, phone2, mobile, primaryAddress, town, postcode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, selectedRecommendation, balance, balanceReason);
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
        customer.setName(name);
        String title = tfTitle.getText();
        customer.setTitle(title);
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        customer.setJob(job);
        String afm = tfAfm.getText();
        customer.setAfm(afm);
        String phone1 = (tfPhone1.getText() != null ? tfPhone1.getText() : "");
        String phone2 = (tfPhone2.getText() != null ? tfPhone2.getText() : "");
        String mobile = (tfMobile.getText() != null ? tfMobile.getText() : "");
        String address = tfAddress.getText();
        customer.setAddress(address);
        String town = tfTown.getText();
        customer.setTown(town);
        String posCode = tfPostCode.getText();
        customer.setPostcode(posCode);
        String email = (tfEmail.getText() != null ? tfEmail.getText() : "");
        customer.setEmail(email);
        String email2 = (tfEmail2.getText() != null ? tfEmail2.getText() : "");
        customer.setEmail2(email2);
        String manager = (tfManager.getText() != null ? tfManager.getText() : "");
        customer.setManager(manager);
        String managerPhone = (tfManagerPhone.getText() != null ? tfManagerPhone.getText() : "");
        String notes = taNotes.getText();
        customer.setNotes(notes);
        String accName1 = (tfAccName1.getText() != null ? tfAccName1.getText() : "");
        customer.setAccName1(accName1);
        String accEmail1 = (tfAccEmail1.getText() != null ? tfAccEmail1.getText() : "");
        customer.setAccEmail1(accEmail1);
        String selectedRecommendation = tfRecommendation.getEditor().getText().trim();
        customer.setRecommendation(selectedRecommendation);
        String balance = tfBalance.getText();
        customer.setBalance(balance);
        String balanceReason = taBalanceReason.getText();
        customer.setBalanceReason(balanceReason);

        Accountant selectedAccountant = tfAccName.getSelectionModel().getSelectedItem();
        int accId = (selectedAccountant != null) ? selectedAccountant.getId() : 0;
        customer.setAccId(accId);
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
        customer.setMobile(mobile);
        customer.setPhone1(phone1);
        customer.setPhone2(phone2);
        customer.setManagerPhone(managerPhone);


        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, posCode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, selectedRecommendation, balance, balanceReason);

        String accName = tfAccName.getValue().toString();
        String accPhone = tfAccPhone.getText();
        if (accPhone.startsWith("+30"))
            accPhone = accPhone.substring(3);
        String accMobile = tfAccMobile.getText();
        if (accMobile.startsWith("+30"))
            accMobile = accMobile.substring(3);
        String accEmail = tfAccEmail.getText();
        String accErganiEmail = tfAccErganiEmail.getText();

        dbHelper.updateAccountant(accId, accName, accPhone, accMobile, accEmail, accErganiEmail);

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

    public void copyClick (ActionEvent event){
        String msg = "Στοιχεία πελάτη" +
                "\nΕπωνυμία: " + customer.getName() +
                "\nΤίτλος: " + customer.getTitle() +
                "\nΕπάγγελμα: " + customer.getJob() +
                "\nΔιεύθυνση: " + customer.getAddress() +
                "\nΠόλη: " + customer.getTown() +
                "\nΤ.Κ.: " + customer.getPostcode() +
                "\nΑΦΜ: " + customer.getAfm() +
                "\nEmail: " + customer.getEmail() +
                "\nΤηλέφωνο: " + customer.getPhone1() +
                "\nΚινητό: " + customer.getMobile();
        copyTextToClipboard(msg);
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

    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        Notifications notifications = Notifications.create()
                .title("Αντιγραγή στο πρόχειρο")
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

    private void showEmailDialog(ActionEvent actionEvent) {
        Button clickedButton = (Button) actionEvent.getSource(); // Ποιο κουμπί πατήθηκε;
        TextField emailField = (TextField) clickedButton.getUserData(); // Παίρνουμε το TextField που είναι συνδεδεμένο με το κουμπί
        if (emailField != null && !emailField.getText().isEmpty()) {
            try {
                String email = emailField.getText();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Αποστολή Email");
                EmailDialogController controller = loader.getController();
                controller.setCustomer(customer);
                controller.setEmail(email);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                dialog.show();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
            }
        }
    }
    public void addAccountant(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newAccountant.fxml"));
            Parent root = loader.load();

            AddAccountantController controller = loader.getController();
            controller.setCallback(this::updateAccountantComboBox); // Ορισμός callback για ενημέρωση

            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setTitle("Νέος Λογιστής");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Περιμένει να κλείσει το παράθυρο
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateAccountantComboBox(Accountant newAccountant) {
        tfAccName.getItems().add(newAccountant); // Προσθήκη στη λίστα
        tfAccName.getSelectionModel().select(newAccountant); // Επιλογή
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog();
        }
    }

    private void openNotesDialog() {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Σημειώσεων");

        TextArea expandedTextArea = new TextArea(customer.getBalanceReason());
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(600, 500);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (customer.getBalanceReason() != null && !customer.getBalanceReason().isEmpty()) {
            expandedTextArea.setText(customer.getBalanceReason());
            expandedTextArea.positionCaret(customer.getBalanceReason().length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(evt -> {
            taBalanceReason.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);
        //vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void addMegasoft(ActionEvent event) {
        DBHelper dbHelper = new DBHelper();
        if (dbHelper.isAfmExistsMegasoft(customer.getAfm()))
            PrismaWinAutomation.showCustomer(customer);
        else
            PrismaWinAutomation.addCustomer(customer);
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

    public void selectErganiTab(){ Platform.runLater(() -> tabPane.getSelectionModel().select(tabErgani)); }
}
