package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.ItemDao;
import org.easytech.pelatologio.models.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDaoImpl implements ItemDao {

    private final HikariDataSource dataSource;

    public ItemDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Item> getItems() throws SQLException {
        List<Item> dataList = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            String query = "SELECT * FROM Items where id > 0 order by id desc";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Item item = new Item();
                item.setId(resultSet.getInt("id"));
                item.setName(resultSet.getString("name"));
                item.setDescription(resultSet.getString("description"));
                item.setCategory(resultSet.getString("category"));
                dataList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    @Override
    public boolean isItemExists(String name) {
        String query = "SELECT COUNT(*) FROM Items WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int insertItem(String name, String description, String category) {
        String insertQuery = "INSERT INTO Items (name, description, category) "
                + "VALUES (?, ?, ?)";
        int newItemId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, category);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του είδους ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newItemId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του είδους απέτυχε.");
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του είδους: " + e.getMessage());
        }

        return newItemId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    @Override
    public void updateItem(int code, String name, String description, String category) {
        String sql = "UPDATE items SET name = ?, description = ?, category = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, category);
            pstmt.setInt(4, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε είδος με αυτό το κωδικό.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}