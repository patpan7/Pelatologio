package org.easytech.pelatologio.models;

import java.time.LocalDateTime;

public class Appointment {
    private int id;                 // Μοναδικό ID ραντεβού
    private int customerId;         // Αναφορά στον πελάτη
    private String title;           // Τίτλος ραντεβού
    private String description;     // Περιγραφή
    private LocalDateTime startTime; // Ώρα έναρξης
    private LocalDateTime endTime;   // Ώρα λήξης
    private int calendarId; // ID του ημερολογίου
    private boolean completed;


    // Constructor
    public Appointment(int id, int customerId, String title, String description, int calendarId, LocalDateTime startTime, LocalDateTime endTime, Boolean completed) {
        this.id = id;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.calendarId = calendarId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.completed = completed;
    }

    public Appointment() {
    }

    // Getters και Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
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

    public int getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
