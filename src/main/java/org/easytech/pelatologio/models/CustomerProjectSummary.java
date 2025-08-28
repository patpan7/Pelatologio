package org.easytech.pelatologio.models;

import java.time.LocalDate;

public class CustomerProjectSummary {
    private int customerId;
    private String customerName;
    private String customerAfm;
    private int projectId;
    private String projectName;
    private int totalSteps;
    private int completedSteps;
    private String nextPendingStepName;
    private LocalDate lastCompletionDate;
    private String lastCompletedStepName;

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

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public int getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }

    public String getNextPendingStepName() { return nextPendingStepName; }
    public void setNextPendingStepName(String nextPendingStepName) { this.nextPendingStepName = nextPendingStepName; }

    public LocalDate getLastCompletionDate() { return lastCompletionDate; }
    public void setLastCompletionDate(LocalDate lastCompletionDate) { this.lastCompletionDate = lastCompletionDate; }

    public String getLastCompletedStepName() { return lastCompletedStepName; }
    public void setLastCompletedStepName(String lastCompletedStepName) { this.lastCompletedStepName = lastCompletedStepName; }
}
