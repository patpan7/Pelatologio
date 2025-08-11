package org.easytech.pelatologio.models;

public class CustomerMyPosDetails {
    private int id;
    private int customerId;
    private String myposClientId;
    private String verificationStatus;
    private String accountStatus;

    // Constructors
    public CustomerMyPosDetails() {}

    // Getters and Setters
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

    public String getMyposClientId() {
        return myposClientId;
    }

    public void setMyposClientId(String myposClientId) {
        this.myposClientId = myposClientId;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
