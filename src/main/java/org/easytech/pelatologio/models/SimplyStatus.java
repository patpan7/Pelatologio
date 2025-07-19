package org.easytech.pelatologio.models;

public class SimplyStatus {
    int id;
    int app_login_id;
    boolean stock;
    boolean register;
    boolean auth;
    boolean accept;
    boolean mail;
    boolean param;
    boolean mydata;
    boolean delivered;
    boolean paid;
    String years;
    int custId;
    String customer;
    String custMail;


    public SimplyStatus(int id, int app_login_id, boolean stock, boolean register, boolean auth, boolean accept, boolean mail, boolean param, boolean mydata, boolean delivered, boolean paid, String years, int custId, String customer, String custMail) {
        this.id = id;
        this.app_login_id = app_login_id;
        this.stock = stock;
        this.register = register;
        this.auth = auth;
        this.accept = accept;
        this.mail = mail;
        this.param = param;
        this.mydata = mydata;
        this.delivered = delivered;
        this.paid = paid;
        this.years = years;
        this.custId = custId;
        this.customer = customer;
        this.custMail = custMail;
    }

    public SimplyStatus() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApp_login_id() {
        return app_login_id;
    }

    public void setApp_login_id(int app_login_id) {
        this.app_login_id = app_login_id;
    }

    public boolean isStock() {
        return stock;
    }

    public void setStock(boolean stock) {
        this.stock = stock;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isMail() {
        return mail;
    }

    public void setMail(boolean mail) {
        this.mail = mail;
    }

    public boolean isParam() {
        return param;
    }

    public void setParam(boolean param) {
        this.param = param;
    }

    public boolean isMydata() {
        return mydata;
    }

    public void setMydata(boolean mydata) {
        this.mydata = mydata;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }

    public int getCustId() {
        return custId;
    }

    public void setCustId(int custId) {
        this.custId = custId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustMail() {
        return custMail;
    }

    public void setCustMail(String custMail) {
        this.custMail = custMail;
    }
}
