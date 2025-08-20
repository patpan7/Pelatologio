package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.easytech.pelatologio.dao.SupplierPaymentDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Supplier;
import org.easytech.pelatologio.models.SupplierPayment;

import java.math.BigDecimal;
import java.util.List;

public class EditSupplierPaymentDialogController {

    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea descriptionArea;

    private Supplier supplier;
    private SupplierPayment payment;
    private SupplierPaymentDao paymentDao;

    @FXML
    public void initialize() {
        this.paymentDao = DBHelper.getSupplierPaymentDao();
        // Customers will be loaded dynamically based on the supplier

        // Add listener to amountField to ensure it's numeric
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                amountField.setText(oldValue);
            }
        });
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        loadCustomersForSupplier(supplier.getId());
    }

    private void loadCustomersForSupplier(int supplierId) {
        customerComboBox.getItems().clear();
        List<Customer> customers = DBHelper.getCustomerDao().getCustomersWithCommissionForSupplier(supplierId);
        customerComboBox.getItems().addAll(customers);
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getName();
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
    }

    public void setPayment(SupplierPayment payment) {
        this.payment = payment;
        if (payment != null) {
            paymentDatePicker.setValue(payment.getPaymentDate());
            amountField.setText(payment.getAmount().toPlainString());
            descriptionArea.setText(payment.getDescription());
            customerComboBox.getItems().stream()
                    .filter(c -> c.getCode() == payment.getCustomerId())
                    .findFirst()
                    .ifPresent(customerComboBox::setValue);
        }
    }

    public boolean handleOk() {
        // Validation
        if (customerComboBox.getValue() == null) {
            AlertDialogHelper.showDialog("Validation Error", "Missing Information", "Please select a Customer.", Alert.AlertType.ERROR);
            return false;
        }
        if (paymentDatePicker.getValue() == null) {
            AlertDialogHelper.showDialog("Validation Error", "Missing Information", "Please select a Payment Date.", Alert.AlertType.ERROR);
            return false;
        }
        if (amountField.getText().isEmpty() || !amountField.getText().matches("\\d*([\\.,]\\d*)?")) {
            AlertDialogHelper.showDialog("Validation Error", "Invalid Amount", "Please enter a valid numeric amount.", Alert.AlertType.ERROR);
            return false;
        }

        if (payment == null) {
            payment = new SupplierPayment();
            payment.setSupplierId(supplier.getId());
        }

        payment.setCustomerId(customerComboBox.getValue().getCode());
        payment.setPaymentDate(paymentDatePicker.getValue());
        payment.setAmount(new BigDecimal(amountField.getText().replace(",", ".")));
        payment.setDescription(descriptionArea.getText());

        if (payment.getId() == 0) {
            paymentDao.addPayment(payment);
        } else {
            paymentDao.updatePayment(payment);
        }
        return true;
    }
}