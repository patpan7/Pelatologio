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
    String email;

    public Customer() {

    }
    public Customer(String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String email) {
        this.name = name;
        this.title = title;
        this.job = job;
        this.afm = afm;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.mobile = mobile;
        this.address = address;
        this.town = town;
        this.email = email;
    }

    public Customer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String email) {
        this.code = code;
        this.name = name;
        this.title = title;
        this.job = job;
        this.afm = afm;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.mobile = mobile;
        this.address = address;
        this.town = town;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
