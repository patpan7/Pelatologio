package org.easytech.pelatologio.models;

import java.time.LocalDate;

public class Subscription {
    public int id;
    public String title;
    public LocalDate endDate;
    public Integer customerId;
    public String customerName;
    public Integer categoryId;
    public String category;
    public String price;
    public String note;
    public String sended;
    public LocalDate startDate;
    private boolean active;

    public Subscription(int id, String title, LocalDate startDate, LocalDate endDate, Integer customerId, Integer categoryId, String price, String note, String sended, boolean active) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerId = customerId;
        this.categoryId = categoryId;
        this.price = price;
        this.note = note;
        this.sended = sended;
        this.active = active;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
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

    public String getSended() {
        return sended;
    }

    public void setSended(String sended) {
        this.sended = sended;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
