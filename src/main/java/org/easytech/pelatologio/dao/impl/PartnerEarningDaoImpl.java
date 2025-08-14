package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.PartnerEarningDao;
import org.easytech.pelatologio.models.PartnerEarning;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerEarningDaoImpl implements PartnerEarningDao {

    private final HikariDataSource dataSource;

    public PartnerEarningDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void addEarning(PartnerEarning earning) {
        String sql = "INSERT INTO PartnerEarnings (partner_id, supplier_payment_id, customer_id, commission_id, earning_date, earning_amount, partner_invoice_status, partner_invoice_ref, payment_to_partner_status, payment_to_partner_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, earning.getPartnerId());
            pstmt.setInt(2, earning.getSupplierPaymentId());
            pstmt.setInt(3, earning.getCustomerId());
            pstmt.setInt(4, earning.getCommissionId());
            pstmt.setDate(5, Date.valueOf(earning.getEarningDate()));
            pstmt.setBigDecimal(6, earning.getEarningAmount());
            pstmt.setString(7, earning.getPartnerInvoiceStatus());
            pstmt.setString(8, earning.getPartnerInvoiceRef());
            pstmt.setString(9, earning.getPaymentToPartnerStatus());
            pstmt.setDate(10, earning.getPaymentToPartnerDate() != null ? Date.valueOf(earning.getPaymentToPartnerDate()) : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEarningStatus(int earningId, String invoiceStatus, String paymentStatus, java.time.LocalDate paymentDate) {
        String sql = "UPDATE PartnerEarnings SET partner_invoice_status = ?, payment_to_partner_status = ?, payment_to_partner_date = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoiceStatus);
            pstmt.setString(2, paymentStatus);
            pstmt.setDate(3, paymentDate != null ? Date.valueOf(paymentDate) : null);
            pstmt.setInt(4, earningId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PartnerEarning> getEarningsForPartner(int partnerId) {
        List<PartnerEarning> earnings = new ArrayList<>();
        String sql = "SELECT pe.*, p.name AS partnerName, c.name AS customerName, s.name AS supplierName, com.rate AS commissionRate " +
                     "FROM PartnerEarnings pe " +
                     "JOIN Partners p ON pe.partner_id = p.id " +
                     "JOIN Customers c ON pe.customer_id = c.code " +
                     "JOIN SupplierPayments sp ON pe.supplier_payment_id = sp.id " +
                     "JOIN Suppliers s ON sp.supplier_id = s.id " +
                     "JOIN Commissions com ON pe.commission_id = com.id " +
                     "WHERE pe.partner_id = ? ORDER BY pe.earning_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partnerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                earnings.add(mapResultSetToPartnerEarning(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return earnings;
    }

    @Override
    public List<PartnerEarning> getAllEarnings() {
        List<PartnerEarning> earnings = new ArrayList<>();
        String sql = "SELECT pe.*, p.name AS partnerName, c.name AS customerName, s.name AS supplierName, com.rate AS commissionRate " +
                     "FROM PartnerEarnings pe " +
                     "JOIN Partners p ON pe.partner_id = p.id " +
                     "JOIN Customers c ON pe.customer_id = c.code " +
                     "JOIN SupplierPayments sp ON pe.supplier_payment_id = sp.id " +
                     "JOIN Suppliers s ON sp.supplier_id = s.id " +
                     "JOIN Commissions com ON pe.commission_id = com.id " +
                     "ORDER BY pe.earning_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                earnings.add(mapResultSetToPartnerEarning(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return earnings;
    }

    @Override
    public boolean earningExists(int partnerId, int supplierPaymentId) {
        String sql = "SELECT COUNT(*) FROM PartnerEarnings WHERE partner_id = ? AND supplier_payment_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partnerId);
            pstmt.setInt(2, supplierPaymentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PartnerEarning mapResultSetToPartnerEarning(ResultSet rs) throws SQLException {
        PartnerEarning earning = new PartnerEarning();
        earning.setId(rs.getInt("id"));
        earning.setPartnerId(rs.getInt("partner_id"));
        earning.setSupplierPaymentId(rs.getInt("supplier_payment_id"));
        earning.setCustomerId(rs.getInt("customer_id"));
        earning.setCommissionId(rs.getInt("commission_id"));
        earning.setEarningDate(rs.getDate("earning_date").toLocalDate());
        earning.setEarningAmount(rs.getBigDecimal("earning_amount"));
        earning.setPartnerInvoiceStatus(rs.getString("partner_invoice_status"));
        earning.setPartnerInvoiceRef(rs.getString("partner_invoice_ref"));
        earning.setPaymentToPartnerStatus(rs.getString("payment_to_partner_status"));
        if (rs.getDate("payment_to_partner_date") != null) {
            earning.setPaymentToPartnerDate(rs.getDate("payment_to_partner_date").toLocalDate());
        }
        earning.setPartnerName(rs.getString("partnerName"));
        earning.setCustomerName(rs.getString("customerName"));
        earning.setSupplierName(rs.getString("supplierName"));
        earning.setCommissionRate(rs.getDouble("commissionRate"));
        return earning;
    }
}
