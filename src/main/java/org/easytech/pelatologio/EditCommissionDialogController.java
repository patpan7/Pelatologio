package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.easytech.pelatologio.dao.CommissionDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Commission;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Partner;
import javafx.util.StringConverter;
import org.easytech.pelatologio.models.Supplier;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EditCommissionDialogController {

    @FXML private ComboBox<Partner> partnerComboBox;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private TextField rateField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private Commission commission;
    private CommissionDao commissionDao;

    private FilteredList<Customer> filteredCustomers;

    @FXML
    public void initialize() throws SQLException {
        this.commissionDao = DBHelper.getCommissionDao();
        loadComboBoxData();
        customerComboBox.setPrefWidth(400.0);
    }

    private void loadComboBoxData() throws SQLException {
        // Load Partners
        partnerComboBox.getItems().addAll(DBHelper.getPartnerDao().findAll());
        partnerComboBox.setConverter(new StringConverter<Partner>() {
            @Override
            public String toString(Partner partner) {
                return partner == null ? "" : partner.getName();
            }
            @Override
            public Partner fromString(String string) { return null; } // Not needed for selection
        });

        // Load Customers
        List<Customer> customers = DBHelper.getCustomerDao().getCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers));
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        customerComboBox.setPromptText("Επιλέξτε Πελάτη");


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

        // Load Suppliers that participate in commissions
        supplierComboBox.getItems().addAll(DBHelper.getSupplierDao().getCommissionSuppliers());
        supplierComboBox.setConverter(new StringConverter<Supplier>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier == null ? "" : supplier.getName();
            }
            @Override
            public Supplier fromString(String string) { return null; }
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

    public void setCommission(Commission commission) {
        this.commission = commission;
        if (commission != null) {
            // Populate fields for editing
            rateField.setText(String.valueOf(commission.getRate()));
            startDatePicker.setValue(commission.getStartDate());
            endDatePicker.setValue(commission.getEndDate());

            // Find and select the correct items in the ComboBoxes based on their IDs
            partnerComboBox.getItems().stream()
                .filter(p -> p.getId() == commission.getPartnerId())
                .findFirst()
                .ifPresent(partnerComboBox::setValue);

            customerComboBox.getItems().stream()
                .filter(c -> c.getCode() == commission.getCustomerId())
                .findFirst()
                .ifPresent(customerComboBox::setValue);

            supplierComboBox.getItems().stream()
                .filter(s -> s.getId() == commission.getSupplierId())
                .findFirst()
                .ifPresent(supplierComboBox::setValue);
        }
    }

    public boolean handleOk() {
        // Basic Validation
        if (partnerComboBox.getValue() == null || customerComboBox.getValue() == null || supplierComboBox.getValue() == null || rateField.getText().isEmpty() || startDatePicker.getValue() == null) {
            // Show an alert to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please fill all required fields: Partner, Customer, Supplier, Rate, and Start Date.");
            alert.showAndWait();
            return false; // Indicate failure
        }

        if (commission == null) {
            commission = new Commission();
        }
        
        // Set commission properties from the UI fields
        commission.setPartnerId(partnerComboBox.getValue().getId());
        commission.setCustomerId(customerComboBox.getValue().getCode());
        commission.setSupplierId(supplierComboBox.getValue().getId());
        commission.setRate(Double.parseDouble(rateField.getText()));
        commission.setStartDate(startDatePicker.getValue());
        commission.setEndDate(endDatePicker.getValue());

        if (commission.getId() == 0) {
            commissionDao.addCommission(commission);
        } else {
            commissionDao.updateCommission(commission);
        }
        return true; // Indicate success
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
