package org.easytech.pelatologio;

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
    String accName;
    String accPhone;
    String accMobile;
    String accEmail;

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

    public String getPostcode() {return postcode;}

    public void setPostcode(String postcode) {this.postcode = postcode;}

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

    public String getNotes() {return notes;}

    public void setNotes(String notes) {this.notes = notes;}

    public String getAccEmail() {
        return accEmail;
    }

    public void setAccEmail(String accEmail) {
        this.accEmail = accEmail;
    }

    public String getAccMobile() {
        return accMobile;
    }

    public void setAccMobile(String accMobile) {
        this.accMobile = accMobile;
    }

    public String getAccPhone() {
        return accPhone;
    }

    public void setAccPhone(String accPhone) {
        this.accPhone = accPhone;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
