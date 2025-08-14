package org.easytech.pelatologio.models;

public class Partner {
    int id;
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

    public Partner() {
        // Default constructor
    }

    public Partner(int id, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String email2, String manager, String managerPhone, String notes) {
        this.id = id;
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
        this.manager = manager;
        this.managerPhone = managerPhone;
        this.notes = notes;
    }

    public Partner(int id, String name) {
        this.id = id;
        this.name = name;
    }
    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
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

    public void setEmail2(String email2) {
        this.email2 = email2;
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
}