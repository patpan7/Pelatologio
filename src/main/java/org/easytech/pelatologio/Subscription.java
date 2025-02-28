package org.easytech.pelatologio;

import java.time.LocalDate;

public class Subscription {
    private int id;
    private String title;
    private LocalDate endDate;
    private Integer customerId;
    private String customerName;
    private Integer categoryId;
    private String category;
    private String price;
    private String note;

    public Subscription(int id, String title, LocalDate endDate, Integer customerId, Integer categoryId, String price, String note) {
        this.id = id;
        this.title = title;
        this.endDate = endDate;
        this.customerId = customerId;
        this.categoryId = categoryId;
        this.price = price;
        this.note = note;
    }

    public Subscription() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
