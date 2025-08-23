package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.easytech.pelatologio.dao.PartnerDao;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Partner;
import org.easytech.pelatologio.models.PartnerCustomer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.io.IOException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class AddPartnerController {

    private TabPane mainTabPane;
    private Tab myTab;
    @FXML
    private TabPane tabPane;
    @FXML
    public Label lblCode;
    @FXML
    private TextField tfName, tfTitle, tfJob, tfAfm, tfPhone1, tfPhone2, tfMobile, tfAddress, tfTown, tfPostCode, tfEmail, tfEmail2, tfManager, tfManagerPhone;
    @FXML
    private Tab tabNotes;
    @FXML
    private TextArea taNotes;
    
    @FXML
    private Tab tabCustomers;
    @FXML
    private TableView<PartnerCustomer> tblPartnerCustomers;
    @FXML
    private TableColumn<PartnerCustomer, String> colCustomerCode;
    @FXML
    private TableColumn<PartnerCustomer, String> colCustomerName;
    @FXML
    private TableColumn<PartnerCustomer, String> colCustomerAfm;
    @FXML
    private TableColumn<PartnerCustomer, LocalDate> colContractDate;
    @FXML
    private TableColumn<PartnerCustomer, BigDecimal> colTotalPaid;
    @FXML
    private TableColumn<PartnerCustomer, BigDecimal> colCommission;
    @FXML
    private Button btnAfmSearch;
    @FXML
    private Button btnEmail, btnEmail2;
    @FXML
    Button btnPhone1, btnPhone2, btnMobile, btnPhoneManager;

    int code = 0;
    private boolean hasUnsavedChanges = false;
    private boolean isLoading = false; // Flag to prevent listeners from firing during data load
    private TextField currentTextField;

    private PartnerDao partnerDao;
    private Partner currentPartner;
    private Runnable onSaveCallback;

    private PartnersController partnersController;
    private Consumer<Integer> openCustomerCallback;

    public void setOpenCustomerCallback(Consumer<Integer> callback) {
        this.openCustomerCallback = callback;
    }

    // Θα περάσουμε το TabPane από τον MainMenuController
    public void setMainTabPane(TabPane mainTabPane, Tab myTab) {
        this.mainTabPane = mainTabPane;
        this.myTab = myTab;
        setupCloseHandler();
    }

    public void setPartnersController(PartnersController controller) {
        this.partnersController = controller;
    }

    private void setupCloseHandler() {
        myTab.setOnCloseRequest(event -> {
            if (!handleTabCloseRequest()) {
                event.consume();
            }
        });
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
                } else return result.get() == discardButton;
            }
            return false;
        }
        return true;
    }

    public void handleOkButton() {
        handleSave();
        hasUnsavedChanges = false;
        updateTabTitle(""); // Αφαίρεση αστερίσκου
    }

    private void initializePartnerCustomersTable() {
        colCustomerCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        colCustomerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colCustomerAfm.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAfm()));
        colContractDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getContractDate();
            return new SimpleObjectProperty<>(date);
        });
        
        // Format currency columns
        colTotalPaid.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalPaid()));
        colTotalPaid.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", amount.doubleValue()));
                }
            }
        });
        
        colCommission.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCommission()));
        colCommission.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", amount.doubleValue()));
                }
            }
        });
        
        // Set column widths
        colCustomerCode.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.15));
        colCustomerName.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.25));
        colCustomerAfm.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.15));
        colContractDate.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.15));
        colTotalPaid.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.15));
        colCommission.prefWidthProperty().bind(tblPartnerCustomers.widthProperty().multiply(0.15));
    }
    
    private void loadPartnerCustomers() {
        if (currentPartner.getId() <= 0) return;
        System.out.println("Loading customers for partner ID: " + code);
        // Show loading indicator
        tblPartnerCustomers.setPlaceholder(new Label("Φόρτωση δεδομένων..."));
        
        // Run database operation in background thread
        new Thread(() -> {
            try {
                List<PartnerCustomer> customers = partnerDao.getByPartnerId(code);
                System.out.println("Loaded " + customers.size() + " customers for partner ID " + code);
                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    if (customers.isEmpty()) {
                        tblPartnerCustomers.setPlaceholder(new Label("Δεν βρέθηκαν πελάτες για αυτόν τον συνεργάτη"));
                    } else {
                        tblPartnerCustomers.getItems().setAll(customers);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Show error on JavaFX Application Thread
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Σφάλμα");
                    alert.setHeaderText("Σφάλμα φόρτωσης πελατών");
                    alert.setContentText("Παρουσιάστηκε σφάλμα κατά τη φόρτωση των πελατών: " + 
                                     (e.getMessage() != null ? e.getMessage() : "Άγνωστο σφάλμα"));
                    alert.showAndWait();
                    
                    tblPartnerCustomers.setPlaceholder(new Label("Σφάλμα κατά τη φόρτωση των δεδομένων"));
                });
            }
        }).start();
    }

    private void updateTabTitle(String suffix) {
        if (myTab != null) {
            String title = myTab.getText().replace("*", "");
            myTab.setText(title + suffix);
        }
    }

    public void initialize() {
        this.partnerDao = DBHelper.getPartnerDao();
        btnAfmSearch.setOnAction(event -> handleAfmSearch());
        
        // Initialize partner customers table
        initializePartnerCustomersTable();
        
        // Add listener to tab selection to load data when tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabCustomers) {
                loadPartnerCustomers();
            }
        });

        tblPartnerCustomers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PartnerCustomer selectedCustomer = tblPartnerCustomers.getSelectionModel().getSelectedItem();
                if (selectedCustomer != null && openCustomerCallback != null) {
                    try {
                        int customerId = Integer.parseInt(selectedCustomer.getCode());
                        openCustomerCallback.accept(customerId);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid customer code format: " + selectedCustomer.getCode());
                    }
                }
            }
        });


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

        // Ανάθεση emailContextMenu στο tfEmail
        tfEmail.setContextMenu(emailContextMenu);
        tfEmail.setOnContextMenuRequested(e -> currentTextField = tfEmail);
        tfEmail2.setContextMenu(emailContextMenu);
        tfEmail2.setOnContextMenuRequested(e -> currentTextField = tfEmail2);

        btnEmail.setUserData(tfEmail);
        btnEmail2.setUserData(tfEmail2);

        btnEmail.setOnAction(this::showEmailDialog);
        btnEmail2.setOnAction(this::showEmailDialog);

        // Setup phone buttons with left and right click actions
        setupPhoneButton(btnPhone1, tfPhone1);
        setupPhoneButton(btnPhone2, tfPhone2);
        setupPhoneButton(btnMobile, tfMobile);
        setupPhoneButton(btnPhoneManager, tfManagerPhone);

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
        });
    }


    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    void handleSave() {
        if (currentPartner == null) {
            currentPartner = new Partner();
        }
        String name = tfName.getText();
        currentPartner.setName(name);
        String title = tfTitle.getText();
        currentPartner.setTitle(title);
        String job = tfJob.getText().substring(0, Math.min(tfJob.getText().length(), 255));
        currentPartner.setJob(job);
        String afm = tfAfm.getText();
        currentPartner.setAfm(afm);
        String phone1 = tfPhone1.getText() == null ? "" : tfPhone1.getText();
        String phone2 = tfPhone2.getText() == null ? "" : tfPhone2.getText();
        String mobile = tfMobile.getText() == null ? "" : tfMobile.getText();
        String address = tfAddress.getText();
        currentPartner.setAddress(address);
        String town = tfTown.getText();
        currentPartner.setTown(town);
        String posCode = tfPostCode.getText();
        currentPartner.setPostcode(posCode);
        String email = tfEmail.getText() == null ? "" : tfEmail.getText();
        currentPartner.setEmail(email);
        String email2 = tfEmail2.getText() == null ? "" : tfEmail2.getText();
        currentPartner.setEmail2(email2);
        String manager = tfManager.getText() == null ? "" : tfManager.getText();
        currentPartner.setManager(manager);
        String managerPhone = tfManagerPhone.getText() == null ? "" : tfManagerPhone.getText();
        String notes = taNotes.getText();
        currentPartner.setNotes(notes);

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
        currentPartner.setMobile(mobile);
        currentPartner.setPhone1(phone1);
        currentPartner.setPhone2(phone2);
        currentPartner.setManagerPhone(managerPhone);

        if (currentPartner.getId() == 0) {
            partnerDao.insert(currentPartner);
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Ο πελάτης εισήχθη με επιτυχία στη βάση δεδομένων.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                notifications.showInformation();
                closeCurrentTab(); // Κλείσιμο του "Νέος Πελάτης"
                openPartnerTab(currentPartner.getId()); // Άνοιγμα καρτέλας με τον νέο πελάτη
            });
        } else {
            partnerDao.update(currentPartner);
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }

    public void setPartnerForEdit(Partner partner) {
        this.isLoading = true; // Start loading data

        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(partner.getName());
        tfTitle.setText(partner.getTitle());
        tfJob.setText(partner.getJob());
        tfAfm.setText(partner.getAfm());
        tfPhone1.setText(partner.getPhone1());
        tfPhone2.setText(partner.getPhone2());
        tfMobile.setText(partner.getMobile());
        tfAddress.setText(partner.getAddress());
        tfTown.setText(partner.getTown());
        tfPostCode.setText(partner.getPostcode());
        tfEmail.setText(partner.getEmail());
        tfEmail2.setText(partner.getEmail2());
        tfManager.setText(partner.getManager());
        tfManagerPhone.setText(partner.getManagerPhone());
        taNotes.setText(partner.getNotes());

        // Add listeners after populating the fields to avoid premature firing
        Platform.runLater(() -> {
            setupFieldListeners();
            this.hasUnsavedChanges = false; // Reset again after listeners are set
            updateTabTitle("");
        });

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = partner.getId();
        this.currentPartner = partner;


        this.isLoading = false; // Finish loading data
        this.hasUnsavedChanges = false; // Ensure it's clean after loading
        updateTabTitle("");
        lblCode.setText("Κωδικός: " + partner.getId());
    }

    private void closeCurrentTab() {
        if (mainTabPane != null && myTab != null) {
            Platform.runLater(() -> mainTabPane.getTabs().remove(myTab));
        }
    }

    private void openPartnerTab(int partnerId) {
        if (partnersController != null) {
            partnersController.openPartnerTab(partnerId);
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

        // TextAreas
        textListener.accept(taNotes);
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

    private void showEmailDialog(ActionEvent actionEvent) {
//        Button clickedButton = (Button) actionEvent.getSource(); // Ποιο κουμπί πατήθηκε;
//        TextField emailField = (TextField) clickedButton.getUserData(); // Παίρνουμε το TextField που είναι συνδεδεμένο με το κουμπί
//        if (emailField != null && !emailField.getText().isEmpty()) {
//            try {
//                String email = emailField.getText();
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
//                Dialog<ButtonType> dialog = new Dialog<>();
//                dialog.setDialogPane(loader.load());
//                dialog.setTitle("Αποστολή Email");
//                EmailDialogController controller = loader.getController();
//                controller.setCustomer(currentPartner);
//                controller.setEmail(email);
//                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
//                dialog.show();
//            } catch (IOException e) {
//                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
//            }
//        }
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

    private void sendTestEmail(TextField emailField) {
        String email = emailField.getText();
        if (email != null && !email.isEmpty()) {
            // Εμφάνιση του progress indicator
            //progressIndicator.setVisible(true);

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
                        //progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποστολή του email.", e.getMessage(), Alert.AlertType.ERROR);
                        //progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
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
}
