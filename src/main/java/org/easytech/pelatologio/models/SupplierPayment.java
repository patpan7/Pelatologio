package org.easytech.pelatologio.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplierPayment {
    private int id;
    private int supplierId;
    private int customerId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String description;
    private boolean calculated;

    // For display purposes in UI
    private String customerName;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCalculated() { return calculated; }
    public void setCalculated(boolean calculated) { this.calculated = calculated; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
