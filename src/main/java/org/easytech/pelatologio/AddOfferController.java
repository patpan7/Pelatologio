package org.easytech.pelatologio;

import atlantafx.base.controls.ToggleSwitch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Offer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddOfferController {

    @FXML
    private ListView<File> attachmentList;
    @FXML
    private TextField hoursField, statusField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private Button btnCustomer, btnSelectFile;
    @FXML
    private ToggleSwitch isArchived;

    private Offer offer;
    private Customer selectedCustomer;
    private FilteredList<Customer> filteredCustomers;
    private final List<File> attachments = new ArrayList<>();
    private Stage dialogStage;

    public void setCustomer(Customer customer) {
        this.selectedCustomer = customer;
    }


    public void setCustomerName(String custName) {
        for (Customer customer : customerComboBox.getItems()) {
            if (customer.getName().equals(custName)) {
                customerComboBox.setValue(customer);
                break;
            }
        }
    }

    public void setDescription(String description) {
        descriptionField.setText(description);
    }

    public void setOfferForEdit(Offer offer) {
        this.offer = offer;
        dueDatePicker.setValue(offer.getOfferDate());
        descriptionField.setText(offer.getDescription().trim());
        hoursField.setText(offer.getHours().trim());
        statusField.setText(offer.getStatus().trim());
        // Αν υπάρχει πελάτης, προ-συμπλήρωσε την επιλογή
        if (offer.getCustomerId() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCode() == offer.getCustomerId()) {
                    customerComboBox.setValue(customer);
                    this.selectedCustomer = customer;
                    break;
                }
            }
        }
        String[] offerPaths = offer.getPaths().split(";");
        for (String path : offerPaths) {
            if (path.equals(""))
                break;
            File file = new File(path);
            attachmentList.getItems().add(file);
            attachments.add(file);
        }
        isArchived.setSelected(offer.getArchived());
    }


    public void initialize() throws SQLException {
        // Φόρτωση πελατών
        List<Customer> customers = DBHelper.getCustomerDao().getCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers));
        //customerComboBox.getItems().addAll(filteredCustomers); // Προσθήκη αντικειμένων Customer
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        // StringConverter για σωστή διαχείριση αντικειμένων
        customerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return customers.stream()
                        .filter(c -> c.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        ComboBoxHelper.setupFilter(customerComboBox, filteredCustomers);
        customerComboBox.setVisibleRowCount(5);
        dueDatePicker.setValue(LocalDate.now());
        statusField.setText("Αναμονή");

        // Δημιουργία του context menu για διαγραφή
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Διαγραφή");
        MenuItem openItem = new MenuItem("Άνοιγμα");
        contextMenu.getItems().add(deleteItem);
        contextMenu.getItems().add(openItem);

        // Λειτουργία διαγραφής από το context menu
        deleteItem.setOnAction(event -> {
            File selectedFile = attachmentList.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                attachments.remove(selectedFile);
                attachmentList.getItems().remove(selectedFile);
                if (selectedFile.exists()) {
                    selectedFile.delete(); // Διαγραφή του αρχείου
                }
            }
        });

        // Λειτουργία διαγραφής από το context menu
        openItem.setOnAction(event -> {
            File selectedFile = attachmentList.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                if (selectedFile.exists()) {
                    try {
                        Desktop.getDesktop().open(selectedFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Εφαρμογή του context menu στην λίστα
        attachmentList.setContextMenu(contextMenu);
        attachmentList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Διπλό κλικ
                File selectedFile = attachmentList.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    System.out.println(selectedFile.getAbsolutePath());
                    if (selectedFile.exists()) {
                        try {

                            Desktop.getDesktop().open(selectedFile);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }


    public boolean handleSaveOffer() {
        try {
            if (dueDatePicker.getValue() == null || descriptionField.getText() == null) {
                Platform.runLater(() -> {
                    CustomNotification.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT)
                            .showWarning();
                });
                return false;
            }

            LocalDate offerDate = dueDatePicker.getValue();
            String description = descriptionField.getText();
            String hours = hoursField.getText();
            String status = statusField.getText();

            Customer selectedCustomer = customerComboBox.getValue();
            String paths = attachments.stream()
                    .map(File::getAbsolutePath) // Παίρνουμε τα paths
                    .collect(Collectors.joining(";")); // Ενώνουμε με ";"

            if (offer == null) {
                //Δημιουργία νέας εργασίας
                Offer newOffer = new Offer(0, offerDate, description, hours, status, selectedCustomer.getCode(), null, null, paths, "Όχι", false);
                DBHelper.getOfferDao().saveOffer(newOffer);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                offer.setOfferDate(offerDate);
                offer.setDescription(description);
                offer.setHours(hours);
                offer.setStatus(status);
                offer.setCustomerId(selectedCustomer.getCode());
                offer.setPaths(paths);
                offer.setArchived(isArchived.isSelected());
                DBHelper.getOfferDao().updateOffer(offer);
            }

            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Επιτυχία")
                        .text("Η εργασία αποθηκεύτηκε!")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showConfirmation();
            });
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποθήκευση της εργασίας.", e.getMessage(), Alert.AlertType.ERROR));
            return false;
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(descriptionField.getText());
        }
    }

    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Σημειώσεων");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(400, 300);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            descriptionField.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void showCustomer(ActionEvent evt) {
        Customer selectedCustomer = DBHelper.getCustomerDao().getSelectedCustomer(offer.getCustomerId());
        if (selectedCustomer.getCode() == 0) {
            return;
        }
        try {
            String res = DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerForEdit(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText(res);
                alert.showAndWait();
            }
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την εμφάνιση του πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public void selectFile(ActionEvent event) {
        CustomerFolderManager folderManager = new CustomerFolderManager();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Επιλογή αρχείων");
        fileChooser.setInitialDirectory(folderManager.createCustomerOfferFolder(selectedCustomer.getName(), selectedCustomer.getAfm()));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dialogStage);
        if (selectedFiles != null) {
            attachments.addAll(selectedFiles);
            attachmentList.getItems().addAll(selectedFiles);
        }
    }

    public void lock() {
        customerComboBox.setDisable(true);
        btnCustomer.setDisable(true);
    }

    public void lockFile() {
        btnSelectFile.setDisable(true);
    }


}
