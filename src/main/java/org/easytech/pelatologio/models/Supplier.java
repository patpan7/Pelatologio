package org.easytech.pelatologio.models;

public class Supplier {
    private int id;
    private String name;
    private String title;
    private String afm;
    private String phone;
    private String mobile;
    private String contact;
    private String email;
    private String email2;
    private String site;
    private String notes;
    private boolean hasCommissions;

    public Supplier(int id, String name, String title, String afm, String phone, String mobile, String contact, String email,String email2, String site, String notes, boolean hasCommissions) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.afm = afm;
        this.phone = phone;
        this.mobile = mobile;
        this.contact = contact;
        this.email = email;
        this.email2 = email2;
        this.site = site;
        this.notes = notes;
        this.hasCommissions = hasCommissions;
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

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
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

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean hasCommissions() {
        return hasCommissions;
    }

    public void setHasCommissions(boolean hasCommissions) {
        this.hasCommissions = hasCommissions;
    }

    @Override
    public String toString() {
        return name;
    }
}
