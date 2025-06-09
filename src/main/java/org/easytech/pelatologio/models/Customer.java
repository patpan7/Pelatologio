package org.easytech.pelatologio.models;

public class Customer {

    int code;
    String name;
    String title;
    String job;
    String afm;
    String phone1;
    String phone2;
    String mobile;
    String address;
    String town;
    String postcode;
    String email;
    String email2;
    String manager;
    String managerPhone;
    String notes;
    int accId;
    String accName1;
    String accEmail1;
    String recommendation;
    String balance;
    String balanceReason;
    Boolean isActive;

    public Customer() {

    }

    public Customer(String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String email2) {
        this.name = name;
        this.title = title;
        this.job = job;
        this.afm = afm;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.mobile = mobile;
        this.address = address;
        this.town = town;
        this.postcode = postcode;
        this.email = email;
        this.email2 = email2;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email) {
        this.email2 = email;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getAccId() {
        return accId;
    }

    public void setAccId(int accId) {
        this.accId = accId;
    }

    public String getAccName1() {
        return accName1;
    }

    public void setAccName1(String accName1) {
        this.accName1 = accName1;
    }

    public String getAccEmail1() {
        return accEmail1;
    }

    public void setAccEmail1(String accEmail1) {
        this.accEmail1 = accEmail1;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBalanceReason() {
        return balanceReason;
    }

    public void setBalanceReason(String balanceReason) {
        this.balanceReason = balanceReason;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
