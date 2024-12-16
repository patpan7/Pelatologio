package org.easytech.pelatologio;

import java.time.LocalDateTime;

public class Appointment {
    private int id;                 // Μοναδικό ID ραντεβού
    private int customerId;         // Αναφορά στον πελάτη
    private String title;           // Τίτλος ραντεβού
    private String description;     // Περιγραφή
    private LocalDateTime startTime; // Ώρα έναρξης
    private LocalDateTime endTime;   // Ώρα λήξης

    // Constructor
    public Appointment(int id, int customerId, String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.customerId = customerId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters και Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
