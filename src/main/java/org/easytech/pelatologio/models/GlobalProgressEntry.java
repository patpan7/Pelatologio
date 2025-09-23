package org.easytech.pelatologio.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;

public class GlobalProgressEntry {
    private int customerId;
    private String customerName;
    private String customerAfm;
    private int projectId;
    private String projectName;
    private int currentStepId;
    private String currentStepName;
    private final BooleanProperty completed; // Changed to BooleanProperty
    private LocalDate completionDate;
    private String notes;

    public GlobalProgressEntry() {
        this.completed = new SimpleBooleanProperty(false);
    }

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAfm() { return customerAfm; }
    public void setCustomerAfm(String customerAfm) { this.customerAfm = customerAfm; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public int getCurrentStepId() { return currentStepId; }
    public void setCurrentStepId(int currentStepId) { this.currentStepId = currentStepId; }

    public String getCurrentStepName() { return currentStepName; }
    public void setCurrentStepName(String currentStepName) { this.currentStepName = currentStepName; }

    // Property for completed
    public BooleanProperty completedProperty() {
        return completed;
    }
    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean completed) { this.completed.set(completed); }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}