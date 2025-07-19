package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.SupplierDao;
import org.easytech.pelatologio.models.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDaoImpl implements SupplierDao {

    private final HikariDataSource dataSource;

    public SupplierDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Supplier> getSuppliersFromOrders() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT DISTINCT s.id, s.name, s.title, s.afm, s.phone, s.mobile, s.contact, s.email, s.email2, s.site, s.notes " +
                "FROM Suppliers s " +
                "JOIN Orders o ON s.id = o.supplierId";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public List<Supplier> getSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public Supplier getSelectedSupplier(int supplierId) {
        String query = "SELECT * FROM Suppliers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insertSupplier(String name, String title, String afm, String phone, String mobile, String contact, String email, String email2, String site, String notes) {
        String insertQuery = "INSERT INTO Suppliers (name, title, afm, phone, mobile, contact, email, email2, site, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int newSupplierId = -1;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, afm);
            pstmt.setString(4, phone);
            pstmt.setString(5, mobile);
            pstmt.setString(6, contact);
            pstmt.setString(7, email);
            pstmt.setString(8, email2);
            pstmt.setString(9, site);
            pstmt.setString(10, notes);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newSupplierId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newSupplierId;
    }

    @Override
    public void updateSupplier(int code, String name, String title, String afm, String phone, String mobile, String contact, String email, String email2, String site, String notes) {
        String sql = "UPDATE Suppliers SET name = ?, title = ?, afm = ?, phone = ?, mobile = ?, contact = ?, email = ?, email2 = ?, site = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, afm);
            pstmt.setString(4, phone);
            pstmt.setString(5, mobile);
            pstmt.setString(6, contact);
            pstmt.setString(7, email);
            pstmt.setString(8, email2);
            pstmt.setString(9, site);
            pstmt.setString(10, notes);
            pstmt.setInt(11, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id"));
        supplier.setName(rs.getString("name"));
        supplier.setTitle(rs.getString("title"));
        supplier.setAfm(rs.getString("afm"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setMobile(rs.getString("mobile"));
        supplier.setContact(rs.getString("contact"));
        supplier.setEmail(rs.getString("email"));
        supplier.setEmail2(rs.getString("email2"));
        supplier.setSite(rs.getString("site"));
        supplier.setNotes(rs.getString("notes"));
        return supplier;
    }
}