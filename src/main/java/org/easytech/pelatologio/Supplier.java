package org.easytech.pelatologio;

public class Supplier {
    private int id;
    private String name;
    private String title;
    private String phone;
    private String mobile;
    private String contact;
    private String email;
    private String site;

    public Supplier(int id, String name, String title, String phone, String mobile, String contact, String email, String site) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.phone = phone;
        this.mobile = mobile;
        this.contact = contact;
        this.email = email;
        this.site = site;
    }

    public Supplier() {

    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return name;
    }
}
