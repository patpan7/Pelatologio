package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerOffersController {
    @FXML
    private TableView<Offer> offersTable;
    @FXML
    private TableColumn idColumn, descriptionColumn, offerDateColumn, statusColumn, response_dateColumn, sendedColumn;
    @FXML
    private Button createOfferButton, addOfferButton, editOfferButton, deleteOfferButton;

    private ObservableList<Offer> allOffers;

    Customer customer;

    @FXML
    public void initialize() {
        setTooltip(createOfferButton, "Δημιουργία προσφοράς από πρότυπα");
        setTooltip(addOfferButton, "Προσθήκη νέας προσφοράς");
        setTooltip(editOfferButton, "Επεξεργασία προσφοράς");
        setTooltip(deleteOfferButton, "Διαγραφή προσφοράς");

        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        offerDateColumn.setCellValueFactory(new PropertyValueFactory<>("offerDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        response_dateColumn.setCellValueFactory(new PropertyValueFactory<>("response_date"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));

        allOffers = FXCollections.observableArrayList();
        offersTable.setItems(allOffers);


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

        // Κουμπιά
        createOfferButton.setOnAction(event -> {
            ContextMenu contextMenu = new ContextMenu();
            File folder = new File(AppSettings.loadSetting("datafolder") + "\\Templates");

            // Δημιουργία φακέλου αν δεν υπάρχει
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Δημιουργία MenuItem για κάθε αρχείο στον φάκελο
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    String displayName = file.getName();
                    MenuItem fileItem = new MenuItem(displayName);
                    fileItem.setOnAction(e -> {
                        try {
                            CustomerFolderManager folderManager = new CustomerFolderManager();
                            // Κλήση της μεθόδου για δημιουργία ή άνοιγμα του φακέλου
                            folderManager.createCustomerOfferFolder(customer.getName(), customer.getAfm());

                            Desktop.getDesktop().open(file);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    contextMenu.getItems().add(fileItem);
                }
            }

            // Προσθήκη επιλογής για άνοιγμα του φακέλου
            MenuItem openFolderItem = new MenuItem("Άνοιγμα φακέλου");
            openFolderItem.setOnAction(e -> openFolder(AppSettings.loadSetting("datafolder") + "\\Templates"));
            contextMenu.getItems().add(openFolderItem);

            // Εμφάνιση του ContextMenu πάνω από το κουμπί
            double buttonX = createOfferButton.localToScene(createOfferButton.getBoundsInLocal()).getMinX();
            double buttonY = createOfferButton.localToScene(createOfferButton.getBoundsInLocal()).getMinY() - 2 * createOfferButton.getHeight();
            contextMenu.show(createOfferButton, createOfferButton.getScene().getWindow().getX() + buttonX,
                    createOfferButton.getScene().getWindow().getY() + buttonY);
        });

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


    private void loadOffers(int customerCode) {
        allOffers.clear();
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allOffers.setAll(dbHelper.getAllCustomerOffers(customerCode));
    }


    @FXML
    private void handleAddOffer() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOffer.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Προσφοράς");
            AddOfferController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setCustomerName(customer.getName());
            controller.lock();
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
            loadOffers(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
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
            controller.setCustomer(customer);
            controller.lock();
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
            loadOffers(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
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
            loadOffers(customer.getCode());
        }
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadOffers(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
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
                notifications.showError();
            });
            return;
        }
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Εργασίας");
            AddTaskController controller = loader.getController();
            controller.setTaskTitle("Προσφορά " + selectedOffer.getId() + ": " + selectedOffer.getCustomerName());
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
                notifications.showError();
            });
            return;
        }
        String msg = "Επωνυμία: " + selectedOffer.getCustomerName() +
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
            String email = customer.getEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");
            EmailDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setEmail(email);
            controller.setSubject("Προσφορά " + selectedOffer.getId() + ": " + selectedOffer.getCustomerName());
            controller.setBody(selectedOffer.getDescription() +
                    "<br><br>Μπορείτε να την δείτε και να την αποδεχτείτε ή να την απορρίψετε μέσω του παρακάτω συνδέσμου:" +
                    "<br>http://dgou.dynns.com:8090/portal/offer.php?id=" + selectedOffer.getId() +
                    "<br><br>Μπορείτε δείτε τους τραπεζικούς μας λογαριασμούς στην παρακάτω διεύθυνση:" +
                    "<br>http://dgou.dynns.com:8090/portal/bank_accounts.php" +
                    "<br><br>Για οποιαδήποτε διευκρίνιση, είμαστε στη διάθεσή σας.");
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
                    DBHelper dbHelper = new DBHelper();
                    dbHelper.updateOfferSent(selectedOffer.getId());
                    loadOffers(customer.getCode());
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
            System.out.println("Task completion status updated.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση προσφοράς επιτυχής.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
            loadOffers(customer.getCode()); // Φορτώνει ξανά τις εργασίες
        } else {
            System.out.println("Failed to update task completion status.");
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

    // Μέθοδος για άνοιγμα του φακέλου
    private void openFolder(String folderPath) {
        try {
            Desktop.getDesktop().open(new File(folderPath));
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα φακέλου.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }
}
