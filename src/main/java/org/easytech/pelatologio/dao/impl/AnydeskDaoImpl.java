package org.easytech.pelatologio.dao.impl;

import org.easytech.pelatologio.dao.AnydeskDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Anydesk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnydeskDaoImpl implements AnydeskDao {

    @Override
    public List<Anydesk> getAnydeskIdsForCustomer(int customerId) {
        List<Anydesk> anydeskList = new ArrayList<>();
        String sql = "SELECT id, customer_id, anydesk_id, description FROM customer_anydesk_ids WHERE customer_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Anydesk anydesk = new Anydesk();
                anydesk.setId(rs.getInt("id"));
                anydesk.setCustomerId(rs.getInt("customer_id"));
                anydesk.setAnydeskId(rs.getString("anydesk_id"));
                anydesk.setDescription(rs.getString("description"));
                anydeskList.add(anydesk);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Proper logging should be implemented
        }
        return anydeskList;
    }

    @Override
    public void addAnydeskId(Anydesk anydesk) {
        String sql = "INSERT INTO customer_anydesk_ids (customer_id, anydesk_id, description) VALUES (?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, anydesk.getCustomerId());
            pstmt.setString(2, anydesk.getAnydeskId());
            pstmt.setString(3, anydesk.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAnydeskId(int id) {
        String sql = "DELETE FROM customer_anydesk_ids WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateAnydeskId(Anydesk anydesk) {
        String sql = "UPDATE customer_anydesk_ids SET anydesk_id = ?, description = ? WHERE id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, anydesk.getAnydeskId());
            pstmt.setString(2, anydesk.getDescription());
            pstmt.setInt(3, anydesk.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
