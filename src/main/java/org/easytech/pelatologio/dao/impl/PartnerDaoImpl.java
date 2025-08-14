package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.PartnerDao;
import org.easytech.pelatologio.models.Partner;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerDaoImpl implements PartnerDao {
    private final HikariDataSource dataSource;

    public PartnerDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Partner findById(int id) {
        String sql = "SELECT * FROM Partners WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToPartner(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Partner> findAll() {
        List<Partner> partners = new ArrayList<>();
        String sql = "SELECT * FROM Partners ORDER BY name";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                partners.add(mapRowToPartner(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partners;
    }

    @Override
    public void insert(Partner partner) {
        String sql = "INSERT INTO Partners (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email, email2, manager, managerPhone, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, partner.getName());
            pstmt.setString(2, partner.getTitle());
            pstmt.setString(3, partner.getJob());
            pstmt.setString(4, partner.getAfm());
            pstmt.setString(5, partner.getPhone1());
            pstmt.setString(6, partner.getPhone2());
            pstmt.setString(7, partner.getMobile());
            pstmt.setString(8, partner.getAddress());
            pstmt.setString(9, partner.getTown());
            pstmt.setString(10, partner.getPostcode());
            pstmt.setString(11, partner.getEmail());
            pstmt.setString(12, partner.getEmail2());
            pstmt.setString(13, partner.getManager());
            pstmt.setString(14, partner.getManagerPhone());
            pstmt.setString(15, partner.getNotes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Partner partner) {
        String sql = "UPDATE Partners SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, postcode = ?, email = ?, email2 = ?, manager = ?, managerPhone = ?, notes = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, partner.getName());
            pstmt.setString(2, partner.getTitle());
            pstmt.setString(3, partner.getJob());
            pstmt.setString(4, partner.getAfm());
            pstmt.setString(5, partner.getPhone1());
            pstmt.setString(6, partner.getPhone2());
            pstmt.setString(7, partner.getMobile());
            pstmt.setString(8, partner.getAddress());
            pstmt.setString(9, partner.getTown());
            pstmt.setString(10, partner.getPostcode());
            pstmt.setString(11, partner.getEmail());
            pstmt.setString(12, partner.getEmail2());
            pstmt.setString(13, partner.getManager());
            pstmt.setString(14, partner.getManagerPhone());
            pstmt.setString(15, partner.getNotes());
            pstmt.setInt(16, partner.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Partners WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Partner mapRowToPartner(ResultSet rs) throws SQLException {
        Partner partner = new Partner();
        partner.setId(rs.getInt("id"));
        partner.setName(rs.getString("name"));
        partner.setTitle(rs.getString("title"));
        partner.setJob(rs.getString("job"));
        partner.setAfm(rs.getString("afm"));
        partner.setPhone1(rs.getString("phone1"));
        partner.setPhone2(rs.getString("phone2"));
        partner.setMobile(rs.getString("mobile"));
        partner.setAddress(rs.getString("address"));
        partner.setTown(rs.getString("town"));
        partner.setPostcode(rs.getString("postcode"));
        partner.setEmail(rs.getString("email"));
        partner.setEmail2(rs.getString("email2"));
        partner.setManager(rs.getString("manager"));
        partner.setManagerPhone(rs.getString("managerPhone"));
        partner.setNotes(rs.getString("notes"));
        return partner;
    }
}
