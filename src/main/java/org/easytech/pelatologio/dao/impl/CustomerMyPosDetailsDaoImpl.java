package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.CustomerMyPosDetailsDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CustomerMyPosDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerMyPosDetailsDaoImpl implements CustomerMyPosDetailsDao {

    private final HikariDataSource dataSource;

    public CustomerMyPosDetailsDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<CustomerMyPosDetails> getByCustomerId(int customerId) {
        List<CustomerMyPosDetails> detailsList = new ArrayList<>();
        String sql = "SELECT * FROM CustomerMyPosDetails WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CustomerMyPosDetails details = new CustomerMyPosDetails();
                details.setId(rs.getInt("id"));
                details.setCustomerId(rs.getInt("customer_id"));
                details.setMyposClientId(rs.getString("mypos_client_id"));
                details.setVerificationStatus(rs.getString("verification_status"));
                details.setAccountStatus(rs.getString("account_status"));
                detailsList.add(details);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList;
    }

    @Override
    public void saveOrUpdate(CustomerMyPosDetails details) {
        // If the ID is not 0, it's an existing record, so we UPDATE.
        if (details.getId() != 0) {
            String sql = "UPDATE CustomerMyPosDetails SET mypos_client_id = ?, verification_status = ?, account_status = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, details.getMyposClientId());
                pstmt.setString(2, details.getVerificationStatus());
                pstmt.setString(3, details.getAccountStatus());
                pstmt.setInt(4, details.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // If the ID is 0, it's a new record, so we INSERT.
            String sql = "INSERT INTO CustomerMyPosDetails (customer_id, mypos_client_id, verification_status, account_status) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, details.getCustomerId());
                pstmt.setString(2, details.getMyposClientId());
                pstmt.setString(3, details.getVerificationStatus());
                pstmt.setString(4, details.getAccountStatus());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM CustomerMyPosDetails WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int countByVerificationStatus(String status) {
        return countByStatus("verification_status", status);
    }

    @Override
    public int countByAccountStatus(String status) {
        return countByStatus("account_status", status);
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM CustomerMyPosDetails";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int countByStatus(String columnName, String status) {
        String sql = "SELECT COUNT(*) FROM CustomerMyPosDetails WHERE " + columnName + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<CustomerMyPosDetails> getAll() {
        List<CustomerMyPosDetails> detailsList = new ArrayList<>();
        String sql = "SELECT c.name, c.code, cmd.* FROM CustomerMyPosDetails cmd JOIN Customers c ON cmd.customer_id = c.code order by c.code DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                CustomerMyPosDetails details = new CustomerMyPosDetails();
                details.setId(rs.getInt("id"));
                details.setCustomerId(rs.getInt("customer_id"));
                details.setMyposClientId(rs.getString("mypos_client_id"));
                details.setVerificationStatus(rs.getString("verification_status"));
                details.setAccountStatus(rs.getString("account_status"));
                // Set customer name and code for display
                details.setCustomerName(rs.getString("name"));
                details.setCustomerCode(rs.getInt("code"));
                detailsList.add(details);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detailsList;
    }
}
