package org.easytech.pelatologio.models;

import java.time.LocalDate;

public class ProjectStepProgress {
    private int id;
    private int projectId;
    private int stepId;
    private boolean completed;
    private LocalDate completionDate;
    private String notes;

    // Transient fields for display
    private String stepName;
    private String actionType;
    private String actionConfigJson; // New field

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getStepId() { return stepId; }
    public void setStepId(int stepId) { this.stepId = stepId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionConfigJson() { return actionConfigJson; }
    public void setActionConfigJson(String actionConfigJson) { this.actionConfigJson = actionConfigJson; }
}
