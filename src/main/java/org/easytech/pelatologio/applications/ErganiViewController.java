package org.easytech.pelatologio.applications;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.settings.AppSettings;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.EmailSender;
import org.easytech.pelatologio.helper.LabelPrintHelper;
import org.easytech.pelatologio.logins.AddLoginController;
import org.easytech.pelatologio.logins.EditLoginController;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.ErganiRegistration;
import org.easytech.pelatologio.models.Logins;
import org.easytech.pelatologio.subs.AddSubController;
import org.easytech.pelatologio.tasks.AddTaskController;

import java.io.IOException;
import java.util.Optional;

public class ErganiViewController {
    private static final String WARNING_TITLE = "Προσοχή";
    private static final String SELECT_LOGIN_MSG = "Παρακαλώ επιλέξτε ένα login.";

    @FXML
    public Button btnErganiRegister, btnErganiLogin, btnErganiOffer;
    @FXML
    private TableView<Logins> loginTable;

    @FXML
    private TableColumn<Logins, String> usernameColumn;
    @FXML
    private TableColumn<Logins, String> passwordColumn;
    @FXML
    private TableColumn<Logins, String> tagColumn;
    @FXML
    private TableColumn<Logins, String> phoneColumn;

    Customer customer;

    private ObservableList<Logins> loginList;

    @FXML
    public void initialize() {
        setTooltip(btnErganiRegister, "Εγγραφή πελάτη στο Ergani");
        setTooltip(btnErganiLogin, "Πρόσβαση στο Ergani");

        loginList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        // Ρύθμιση στήλης username
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        // Ρύθμιση στήλης tag
        tagColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTag()));
        // Ρύθμιση στήλης phone
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));

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
        loginList.addAll(dbHelper.getLogins(customerId,5));
        if (loginTable.getItems().size() == 1)
            loginTable.getSelectionModel().select(0);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadLoginsForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }

    public void handleAddLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addLogin.fxml"));
            DialogPane dialogPane = loader.load();

            AddLoginController addLoginController = loader.getController();
            addLoginController.setCustomer(customer); // Ορίζει τον πελάτη
            addLoginController.setUsername(customer.getEmail());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Νέου Login");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, e -> {
                if (!addLoginController.validateInputs()) {
                    e.consume(); // Εμποδίζει το κλείσιμο του dialog
                }
                else {
                    // Εάν οι εισαγωγές είναι έγκυρες, συνεχίστε με την αποθήκευση
                    addLoginController.handleSaveLogin(event,5);
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadLoginsForCustomer(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public void handleDeleteLogin(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

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
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editLogin.fxml"));
            DialogPane dialogPane = loader.load();

            EditLoginController editController = loader.getController();
            editController.setLogin(selectedLogin);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Login");

            // Προσθήκη των παρακάτω 2 γραμμών
            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Αλλαγή σε show() και χρήση setOnHidden
            dialog.show();

            // Μετακίνηση της λογικής στο OnHidden
            dialog.setOnHidden(e -> {
                ButtonType result = dialog.getResult();
                if (result != null && result == ButtonType.OK) {
                    Logins updatedLogin = editController.getUpdatedLogin();
                    new DBHelper().updateLogin(updatedLogin); // Χρήση νέου instance για thread safety
                    Platform.runLater(() -> loginTable.refresh());
                }
            });

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleLabel(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        LabelPrintHelper.printLoginLabel(selectedLogin,customer,"Στοιχεία "+selectedLogin.getTag());
    }

    public void handleCopy(ActionEvent event) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        String msg ="Στοιχεία εισόδου " + selectedLogin.getTag() +
                "\nΕπωνυμία: "+customer.getName()+
                "\nΑΦΜ: "+customer.getAfm()+
                "\nEmail: "+selectedLogin.getUsername()+
                "\nΚωδικός: "+selectedLogin.getPassword()+
                "\nΚινητό: "+customer.getMobile()+
                "\n";
        copyTextToClipboard(msg);
    }

    public void handleAddTask(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle(selectedLogin.getTag() +": "+ customer.getName());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());
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

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public void handleAddSub(ActionEvent evt) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddSubController controller = loader.getController();
            controller.setSubTitle(selectedLogin.getTag());
            controller.setCustomerName(customer.getName());
            controller.setCustomerId(customer.getCode());
            controller.setNote(selectedLogin.getUsername());
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveSub();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void registerErgani(ActionEvent actionEvent) {
        Logins selectedLogin = checkSelectedLogin();
        if (selectedLogin == null) return;

        Dialog<ErganiRegistration> dialog = new Dialog<>();
        dialog.setTitle("Εγγραφή στο Εργάνη");

        // Δημιουργία των στοιχείων εισαγωγής
        ComboBox<String> comboProgram = new ComboBox<>();
        comboProgram.getItems().addAll("1-2 Υπάλληλοι", "3-5 Υπάλληλοι", "6-20 Υπάλληλοι", "21-50 Υπάλληλοι"); // Σταθερές επιλογές
        //comboProgram.setValue("Πρόγραμμα 1"); // Προεπιλογή

        TextField yearsField = new TextField();
        yearsField.setPromptText("Αριθμός Ετών");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Λογιστή");
        DBHelper dbHelper = new DBHelper();
        String erganiEmail = dbHelper.getErganiEmail(selectedLogin.getCustomerId());
        emailField.setText(erganiEmail);

        TextField entranceField = new TextField();
        entranceField.setPromptText("Είσοδος");
        entranceField.setText("Όχι");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Επιλογή Προγράμματος:"), 0, 0);
        grid.add(comboProgram, 1, 0);
        grid.add(new Label("Σύνολο Ετών:"), 0, 1);
        grid.add(yearsField, 1, 1);
        grid.add(new Label("Email Λογιστή:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Extra Είσοδος:"), 0, 3);
        grid.add(entranceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Ενεργοποίηση του κουμπιού OK μόνο αν έχουν συμπληρωθεί όλα τα πεδία
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        yearsField.textProperty().addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.trim().isEmpty() || emailField.getText().trim().isEmpty()));

        emailField.textProperty().addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.trim().isEmpty() || yearsField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new ErganiRegistration(
                        comboProgram.getValue(),
                        yearsField.getText().trim(),
                        emailField.getText().trim(),
                        entranceField.getText().trim()
                );
            }
            return null; // Αν ο χρήστης πατήσει "Άκυρο"
        });


        Optional<ErganiRegistration> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String program = data.getProgram();
            String years = data.getYears();
            String emailAcc = data.getEmail();
            String entrance = data.getEntrance();
            String subject = "Νέος πελάτης Εργάνη";
            String msg = "<b>Νέος πελάτης Εργάνη</b>" +
                    "<br><b>Επωνυμία:</b> " + customer.getName() +
                    "<br><b>ΑΦΜ:</b> " + customer.getAfm() +
                    "<br><b>E-mai:</b> " + selectedLogin.getUsername() +
                    "<br><b>Κινητό:</b> " + selectedLogin.getPhone() +
                    "<br><b>E-mail Λογιστή:</b> " + emailAcc +
                    "<br><b>Προγράμματα:</b> " + program +
                    "<br><b>Σύνολο Ετών:</b> " + years +
                    "<br><b>Extra Είσοδος:</b> " + entrance;
            sendEmail(subject, msg);
            if (dbHelper.hasAccountant(selectedLogin.getCustomerId())) {
                dbHelper.updateErganiEmail(selectedLogin.getCustomerId(), emailAcc);
            }
        });


    }

    public void loginErgani (ActionEvent actionEvent) {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα login.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString("https://myaccount.epsilonnet.gr/Identity/Account/Login?product=8fd59003-5af4-4ca7-6fbd-08dace2c8999");
            clipboard.setContent(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Μέθοδος αποστολής email
    private void sendEmail(String subject, String msg) {
        // Κώδικας για αποστολή email
        EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
        emailSender.sendEmail(AppSettings.loadSetting("erganiRegisterMail"), subject, msg);
    }

    // Μέθοδος αντιγραφής κειμένου στο πρόχειρο
    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        Notifications notifications = Notifications.create()
                .title("Αντιγραφή στο πρόχειρο")
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

    private Logins checkSelectedLogin() {
        Logins selectedLogin = loginTable.getSelectionModel().getSelectedItem();
        if (selectedLogin == null) {
            showErrorNotification(WARNING_TITLE, SELECT_LOGIN_MSG);
        }
        return selectedLogin;
    }

    private void showErrorNotification(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showError();
    }

    public void erganiOffer(ActionEvent actionEvent) {
//        Logins selectedLogin = checkSelectedLogin();
//        DBHelper dbHelper = new DBHelper();
//        Customer customer = dbHelper.getSelectedCustomer(selectedLogin.getCustomerId());
//        if (selectedLogin == null) {
//            return;
//        }
//        // GUI σε νέο παράθυρο
//        Stage popupStage = new Stage();
//        popupStage.initModality(Modality.APPLICATION_MODAL);
//        popupStage.setTitle("Δημιουργία Προσφοράς");
//
//        Map<String, Double> services = Map.of(
//                "Epsilon Smart Ergnani\n" +
//                        "Accounting Edition\n" +
//                        "1-2 Υπάλληλοι\n", 110.0,
//                "Epsilon Smart Ergnani\n" +
//                        "Accounting Edition\n" +
//                        "3-5 Υπάλληλοι\n", 135.0,
//                "Epsilon Smart Ergnani\n" +
//                        "Accounting Edition\n" +
//                        "6-20 Υπάλληλοι\n", 190.0,
//                "Epsilon Smart Ergnani\n" +
//                        "Accounting Edition\n" +
//                        "21-50 Υπάλληλοι\n", 250.0
//        );
//
//        ComboBox<String> serviceBox = new ComboBox<>();
//        serviceBox.getItems().addAll(services.keySet());
//        serviceBox.setValue("Epsilon Smart Ergnani\n" +
//                "Accounting Edition\n" +
//                "1-2 Υπάλληλοι\n");
//
//        CheckBox cbEntrance = new CheckBox("2η Είσοδος (+20€)");
//        CheckBox cbTablet = new CheckBox("Tablet (+135€)");
//        Button generateBtn = new Button("Δημιουργία");
//
//        generateBtn.setOnAction(e -> {
//            try {
//                CustomerFolderManager folderManager = new CustomerFolderManager();
//                folderManager.createCustomerOfferFolder(customer.getName(), customer.getAfm());
//
//                String service = serviceBox.getValue();
//                boolean extraEntrance = cbEntrance.isSelected();
//                boolean withTablet = cbTablet.isSelected();
//
//                FileChooser chooser = new FileChooser();
//                chooser.setTitle("Αποθήκευση Προσφοράς");
//                chooser.setInitialDirectory(folderManager.createCustomerOfferFolder(customer.getName(), customer.getAfm()));
//                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DOCX", "*.docx"));
//                File saveFile = chooser.showSaveDialog(popupStage);
//                if (saveFile == null) return;
//
//                double basePrice = services.get(service);
//                double total = basePrice + (extraEntrance ? 20 : 0) + (withTablet ? 135 : 0);
//
//                // 🔐 Λύση για το "zip bomb"
//                ZipSecureFile.setMinInflateRatio(0.001);
//
//                FileInputStream fis = new FileInputStream(AppSettings.loadSetting("datafolder") + "\\Templates\\EPSILON PROSFORA.docx");
//                XWPFDocument doc = new XWPFDocument(fis);
//
//                Map<String, String> values = Map.of(
//                        "{CLIENT_NAME}", customer.getName(),
//                        "{SERVICE_NAME}", service,
//                        "{SERVICE_PRICE}", String.format("%.2f", basePrice) + "€",
//                        "{SECOND_ENTRANCE}", extraEntrance ? "2η είσοδος: 20€" : "",
//                        "{TABLET}", withTablet ? "Tablet: 135€" : "",
//                        "{TOTAL}", String.format("%.2f", total) + "€"
//                );
//
//                replacePlaceholders(doc, values);
//
//                List<XWPFParagraph> toRemove = doc.getParagraphs().stream()
//                        .filter(p -> p.getText().contains("{SECOND_ENTRANCE}") || p.getText().contains("{TABLET}"))
//                        .collect(Collectors.toList());
//
//                toRemove.forEach(p -> doc.removeBodyElement(doc.getPosOfParagraph(p)));
//
//                try (FileOutputStream out = new FileOutputStream(saveFile)) {
//                    doc.write(out);
//                }
//
////                // PDF conversion
////                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(saveFile);
////                File pdfFile = new File(saveFile.getAbsolutePath().replace(".docx", ".pdf"));
////                Docx4J.toPDF(wordMLPackage, new FileOutputStream(pdfFile));
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Η προσφορά δημιουργήθηκε!", ButtonType.OK);
//                alert.showAndWait();
//                popupStage.close();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                new Alert(Alert.AlertType.ERROR, "Σφάλμα: " + ex.getMessage(), ButtonType.OK).showAndWait();
//            }
//        });
//
//        VBox layout = new VBox(10, new Label("Επιλέξτε Υπηρεσία:"), serviceBox, cbEntrance, cbTablet, generateBtn);
//        layout.setPadding(new Insets(15));
//        popupStage.setScene(new Scene(layout, 350, 250));
//        popupStage.show();
    }

//    private void replacePlaceholders(XWPFDocument doc, Map<String, String> values) {
//        for (XWPFParagraph para : doc.getParagraphs()) {
//            for (XWPFRun run : para.getRuns()) {
//                String text = run.getText(0);
//                if (text != null) {
//                    for (Map.Entry<String, String> entry : values.entrySet()) {
//                        if (text.contains(entry.getKey())) {
//                            text = text.replace(entry.getKey(), entry.getValue());
//                            run.setText(text, 0);
//                        }
//                    }
//                }
//            }
//        }
//    }

}
