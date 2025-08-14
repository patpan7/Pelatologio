package org.easytech.pelatologio.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PartnerEarning {
    private int id;
    private int partnerId;
    private int supplierPaymentId;
    private int customerId;
    private int commissionId;
    private LocalDate earningDate;
    private BigDecimal earningAmount;
    private String partnerInvoiceStatus;
    private String partnerInvoiceRef;
    private String paymentToPartnerStatus;
    private LocalDate paymentToPartnerDate;

    // Transient fields for display purposes
    private String partnerName;
    private String customerName;
    private String supplierName;
    private double commissionRate;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }

    public int getSupplierPaymentId() { return supplierPaymentId; }
    public void setSupplierPaymentId(int supplierPaymentId) { this.supplierPaymentId = supplierPaymentId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getCommissionId() { return commissionId; }
    public void setCommissionId(int commissionId) { this.commissionId = commissionId; }

    public LocalDate getEarningDate() { return earningDate; }
    public void setEarningDate(LocalDate earningDate) { this.earningDate = earningDate; }

    public BigDecimal getEarningAmount() { return earningAmount; }
    public void setEarningAmount(BigDecimal earningAmount) { this.earningAmount = earningAmount; }

    public String getPartnerInvoiceStatus() { return partnerInvoiceStatus; }
    public void setPartnerInvoiceStatus(String partnerInvoiceStatus) { this.partnerInvoiceStatus = partnerInvoiceStatus; }

    public String getPartnerInvoiceRef() { return partnerInvoiceRef; }
    public void setPartnerInvoiceRef(String partnerInvoiceRef) { this.partnerInvoiceRef = partnerInvoiceRef; }

    public String getPaymentToPartnerStatus() { return paymentToPartnerStatus; }
    public void setPaymentToPartnerStatus(String paymentToPartnerStatus) { this.paymentToPartnerStatus = paymentToPartnerStatus; }

    public LocalDate getPaymentToPartnerDate() { return paymentToPartnerDate; }
    public void setPaymentToPartnerDate(LocalDate paymentToPartnerDate) { this.paymentToPartnerDate = paymentToPartnerDate; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
}
