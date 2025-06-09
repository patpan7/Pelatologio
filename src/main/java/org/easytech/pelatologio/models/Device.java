package org.easytech.pelatologio.models;

public class Device {
    int id;
    String serial;
    String description;
    String rate;
    int itemId;
    Integer customerId;
    String itemName;
    String customerName;

    public Device(int id, String serial, String description, String rate, int itemId, int customerId) {
        this.id = id;
        this.serial = serial;
        this.description = description;
        this.rate = rate;
        this.itemId = itemId;
        this.customerId = customerId;
    }

    public Device() {
    }

    public Device(int id, String serial, String description, String rate, int itemId, int customerId, String itemName) {
        this.id = id;
        this.serial = serial;
        this.description = description;
        this.rate = rate;
        this.itemId = itemId;
        this.customerId = customerId;
        this.itemName = itemName;
    }

    public Device(int id, String serial, String description, String rate, int itemId, int customerId, String itemName, String customerName) {
        this.id = id;
        this.serial = serial;
        this.description = description;
        this.rate = rate;
        this.itemId = itemId;
        this.customerId = customerId;
        this.itemName = itemName;
        this.customerName = customerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
