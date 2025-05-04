package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SubsController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn idColumn, titleColumn, endDateColumn, customerColumn, categoryColumn, priceColumn, sendedColumn;
    @FXML
    private DatePicker dateFrom, dateTo;
    @FXML
    private ComboBox<SubsCategory> categoryFilterComboBox;
    @FXML
    private Button addCategoryButton, addSubButton, editSubButton, deleteSubButton, renewButton;

    private ObservableList<Subscription> allSubs = FXCollections.observableArrayList();

    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addSubButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editSubButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteSubButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Aνανέωση συμβολαίου");
        setTooltip(addCategoryButton, "Προσθήκη/Επεξεργασία κατηγοριών εργασιών");
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));

        // Υπολογισμός της πρώτης και τελευταίας ημέρας του τρέχοντος μήνα
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = YearMonth.from(today).atEndOfMonth();

        // Ορισμός των ημερομηνιών στα DatePicker
        dateFrom.setValue(firstDay);
        dateTo.setValue(lastDay);

        // Προσθήκη listener ώστε να φορτώνεται ξανά η λίστα όταν αλλάζει ημερομηνία
        ChangeListener<LocalDate> dateChangeListener = (ObservableValue<? extends LocalDate> obs, LocalDate oldValue, LocalDate newValue) -> {
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        };

        dateFrom.valueProperty().addListener(dateChangeListener);
        dateTo.valueProperty().addListener(dateChangeListener);

        // Αρχικό γέμισμα του πίνακα
        loadSubs(dateFrom.getValue(), dateTo.getValue());

        // RowFactory για διαφορετικά χρώματα
        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null) {
                    setStyle("");
                } else {
                    if (sub.getEndDate().isBefore(LocalDate.now())) {
                        setStyle("-fx-background-color: #edd4d4; -fx-text-fill: #155724;"); // Πράσινο
                    } else {
                        setStyle(""); // Προεπιλογή
                    }
                }
            }
        });

        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null) {
                    setStyle("");
                } else {
                    LocalDate today = LocalDate.now();
                    LocalDate tenDaysLater = today.plusDays(10);

                    if (sub.getEndDate().isBefore(today)) {
                        setStyle("-fx-background-color: #edd4d4; -fx-text-fill: #155724;"); // Κόκκινο για ληγμένες συνδρομές
                    } else if (!sub.getEndDate().isBefore(today) && !sub.getEndDate().isAfter(tenDaysLater)) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;"); // Κίτρινο για συνδρομές που λήγουν σύντομα
                    } else {
                        setStyle(""); // Κανονικό χρώμα
                    }
                }
            }
        });

        subsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedSub != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditSub();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        DBHelper dbHelper = new DBHelper();
        List<SubsCategory> categories = dbHelper.getAllSubsCategory();
        categoryFilterComboBox.getItems().add(new SubsCategory(0, "Όλες"));
        categoryFilterComboBox.getItems().addAll(categories);
        categoryFilterComboBox.getSelectionModel().selectFirst();
        categoryFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SubsCategory subsCategory) {
                return subsCategory != null ? subsCategory.getName() : "";
            }

            @Override
            public SubsCategory fromString(String string) {
                return categoryFilterComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTaskTable());


        // Κουμπιά
        addCategoryButton.setOnAction(e -> TaskCategoryManager());
        addSubButton.setOnAction(e -> handleAddSub());
        editSubButton.setOnAction(e -> {
            try {
                handleEditSub();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteSubButton.setOnAction(e -> {
            try {
                handleDeleteSub();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        renewButton.setOnAction(e -> {
            handleRenewSub();
        });

    }

    private void loadSubs(LocalDate from, LocalDate to) {
        List<TableColumn<Subscription, ?>> sortOrder = new ArrayList<>(subsTable.getSortOrder());
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allSubs.setAll(dbHelper.getAllSubs(from, to));
        updateTaskTable();
        subsTable.getSortOrder().setAll(sortOrder);
    }

    private void updateTaskTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Subscription> filteredTasks = FXCollections.observableArrayList(allSubs);
        // Φιλτράρισμα βάσει κατηγορίας
        SubsCategory selectedCategory = categoryFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedCategory != null && selectedCategory.getId() != 0) { // Εξαιρείται η κατηγορία "Όλες"
            filteredTasks.removeIf(sub -> !sub.getCategory().equals(selectedCategory.getName()));
        }
        // Ανανεώνουμε τα δεδομένα του πίνακα
        subsTable.setItems(filteredTasks);
    }

    private void handleAddSub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Προσθήκη Συμβολαίου");


            AddSubController controller = loader.getController();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
            });

            dialog.setOnHidden(e -> {
                loadSubs(dateFrom.getValue(), dateTo.getValue());
            });

            dialog.initModality(Modality.NONE);  // <-- Εδώ γίνεται η κύρια αλλαγή
            dialog.initOwner(null);  // Προαιρετικό για καλύτερη εμφάνιση
            dialog.showAndWait();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα",
                    "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleEditSub() throws IOException {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Επεξεργασία Συμβολαίου");

            AddSubController controller = loader.getController();
            controller.setSubForEdit(selectedSub);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
            });

            dialog.setOnHidden(e -> {
                loadSubs(dateFrom.getValue(), dateTo.getValue());
            });

            dialog.initModality(Modality.NONE);
            dialog.showAndWait();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα",
                    "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleDeleteSub() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε το συμβόλαιο " + selectedSub.getTitle() + ";");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteSub(selectedSub.getId());
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        }
    }

    private void handleRenewSub() {
        // Επεξεργασία επιλεγμένης εργασίας
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        // Δημιουργία διαλόγου με επιλογές
        List<String> choices = Arrays.asList("+1 χρόνο", "+2 χρόνια", "+3 χρόνια");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("+1 χρόνο", choices);
        dialog.setTitle("Ανανέωση Συμβολαίου");
        dialog.setHeaderText("Επιλέξτε διάρκεια ανανέωσης");
        dialog.setContentText("Διάρκεια:");

        // Αν ο χρήστης επιλέξει κάτι
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            int yearsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", ""));
            DBHelper dbHelper = new DBHelper();
            dbHelper.renewSub(selectedSub.getId(), yearsToAdd);
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        });
    }

    public void TaskCategoryManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subsCategoryManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            SubsCategoryManagerViewController controller = loader.getController();
            controller.loadSubsCategories();


            dialog.setTitle("Κατηγορίες Εργασιών");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void handleSendMail(ActionEvent event) {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο για να στείλετε το e-mail.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        DBHelper dbHelper = new DBHelper();
        Customer customer = dbHelper.getSelectedCustomer(selectedSub.getCustomerId());
        String msg = "Αγαπητέ/ή " + selectedSub.getCustomerName() + ",\n" +
                "<br>Σας υπενθυμίζουμε ότι η συνδρομή σας στο " + selectedSub.getTitle().trim() + " λήγει στις " + selectedSub.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "." +
                "<br>Για να συνεχίσετε να απολαμβάνετε τα προνόμια της συνδρομής σας, σας προσκαλούμε να την ανανεώσετε το συντομότερο δυνατόν." +
                "<br>Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [" + selectedSub.getPrice().trim() + "€ + φπα] = " + String.format("%.02f",Float.parseFloat(selectedSub.getPrice().trim()) * 1.24) + "€ στους παρακάτω τραπεζικούς λογαριασμούς." +
                "<br>Εναλλακτικά επισκεφθείτε  το κατάστημα μας για χρήση μετρητών για ποσά έως 500€ ή με χρήση τραπεζικής κάρτας." +
                "<br>Εάν έχετε οποιαδήποτε ερώτηση, μη διστάσετε να επικοινωνήσετε μαζί μας." +
                "<br>" +
                "<br><b>Τραπεζικοί Λογαριασμοί:</b>" +
                "<br>" +
                "<br><b>ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ</b>" +
                "<br><b>Λογαριασμός:</b> 29700119679" +
                "<br><b>Λογαριασμός (IBAN):</b> GR6201102970000029700119679" +
                "<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ" +
                "<br><b>EUROBANK</b>" +
                "<br><b>Λογαριασμός:</b> 0026.0451.27.0200083481" +
                "<br><b>Λογαριασμός (IBAN):</b> GR7902604510000270200083481" +
                "<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ" +
                "<br><b>myPOS</b>" +
                "<br><b>ΑΡ.ΠΟΡΤΟΦΟΛΙΟΥ:</b> 40005794314" +
                "<br><b>Όνομα δικαιούχου:</b> GKOUMAS DIMITRIOS " +
                "<br><b>IBAN:</b> IE27MPOS99039012868261 " +
                "<br><b>ΑΡΙΘΜΟΣ ΛΟΓΑΡΙΑΣΜΟΥ:</b> 12868261" +
                "<br><b>myPOS Ltd</b>" +
                "<br><b>BIC: MPOSIE2D</b>";
        try {
            String email = customer.getEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");
            EmailDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setEmail(email);
            controller.setSubject("Υπενθύμιση λήξης συνδρομής " + selectedSub.getTitle());
            controller.setBody(msg);
            controller.setCopy(false);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.show();
            dialog.setOnCloseRequest(evt -> {
                if (controller.isSended) {
                    // Εκτελούμε το handleSendEmail
                    dbHelper.updateSubSent(selectedSub.getId());
                    loadSubs(dateFrom.getValue(), dateTo.getValue());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleCopy(ActionEvent event) {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return;
        }
        String msg = "Αγαπητέ/ή " + selectedSub.getCustomerName() + ",\n" +
                "Σας υπενθυμίζουμε ότι η συνδρομή σας στο " + selectedSub.getTitle().trim() + " λήγει στις " + selectedSub.getEndDate() + ".\n" +
                "Για να συνεχίσετε να απολαμβάνετε τα προνόμια της συνδρομής σας, σας προσκαλούμε να την ανανεώσετε το συντομότερο δυνατόν.\n" +
                "Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [" + selectedSub.getPrice().trim() + "€ + φπα] = " + String.format("%.02f",Float.parseFloat(selectedSub.getPrice().trim()) * 1.24) + "€ στους παρακάτω τραπεζικούς λογαριασμούς.\n" +
                "Εναλλακτικά επισκεφθείτε  το κατάστημα μας για χρήση μετρητών για ποσά έως 500€ ή με χρήση τραπεζικής κάρτας.\n" +
                "Εάν έχετε οποιαδήποτε ερώτηση, μη διστάσετε να επικοινωνήσετε μαζί μας.\n" +
                "Με εκτίμηση,\n" +
                "\n" +
                "EasyTech\n" +
                "Γκούμας Δημήτρης \n" +
                "Δενδρινού & Γρηγορίου Ε’ 10\n" +
                "85100 Ρόδος\n" +
                "Τηλ. 22410 36750 \n" +
                "Κιν.6944570089\n" +
                "\n" +
                "Τραπεζικοί Λογαριασμοί:\n" +
                "\n" +
                "ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ\n" +
                "Λογαριασμός: 29700119679\n" +
                "Λογαριασμός (IBAN): GR6201102970000029700119679\n" +
                "Με Δικαιούχους: ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ\n" +
                "EUROBANK\n" +
                "Λογαριασμός: 0026.0451.27.0200083481\n" +
                "Λογαριασμός (IBAN): GR7902604510000270200083481\n" +
                "Με Δικαιούχους: ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ\n" +
                "myPOS\n" +
                "ΑΡ.ΠΟΡΤΟΦΟΛΙΟΥ: 40005794314\n" +
                "Όνομα δικαιούχου: GKOUMAS DIMITRIOS \n" +
                "IBAN: IE27MPOS99039012868261 \n" +
                "ΑΡΙΘΜΟΣ ΛΟΓΑΡΙΑΣΜΟΥ: 12868261\n" +
                "myPOS Ltd \n" +
                "BIC: MPOSIE2D\n";
        copyTextToClipboard(msg);
    }

    // Μέθοδος αντιγραφής κειμένου στο πρόχειρο
    private void copyTextToClipboard(String msg) {
        // Κώδικας για αντιγραφή κειμένου στο πρόχειρο
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(msg);  // Replace with the desired text
        clipboard.setContent(content);
        //showAlert("Copied to Clipboard", msg);
        Notifications notifications = Notifications.create()
                .title("Αντιγραφή στο πρόχειρο")
                .text(msg)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
        notifications.showInformation();
    }
}
