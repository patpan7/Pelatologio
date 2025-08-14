package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.easytech.pelatologio.dao.CommissionDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Commission;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Partner;
import javafx.util.StringConverter;
import org.easytech.pelatologio.models.Supplier;

import java.sql.SQLException;

public class EditCommissionDialogController {

    @FXML private ComboBox<Partner> partnerComboBox;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private TextField rateField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private Commission commission;
    private CommissionDao commissionDao;

    @FXML
    public void initialize() throws SQLException {
        this.commissionDao = DBHelper.getCommissionDao();
        loadComboBoxData();
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
        customerComboBox.getItems().addAll(DBHelper.getCustomerDao().getCustomers());
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getName();
            }
            @Override
            public Customer fromString(String string) { return null; }
        });

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
}
