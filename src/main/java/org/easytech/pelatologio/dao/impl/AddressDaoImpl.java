package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.AddressDao;
import org.easytech.pelatologio.models.CustomerAddress;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDaoImpl implements AddressDao {

    private final HikariDataSource dataSource;

    public AddressDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<CustomerAddress> getCustomerAddresses(int customerId) {
        String sql = "SELECT * FROM CustomerAddresses WHERE CustomerID = ?";
        List<CustomerAddress> customerAddresses = new ArrayList<>();


        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int addressId = rs.getInt("AddressID");
                String address = rs.getString("Address");
                String town = rs.getString("Town");
                String postcode = rs.getString("Postcode");
                String store = rs.getString("Store");

                CustomerAddress newCustomerAddress = new CustomerAddress();
                newCustomerAddress.setAddressId(addressId);
                newCustomerAddress.setAddress(address);
                newCustomerAddress.setTown(town);
                newCustomerAddress.setPostcode(postcode);
                newCustomerAddress.setStore(store);
                customerAddresses.add(newCustomerAddress);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Αν υπάρχει κάποιο σφάλμα, το εκτυπώνουμε
        }

        return customerAddresses;
    }

    @Override
    public void addAddress(int code, CustomerAddress newCustomerAddress) {
        String sql = "INSERT INTO CustomerAddresses (CustomerID, Address, Town, Postcode, Store) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, code);
            statement.setString(2, newCustomerAddress.getAddress());
            statement.setString(3, newCustomerAddress.getTown());
            statement.setString(4, newCustomerAddress.getPostcode());
            statement.setString(5, newCustomerAddress.getStore());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateAddress(CustomerAddress updatedCustomerAddress) {
        String query = "UPDATE CustomerAddresses SET Address = ?, Town = ?, Postcode = ?, Store = ? WHERE AddressID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedCustomerAddress.getAddress());
            pstmt.setString(2, updatedCustomerAddress.getTown());
            pstmt.setString(3, updatedCustomerAddress.getPostcode());
            pstmt.setString(4, updatedCustomerAddress.getStore());
            pstmt.setInt(5, updatedCustomerAddress.getAddressId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAddress(int addressId) {
        String query = "DELETE FROM CustomerAddresses WHERE addressID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, addressId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}