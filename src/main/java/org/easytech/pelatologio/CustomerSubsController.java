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
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CustomerSubsController {
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn idColumn, titleColumn, endDateColumn, categoryColumn, priceColumn;
    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton, renewButton;

    private ObservableList<Subscription> allSubs;

    Customer customer;

    @FXML
    public void initialize() {
        setTooltip(addTaskButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editTaskButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteTaskButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Ανανέωση συμβολαίου");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        allSubs = FXCollections.observableArrayList();
        subsTable.setItems(allSubs);


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
        renewButton.setOnAction(e -> {
            handleRenewSub();
        });
    }


    private void loadSubs(int customerCode) {
        allSubs.clear();
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allSubs.setAll(dbHelper.getAllCustomerSubs(customerCode));
    }


    @FXML
    private void handleAddSub() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Συμβολαίου");
            AddSubController controller = loader.getController();
            controller.setCustomerId(customer.getCode());
            controller.setCustomerName(customer.getName());
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

            dialog.showAndWait();
            loadSubs(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditSub() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
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
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Συμβολαίου");
            AddSubController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setSubForEdit(selectedSub);
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveSub();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadSubs(customer.getCode());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
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
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την εργασία " + selectedSub.getTitle() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteSub(selectedSub.getId());
            loadSubs(customer.getCode());
        }
    }

    private void handleRenewSub () {
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
            loadSubs(customer.getCode());
        });
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        loadSubs(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}
