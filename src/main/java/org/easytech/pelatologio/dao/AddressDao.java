package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.CustomerAddress;
import java.util.List;

public interface AddressDao {
    List<CustomerAddress> getCustomerAddresses(int customerId);
    void addAddress(int code, CustomerAddress newCustomerAddress);
    void updateAddress(CustomerAddress updatedCustomerAddress);
    void deleteAddress(int addressId);
}