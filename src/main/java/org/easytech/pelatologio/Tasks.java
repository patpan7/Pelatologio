package org.easytech.pelatologio;



import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tasks {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime startTime; // Ώρα έναρξης
    private LocalDateTime endTime;   // Ώρα λήξης
    private Boolean isCompleted;
    private Integer customerId;
    private String category;
    private String customerName;
    private Boolean isErgent;
    private Boolean isWait;
    private Boolean isCalendar;

    public Tasks () {
    }

    public Tasks(Integer id, String title, String description, LocalDate dueDate, Boolean isCompleted, String category, Integer customerId, Boolean isErgent, Boolean isWait, Boolean isCalendar, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.category = category;
        this.customerId = customerId;
        this.isErgent = isErgent;
        this.isWait = isWait;
        this.isCalendar = isCalendar;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Tasks(int id, String title, String description, LocalDate dueDate, Boolean isCompleted, String category, Integer customerId, String customerName, Boolean isErgent, Boolean isWait, Boolean isCalendar, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.category = category;
        this.customerId = customerId;
        this.customerName = customerName;
        this.isErgent = isErgent;
        this.isWait = isWait;
        this.isCalendar = isCalendar;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public Boolean getIsCalendar() {
        return isCalendar;
    }

    public void setCalendar(Boolean calendar) {
        isCalendar = calendar;
    }
}


