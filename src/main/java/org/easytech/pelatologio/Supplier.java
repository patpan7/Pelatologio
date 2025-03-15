package org.easytech.pelatologio;

public class Supplier {
    private int id;
    private String name;
    private String phone;
    private String mobile;
    private String email;

    public Supplier(int id, String name, String phone, String mobile, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
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

    @Override
    public String toString() {
        return name;
    }
}
