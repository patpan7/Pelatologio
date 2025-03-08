package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CustomerOffersController {
    @FXML
    private TableView<Offer> offersTable;
    @FXML
    private TableColumn idColumn, descriptionColumn, offerDateColumn, statusColumn, response_dateColumn;
    @FXML
    private Button addOfferButton, editOfferButton, deleteOfferButton, renewButton;

    private ObservableList<Offer> allOffers;

    Customer customer;

    @FXML
    public void initialize() {
        setTooltip(addOfferButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editOfferButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteOfferButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Ανανέωση συμβολαίου");

        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        offerDateColumn.setCellValueFactory(new PropertyValueFactory<>("offerDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        response_dateColumn.setCellValueFactory(new PropertyValueFactory<>("response_date"));

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
            dialog.setTitle("Προσθήκη Συμβολαίου");
            AddOfferController controller = loader.getController();
            controller.setCustomerId(customer.getCode());
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
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την προσφορά " + selectedOffer.getDescription() + ";" );
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


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }
}
