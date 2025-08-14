package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.CommissionDao;
import org.easytech.pelatologio.models.Commission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommissionDaoImpl implements CommissionDao {

    private final HikariDataSource dataSource;

    public CommissionDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void addCommission(Commission commission) {
        String sql = "INSERT INTO Commissions (partner_id, customer_id, supplier_id, rate, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commission.getPartnerId());
            pstmt.setInt(2, commission.getCustomerId());
            pstmt.setInt(3, commission.getSupplierId());
            pstmt.setDouble(4, commission.getRate());
            pstmt.setDate(5, Date.valueOf(commission.getStartDate()));
            pstmt.setDate(6, commission.getEndDate() != null ? Date.valueOf(commission.getEndDate()) : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCommission(Commission commission) {
        String sql = "UPDATE Commissions SET partner_id = ?, customer_id = ?, supplier_id = ?, rate = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commission.getPartnerId());
            pstmt.setInt(2, commission.getCustomerId());
            pstmt.setInt(3, commission.getSupplierId());
            pstmt.setDouble(4, commission.getRate());
            pstmt.setDate(5, Date.valueOf(commission.getStartDate()));
            pstmt.setDate(6, commission.getEndDate() != null ? Date.valueOf(commission.getEndDate()) : null);
            pstmt.setInt(7, commission.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCommission(int commissionId) {
        String sql = "DELETE FROM Commissions WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commissionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Commission findCommission(int customerId, int supplierId) {
        String sql = "SELECT * FROM Commissions WHERE customer_id = ? AND supplier_id = ? AND (end_date IS NULL OR end_date >= GETDATE())";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, supplierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Commission commission = new Commission();
                commission.setId(rs.getInt("id"));
                commission.setPartnerId(rs.getInt("partner_id"));
                commission.setCustomerId(rs.getInt("customer_id"));
                commission.setSupplierId(rs.getInt("supplier_id"));
                commission.setRate(rs.getDouble("rate"));
                commission.setStartDate(rs.getDate("start_date").toLocalDate());
                if (rs.getDate("end_date") != null) {
                    commission.setEndDate(rs.getDate("end_date").toLocalDate());
                }
                return commission;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Commission> getCommissionsForPartner(int partnerId) {
        List<Commission> commissions = new ArrayList<>();
        String sql = "SELECT * FROM Commissions WHERE partner_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partnerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Commission commission = new Commission();
                // ... (fill commission object as above)
                commissions.add(commission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commissions;
    }

    @Override
    public List<Commission> getAllCommissions() {
        List<Commission> commissions = new ArrayList<>();
        String sql = "SELECT c.id, c.partner_id, c.customer_id, c.supplier_id, c.rate, c.start_date, c.end_date, " +
                     "p.name AS partnerName, cust.name AS customerName, s.name AS supplierName " +
                     "FROM Commissions c " +
                     "JOIN Partners p ON c.partner_id = p.id " +
                     "JOIN Customers cust ON c.customer_id = cust.code " +
                     "JOIN Suppliers s ON c.supplier_id = s.id " +
                     "ORDER BY c.start_date DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Commission commission = new Commission();
                commission.setId(rs.getInt("id"));
                commission.setPartnerId(rs.getInt("partner_id"));
                commission.setCustomerId(rs.getInt("customer_id"));
                commission.setSupplierId(rs.getInt("supplier_id"));
                commission.setRate(rs.getDouble("rate"));
                commission.setStartDate(rs.getDate("start_date").toLocalDate());
                if (rs.getDate("end_date") != null) {
                    commission.setEndDate(rs.getDate("end_date").toLocalDate());
                }
                commission.setPartnerName(rs.getString("partnerName"));
                commission.setCustomerName(rs.getString("customerName"));
                commission.setSupplierName(rs.getString("supplierName"));
                commissions.add(commission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commissions;
    }
}