package org.easytech.pelatologio;

import java.time.LocalDate;
import java.util.Date;

public class Offer {
    private int id;
    private LocalDate offerDate;
    private String description;
    private String hours;
    private String status;
    private Integer customerId;
    private LocalDate response_date;
    private String customerName;

    public Offer(int id, LocalDate offerDate, String description, String hours, String status, int customerId, LocalDate response_date, String customerName) {
        this.id = id;
        this.offerDate = offerDate;
        this.description = description;
        this.hours = hours;
        this.status = status;
        this.customerId = customerId;
        this.response_date = response_date;
        this.customerName = customerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(LocalDate offerDate) {
        this.offerDate = offerDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDate getResponse_date() {
        return response_date;
    }

    public void setResponse_date(LocalDate response_date) {
        this.response_date = response_date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
