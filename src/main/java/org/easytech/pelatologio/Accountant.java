package org.easytech.pelatologio;

public class Accountant {
    private int id;
    private String name;
    private String phone;
    private String mobile;
    private String email;
    private String erganiEmail;

    public Accountant(int id, String name, String phone, String mobile, String email, String erganiEmail) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
        this.erganiEmail = erganiEmail;
    }

    public Accountant() {

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getErganiEmail() {
        return erganiEmail;
    }

    public void setErganiEmail(String erganiEmail) {
        this.erganiEmail = erganiEmail;
    }

    @Override
    public String toString() {
        return name;
    }
}
