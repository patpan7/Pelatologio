package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.EmailTemplateHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.SubsCategory;
import org.easytech.pelatologio.models.Subscription;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SubsViewController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn idColumn, titleColumn, endDateColumn, customerColumn, categoryColumn, priceColumn, sendedColumn;
    @FXML
    private TableColumn<Subscription, Boolean> activeColumn;

    @FXML
    private DatePicker dateFrom, dateTo;
    @FXML
    private ComboBox<SubsCategory> categoryFilterComboBox;
    @FXML
    private Button addCategoryButton, addSubButton, editSubButton, deleteSubButton, renewButton;

    private final ObservableList<Subscription> allSubs = FXCollections.observableArrayList();

    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @FXML
    private CheckBox showInactiveCheckBox;

    private FilteredList<Subscription> filteredSubs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addSubButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editSubButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteSubButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Aνανέωση συμβολαίου");
        setTooltip(addCategoryButton, "Προσθήκη/Επεξεργασία κατηγοριών εργασιών");

        // Initialize FilteredList
        filteredSubs = new FilteredList<>(allSubs, p -> true);
        subsTable.setItems(filteredSubs);

        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        activeColumn.setCellFactory(param -> new TableCell<Subscription, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Subscription sub = getTableView().getItems().get(getIndex());
                    sub.setActive(checkBox.isSelected());
                    DBHelper.getSubscriptionDao().updateSubscriptionStatus(sub.getId(), sub.isActive());
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });

        // Custom cell factory for endDateColumn to format LocalDate
        endDateColumn.setCellFactory(column -> {
            return new TableCell<Subscription, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });

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

        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                getStyleClass().removeAll("expired-row", "expiring-soon-row");
                if (empty || sub == null || sub.getEndDate() == null) {
                    setStyle("");
                } else {
                    LocalDate today = LocalDate.now();
                    LocalDate tenDaysLater = today.plusDays(10);

                    if (sub.getEndDate().isBefore(today)) {
                        getStyleClass().add("expired-row");
                    } else if (!sub.getEndDate().isAfter(today) && sub.getEndDate().isBefore(tenDaysLater)) {
                        getStyleClass().add("expiring-soon-row");
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
        List<SubsCategory> categories = DBHelper.getSubscriptionDao().getAllSubsCategory();
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

        showInactiveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateTaskTable());


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
        allSubs.setAll(DBHelper.getSubscriptionDao().getAllSubs(from, to));
        updateTaskTable();
        subsTable.getSortOrder().setAll(sortOrder);
    }

    private void updateTaskTable() {
        filteredSubs.setPredicate(sub -> {
            boolean categoryMatch = true;
            SubsCategory selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null && selectedCategory.getId() != 0) {
                categoryMatch = sub.getCategory().equals(selectedCategory.getName());
            }

            boolean activeMatch = true;
            if (!showInactiveCheckBox.isSelected()) {
                activeMatch = sub.isActive();
            }

            return categoryMatch && activeMatch;
        });
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
            DBHelper.getSubscriptionDao().deleteSub(selectedSub.getId());
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        }
    }

    private void handleRenewSub() {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        List<String> choices = Arrays.asList("+1 μήνας", "+3 μήνες", "+6 μήνες", "+1 χρόνος", "+2 χρόνια", "+3 χρόνια");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("+1 χρόνος", choices);
        dialog.setTitle("Ανανέωση Συμβολαίου");
        dialog.setHeaderText("Επιλέξτε διάρκεια ανανέωσης");
        dialog.setContentText("Διάρκεια:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            int monthsToAdd = 0;
            if (selected.contains("μήνας")) {
                monthsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", ""));
            } else if (selected.contains("χρόνος")) {
                monthsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", "")) * 12;
            }
            DBHelper.getSubscriptionDao().renewSub(selectedSub.getId(), monthsToAdd);
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
            Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο για να στείλετε το e-mail.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showError();
            return;
        }
        Customer customer = DBHelper.getCustomerDao().getSelectedCustomer(selectedSub.getCustomerId());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{calculatedPrice}", String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24));

        EmailTemplateHelper.EmailContent emailContent = EmailTemplateHelper.prepareEmail("subsReminder", customer, selectedSub, placeholders);

        try {
            String email = customer.getEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");
            EmailDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setEmail(email);
            controller.setSubject(emailContent.subject);
            controller.setBody(emailContent.body);
            controller.setCopy(false);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.show();
            dialog.setOnCloseRequest(evt -> {
                if (controller.isSended) {
                    DBHelper.getSubscriptionDao().updateSubSent(selectedSub.getId());
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
                "Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [" + selectedSub.getPrice().trim() + "€ + φπα] = " + String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24) + "€ στους παρακάτω τραπεζικούς λογαριασμούς.\n" +
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
