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
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.easytech.pelatologio.helper.ComboBoxHelper;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.*;
import org.easytech.pelatologio.helper.Features;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class AddCustomerController {
    private TabPane mainTabPane;
    private Tab myTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane taxisContainer, myposContainer, simplyContainer, emblemContainer, erganiContainer, pelatologioContainer, nineposContainer, devicesContainer, invoicesContainer, tasksContainer, subsContainer, offersContainer, ordersContainer, callLogContainer, notesContainer;
    @FXML
    private Tab tabTaxis, tabMypos, tabSimply, tabEmblem, tabErgani, tabPelatologio, tabNinepos, tabDevices, tabInvoices, tabTasks, tabAccountant, tabSubs, tabOffers, tabOrders, tabCallLog, tabNotes;
    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfPostCode, tfEmail, tfEmail2, tfManager, tfManagerPhone, tfBalance, tfBalanceMega;
    @FXML
    private ComboBox<Recommendation> tfRecommendation;
    @FXML
    private ComboBox<JobTeam> tfJobTeam;
    @FXML
    private ComboBox<SubJobTeam> tfSubJobTeam;
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
    private Label lblBlance, lblBlanceMega, lblCode;
    @FXML
    public JFXCheckBox checkboxActive;
    @FXML
    Button btnPhone1, btnPhone2, btnMobile, btnPhoneManager, btnAccPhone, btnAccMobile, startCallLogButton, btnAnydesk;


    private TaxisViewController taxisViewController;
    private MyposViewController myposViewController;
    private SimplyViewController simplyViewController;
    private EmblemViewController emblemViewController;
    private ErganiViewController erganiViewController;
    private PelatologioViewController pelatologioViewController;
    private NineposViewController nineposViewController;
    private CustomerDevicesController customerDevicesController;
    private InvoicesViewController invoicesViewController;
    private CustomerTasksController customerTasksController;
    private CustomerSubsController customerSubsController;
    private CustomerOffersController customerOffersController;
    private CustomerOrdersController customerOrdersController;
    private CustomerCallLogController customerCallLogController;

    private final Map<Tab, String> tabToFxml = new HashMap<>();
    private final Map<Tab, AnchorPane> tabToContainer = new HashMap<>();
    private final Set<Tab> loadedTabs = new HashSet<>();


    int code = 0;
    private boolean hasUnsavedChanges = false;
    private boolean isLoading = false; // Flag to prevent listeners from firing during data load

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Customer customer;
    private FilteredList<Accountant> filteredAccountants;
    private ObservableList<Recommendation> recommendationList = FXCollections.observableArrayList();
    private FilteredList<Recommendation> filteredRecommendations;
    private ObservableList<JobTeam> jobTeamList = FXCollections.observableArrayList();
    private FilteredList<JobTeam> filteredJobTeams;
    private ObservableList<SubJobTeam> subJobTeamList = FXCollections.observableArrayList();
    private FilteredList<SubJobTeam> filteredSubJobTeams;
    private CustomersController customersController;
    private Consumer<String> originateCallCallback;

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane, Tab myTab) {
        this.mainTabPane = mainTabPane;
        this.myTab = myTab;
        setupCloseHandler();
    }

    public void setCustomersController(CustomersController controller) {
        this.customersController = controller;
    }

    public void setOriginateCallCallback(Consumer<String> callback) {
        this.originateCallCallback = callback;
    }


    public void initialize() {
        Platform.runLater(() -> tabPane.requestFocus());
        setupTabs();
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (!loadedTabs.contains(newTab)) {
                loadTabContent(newTab);
            }
        });
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
        startCallLogButton.setDisable(true);
        startCallLogButton.setVisible(false);
        btnAnydesk.setDisable(true);
        btnAnydesk.setVisible(false);
        tfBalance.setDisable(true);
        tfBalance.setVisible(false);
        lblBlance.setVisible(false);
        tfBalanceMega.setDisable(true);
        tfBalanceMega.setVisible(false);
        lblBlanceMega.setVisible(false);
        checkboxActive.setVisible(false);
        checkboxActive.setDisable(true);
        btnTask.setVisible(false);
        tabTaxis.setDisable(true);
        tabMypos.setDisable(true);
        // Disable tabs based on feature flags
        if (!Features.isEnabled("taxis")) {
            tabTaxis.setDisable(true);
        }
        if (!Features.isEnabled("mypos")) {
            tabMypos.setDisable(true);
        }
        if (!Features.isEnabled("simply")) {
            tabSimply.setDisable(true);
        }
        if (!Features.isEnabled("emblem")) {
            tabEmblem.setDisable(true);
        }
        if (!Features.isEnabled("ergani")) {
            tabErgani.setDisable(true);
        }
        if (!Features.isEnabled("pelatologio")) {
            tabPelatologio.setDisable(true);
        }
        if (!Features.isEnabled("ninepos")) {
            tabNinepos.setDisable(true);
        }
        if (!Features.isEnabled("devices")) {
            tabDevices.setDisable(true);
        }
        if (!Features.isEnabled("megasoft")) {
            tabInvoices.setDisable(true);
        }
        tabInvoices.setDisable(true);
        if (!Features.isEnabled("tasks")) {
            tabTasks.setDisable(true);
        }
        if (!Features.isEnabled("subs")) {
            tabSubs.setDisable(true);
        }
        if (!Features.isEnabled("offers")) {
            tabOffers.setDisable(true);
        }
        if (!Features.isEnabled("orders")) {
            tabOrders.setDisable(true);
        }
        if (!Features.isEnabled("calls")) {
            tabCallLog.setDisable(true);
        }

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
        if (Features.isEnabled("ergani")) {
            tfAccErganiEmail.setContextMenu(emailContextMenu);
            tfAccErganiEmail.setOnContextMenuRequested(e -> currentTextField = tfAccErganiEmail);
        }

        btnEmail.setUserData(tfEmail);
        btnEmail2.setUserData(tfEmail2);
        btnAccEmail.setUserData(tfAccEmail);
        btnAccEmail1.setUserData(tfAccEmail1);

        btnEmail.setOnAction(this::showEmailDialog);
        btnEmail2.setOnAction(this::showEmailDialog);
        btnAccEmail.setOnAction(this::showEmailDialog);
        btnAccEmail1.setOnAction(this::showEmailDialog);

        // Setup phone buttons with left and right click actions
        setupPhoneButton(btnPhone1, tfPhone1);
        setupPhoneButton(btnPhone2, tfPhone2);
        setupPhoneButton(btnMobile, tfMobile);
        setupPhoneButton(btnPhoneManager, tfManagerPhone);
        setupPhoneButton(btnAccPhone, tfAccPhone);
        setupPhoneButton(btnAccMobile, tfAccMobile);

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
        List<Accountant> accountants = DBHelper.getAccountantDao().getAccountants();
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
        ComboBoxHelper.setupFilter(tfAccName, filteredAccountants);
        recommendationList.clear();
        recommendationList.addAll(DBHelper.getRecommendationDao().getRecommendations());
//        tfRecommendation.setItems(recommendationList);
        filteredRecommendations = new FilteredList<>(recommendationList);
        tfRecommendation.setItems(filteredRecommendations);
        ComboBoxHelper.setupFilter(tfRecommendation, filteredRecommendations);

        jobTeamList.clear();
        jobTeamList.addAll(DBHelper.getJobTeamDao().getJobTeams());
        filteredJobTeams = new FilteredList<>(jobTeamList);
        tfJobTeam.setItems(filteredJobTeams);
        ComboBoxHelper.setupFilter(tfJobTeam, filteredJobTeams); // ✅ μόνο μία φορά

        // Action handler για την επιλογή JobTeam
        tfJobTeam.setOnAction(event -> {
            System.out.println("JobTeam selection changed!");
            handleJobTeamSelection();
        });

        tfSubJobTeam.setConverter(new StringConverter<SubJobTeam>() {
            @Override
            public String toString(SubJobTeam subJobTeam) {
                return subJobTeam != null ? subJobTeam.getName() : "";
            }

            @Override
            public SubJobTeam fromString(String string) {
                return subJobTeamList.stream()
                        .filter(subJobTeam -> subJobTeam.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Προσθήκη ακροατών αλλαγών στα πεδία
        // This is now called from setCustomerForEdit to avoid premature firing
        // setupFieldListeners(); 

        // Check for active call and show button if necessary
        if (ActiveCallState.hasPendingCall()) {
            startCallLogButton.setVisible(true);
            startCallLogButton.setOnAction(e -> handleStartCallLogging());
        }
    }

    private void handleJobTeamSelection() {
        JobTeam selectedJobTeam = null;
        Object value = tfJobTeam.getValue(); // Παίρνουμε την τιμή ως γενικό Object
        if (value instanceof JobTeam) {
            // Αν είναι ήδη αντικείμενο Recommendation, το παίρνουμε.
            selectedJobTeam = (JobTeam) value;
        } else if (value instanceof String) {
            // Αν είναι String, ψάχνουμε στη λίστα για το αντίστοιχο αντικείμενο.
            String typedValue = (String) value;
            selectedJobTeam = jobTeamList.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(typedValue))
                    .findFirst().orElse(null);
        }

        subJobTeamList.clear();
        tfSubJobTeam.getItems().clear();

        if (selectedJobTeam != null && selectedJobTeam.getId() != 0) {
            // Μια έγκυρη ομάδα έχει επιλεγεί
            tfSubJobTeam.setDisable(false);

            subJobTeamList.addAll(DBHelper.getSubJobTeamDao().getSubJobTeams(selectedJobTeam.getId()));
            filteredSubJobTeams = new FilteredList<>(subJobTeamList);
            tfSubJobTeam.setItems(filteredSubJobTeams);
        } else {
            // "Όλες" ή καμία ομάδα δεν έχει επιλεγεί
            tfSubJobTeam.setDisable(true);
        }
        //tfSubJobTeam.getSelectionModel().selectFirst();
        if (!tfSubJobTeam.getItems().isEmpty()) {
            tfSubJobTeam.getSelectionModel().selectFirst();
        }
    }


    private void setupTabs() {
        if (Features.isEnabled("taxis")){
            tabToFxml.put(tabTaxis, "taxisView.fxml");
            tabToContainer.put(tabTaxis, taxisContainer);
        }
        if (Features.isEnabled("mypos")) {
            tabToFxml.put(tabMypos, "myposView.fxml");
            tabToContainer.put(tabMypos, myposContainer);
        }
        if (Features.isEnabled("simply")) {
            tabToFxml.put(tabSimply, "simplyView.fxml");
            tabToContainer.put(tabSimply, simplyContainer);
        }
        if (Features.isEnabled("emblem")) {
            tabToFxml.put(tabEmblem, "emblemView.fxml");
            tabToContainer.put(tabEmblem, emblemContainer);
        }
        if (Features.isEnabled("ergani")) {
            tabToFxml.put(tabErgani, "erganiView.fxml");
            tabToContainer.put(tabErgani, erganiContainer);
        }
        if (Features.isEnabled("pelatologio")) {
            tabToFxml.put(tabPelatologio, "pelatologioView.fxml");
            tabToContainer.put(tabPelatologio, pelatologioContainer);
        }
        if (Features.isEnabled("ninepos")) {
            tabToFxml.put(tabNinepos, "nineposView.fxml");
            tabToContainer.put(tabNinepos, nineposContainer);
        }
        if (Features.isEnabled("devices")) {
            tabToFxml.put(tabDevices, "customerDevicesView.fxml");
            tabToContainer.put(tabDevices, devicesContainer);
        }
        if (Features.isEnabled("megasoft")) {
            tabToFxml.put(tabInvoices, "invoicesView.fxml");
            tabToContainer.put(tabInvoices, invoicesContainer);
        }
        if (Features.isEnabled("tasks")) {
            tabToFxml.put(tabTasks, "customerTasksView.fxml");
            tabToContainer.put(tabTasks, tasksContainer);
        }
        if (Features.isEnabled("subs")) {
            tabToFxml.put(tabSubs, "customerSubsView.fxml");
            tabToContainer.put(tabSubs, subsContainer);
        }
        if (Features.isEnabled("offers")) {
            tabToFxml.put(tabOffers, "customerOffersView.fxml");
            tabToContainer.put(tabOffers, offersContainer);
        }
        if (Features.isEnabled("orders")) {
            tabToFxml.put(tabOrders, "ordersCustView.fxml");
            tabToContainer.put(tabOrders, ordersContainer);
        }
        if (Features.isEnabled("calls")) {
            tabToFxml.put(tabCallLog, "customerCallLogView.fxml");
            tabToContainer.put(tabCallLog, callLogContainer);
        }
    }

    private void loadTabContent(Tab tab) {
        String fxmlPath = tabToFxml.get(tab);
        AnchorPane container = tabToContainer.get(tab);

        if (fxmlPath == null || container == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            Object controller = loader.getController();
            switch (tab.getId()) {
                case "tabTaxis" -> {
                    taxisViewController = (TaxisViewController) controller;
                    taxisViewController.setCustomer(customer);
                }
                case "tabMypos" -> {
                    myposViewController = (MyposViewController) controller;
                    myposViewController.setCustomer(customer);
                }
                case "tabSimply" -> {
                    simplyViewController = (SimplyViewController) controller;
                    simplyViewController.setCustomer(customer);
                }
                case "tabEmblem" -> {
                    emblemViewController = (EmblemViewController) controller;
                    emblemViewController.setCustomer(customer);
                }
                case "tabErgani" -> {
                    erganiViewController = (ErganiViewController) controller;
                    erganiViewController.setCustomer(customer);
                }
                case "tabPelatologio" -> {
                    pelatologioViewController = (PelatologioViewController) controller;
                    pelatologioViewController.setCustomer(customer);
                }
                case "tabNinepos" -> {
                    nineposViewController = (NineposViewController) controller;
                    nineposViewController.setCustomer(customer);
                }
                case "tabDevices" -> {
                    customerDevicesController = (CustomerDevicesController) controller;
                    customerDevicesController.setCustomer(customer);
                }
                case "tabInvoices" -> {
                    invoicesViewController = (InvoicesViewController) controller;
                    invoicesViewController.setCustomer(customer);
                }
                case "tabTasks" -> {
                    customerTasksController = (CustomerTasksController) controller;
                    customerTasksController.setCustomer(customer);
                }
                case "tabSubs" -> {
                    customerSubsController = (CustomerSubsController) controller;
                    customerSubsController.setCustomer(customer);
                }
                case "tabOffers" -> {
                    customerOffersController = (CustomerOffersController) controller;
                    customerOffersController.setCustomer(customer);
                }
                case "tabOrders" -> {
                    customerOrdersController = (CustomerOrdersController) controller;
                    customerOrdersController.setCustomer(customer);
                }
                case "tabCallLog" -> {
                    customerCallLogController = (CustomerCallLogController) controller;
                    customerCallLogController.setCustomer(customer);
                }
            }

            container.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            loadedTabs.add(tab);
        } catch (IOException e) {
            e.printStackTrace();
            container.getChildren().setAll(new Label("⚠️ Σφάλμα φόρτωσης FXML"));
        }
    }


    private void handleStartCallLogging() {
        try {
            String callerId = ""; // Default to empty
            String callType = "MANUAL"; // Default to manual entry

            // Check if there's an active call for this customer
            String pendingCallNumber = ActiveCallState.getPendingCallNumber();
            if (pendingCallNumber != null && customer != null) {
                String phone1 = customer.getPhone1() != null ? customer.getPhone1().replaceAll("[^\\d]", "") : "";
                String phone2 = customer.getPhone2() != null ? customer.getPhone2().replaceAll("[^\\d]", "") : "";
                String mobile = customer.getMobile() != null ? customer.getMobile().replaceAll("[^\\d]", "") : "";

                if (pendingCallNumber.equals(phone1) || pendingCallNumber.equals(phone2) || pendingCallNumber.equals(mobile)) {
                    callerId = pendingCallNumber;
                    callType = "INCOMING";
                    ActiveCallState.clearPendingCall(); // Clear the state as we are now logging it
                }
            }

            // Create a new call log entry
            CallLog newCall = new CallLog(callerId, customer.getName(), callType, java.time.LocalDateTime.now(), customer.getCode());
            newCall.setAppUser(AppSettings.loadSetting("appuser"));
            DBHelper.getCallLogDao().insertCallLog(newCall);

            // Open the notes window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/easytech/pelatologio/call_notes.fxml"));
            Parent root = loader.load();
            CallNotesController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Σημειώσεις Κλήσης");
            stage.setScene(new Scene(root));

            controller.initialize(stage, newCall, customer);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Αδυναμία έναρξης καταγραφής κλήσης.", e.getMessage(), Alert.AlertType.ERROR);
        }
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
            Thread emailThread = new Thread(() -> {
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
            });
            emailThread.setDaemon(true);
            emailThread.start(); // Ξεκινάμε το thread για την αποστολή του email
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


    public void setCustomerForEdit(Customer customer) {
        this.isLoading = true; // Start loading data

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
        tfBalanceMega.setText(DBHelper.getMegasoftDao().getMegasoftBalance(customer.getAfm()));

        if (customer.getActive())
            checkboxActive.setSelected(true);
        else
            checkboxActive.setSelected(false);

        // Add listeners after populating the fields to avoid premature firing
        Platform.runLater(() -> {
            setupFieldListeners();
            this.hasUnsavedChanges = false; // Reset again after listeners are set
            updateTabTitle("");
        });


        btnAddressAdd.setDisable(false);


        btnData.setDisable(false);
        btnData.setVisible(true);
        btnLabel.setDisable(false);
        btnLabel.setVisible(true);
        btnCopy.setDisable(false);
        btnCopy.setVisible(true);

        btnAcs.setDisable(false);
        btnAcs.setVisible(true);

        btnAnydesk.setDisable(false);
        btnAnydesk.setVisible(true);
        tfBalance.setDisable(false);
        tfBalance.setVisible(true);
        lblBlance.setVisible(true);
        tfBalanceMega.setDisable(false);
        tfBalanceMega.setVisible(true);
        lblBlanceMega.setVisible(true);
        checkboxActive.setVisible(true);
        checkboxActive.setDisable(false);

        if (Features.isEnabled("taxis")) {
            tabTaxis.setDisable(false);
        }
        if (Features.isEnabled("mypos")) {
            tabMypos.setDisable(false);
        }
        if (Features.isEnabled("simply")) {
            tabSimply.setDisable(false);
        }
        if (Features.isEnabled("emblem")) {
            tabEmblem.setDisable(false);
        }
        if (Features.isEnabled("ergani")) {
            tabErgani.setDisable(false);
        }
        if (Features.isEnabled("pelatologio")) {
            tabPelatologio.setDisable(false);
        }
        if (Features.isEnabled("ninepos")) {
            tabNinepos.setDisable(false);
        }
        if (Features.isEnabled("devices")) {
            tabDevices.setDisable(false);
        }
        if (Features.isEnabled("megasoft")) {
            tabInvoices.setDisable(false);
            btnAddToMegasoft.setDisable(false);
            btnAddToMegasoft.setVisible(true);
        }
        if (Features.isEnabled("tasks")) {
            tabTasks.setDisable(false);
            btnTask.setDisable(false);
            btnTask.setVisible(true);
        }
        if (Features.isEnabled("subs")) {
            tabSubs.setDisable(false);
        }
        if (Features.isEnabled("offers")) {
            tabOffers.setDisable(false);
        }
        if (Features.isEnabled("calls")) {
            tabCallLog.setDisable(false);
            startCallLogButton.setDisable(false);
            startCallLogButton.setVisible(true);
        }
        if (Features.isEnabled("orders")) {
            tabOrders.setDisable(false);
        }

        startCallLogButton.setOnAction(e -> handleStartCallLogging());
        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = customer.getCode();
        this.customer = customer;


        // Find and select the parent job team based on the sub job team ID
        if (customer.getSubJobTeam() != 0) {
            int parentTeamId = DBHelper.getJobTeamDao().getParentTeamIdBySubTeamId(customer.getSubJobTeam());
            if (parentTeamId != 0) {
                for (JobTeam jobTeam : jobTeamList) {
                    if (jobTeam.getId() == parentTeamId) {
                        tfJobTeam.getSelectionModel().select(jobTeam);
                        // The listener of tfJobTeam has now populated the sub-teams.
                        // We can now select the correct sub-team directly.
                        for (SubJobTeam s : subJobTeamList) {
                            if (s.getId() == customer.getSubJobTeam()) {
                                tfSubJobTeam.getSelectionModel().select(s);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        hasTabs();
        setAccountant();
        setRecommendation();
        setJobTeam();

        this.isLoading = false; // Finish loading data
        this.hasUnsavedChanges = false; // Ensure it's clean after loading
        updateTabTitle("");
        lblCode.setText("Κωδικός: " + customer.getCode());
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
    }

    private void setRecommendation() {
        recommendationList.clear();
        recommendationList.addAll(DBHelper.getRecommendationDao().getRecommendations());
        filteredRecommendations = new FilteredList<>(recommendationList);
        tfRecommendation.setItems(filteredRecommendations);
        //tfRecommendation.setItems(recommendationList);

        tfRecommendation.setConverter(new StringConverter<Recommendation>() {
            @Override
            public String toString(Recommendation recommendation) {
                return recommendation != null ? recommendation.getName() : "";
            }

            @Override
            public Recommendation fromString(String string) {
                return recommendationList.stream()
                        .filter(recommendation -> recommendation.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        for (Recommendation rec : recommendationList) {
            if (rec.getId() == customer.getRecommendation()) {
                tfRecommendation.getSelectionModel().select(rec);
                break;
            }
        }
        ComboBoxHelper.setupFilter(tfRecommendation, filteredRecommendations);
    }


    private void setAccountant() {
        List<Accountant> accountants = DBHelper.getAccountantDao().getAccountants();
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
        ComboBoxHelper.setupFilter(tfAccName, filteredAccountants);
    }

    private void setJobTeam() {
        jobTeamList.clear();
        jobTeamList.addAll(DBHelper.getJobTeamDao().getJobTeams());
        filteredJobTeams = new FilteredList<>(jobTeamList);
        tfJobTeam.setItems(filteredJobTeams);

        tfJobTeam.setConverter(new StringConverter<JobTeam>() {
            @Override
            public String toString(JobTeam jobTeam) {
                return jobTeam != null ? jobTeam.getName() : "";
            }

            @Override
            public JobTeam fromString(String string) {
                return jobTeamList.stream()
                        .filter(jobTeam -> jobTeam.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Find and select the parent job team based on the sub job team ID
        if (customer != null && customer.getSubJobTeam() != 0) {
            int parentTeamId = DBHelper.getJobTeamDao().getParentTeamIdBySubTeamId(customer.getSubJobTeam());
            if (parentTeamId != 0) {
                for (JobTeam jobTeam : jobTeamList) {
                    if (jobTeam.getId() == parentTeamId) {
                        // Select the job team first
                        tfJobTeam.getSelectionModel().select(jobTeam);

                        // This will trigger the handler and load the sub-teams
                        handleJobTeamSelection();

                        // Now select the sub-job-team
                        for (SubJobTeam s : subJobTeamList) {
                            if (s.getId() == customer.getSubJobTeam()) {
                                tfSubJobTeam.getSelectionModel().select(s);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }

        ComboBoxHelper.setupFilter(tfJobTeam, filteredJobTeams);
    }

    private void hasTabs() {
        if (DBHelper.getCustomerDao().hasSubAddress(customer.getCode())) {
            btnAddressAdd.setStyle("-fx-border-color: #FF0000;");
        }

        if (Features.isEnabled("taxis")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 3)) {
                tabTaxis.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("mypos")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 1)) {
                tabMypos.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("simply")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 2)) {
                tabSimply.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("emblem")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 4)) {
                tabEmblem.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("ergani")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 5)) {
                tabErgani.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("pelatologio")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 6)) {
                tabPelatologio.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("ninepos")) {
            if (DBHelper.getCustomerDao().hasApp(customer.getCode(), 7)) {
                tabNinepos.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("devices")) {
            if (DBHelper.getCustomerDao().hasDevice(customer.getCode())) {
                tabDevices.getStyleClass().add("tabHas");
            }
        }
//        if (DBHelper.getCustomerDao().hasInvoices(customer.getAfm())) {
//            tabInvoices.getStyleClass().add("tabHas");
//        }
        if (Features.isEnabled("tasks")) {
            if (DBHelper.getCustomerDao().hasTask(customer.getCode())) {
                tabTasks.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("subs")) {
            if (DBHelper.getCustomerDao().hasSub(customer.getCode())) {
                tabSubs.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("offers")) {
            if (DBHelper.getCustomerDao().hasOffer(customer.getCode())) {
                tabOffers.getStyleClass().add("tabHas");
            }
        }
        if (Features.isEnabled("orders")) {
            if (DBHelper.getCustomerDao().hasOrders(customer.getCode())) {
                tabOrders.getStyleClass().add("tabHas");
            }
        }

        if (customer.getAccId() != 0) {
            tabAccountant.getStyleClass().add("tabHas");
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
        Recommendation selectedRec = null;
        Object value = tfRecommendation.getValue(); // Παίρνουμε την τιμή ως γενικό Object
        if (value instanceof Recommendation) {
            // Αν είναι ήδη αντικείμενο Recommendation, το παίρνουμε.
            selectedRec = (Recommendation) value;
        } else if (value instanceof String) {
            // Αν είναι String, ψάχνουμε στη λίστα για το αντίστοιχο αντικείμενο.
            String typedValue = (String) value;
            selectedRec = recommendationList.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(typedValue))
                    .findFirst().orElse(null);
        }
        // Παίρνουμε το ID με ασφάλεια, αφού έχουμε βρει το σωστό αντικείμενο.
        int selectedRecommendation = (selectedRec != null) ? selectedRec.getId() : 0;
        //int selectedRecommendation = tfRecommendation.getSelectionModel().getSelectedItem() != null ? tfRecommendation.getSelectionModel().getSelectedItem().getId() : 0;
        Accountant selectedAccountant = tfAccName.getSelectionModel().getSelectedItem();
        int accId = (selectedAccountant != null) ? selectedAccountant.getId() : 0;
        String balance = tfBalance.getText();
        String balanceReason = taBalanceReason.getText();
        SubJobTeam selectedSubJob = null;
        value = tfSubJobTeam.getValue(); // Παίρνουμε την τιμή ως γενικό Object
        if (value instanceof SubJobTeam) {
            // Αν είναι ήδη αντικείμενο Recommendation, το παίρνουμε.
            selectedSubJob = (SubJobTeam) value;
        } else if (value instanceof String) {
            // Αν είναι String, ψάχνουμε στη λίστα για το αντίστοιχο αντικείμενο.
            String typedValue = (String) value;
            selectedSubJob = subJobTeamList.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(typedValue))
                    .findFirst().orElse(null);
        }
        // Παίρνουμε το ID με ασφάλεια, αφού έχουμε βρει το σωστό αντικείμενο.
        int selectedJobTeam = (selectedSubJob != null) ? selectedSubJob.getId() : 0;
        //int selectedJobTeam = tfJobTeam.getSelectionModel().getSelectedItem() != null ? tfJobTeam.getSelectionModel().getSelectedItem().getId() : 0;


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

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        int customerId;
        if (DBHelper.getCustomerDao().isAfmExists(afm)) {
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
            customerId = DBHelper.getCustomerDao().insertCustomer(name, title, job, afm, phone1, phone2, mobile, primaryAddress, town, postcode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, selectedRecommendation, balance, balanceReason, selectedJobTeam);
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


    public Customer getUpdatedCustomer() {
        return this.customer;
    }

    void updateCustomer() {
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
        Recommendation selectedRecommendation = tfRecommendation.getSelectionModel().getSelectedItem();
        customer.setRecommendation(selectedRecommendation == null ? 0 : selectedRecommendation.getId());
        String balance = tfBalance.getText();
        customer.setBalance(balance);
        String balanceReason = taBalanceReason.getText();
        customer.setBalanceReason(balanceReason);
        boolean isActive = checkboxActive.isSelected();
        customer.setActive(isActive);
        SubJobTeam subjobTeam = tfSubJobTeam.getSelectionModel().getSelectedItem();
        if (subjobTeam == null) {
            subjobTeam = new SubJobTeam(0, "",0); // Create a new SubJobTeam with ID 0 if none is selected
            subjobTeam.setId(0); // Set to 0 if no sub-job team is selected
        }
        customer.setSubJobTeam(subjobTeam == null ? 0 : subjobTeam.getId());

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

        DBHelper.getCustomerDao().updateCustomer(code, name, title, job, afm, phone1, phone2, mobile, address, town, posCode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, selectedRecommendation.getId(), balance, balanceReason, isActive, subjobTeam.getId());

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

        DBHelper.getAccountantDao().updateAccountant(accId, accName, accPhone, accMobile, accEmail, accErganiEmail);

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
        if (DBHelper.getCustomerDao().isAfmExistsMegasoft(customer.getAfm()))
            PrismaWinAutomation.showCustomer(customer);
        else
            PrismaWinAutomation.addCustomer(customer);
    }

    public void selectTaxisTab() {
        if (Features.isEnabled("taxis")) {
            Platform.runLater(() -> tabPane.getSelectionModel().select(tabTaxis));
        }
    }

    public void selectMyPOSTab() {
        if (Features.isEnabled("mypos")) {
            Platform.runLater(() -> tabPane.getSelectionModel().select(tabMypos));
        }
    }

    public void selectSimplyTab() {
        if (Features.isEnabled("simply")) {
            Platform.runLater(() -> tabPane.getSelectionModel().select(tabSimply));
        }
    }

    public void selectEmbelmTab() {
        if (Features.isEnabled("emblem")) {
            Platform.runLater(() -> tabPane.getSelectionModel().select(tabEmblem));
        }
    }

    public void selectErganiTab() {
        if (Features.isEnabled("ergani")) {
            Platform.runLater(() -> tabPane.getSelectionModel().select(tabErgani));
        }
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
            DBHelper.getTrackingDao().saveTrackingNumber(tracking, date, customer.getCode());
        });
    }

    public void showTrackingHistory() {
        ListView<String> listView = new ListView<>();
        DBHelper dbHelper = new DBHelper();
        int customerId = customer.getCode();
        List<String> trackingNumbers = DBHelper.getTrackingDao().getTrackingNumbers(customerId);
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
                field.textProperty().addListener((obs, oldVal, newVal) -> markAsChanged(field));
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
        checkboxActive.selectedProperty().addListener((obs, oldVal, newVal) -> markAsChanged(checkboxActive));

        // ComboBoxes
        tfAccName.valueProperty().addListener((obs, oldVal, newVal) -> markAsChanged(tfAccName));
        tfRecommendation.valueProperty().addListener((obs, oldVal, newVal) -> markAsChanged(tfRecommendation));
        tfJobTeam.valueProperty().addListener((obs, oldVal, newVal) -> {
            markAsChanged(tfJobTeam);
            handleJobTeamSelection();
        });
        tfSubJobTeam.valueProperty().addListener((obs, oldVal, newVal) -> markAsChanged(tfSubJobTeam));
    }

    private void markAsChanged(Object source) {
        if (isLoading) return; // Don't mark as changed if data is being loaded

        // Logging to identify the source of the change
        String sourceId = "Unknown";
        if (source instanceof Control) {
            sourceId = ((Control) source).getId();
        }
        System.out.println("Change detected from: " + sourceId + " | Current hasUnsavedChanges: " + hasUnsavedChanges);

        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateTabTitle("*");
        }
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
            alert.setContentText("Τι θέλετε να κάνετε?");

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

    /**
     * Sets up a phone button to handle left and right clicks.
     * Left-click triggers the primary call action.
     * Right-click triggers the secondary call action.
     *
     * @param button    The button to set up.
     * @param textField The text field containing the phone number.
     */
    private void setupPhoneButton(Button button, TextField textField) {
        button.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                PhoneCall.callHandle(textField.getText());
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                PhoneCall.callHandle2(textField.getText());
            }
        });
    }

    public void anydesk(MouseEvent mouseEvent) {
        openAnydeskWindow(customer);
    }

    private void openAnydeskWindow(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("anydeskView.fxml"));
            Parent root = loader.load();

            AnydeskViewController controller = loader.getController();
            controller.setCustomer(customer);

            Stage stage = new Stage();
            stage.setTitle("Anydesk IDs for " + customer.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    public void handleAddRecommendation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recomManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            RecomManagerViewController controller = loader.getController();
            controller.loadRecommendations();


            dialog.setTitle("Συστάσεις");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleAddJobTeam(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("jobTeamManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            JobTeamManagerViewController controller = loader.getController();
            controller.loadJobTeams();


            dialog.setTitle("Ομάδες εργασιών");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleAddSubJobTeam(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subJobTeamManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            SubJobTeamManagerViewController controller = loader.getController();
            controller.loadSubJobTeams(tfJobTeam.getSelectionModel().getSelectedItem().getId());


            dialog.setTitle("Ομάδες εργασιών");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}