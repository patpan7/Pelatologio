package org.easytech.pelatologio.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PartnerCustomer {
    private String code;
    private String name;
    private String afm;
    private String phone;
    private String email;
    private LocalDate contractDate;
    private BigDecimal totalPaid;
    private BigDecimal commission;

    public PartnerCustomer() {
        // Default constructor
    }

    public PartnerCustomer(String code, String name, String afm, String phone, String email, 
                          LocalDate contractDate, BigDecimal totalPaid, BigDecimal commission) {
        this.code = code;
        this.name = name;
        this.afm = afm;
        this.phone = phone;
        this.email = email;
        this.contractDate = contractDate;
        this.totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
        this.commission = commission != null ? commission : BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission != null ? commission : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
