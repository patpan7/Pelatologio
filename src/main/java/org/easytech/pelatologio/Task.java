package org.easytech.pelatologio;

import java.time.LocalDate;

public class Task {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean isCompleted;
    private Integer customerId;
    private String category;
    private String customerName;

    public Task(Integer id, String title, String description, LocalDate dueDate, Boolean isCompleted,String category, Integer customerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.category = category;
        this.customerId = customerId;
    }

    public Task(int id, String title, String description, LocalDate dueDate, Boolean isCompleted, String category, Integer customerId, String customerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.category = category;
        this.customerId = customerId;
        this.customerName = customerName;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}


