package org.easytech.pelatologio.models;

import java.time.LocalDateTime;

public class CallLog {
    private int id;
    private String callerNumber;
    private String callerName;
    private String callType; // e.g., "INCOMING", "OUTGOING"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
    private int customerId;
    private String notes;
    private String appUser;

    public CallLog() {
    }

    public CallLog(String callerNumber, String callerName, String callType, LocalDateTime startTime, int customerId) {
        this.callerNumber = callerNumber;
        this.callerName = callerName;
        this.callType = callType;
        this.startTime = startTime;
        this.customerId = customerId;
    }

    // Getters and Setters
    public String getAppUser() {
        return appUser;
    }

    public void setAppUser(String appUser) {
        this.appUser = appUser;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
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

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CallLog{" +
               "id=" + id +
               ", callerNumber='" + callerNumber + '\'' +
               ", callerName='" + callerName + '\'' +
               ", callType='" + callType + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", durationSeconds=" + durationSeconds +
               ", customerId=" + customerId +
               ", notes='" + notes + '\'' +
               '}';
    }

    public String getType() {
        return callType;
    }

    public void setType(String callType) {
        this.callType = callType;
    }
}