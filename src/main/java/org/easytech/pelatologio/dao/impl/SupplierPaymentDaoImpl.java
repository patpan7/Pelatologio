package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.SupplierPaymentDao;
import org.easytech.pelatologio.models.SupplierPayment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierPaymentDaoImpl implements SupplierPaymentDao {

    private final HikariDataSource dataSource;

    public SupplierPaymentDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void addPayment(SupplierPayment payment) {
        String sql = "INSERT INTO SupplierPayments (supplier_id, customer_id, payment_date, amount, description, is_calculated) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, payment.getSupplierId());
            pstmt.setInt(2, payment.getCustomerId());
            pstmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            pstmt.setBigDecimal(4, payment.getAmount());
            pstmt.setString(5, payment.getDescription());
            pstmt.setBoolean(6, payment.isCalculated());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePayment(SupplierPayment payment) {
        String sql = "UPDATE SupplierPayments SET supplier_id = ?, customer_id = ?, payment_date = ?, amount = ?, description = ?, is_calculated = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, payment.getSupplierId());
            pstmt.setInt(2, payment.getCustomerId());
            pstmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            pstmt.setBigDecimal(4, payment.getAmount());
            pstmt.setString(5, payment.getDescription());
            pstmt.setBoolean(6, payment.isCalculated());
            pstmt.setInt(7, payment.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePayment(int paymentId) {
        String sql = "DELETE FROM SupplierPayments WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SupplierPayment> getPaymentsForSupplier(int supplierId) {
        List<SupplierPayment> payments = new ArrayList<>();
        String sql = "SELECT sp.*, c.name AS customerName FROM SupplierPayments sp " +
                "JOIN Customers c ON sp.customer_id = c.code " +
                "WHERE sp.supplier_id = ? ORDER BY sp.payment_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SupplierPayment payment = new SupplierPayment();
                payment.setId(rs.getInt("id"));
                payment.setSupplierId(rs.getInt("supplier_id"));
                payment.setCustomerId(rs.getInt("customer_id"));
                payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setDescription(rs.getString("description"));
                payment.setCalculated(rs.getBoolean("is_calculated"));
                payment.setCustomerName(rs.getString("customerName")); // Set the customer name
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public List<SupplierPayment> getUncalculatedPayments() {
        List<SupplierPayment> payments = new ArrayList<>();
        String sql = "SELECT sp.*, c.name AS customerName FROM SupplierPayments sp " +
                "JOIN Customers c ON sp.customer_id = c.code " +
                "WHERE sp.is_calculated = 0";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                SupplierPayment payment = new SupplierPayment();
                payment.setId(rs.getInt("id"));
                payment.setSupplierId(rs.getInt("supplier_id"));
                payment.setCustomerId(rs.getInt("customer_id"));
                payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setDescription(rs.getString("description"));
                payment.setCalculated(rs.getBoolean("is_calculated"));
                payment.setCustomerName(rs.getString("customerName")); // Set the customer name
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public void markAsCalculated(int paymentId) throws SQLException {
        String sql = "UPDATE SupplierPayments SET is_calculated = 1 WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            pstmt.executeUpdate();
        }
    }
}
