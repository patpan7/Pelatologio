package org.easytech.pelatologio;

import javafx.application.Platform;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OffersController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Offer> offersTable;
    @FXML
    private TableColumn idColumn, descriptionColumn, offerDateColumn, cucstomerColum, statusColumn, response_dateColumn, sendedColumn;
    @FXML
    private CheckBox showAllCheckbox, acceptCheckbox, rejectCheckbox, pendingCheckbox, archivedCheckbox;
    @FXML
    private Button addOfferButton, editOfferButton, deleteOfferButton;

    private ObservableList<Offer> allOffers = FXCollections.observableArrayList();

    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addOfferButton, "Προσθήκη νέας προσφοράς");
        setTooltip(editOfferButton, "Επεξεργασία προσφοράς");
        setTooltip(deleteOfferButton, "Διαγραφή προσφοράς");

        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        offerDateColumn.setCellValueFactory(new PropertyValueFactory<>("offerDate"));
        cucstomerColum.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        response_dateColumn.setCellValueFactory(new PropertyValueFactory<>("response_date"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));

        // Υπολογισμός της πρώτης και τελευταίας ημέρας του τρέχοντος μήνα
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = YearMonth.from(today).atEndOfMonth();


        // Αρχικό γέμισμα του πίνακα
        loadOffers();


        offersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedOffer != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditOffer();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Φίλτρα
        CheckBox[] checkBoxes1 = {
                showAllCheckbox,
                acceptCheckbox,
                rejectCheckbox,
                pendingCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes1);

        showAllCheckbox.setOnAction(e -> updateOffersTable());
        acceptCheckbox.setOnAction(e -> updateOffersTable());
        rejectCheckbox.setOnAction(e -> updateOffersTable());
        pendingCheckbox.setOnAction(e -> updateOffersTable());
        archivedCheckbox.setOnAction(e -> updateOffersTable());

        // Κουμπιά
        addOfferButton.setOnAction(e -> handleAddOffer());
        editOfferButton.setOnAction(e -> {
            try {
                handleEditOffer();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteOfferButton.setOnAction(e -> {
            try {
                handleDeleteOffer();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void configureSingleSelectionCheckBoxes(CheckBox[] checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (CheckBox otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private void loadOffers() {
        List<TableColumn<Offer, ?>> sortOrder = new ArrayList<>(offersTable.getSortOrder());
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allOffers.setAll(dbHelper.getAllOffers());
        updateOffersTable();
        offersTable.getSortOrder().setAll(sortOrder);
    }

    private void updateOffersTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Offer> filteredOffers = FXCollections.observableArrayList(allOffers);
        // Φιλτράρισμα βάσει ολοκλήρωσης
        if (!showAllCheckbox.isSelected()) {
            if (acceptCheckbox.isSelected()) {
                filteredOffers.removeIf(offer -> !offer.getStatus().trim().contains("Αποδοχή"));
            } else if (pendingCheckbox.isSelected()) {
                filteredOffers.removeIf(offer -> !offer.getStatus().trim().contains("Αναμονή"));
            } else if (rejectCheckbox.isSelected()) {
                filteredOffers.removeIf(offer -> !offer.getStatus().trim().contains("Απόρριψη"));
            }
        }
        if (archivedCheckbox.isSelected()) {
            filteredOffers.removeIf(offer -> !offer.getArchived());
        } else {
            filteredOffers.removeIf(offer -> offer.getArchived());
        }
        // Ανανεώνουμε τα δεδομένα του πίνακα
        offersTable.setItems(filteredOffers);
    }

    private void handleAddOffer() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOffer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Προσφοράς");
            AddOfferController controller = loader.getController();
            controller.lockFile();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOffer();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.showAndWait();
            loadOffers();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleEditOffer() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί προσφορά!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOffer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Προσφοράς");
            AddOfferController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setOfferForEdit(selectedOffer);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOffer();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadOffers();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleDeleteOffer() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί προσφορά!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την προσφορά " + selectedOffer.getDescription() + ";");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteOffer(selectedOffer.getId());
            loadOffers();
        }
    }

    public void handleAddTask(ActionEvent evt) {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα προσφορά.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle("Προσφορά "+ selectedOffer.getId() +": "+ selectedOffer.getCustomerName());
            controller.setCustomerName(selectedOffer.getCustomerName());
            controller.setCustomerId(selectedOffer.getCustomerId());
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

    public void handleShareOffer(ActionEvent evt) {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα προσφορά.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
            return;
        }
        String msg ="Επωνυμία: "+selectedOffer.getCustomerName() +
                "\nΣας αποστείλαμε μια νέα προσφορά"+
                "\nΜπορείτε να την δείτε και να την αποδεχτείτε ή να την απορρίψετε μέσω του παρακάτω συνδέσμου:" +
                "\nhttp://dgou.dynns.com:8090/portal/offer.php?id="+selectedOffer.getId()+
                "\n\nΜπορείτε δείτε τους τραπεζικούς μας λογαριασμούς στην παρακάτω διεύθυνση:" +
                "\nhttp://dgou.dynns.com:8090/portal/bank_accounts.php" +
                "\n\nΓια οποιαδήποτε διευκρίνιση, είμαστε στη διάθεσή σας." +
                "\nΕυχαριστώ πολύ";
        copyTextToClipboard(msg);
    }

    public void handleSendEmail(ActionEvent event) {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα προσφορά.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }
        try {
            DBHelper dbHelper = new DBHelper();
            Customer customer = dbHelper.getSelectedCustomer(selectedOffer.getCustomerId());
            String email = customer.getEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");
            EmailDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setEmail(email);
            controller.setSubject("Προσφορά " + selectedOffer.getId() + ": " + selectedOffer.getCustomerName());
            controller.setBody("<h3>"+selectedOffer.getDescription() + "</h3>" +
                    "<br><br><h3>Μπορείτε να την δείτε και να την αποδεχτείτε ή να την απορρίψετε μέσω του παρακάτω συνδέσμου: </h3>" +
                    "<a href=http://dgou.dynns.com:8090/portal/offer.php?id=" + selectedOffer.getId()+"><b><h2>Αποδοχή ή Απόρριψη προσφορά "+selectedOffer.getId()+"</b><h2></a>" +
                    //"<br>http://dgou.dynns.com:8090/portal/offer.php?id=" + selectedOffer.getId() +
                    "<br><br><h3>Μπορείτε δείτε τους τραπεζικούς μας λογαριασμούς </h3>" +
                    "<a href=http://dgou.dynns.com:8090/portal/bank_accounts.php><b><h2>Τραπεζικοί λογαριασμοί</b></h2></a>" +
                    //"<br>http://dgou.dynns.com:8090/portal/bank_accounts.php" +
                    "<br><br><h3>Για οποιαδήποτε διευκρίνιση, είμαστε στη διάθεσή σας.</h3>");
            List<File> attachments = new ArrayList<>();
            String[] offerPaths = selectedOffer.getPaths().split(";");
            for (String path : offerPaths) {
                if (path.equals(""))
                    break;
                File file = new File(path);
                attachments.add(file);
            }
            controller.setAttachments(attachments);
            controller.setCopy(false);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.show();
            dialog.setOnCloseRequest(evt -> {
                if (controller.isSended) {
                    // Εκτελούμε το handleSendEmail
                    dbHelper.updateOfferSent(selectedOffer.getId());
                    loadOffers();
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleAccept (ActionEvent event) {
        toggleAns("Αποδοχή Χειρ.");
    }

    @FXML
    private void handleReject (ActionEvent event) {
        toggleAns("Απόρριψη Χειρ.");
    }

    @FXML
    private void handleArchive (ActionEvent event) {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα προσφορά.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (dbHelper.updateOfferArchived(selectedOffer.getId())) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση προσφοράς επιτυχής.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
            loadOffers(); // Φορτώνει ξανά τις εργασίες
        } else {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Αποτυχία ενημέρωση προσφοράς.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
        }
    }

    private void toggleAns(String ans) {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Παρακαλώ επιλέξτε ένα προσφορά.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (dbHelper.updateOfferStatusManual(selectedOffer.getId(), ans)) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Η προσφορά αρχειοθετήθηκε.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
            loadOffers(); // Φορτώνει ξανά τις εργασίες
        } else {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Αποτυχία αρχειοθέτησης προσφοράς.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();});
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
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

    public void refresh(MouseEvent mouseEvent) {
        loadOffers();
    }
}
