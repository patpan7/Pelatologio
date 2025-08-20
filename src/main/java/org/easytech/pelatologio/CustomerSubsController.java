package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.helper.Features;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Subscription;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CustomerSubsController {
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn<Subscription, Integer> idColumn;
    @FXML
    private TableColumn<Subscription, String> titleColumn;
    @FXML
    private TableColumn<Subscription, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Subscription, String> categoryColumn;
    @FXML
    private TableColumn<Subscription, String> priceColumn;
    @FXML
    private TableColumn<Subscription, Boolean> sendedColumn;
    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton, renewButton;

    private ObservableList<Subscription> allSubs;

    Customer customer;

    @FXML
    public void initialize() {
        System.out.println("CustomerSubsController: Initializing...");

        setTooltip(addTaskButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editTaskButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteTaskButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Ανανέωση συμβολαίου");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));

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

        allSubs = FXCollections.observableArrayList();
        subsTable.setItems(allSubs);

        subsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
                if (selectedSub != null) {
                    try {
                        handleEditSub();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null || sub.getEndDate() == null) { // Added null check for getEndDate()
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

        // Κουμπιά
        addTaskButton.setOnAction(e -> handleAddSub());
        editTaskButton.setOnAction(e -> {
            try {
                handleEditSub();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteTaskButton.setOnAction(e -> {
            try {
                handleDeleteSub();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        renewButton.setOnAction(e -> handleRenewSub());
        System.out.println("CustomerSubsController: Initialization complete.");
    }


    private void loadSubs(int customerCode) {
        allSubs.clear();
        allSubs.setAll(DBHelper.getSubscriptionDao().getAllCustomerSubs(customerCode));
    }


    @FXML
    private void handleAddSub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Συμβολαίου");
            AddSubController controller = loader.getController();
            controller.setCustomerId(customer.getCode());
            controller.setCustomerName(customer.getName());
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditSub() throws IOException {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Συμβολαίου");
            AddSubController controller = loader.getController();

            controller.setSubForEdit(selectedSub);
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
            });
            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleDeleteSub() throws SQLException {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        Optional<ButtonType> result = AlertDialogHelper.showConfirmationDialog("Επιβεβαίωση", "Είστε βέβαιος ότι θέλετε να διαγράψετε την εργασία " + selectedSub.getTitle() + ";", "");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper.getSubscriptionDao().deleteSub(selectedSub.getId());
            loadSubs(customer.getCode());
        }
    }

    private void handleRenewSub() {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        List<String> choices = Arrays.asList("+1 χρόνο", "+2 χρόνια", "+3 χρόνια");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("+1 χρόνο", choices);
        dialog.setTitle("Ανανέωση Συμβολαίου");
        dialog.setHeaderText("Επιλέξτε διάρκεια ανανέωσης");
        dialog.setContentText("Διάρκεια:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            int yearsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", ""));
            DBHelper.getSubscriptionDao().renewSub(selectedSub.getId(), yearsToAdd);
            loadSubs(customer.getCode());
        });
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadSubs(customer.getCode());
    }


    private void setTooltip(Button button, String text) {
        if (button != null) {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(Duration.seconds(0.3));
            button.setTooltip(tooltip);
        }
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
        String msg = "Αγαπητέ/ή " + selectedSub.getCustomerName() + ",<br>"
                + "Σας υπενθυμίζουμε ότι η συνδρομή σας στο " + selectedSub.getTitle().trim() + " λήγει στις " + selectedSub.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".<br>"
                + "Για να συνεχίσετε να απολαμβάνετε τα προνόμια της συνδρομής σας, σας προσκαλούμε να την ανανεώσετε το συντομότερο δυνατόν.<br>"
                + "Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [" + selectedSub.getPrice().trim() + "€ + φπα] = " + String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24) + "€ στους παρακάτω τραπεζικούς λογαριασμούς.<br>"
                + "Εναλλακτικά επισκεφθείτε  το κατάστημα μας για χρήση μετρητών για ποσά έως 500€ ή με χρήση τραπεζικής κάρτας.<br>"
                + "Εάν έχετε οποιαδήποτε ερώτηση, μη διστάσετε να επικοινωνήσετε μαζί μας.<br>"
                + "<br><b>Τραπεζικοί Λογαριασμοί:</b><br>"
                + "<br><b>ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ</b><br><b>Λογαριασμός:</b> 29700119679<br><b>Λογαριασμός (IBAN):</b> GR6201102970000029700119679<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ<br><b>EUROBANK</b><br><b>Λογαριασμός:</b> 0026.0451.27.0200083481<br><b>Λογαριασμός (IBAN):</b> GR7902604510000270200083481<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ<br><b>myPOS</b><br><b>ΑΡ.ΠΟΡΤΟΦΟΦΟΛΙΟΥ:</b> 40005794314<br><b>Όνομα δικαιούχου:</b> GKOUMAS DIMITRIOS <br><b>IBAN:</b> IE27MPOS99039012868261 <br><b>ΑΡΙΘΜΟΣ ΛΟΓΑΡΙΑΣΜΟΥ:</b> 12868261<br><b>myPOS Ltd</b><br><b>BIC: MPOSIE2D</b>";
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
                    DBHelper.getSubscriptionDao().updateSubSent(selectedSub.getId());
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleCopy(ActionEvent event) {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Notifications.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showError();
            return;
        }
        String msg = "Αγαπητέ/ή " + selectedSub.getCustomerName() + ",\n" 
                + "Σας υπενθυμίζουμε ότι η συνδρομή σας στο " + selectedSub.getTitle().trim() + " λήγει στις " + selectedSub.getEndDate() + ".\n"
                + "Για να συνεχίσετε να απολαμβάνετε τα προνόμια της συνδρομής σας, σας προσκαλούμε να την ανανεώσετε το συντομότερο δυνατόν.\n"
                + "Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [" + selectedSub.getPrice().trim() + "€ + φπα] = " + String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24) + "€ στους παρακάτω τραπεζικούς λογαριασμούς.\n"
                + "Εναλλακτικά επισκεφθείτε  το κατάστημα μας για χρήση μετρητών για ποσά έως 500€ ή με χρήση τραπεζικής κάρτας.\n"
                + "Εάν έχετε οποιαδήποτε ερώτηση, μη διστάσετε να επικοινωνήσετε μαζί μας.\n"
                + "Με εκτίμηση,\n"
                + "\n"
                + "EasyTech\n"
                + "Γκούμας Δημήτρης \n"
                + "Δενδρινού & Γρηγορίου Ε’ 10\n"
                + "85100 Ρόδος\n"
                + "Τηλ. 22410 36750 \n"
                + "Κιν.6944570089\n"
                + "\n"
                + "Τραπεζικοί Λογαριασμοί:\n"
                + "\n"
                + "ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ\n"
                + "Λογαριασμός: 29700119679\n"
                + "Λογαριασμός (IBAN): GR6201102970000029700119679\n"
                + "Με Δικαιούχους: ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ\n"
                + "EUROBANK\n"
                + "Λογαριασμός: 0026.0451.27.0200083481\n"
                + "Λογαριασμός (IBAN): GR7902604510000270200083481\n"
                + "Με Δικαιούχους: ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ\n"
                + "myPOS\n"
                + "ΑΡ.ΠΟΡΤΟΦΟΛΙΟΥ: 40005794314\n"
                + "Όνομα δικαιούχου: GKOUMAS DIMITRIOS \n"
                + "IBAN: IE27MPOS99039012868261 \n"
                + "ΑΡΙΘΜΟΣ ΛΟΓΑΡΙΑΣΜΟΥ: 12868261\n"
                + "myPOS Ltd \n"
                + "BIC: MPOSIE2D\n";
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