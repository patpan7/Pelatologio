package org.easytech.pelatologio.models;

public class Invoice {

    String date;
    String type;
    String number;
    String amount;
    String pliromi;
    String par;

    public Invoice(String date, String type, String number, String amount, String pliromi, String par) {
        this.date = date;
        this.type = type;
        this.number = number;
        this.amount = amount;
        this.pliromi = pliromi;
        this.par = par;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAmount() {
        return amount;
    }

    public String getPliromi() {
        return pliromi;
    }

    public void setPliromi(String pliromi) {
        this.pliromi = pliromi;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPar() {
        return par;
    }
    public void setPar(String par) {
        this.par = par;
    }
}
