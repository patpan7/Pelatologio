package org.easytech.pelatologio;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.easytech.pelatologio.dao.SupplierPaymentDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Supplier;
import org.easytech.pelatologio.models.SupplierPayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modified to show a table of customers with editable per-customer allocated amounts
 * Minimal changes: UI now shows allocations table; on save we create/update per-customer payments
 */
public class EditSupplierPaymentDialogController {

    // Previously: ComboBox<Customer> customerComboBox;
    @FXML
    private TableView<CustomerAllocation> allocationsTable;
    @FXML
    private TableColumn<CustomerAllocation, String> customerNameColumn;
    @FXML
    private TableColumn<CustomerAllocation, String> allocatedColumn;

    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private TextField amountField; // total/invoice amount (kept for compatibility)
    @FXML
    private TextArea descriptionArea;

    private Supplier supplier;
    private SupplierPayment payment; // if editing single existing payment
    private SupplierPaymentDao paymentDao;

    private final ObservableList<CustomerAllocation> allocationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.paymentDao = DBHelper.getSupplierPaymentDao();

        // Amount field numeric guard (kept)
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                amountField.setText(oldValue);
            }
        });

        // Setup allocations table
        customerNameColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        allocatedColumn.setCellValueFactory(cellData -> cellData.getValue().allocatedProperty());

        // Make allocated column editable as text field
        allocationsTable.setEditable(true);
        allocatedColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        // Optionally validate input when edited: store as string and parse on save
        allocatedColumn.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            // Allow empty or numeric like "123.45" or "123,45"
            if (newValue == null || newValue.trim().isEmpty() || newValue.matches("\\d*([\\.,]\\d*)?")) {
                event.getRowValue().setAllocated(newValue == null ? "" : newValue.trim());
            } else {
                // revert to old value if invalid
                event.getRowValue().setAllocated(event.getOldValue());
                // show a small warning
                AlertDialogHelper.showDialog("Validation Error", "Invalid Amount", "Please enter a valid numeric amount (or leave empty).", Alert.AlertType.WARNING);
            }
            allocationsTable.refresh();
        });

        allocationsTable.setItems(allocationList);
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        loadCustomersForSupplier(supplier.getId());
    }

    /**
     * Loads customers who have commissions with this supplier and shows them in the table.
     * Allocated amounts are left empty by default.
     */
    private void loadCustomersForSupplier(int supplierId) {
        allocationList.clear();
        List<Customer> customers = DBHelper.getCustomerDao().getCustomersWithCommissionForSupplier(supplierId);
        List<CustomerAllocation> rows = customers.stream()
                .map(c -> new CustomerAllocation(c, ""))
                .collect(Collectors.toList());
        allocationList.addAll(rows);
    }

    /**
     * If editing an existing single SupplierPayment: prefill date/description/amount and the corresponding customer's allocation.
     */
    public void setPayment(SupplierPayment payment) {
        this.payment = payment;
        if (payment != null) {
            // set simple fields
            paymentDatePicker.setValue(payment.getPaymentDate());
            amountField.setText(payment.getAmount() != null ? payment.getAmount().toPlainString() : "");
            descriptionArea.setText(payment.getDescription());

            // ensure customers are loaded first (if setSupplier was called before); if not, attempt to load lazily
            if (supplier != null && allocationList.isEmpty()) {
                loadCustomersForSupplier(supplier.getId());
            }

            // find the matching allocation row and prefill allocated value
            allocationList.stream()
                    .filter(a -> a.getCustomer().getCode() == payment.getCustomerId())
                    .findFirst()
                    .ifPresent(a -> a.setAllocated(payment.getAmount() != null ? payment.getAmount().toPlainString() : ""));
            allocationsTable.refresh();
        }
    }

    /**
     * Save logic:
     * - Validate date present
     * - For each allocation row that has a valid numeric value > 0, create a SupplierPayment and addPayment
     * - If editing an existing payment: update that payment record for its customer; for other allocations create new payments
     */
    public boolean handleOk() {
        if (paymentDatePicker.getValue() == null) {
            AlertDialogHelper.showDialog("Validation Error", "Missing Information", "Please select a Payment Date.", Alert.AlertType.ERROR);
            return false;
        }

        // collect allocations that parse to >0
        List<CustomerAllocation> positiveAllocations = allocationList.stream()
                .filter(a -> {
                    String s = a.getAllocated();
                    if (s == null || s.trim().isEmpty()) return false;
                    String norm = s.replace(",", ".").trim();
                    try {
                        BigDecimal val = new BigDecimal(norm);
                        return val.compareTo(BigDecimal.ZERO) > 0;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        if (positiveAllocations.isEmpty()) {
            AlertDialogHelper.showDialog("Validation Error", "No allocations", "Please enter at least one per-customer amount (or set the one customer).", Alert.AlertType.ERROR);
            return false;
        }

        // If editing an existing payment: if there is allocation for its customer, update that payment; otherwise keep old behavior (update with 0? better to require)
        try {
            if (payment != null && payment.getId() != 0) {
                // find allocation for original payment customer
                CustomerAllocation originalAllocation = allocationList.stream()
                        .filter(a -> a.getCustomer().getCode() == payment.getCustomerId())
                        .findFirst().orElse(null);

                if (originalAllocation != null) {
                    String s = originalAllocation.getAllocated();
                    BigDecimal amt = s == null || s.trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(s.replace(",", ".").trim());
                    // update the existing payment record
                    payment.setPaymentDate(paymentDatePicker.getValue());
                    payment.setAmount(amt);
                    payment.setDescription(descriptionArea.getText());
                    paymentDao.updatePayment(payment);
                }

                // For other allocations (excluding the original's customer) create new payments
                List<CustomerAllocation> others = positiveAllocations.stream()
                        .filter(a -> a.getCustomer().getCode() != payment.getCustomerId())
                        .collect(Collectors.toList());

                for (CustomerAllocation a : others) {
                    BigDecimal amt = new BigDecimal(a.getAllocated().replace(",", ".").trim());
                    SupplierPayment p = new SupplierPayment();
                    p.setSupplierId(supplier.getId());
                    p.setCustomerId(a.getCustomer().getCode());
                    p.setPaymentDate(paymentDatePicker.getValue());
                    p.setAmount(amt);
                    p.setDescription(descriptionArea.getText());
                    paymentDao.addPayment(p);
                }
            } else {
                // New payments: create one SupplierPayment per allocation >0
                for (CustomerAllocation a : positiveAllocations) {
                    BigDecimal amt = new BigDecimal(a.getAllocated().replace(",", ".").trim());
                    SupplierPayment p = new SupplierPayment();
                    p.setSupplierId(supplier.getId());
                    p.setCustomerId(a.getCustomer().getCode());
                    p.setPaymentDate(paymentDatePicker.getValue());
                    p.setAmount(amt);
                    p.setDescription(descriptionArea.getText());
                    paymentDao.addPayment(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Database Error", "Save failed", e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    // Helper inner class that represents a row in the allocations table
    public static class CustomerAllocation {
        private final Customer customer;
        private final StringProperty allocated = new SimpleStringProperty("");

        public CustomerAllocation(Customer customer, String allocated) {
            this.customer = customer;
            this.allocated.set(allocated == null ? "" : allocated);
        }

        public Customer getCustomer() {
            return customer;
        }

        public String getAllocated() {
            return allocated.get();
        }

        public void setAllocated(String value) {
            allocated.set(value == null ? "" : value);
        }

        public StringProperty allocatedProperty() {
            return allocated;
        }

        public StringProperty customerNameProperty() {
            return new SimpleStringProperty(customer == null ? "" : customer.getName());
        }
    }
}