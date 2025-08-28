package org.easytech.pelatologio.models;

public class ApplicationStep {
    private int id;
    private int applicationId;
    private String stepName;
    private int stepOrder;
    private String actionType;
    private String actionConfigJson; // New field for JSON configuration

    // Constructor

    public ApplicationStep() {
    }

    public ApplicationStep(int id, int applicationId, String stepName, int stepOrder, String actionType) {
        this.id = id;
        this.applicationId = applicationId;
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.actionType = actionType;
    }
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionConfigJson() { return actionConfigJson; }
    public void setActionConfigJson(String actionConfigJson) { this.actionConfigJson = actionConfigJson; }
}
