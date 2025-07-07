package org.easytech.pelatologio;

import com.jfoenix.controls.JFXCheckBox;
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
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Accountant;
import org.easytech.pelatologio.models.Customer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class AddCustomerController {
    private TabPane mainTabPane;
    private Tab myTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane taxisContainer, myposContainer, simplyContainer, emblemContainer, erganiContainer, pelatologioContainer, nineposContainer, devicesContainer, tasksContainer, subsContainer, offersContainer, ordersContainer, notesContainer;
    @FXML
    private Tab tabTaxis, tabMypos, tabSimply, tabEmblem, tabErgani, tabPelatologio, tabNinepos, tabDevices, tabTasks, tabAccountant, tabSubs, tabOffers, tabOrders, tabNotes;
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
    Button btnAddToMegasoft, btnData, btnLabel, btnCopy, btnTask, btnAcs;
    @FXML
    private Label lblBlance;
    @FXML
    public JFXCheckBox checkboxActive;
    @FXML
    Button btnPhone1, btnPhone2, btnMobile, btnPhoneManager, btnAccPhone, btnAccMobile;

    private TaxisViewController taxisViewController;
    private MyposViewController myposViewController;
    private SimplyViewController simplyViewController;
    private EmblemViewController emblemViewController;
    private ErganiViewController erganiViewController;
    private PelatologioViewController pelatologioViewController;
    private NineposViewController nineposViewController;
    private CustomerDevicesController customerDevicesController;
    private CustomerTasksController customerTasksController;
    private CustomerSubsController customerSubsController;
    private CustomerOffersController customerOffersController;
    private CustomerOrdersController customerOrdersController;

    int code = 0;
    private boolean hasUnsavedChanges = false; // Νέο flag για παρακολούθηση αλλαγών

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Customer customer;
    private FilteredList<Accountant> filteredAccountants;
    private ObservableList<String> recommendationList = FXCollections.observableArrayList();
    private FilteredList<String> filteredRecommendations;
    private CustomersController customersController;

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane, Tab myTab) {
        this.mainTabPane = mainTabPane;
        this.myTab = myTab;
        setupCloseHandler();
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

            FXMLLoader loaderPelatologio = new FXMLLoader(getClass().getResource("pelatologioView.fxml"));
            Parent pelatologioContent = loaderPelatologio.load();
            pelatologioViewController = loaderPelatologio.getController(); // Πάρε τον controller
            pelatologioContainer.getChildren().setAll(pelatologioContent);
            AnchorPane.setTopAnchor(pelatologioContent, 0.0);
            AnchorPane.setBottomAnchor(pelatologioContent, 0.0);
            AnchorPane.setLeftAnchor(pelatologioContent, 0.0);
            AnchorPane.setRightAnchor(pelatologioContent, 0.0);

            FXMLLoader loaderNinepos = new FXMLLoader(getClass().getResource("nineposView.fxml"));
            Parent nineposContent = loaderNinepos.load();
            nineposViewController = loaderNinepos.getController(); // Πάρε τον controller
            nineposContainer.getChildren().setAll(nineposContent);
            AnchorPane.setTopAnchor(nineposContent, 0.0);
            AnchorPane.setBottomAnchor(nineposContent, 0.0);
            AnchorPane.setLeftAnchor(nineposContent, 0.0);
            AnchorPane.setRightAnchor(nineposContent, 0.0);

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

            FXMLLoader loaderOrders = new FXMLLoader(getClass().getResource("ordersCustView.fxml"));
            Parent ordersContent = loaderOrders.load();
            customerOrdersController = loaderOrders.getController();// Πάρε τον controller
            ordersContainer.getChildren().setAll(ordersContent);
            AnchorPane.setTopAnchor(ordersContent, 0.0);
            AnchorPane.setBottomAnchor(ordersContent, 0.0);
            AnchorPane.setLeftAnchor(ordersContent, 0.0);
            AnchorPane.setRightAnchor(ordersContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        setTooltip(btnData, "Άνοιγμα φακέλου με δεδομένα πελάτη");
        setTooltip(btnLabel, "Εκτύπωση ετικέτας πελάτη");
        setTooltip(btnTask, "Προσθήκη νέας εργασίας");
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
        btnTask.setDisable(true);
        btnTask.setVisible(false);
        btnAcs.setDisable(true);
        btnAcs.setVisible(false);
        tfBalance.setDisable(true);
        tfBalance.setVisible(false);
        lblBlance.setVisible(false);
        checkboxActive.setVisible(false);
        checkboxActive.setDisable(true);
        btnTask.setVisible(false);
        tabTaxis.setDisable(true);
        tabMypos.setDisable(true);
        tabSimply.setDisable(true);
        tabEmblem.setDisable(true);
        tabPelatologio.setDisable(true);
        tabNinepos.setDisable(true);
        tabErgani.setDisable(true);
        tabDevices.setDisable(true);
        tabTasks.setDisable(true);
        tabSubs.setDisable(true);
        tabOffers.setDisable(true);
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
        setupTextFieldContextMenu(tfJob, contextMenu);
        setupTextFieldContextMenu(tfAfm, contextMenu);
        setupTextFieldContextMenu(tfPhone1, phoneContextMenu);
        setupTextFieldContextMenu(tfPhone2, phoneContextMenu);
        setupTextFieldContextMenu(tfMobile, phoneContextMenu);
        setupTextFieldContextMenu(tfAddress, contextMenu);
        setupTextFieldContextMenu(tfTown, contextMenu);
        setupTextFieldContextMenu(tfPostCode, contextMenu);
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

        btnPhone1.setUserData(tfPhone1);
        btnPhone1.setOnAction(PhoneCall::callHandle);
        btnPhone2.setUserData(tfPhone2);
        btnPhone2.setOnAction(PhoneCall::callHandle);
        btnMobile.setUserData(tfMobile);
        btnMobile.setOnAction(PhoneCall::callHandle);
        btnPhoneManager.setUserData(tfManagerPhone);
        btnPhoneManager.setOnAction(PhoneCall::callHandle);
        btnAccPhone.setUserData(tfAccPhone);
        btnAccPhone.setOnAction(PhoneCall::callHandle);
        btnAccMobile.setUserData(tfAccMobile);
        btnAccMobile.setOnAction(PhoneCall::callHandle);


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
            } else if (currentTextField == tfAccEmail) { // Εκτέλεση μόνο αν είναι στο tfEmail2
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
        setupComboBoxFilter(tfAccName, filteredAccountants);
        recommendationList.clear();
        recommendationList.addAll(dbHelper.getRecomedations());
//        tfRecommendation.setItems(recommendationList);
        filteredRecommendations = new FilteredList<>(recommendationList);
        tfRecommendation.setItems(filteredRecommendations);
        setupComboBoxFilterForStrings(tfRecommendation, filteredRecommendations);


        // Προσθήκη ακροατών αλλαγών στα πεδία
        Platform.runLater(() -> {
            setupFieldListeners();
            this.hasUnsavedChanges = false;
            updateTabTitle("");
        });
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
        setupFieldListeners();
        this.hasUnsavedChanges = false;
        updateTabTitle("");


        if (customer.getActive())
            checkboxActive.setSelected(true);
        else
            checkboxActive.setSelected(false);


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
        if (pelatologioViewController != null) {
            pelatologioViewController.setCustomer(customer);
        } else {
            System.out.println("pelatologioViewController δεν είναι ακόμα έτοιμος.");
        }
        if (nineposViewController != null) {
            nineposViewController.setCustomer(customer);
        } else {
            System.out.println("nineposViewController δεν είναι ακόμα έτοιμος.");
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
        if (customerOrdersController != null) {
            customerOrdersController.setCustomer(customer);
        } else {
            System.out.println("customerOrdersController δεν είναι ακόμα έτοιμος.");
        }

        btnAddToMegasoft.setDisable(false);
        btnAddToMegasoft.setVisible(true);
        btnData.setDisable(false);
        btnData.setVisible(true);
        btnLabel.setDisable(false);
        btnLabel.setVisible(true);
        btnCopy.setDisable(false);
        btnCopy.setVisible(true);
        btnTask.setDisable(false);
        btnTask.setVisible(true);
        btnAcs.setDisable(false);
        btnAcs.setVisible(true);
        tfBalance.setDisable(false);
        tfBalance.setVisible(true);
        lblBlance.setVisible(true);
        checkboxActive.setVisible(true);
        checkboxActive.setDisable(false);
        tabTaxis.setDisable(false);
        tabMypos.setDisable(false);
        tabSimply.setDisable(false);
        tabEmblem.setDisable(false);
        tabErgani.setDisable(false);
        tabPelatologio.setDisable(false);
        tabNinepos.setDisable(false);
        tabDevices.setDisable(false);
        tabTasks.setDisable(false);
        tabSubs.setDisable(false);
        tabOffers.setDisable(false);
        tabOrders.setDisable(false);

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
                notifications.showError();
            });
        }
    }


    public void handleOkButton() {
        if (code == 0) {
            addCustomer();
        } else {
            updateCustomer();
        }
        hasUnsavedChanges = false;
        updateTabTitle(""); // Αφαίρεση αστερίσκου
    }

    public void handleRefreshButton() {
        hasTabs();
        setAccountant();
        setRecommendation();
        taxisViewController.setCustomer(customer);
        myposViewController.setCustomer(customer);
        simplyViewController.setCustomer(customer);
        emblemViewController.setCustomer(customer);
        erganiViewController.setCustomer(customer);
        pelatologioViewController.setCustomer(customer);
        nineposViewController.setCustomer(customer);
        customerDevicesController.setCustomer(customer);
        customerTasksController.setCustomer(customer);
        customerSubsController.setCustomer(customer);
        customerOffersController.setCustomer(customer);
        customerOrdersController.setCustomer(customer);

    }

    private void setRecommendation() {
        DBHelper dbHelper = new DBHelper();
        recommendationList.clear();
        recommendationList.addAll(dbHelper.getRecomedations());
        filteredRecommendations = new FilteredList<>(recommendationList);
        tfRecommendation.setItems(filteredRecommendations);
        //tfRecommendation.setItems(recommendationList);

        for (String rec : recommendationList) {
            if (rec.equals(customer.getRecommendation())) {
                tfRecommendation.getSelectionModel().select(rec);
                break;
            }
        }
        setupComboBoxFilterForStrings(tfRecommendation, filteredRecommendations);
    }

    private void setupComboBoxFilterForStrings(ComboBox<String> comboBox, FilteredList<String> filteredList) {
        // Listener for the TextField of the ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toUpperCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Show all items if no filter
                }
                // Check if the item contains the filter text
                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);
                return (item.toUpperCase().contains(search1) || item.toUpperCase().contains(search2));
            });
        });

        // Listener for when an item is selected
        comboBox.setOnHidden(event -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem);
            }
        });

        // Listener for when the selection changes
        comboBox.setOnAction(event -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem);
            }
        });
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
        setupComboBoxFilter(tfAccName, filteredAccountants);
    }

    private void hasTabs() {
        DBHelper dbHelper = new DBHelper();
        if (dbHelper.hasSubAddress(customer.getCode())) {
            btnAddressAdd.setStyle("-fx-border-color: #FF0000;");
        }
        if (dbHelper.hasApp(customer.getCode(), 1)) {
            tabMypos.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 2)) {
            tabSimply.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 3)) {
            tabTaxis.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 4)) {
            tabEmblem.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 5)) {
            tabErgani.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 6)) {
            tabPelatologio.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasApp(customer.getCode(), 7)) {
            tabNinepos.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasDevice(customer.getCode())) {
            tabDevices.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasTask(customer.getCode())) {
            tabTasks.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasSub(customer.getCode())) {
            tabSubs.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasOffer(customer.getCode())) {
            tabOffers.getStyleClass().add("tabHas");
        }
        if (customer.getAccId() != 0) {
            tabAccountant.getStyleClass().add("tabHas");
        }
        if (dbHelper.hasOrders(customer.getCode())) {
            tabOrders.getStyleClass().add("tabHas");
        }
        if (!customer.getNotes().isEmpty()) {
            tabNotes.getStyleClass().add("tabHas");
        }

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
        String phone1 = tfPhone1.getText() == null ? "" : tfPhone1.getText();
        String phone2 = tfPhone2.getText() == null ? "" : tfPhone2.getText();
        String mobile = tfMobile.getText() == null ? "" : tfMobile.getText();
        String address = tfAddress.getText();
        customer.setAddress(address);
        String town = tfTown.getText();
        customer.setTown(town);
        String posCode = tfPostCode.getText();
        customer.setPostcode(posCode);
        String email = tfEmail.getText() == null ? "" : tfEmail.getText();
        customer.setEmail(email);
        String email2 = tfEmail2.getText() == null ? "" : tfEmail2.getText();
        customer.setEmail2(email2);
        String manager = tfManager.getText() == null ? "" : tfManager.getText();
        customer.setManager(manager);
        String managerPhone = tfManagerPhone.getText() == null ? "" : tfManagerPhone.getText();
        String notes = taNotes.getText();
        customer.setNotes(notes);
        String accName1 = tfAccName1.getText() == null ? "" : tfAccName1.getText();
        customer.setAccName1(accName1);
        String accEmail1 = tfAccEmail1.getText() == null ? "" : tfAccEmail1.getText();
        customer.setAccEmail1(accEmail1);
        String selectedRecommendation = tfRecommendation.getEditor().getText().trim();
        customer.setRecommendation(selectedRecommendation);
        String balance = tfBalance.getText();
        customer.setBalance(balance);
        String balanceReason = taBalanceReason.getText();
        customer.setBalanceReason(balanceReason);
        Boolean isActive = checkboxActive.isSelected();
        customer.setActive(isActive);

        Accountant selectedAccountant = tfAccName.getSelectionModel().getSelectedItem();
        int accId = selectedAccountant != null ? selectedAccountant.getId() : 0;
        customer.setAccId(accId);

        if (mobile.startsWith("+30")) {
            mobile = mobile.substring(3);
        }
        if (phone1.startsWith("+30")) {
            phone1 = phone1.substring(3);
        }
        if (phone2.startsWith("+30")) {
            phone2 = phone2.substring(3);
        }
        if (managerPhone.startsWith("+30")) {
            managerPhone = managerPhone.substring(3);
        }
        mobile = mobile.replaceAll("\\s+", "");
        phone1 = phone1.replaceAll("\\s+", "");
        phone2 = phone2.replaceAll("\\s+", "");
        managerPhone = managerPhone.replaceAll("\\s+", "");
        customer.setMobile(mobile);
        customer.setPhone1(phone1);
        customer.setPhone2(phone2);
        customer.setManagerPhone(managerPhone);

        dbHelper.updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, posCode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, selectedRecommendation, balance, balanceReason, isActive);

        String accName = tfAccName.getValue() != null ? tfAccName.getValue().toString() : "";
        String accPhone = tfAccPhone.getText();
        if (accPhone.startsWith("+30")) {
            accPhone = accPhone.substring(3);
        }
        String accMobile = tfAccMobile.getText();
        if (accMobile.startsWith("+30")) {
            accMobile = accMobile.substring(3);
        }
        String accEmail = tfAccEmail.getText();
        String accErganiEmail = tfAccErganiEmail.getText();

        dbHelper.updateAccountant(accId, accName, accPhone, accMobile, accEmail, accErganiEmail);

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

    public void copyClick(ActionEvent event) {
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


    public void newTask(ActionEvent actionEvent) {
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
            expandedTextArea.setText(taBalanceReason.getText());
            expandedTextArea.positionCaret(taBalanceReason.getText().length());
        } else {
            expandedTextArea.setText(taBalanceReason.getText()); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(taBalanceReason.getText().length()); // Τοποθετήστε τον κέρσορα στην αρχή
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

    public void selectErganiTab() {
        Platform.runLater(() -> tabPane.getSelectionModel().select(tabErgani));
    }

    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('\u0041', '\u0391');  // uppercase A
        ENGLISH_TO_GREEK.put('\u0042', '\u0392');  // uppercase B
        ENGLISH_TO_GREEK.put('\u0043', '\u03A8');  // uppercase C
        ENGLISH_TO_GREEK.put('\u0044', '\u0394');  // uppercase D
        ENGLISH_TO_GREEK.put('\u0045', '\u0395');  // uppercase E
        ENGLISH_TO_GREEK.put('\u0046', '\u03A6');  // uppercase F
        ENGLISH_TO_GREEK.put('\u0047', '\u0393');  // uppercase G
        ENGLISH_TO_GREEK.put('\u0048', '\u0397');  // uppercase H
        ENGLISH_TO_GREEK.put('\u0049', '\u0399');  // uppercase I
        ENGLISH_TO_GREEK.put('\u004A', '\u039E');  // uppercase J
        ENGLISH_TO_GREEK.put('\u004B', '\u039A');  // uppercase K
        ENGLISH_TO_GREEK.put('\u004C', '\u039B');  // uppercase L
        ENGLISH_TO_GREEK.put('\u004D', '\u039C');  // uppercase M
        ENGLISH_TO_GREEK.put('\u004E', '\u039D');  // uppercase N
        ENGLISH_TO_GREEK.put('\u004F', '\u039F');  // uppercase O
        ENGLISH_TO_GREEK.put('\u0050', '\u03A0');  // uppercase P
        //ENGLISH_TO_GREEK.put('\u0051', '\u0391');  // uppercase Q
        ENGLISH_TO_GREEK.put('\u0052', '\u03A1');  // uppercase R
        ENGLISH_TO_GREEK.put('\u0053', '\u03A3');  // uppercase S
        ENGLISH_TO_GREEK.put('\u0054', '\u03A4');  // uppercase T
        ENGLISH_TO_GREEK.put('\u0055', '\u0398');  // uppercase U
        ENGLISH_TO_GREEK.put('\u0056', '\u03A9');  // uppercase V
        ENGLISH_TO_GREEK.put('\u0057', '\u03A3');  // uppercase W
        ENGLISH_TO_GREEK.put('\u0058', '\u03A7');  // uppercase X
        ENGLISH_TO_GREEK.put('\u0059', '\u03A5');  // uppercase Y
        ENGLISH_TO_GREEK.put('\u005A', '\u0396');  // uppercase Z
    }

    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        GREEK_TO_ENGLISH.put('\u0391', '\u0041');  // uppercase Α
        GREEK_TO_ENGLISH.put('\u0392', '\u0042');  // uppercase Β
        GREEK_TO_ENGLISH.put('\u03A8', '\u0043');  // uppercase Ψ
        GREEK_TO_ENGLISH.put('\u0394', '\u0044');  // uppercase Δ
        GREEK_TO_ENGLISH.put('\u0395', '\u0045');  // uppercase Ε
        GREEK_TO_ENGLISH.put('\u03A6', '\u0046');  // uppercase Φ
        GREEK_TO_ENGLISH.put('\u0393', '\u0047');  // uppercase Γ
        GREEK_TO_ENGLISH.put('\u0397', '\u0048');  // uppercase Η
        GREEK_TO_ENGLISH.put('\u0399', '\u0049');  // uppercase Ι
        GREEK_TO_ENGLISH.put('\u039E', '\u004A');  // uppercase Ξ
        GREEK_TO_ENGLISH.put('\u039A', '\u004B');  // uppercase Κ
        GREEK_TO_ENGLISH.put('\u039B', '\u004C');  // uppercase Λ
        GREEK_TO_ENGLISH.put('\u039C', '\u004D');  // uppercase Μ
        GREEK_TO_ENGLISH.put('\u039D', '\u004E');  // uppercase Ν
        GREEK_TO_ENGLISH.put('\u039F', '\u004F');  // uppercase Ο
        GREEK_TO_ENGLISH.put('\u03A0', '\u0050');  // uppercase Π
        //GREEK_TO_ENGLISH.put('\u0051', '\u0391');  // uppercase Q
        GREEK_TO_ENGLISH.put('\u03A1', '\u0052');  // uppercase Ρ
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase Σ
        GREEK_TO_ENGLISH.put('\u03A4', '\u0054');  // uppercase Τ
        GREEK_TO_ENGLISH.put('\u0398', '\u0055');  // uppercase Θ
        GREEK_TO_ENGLISH.put('\u03A9', '\u0056');  // uppercase Ω
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase ς
        GREEK_TO_ENGLISH.put('\u03A7', '\u0058');  // uppercase Χ
        GREEK_TO_ENGLISH.put('\u03A5', '\u0059');  // uppercase Υ
        GREEK_TO_ENGLISH.put('\u0396', '\u005A');  // uppercase Ζ
    }

    public void acsVoucher(MouseEvent actionEvent) {
        if (actionEvent.getButton() == MouseButton.PRIMARY) {
            addTrackingNumer();
        } else if (actionEvent.getButton() == MouseButton.SECONDARY) {
            showTrackingHistory();
        }
    }

    void addTrackingNumer() {
        Dialog<Pair<String, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Καταχώριση Αποστολής");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField trackingField = new TextField();
        trackingField.setPromptText("Αριθμός αποστολής");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Αριθμός:"), 0, 0);
        grid.add(trackingField, 1, 0);
        grid.add(new Label("Ημερομηνία:"), 0, 1);
        grid.add(datePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (trackingField.getText().isEmpty()) {
                    return null;
                }
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString("Μπορείτε να δείτε την εξέλιξη της αποστολής σας εδώ: https://www.acscourier.net/el/track-and-trace/?trackingNumber=" + trackingField.getText());  // Replace with the desired text
                clipboard.setContent(content);
                return new Pair<>(trackingField.getText(), datePicker.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            String tracking = pair.getKey();
            LocalDate date = pair.getValue();
            DBHelper dbHelper = new DBHelper();
            dbHelper.saveTrackingNumber(tracking, date, customer.getCode());
        });
    }

    public void showTrackingHistory() {
        ListView<String> listView = new ListView<>();
        DBHelper dbHelper = new DBHelper();
        int customerId = customer.getCode();
        List<String> trackingNumbers = dbHelper.getTrackingNumbers(customerId);
        listView.getItems().addAll(trackingNumbers);
        // Διπλό κλικ -> Αντιγραφή
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString("Αποστολή: " + selected);
                    clipboard.setContent(content);
                }
            }
        });

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ιστορικό Αποστολών");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void setupFieldListeners() {
        // Προσθήκη listeners σε όλα τα input fields
        Consumer<TextInputControl> textListener = field ->
                field.textProperty().addListener((obs, oldVal, newVal) -> markAsChanged());
        // TextFields
        textListener.accept(tfName);
        textListener.accept(tfTitle);
        textListener.accept(tfJob);
        textListener.accept(tfAfm);
        textListener.accept(tfPhone1);
        textListener.accept(tfPhone2);
        textListener.accept(tfMobile);
        textListener.accept(tfAddress);
        textListener.accept(tfTown);
        textListener.accept(tfPostCode);
        textListener.accept(tfEmail);
        textListener.accept(tfEmail2);
        textListener.accept(tfManager);
        textListener.accept(tfManagerPhone);
        textListener.accept(tfBalance);
        textListener.accept(tfAccName1);
        textListener.accept(tfAccEmail1);
        textListener.accept(tfAccPhone);
        textListener.accept(tfAccMobile);
        textListener.accept(tfAccEmail);
        textListener.accept(tfAccErganiEmail);

        // TextAreas
        textListener.accept(taNotes);
        textListener.accept(taBalanceReason);

        // CheckBox
        checkboxActive.selectedProperty().addListener((obs, oldVal, newVal) -> markAsChanged());

        // ComboBoxes
        tfAccName.valueProperty().addListener((obs, oldVal, newVal) -> markAsChanged());
        tfRecommendation.valueProperty().addListener((obs, oldVal, newVal) -> markAsChanged());

    }

    private void markAsChanged() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
        }
        updateTabTitle("*");
    }

    private void updateTabTitle(String suffix) {
        if (myTab != null) {
            String title = myTab.getText().replace("*", "");
            myTab.setText(title + suffix);
        }
    }

    public boolean handleTabCloseRequest() {
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Μη Αποθηκευμένες Αλλαγές");
            alert.setHeaderText("Υπάρχουν μη αποθηκευμένες αλλαγές!");
            alert.setContentText("Τι θέλετε να κάνετε;");

            ButtonType saveButton = new ButtonType("Αποθήκευση");
            ButtonType discardButton = new ButtonType("Απόρριψη");
            ButtonType cancelButton = new ButtonType("Ακύρωση", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleOkButton();
                    return true;
                } else if (result.get() == discardButton) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    //    private void closeCurrentTab() {
//        if (handleTabCloseRequest()) {
//            Platform.runLater(() -> {
//                Tab currentTab = mainTabPane.getSelectionModel().getSelectedItem();
//                mainTabPane.getTabs().remove(currentTab);
//            });
//        }
//    }
    private void setupCloseHandler() {
        myTab.setOnCloseRequest(event -> {
            if (!handleTabCloseRequest()) {
                event.consume();
            }
        });
    }

    private void closeCurrentTab() {
        if (mainTabPane != null && myTab != null) {
            Platform.runLater(() -> mainTabPane.getTabs().remove(myTab));
        }
    }
}
