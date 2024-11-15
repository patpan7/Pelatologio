package org.easytech.pelatologio;

public class Address {
    private int addressId;
    private String address;
    private String city;
    private String postalCode;

    public Address(int addressId, String address, String city, String postalCode) {
        this.addressId = addressId;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    public int getAddressId() {
        return addressId;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
