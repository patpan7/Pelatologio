package org.easytech.pelatologio.models;

import java.time.LocalDate;

public class Order {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean isCompleted;
    private Integer customerId;
    private Integer supplierId;
    private Boolean isErgent;
    private Boolean isWait;
    private Boolean isReceived;
    private Boolean isDelivered;
    private String customerName;
    private String supplierName;

    public Order() {
    }

    public Order(Integer id, String title, String description, LocalDate dueDate, Boolean isCompleted, Integer customerId, Integer supplierId, Boolean isErgent, Boolean isWait, Boolean isReceived, Boolean isDelivered, String customerName, String supplierName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.customerId = customerId;
        this.supplierId = supplierId;
        this.isErgent = isErgent;
        this.isWait = isWait;
        this.isReceived = isReceived;
        this.isDelivered = isDelivered;
        this.customerName = customerName;
        this.supplierName = supplierName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Boolean getErgent() {
        return isErgent;
    }

    public void setErgent(Boolean ergent) {
        isErgent = ergent;
    }

    public Boolean getWait() {
        return isWait;
    }

    public void setWait(Boolean wait) {
        isWait = wait;
    }

    public Boolean getReceived() {
        return isReceived;
    }

    public void setReceived(Boolean received) {
        isReceived = received;
    }

    public Boolean getDelivered() {
        return isDelivered;
    }

    public void setDelivered(Boolean delivered) {
        isDelivered = delivered;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public String toString() {
        return this.title;
    }
}


