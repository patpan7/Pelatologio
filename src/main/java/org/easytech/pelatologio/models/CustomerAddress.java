package org.easytech.pelatologio.models;

public class CustomerAddress {
    private int addressId;
    private int customerCode;
    private String address;
    private String town;
    private String postcode;
    private String store;

    public CustomerAddress(String address, String town, String postcode, String store) {
        this.address = address;
        this.town = town;
        this.postcode = postcode;
        this.store = store;
    }

    public CustomerAddress(int customerCode, String address, String town, String postcode, String store) {
        this.customerCode = customerCode;
        this.address = address;
        this.town = town;
        this.postcode = postcode;
        this.store = store;
    }

    public CustomerAddress() {

    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(int customerCode) {
        this.customerCode = customerCode;
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

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}
