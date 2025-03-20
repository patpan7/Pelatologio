package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private JFXButton btnCustomer, btnSelectFile;
    @FXML
    private CheckBox isArchived;

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
        DBHelper dbHelper = new DBHelper();
        List<Customer> customers = dbHelper.getCustomers();
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

        setupComboBoxFilterCust(customerComboBox, filteredCustomers);
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

    private <T> void setupComboBoxFilterCust(ComboBox<Customer> comboBox, FilteredList<Customer> filteredList) {
        // Ακροατής για το TextField του ComboBox
        comboBox.getEditor().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            comboBox.show();
            String filterText = comboBox.getEditor().getText().toUpperCase();
            filteredList.setPredicate(item -> {
                if (filterText.isEmpty()) {
                    return true; // Εμφάνιση όλων των στοιχείων αν δεν υπάρχει φίλτρο
                }
//                // Ελέγχουμε αν το όνομα του αντικειμένου ταιριάζει με το φίλτρο
//                return item.toString().toLowerCase().contains(filterText);
                // Υποστήριξη Ελληνικών/Αγγλικών
                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);

                // Αν δεν είναι επιλεγμένο κανένα φίλτρο, κάνε αναζήτηση σε όλα τα πεδία
                return (item.getName() != null && (item.getName().toUpperCase().contains(search1) || item.getName().toUpperCase().contains(search2)))
                        || (item.getTitle() != null && (item.getTitle().toUpperCase().contains(search1) || item.getTitle().toUpperCase().contains(search2)))
                        || (item.getJob() != null && (item.getJob().toUpperCase().contains(search1) || item.getJob().toUpperCase().contains(search2)))
                        || (String.valueOf(item.getCode()).contains(search1) || String.valueOf(item.getCode()).contains(search2))
                        || (item.getPhone1() != null && (item.getPhone1().contains(search1) || item.getPhone1().contains(search2)))
                        || (item.getPhone2() != null && (item.getPhone2().contains(search1) || item.getPhone2().contains(search2)))
                        || (item.getMobile() != null && (item.getMobile().contains(search1) || item.getMobile().contains(search2)))
                        || (item.getAfm() != null && (item.getAfm().contains(search1) || item.getAfm().contains(search2)))
                        || (item.getManager() != null && (item.getManager().toUpperCase().contains(search1) || item.getManager().toUpperCase().contains(search2)))
                        || (item.getTown() != null && (item.getTown().toUpperCase().contains(search1) || item.getTown().toUpperCase().contains(search2)));

            });
        });

        // Ακροατής για την επιλογή ενός στοιχείου
        comboBox.setOnHidden(event -> {
            Customer selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });

        // Ακροατής για την αλλαγή της επιλογής
        comboBox.setOnAction(event -> {
            Customer selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                comboBox.getEditor().setText(selectedItem.toString());
            }
        });
    }

    public boolean handleSaveOffer() {
        try {
            if (dueDatePicker.getValue() == null || descriptionField.getText() == null) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Προσοχή")
                            .text("Συμπληρώστε όλα τα απαραίτητα πεδία.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showError();
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
            DBHelper dbHelper = new DBHelper();

            if (offer == null) {
                //Δημιουργία νέας εργασίας
                Offer newOffer = new Offer(0, offerDate, description, hours, status, selectedCustomer.getCode(), null, null, paths, "Όχι",false);
                dbHelper.saveOffer(newOffer);
            } else {
                // Ενημέρωση υπάρχουσας εργασίας
                offer.setOfferDate(offerDate);
                offer.setDescription(description);
                offer.setHours(hours);
                offer.setStatus(status);
                offer.setCustomerId(selectedCustomer.getCode());
                offer.setPaths(paths);
                offer.setArchived(isArchived.isSelected());
                dbHelper.updateOffer(offer);
            }

            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Επιτυχία")
                        .text("Η εργασία αποθηκεύτηκε!")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();
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
        DBHelper dbHelper = new DBHelper();

        Customer selectedCustomer = dbHelper.getSelectedCustomer(offer.getCustomerId());
        if (selectedCustomer.getCode() == 0) {
            return;
        }
        try {
            String res = dbHelper.checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
            if (res.equals("unlocked")) {
                dbHelper.customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Λεπτομέρειες Πελάτη");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Κλειδώνει το parent window αν το θες σαν dialog

                AddCustomerController controller = loader.getController();

                // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                controller.setCustomerData(selectedCustomer);

                stage.show();
                stage.setOnCloseRequest(event -> {
                    System.out.println("Το παράθυρο κλείνει!");
                    dbHelper.customerUnlock(selectedCustomer.getCode());
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

    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('\u0041', '\u0391');  // uppercase A
        ENGLISH_TO_GREEK.put('\u0042', '\u0392');  // uppercase B
        ENGLISH_TO_GREEK.put('\u0043', '\u03A8');  // uppercase C
        ENGLISH_TO_GREEK.put('\u0044', '\u0394');  // uppercase D
        ENGLISH_TO_GREEK.put('\u0045', '\u0395');  // uppercase E
        ENGLISH_TO_GREEK.put('\u0046', '\u03A6');  // uppercase F
        ENGLISH_TO_GREEK.put('\u0047', '\u0393');  // uppercase G
        ENGLISH_TO_GREEK.put('\u0048', '\u0397');  // uppercase H
        ENGLISH_TO_GREEK.put('\u0049', '\u0399');  // uppercase I
        ENGLISH_TO_GREEK.put('\u004A', '\u039E');  // uppercase J
        ENGLISH_TO_GREEK.put('\u004B', '\u039A');  // uppercase K
        ENGLISH_TO_GREEK.put('\u004C', '\u039B');  // uppercase L
        ENGLISH_TO_GREEK.put('\u004D', '\u039C');  // uppercase M
        ENGLISH_TO_GREEK.put('\u004E', '\u039D');  // uppercase N
        ENGLISH_TO_GREEK.put('\u004F', '\u039F');  // uppercase O
        ENGLISH_TO_GREEK.put('\u0050', '\u03A0');  // uppercase P
        //ENGLISH_TO_GREEK.put('\u0051', '\u0391');  // uppercase Q
        ENGLISH_TO_GREEK.put('\u0052', '\u03A1');  // uppercase R
        ENGLISH_TO_GREEK.put('\u0053', '\u03A3');  // uppercase S
        ENGLISH_TO_GREEK.put('\u0054', '\u03A4');  // uppercase T
        ENGLISH_TO_GREEK.put('\u0055', '\u0398');  // uppercase U
        ENGLISH_TO_GREEK.put('\u0056', '\u03A9');  // uppercase V
        ENGLISH_TO_GREEK.put('\u0057', '\u03A3');  // uppercase W
        ENGLISH_TO_GREEK.put('\u0058', '\u03A7');  // uppercase X
        ENGLISH_TO_GREEK.put('\u0059', '\u03A5');  // uppercase Y
        ENGLISH_TO_GREEK.put('\u005A', '\u0396');  // uppercase Z
    }

    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        GREEK_TO_ENGLISH.put('\u0391', '\u0041');  // uppercase Α
        GREEK_TO_ENGLISH.put('\u0392', '\u0042');  // uppercase Β
        GREEK_TO_ENGLISH.put('\u03A8', '\u0043');  // uppercase Ψ
        GREEK_TO_ENGLISH.put('\u0394', '\u0044');  // uppercase Δ
        GREEK_TO_ENGLISH.put('\u0395', '\u0045');  // uppercase Ε
        GREEK_TO_ENGLISH.put('\u03A6', '\u0046');  // uppercase Φ
        GREEK_TO_ENGLISH.put('\u0393', '\u0047');  // uppercase Γ
        GREEK_TO_ENGLISH.put('\u0397', '\u0048');  // uppercase Η
        GREEK_TO_ENGLISH.put('\u0399', '\u0049');  // uppercase Ι
        GREEK_TO_ENGLISH.put('\u039E', '\u004A');  // uppercase Ξ
        GREEK_TO_ENGLISH.put('\u039A', '\u004B');  // uppercase Κ
        GREEK_TO_ENGLISH.put('\u039B', '\u004C');  // uppercase Λ
        GREEK_TO_ENGLISH.put('\u039C', '\u004D');  // uppercase Μ
        GREEK_TO_ENGLISH.put('\u039D', '\u004E');  // uppercase Ν
        GREEK_TO_ENGLISH.put('\u039F', '\u004F');  // uppercase Ο
        GREEK_TO_ENGLISH.put('\u03A0', '\u0050');  // uppercase Π
        //GREEK_TO_ENGLISH.put('\u0051', '\u0391');  // uppercase Q
        GREEK_TO_ENGLISH.put('\u03A1', '\u0052');  // uppercase Ρ
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase Σ
        GREEK_TO_ENGLISH.put('\u03A4', '\u0054');  // uppercase Τ
        GREEK_TO_ENGLISH.put('\u0398', '\u0055');  // uppercase Θ
        GREEK_TO_ENGLISH.put('\u03A9', '\u0056');  // uppercase Ω
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase ς
        GREEK_TO_ENGLISH.put('\u03A7', '\u0058');  // uppercase Χ
        GREEK_TO_ENGLISH.put('\u03A5', '\u0059');  // uppercase Υ
        GREEK_TO_ENGLISH.put('\u0396', '\u005A');  // uppercase Ζ
    }
}
