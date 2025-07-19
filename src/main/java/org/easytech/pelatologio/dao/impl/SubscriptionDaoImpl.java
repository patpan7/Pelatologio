package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.SubscriptionDao;
import org.easytech.pelatologio.models.SubsCategory;
import org.easytech.pelatologio.models.Subscription;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDaoImpl implements SubscriptionDao {

    private final HikariDataSource dataSource;

    public SubscriptionDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<SubsCategory> getAllSubsCategory() {
        List<SubsCategory> subsCategories = new ArrayList<>();
        String query = "SELECT * FROM subsCategories";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                subsCategories.add(new SubsCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subsCategories;

    }

    @Override
    public List<Subscription> getAllSubs(LocalDate fromDate, LocalDate toDate) {
        List<Subscription> subs = new ArrayList<>();
        String query = "SELECT s.id, s.title, s.endDate, s.customerId, s.subCatId, i.name AS catName, s.note, s.price, s.sended, c.name  " +
                "FROM Subscriptions s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "LEFT JOIN SubsCategories i ON s.subCatId = i.id " +
                "WHERE s.endDate BETWEEN ? AND ? " +
                "ORDER BY s.endDate ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    LocalDate endDate = resultSet.getDate("endDate").toLocalDate();
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    Integer categoryId = resultSet.getObject("subCatId", Integer.class);
                    String category = resultSet.getString("catName");
                    String note = resultSet.getString("note");
                    String price = resultSet.getString("price");
                    String customerName = resultSet.getString("name");
                    String sended = resultSet.getString("sended");

                    Subscription sub = new Subscription(id, title, endDate, customerId, categoryId, price, note, sended);
                    sub.setCustomerName(customerName);
                    sub.setCategory(category);
                    subs.add(sub);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subs;

    }

    @Override
    public void deleteSubsCategory(int id) {
        String query = "DELETE FROM SubsCategories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSubsCategory(SubsCategory updatedSubsCategory) {
        String query = "UPDATE SubsCategories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedSubsCategory.getName());
            pstmt.setInt(2, updatedSubsCategory.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSubsCategory(SubsCategory newSubsCategory) {
        String query = "INSERT INTO SubsCategories (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, newSubsCategory.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                newSubsCategory.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean saveSub(Subscription newSub) {
        String query = "INSERT INTO Subscriptions (title, endDate, note, subCatId, customerId, price, sended) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newSub.getTitle());
            stmt.setDate(2, Date.valueOf(newSub.getEndDate()));
            stmt.setString(3, newSub.getNote());
            stmt.setInt(4, newSub.getCategoryId());
            stmt.setInt(5, newSub.getCustomerId());
            stmt.setString(6, newSub.getPrice());
            stmt.setString(7, newSub.getSended());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveSub(newSub);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateSub(Subscription sub) {
        String query = "UPDATE Subscriptions SET title = ?, endDate = ?, note = ?, subCatId = ?, customerId = ?, price = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, sub.getTitle());
            stmt.setDate(2, Date.valueOf(sub.getEndDate()));
            stmt.setString(3, sub.getNote());
            stmt.setInt(4, sub.getCategoryId());
            stmt.setInt(5, sub.getCustomerId());
            stmt.setString(6, sub.getPrice());
            stmt.setInt(7, sub.getId());

            if (stmt.executeUpdate() > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getErganiEmail(int customerId) {
        String query = "SELECT a.erganiEmail " +
                "FROM customers c " +
                "JOIN accountants a ON c.accid = a.id " +
                "WHERE c.code = ?";
        String title = "";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                title = resultSet.getString("erganiEmail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return title;
    }

    @Override
    public boolean updateErganiEmail(int customerId, String emailAcc) {
        String query = "UPDATE ac " +
                "SET ac.erganiEmail = ? " +
                "FROM accountants ac " +
                "INNER JOIN customers c ON c.accid = ac.id " +
                "WHERE c.code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, emailAcc);
            stmt.setInt(2, customerId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // Επιστρέφει true αν ενημερώθηκε τουλάχιστον 1 γραμμή

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Σε περίπτωση σφάλματος
    }

    @Override
    public List<Subscription> getAllCustomerSubs(int customerCode) {
        List<Subscription> subs = new ArrayList<>();
        String query = "SELECT s.id, s.title, s.endDate, s.customerId, s.subCatId, i.name AS catName, s.note, s.price, s.sended, c.name  " +
                "FROM Subscriptions s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "LEFT JOIN SubsCategories i ON s.subCatId = i.id " +
                "WHERE s.customerId = ? " +
                "ORDER BY s.endDate ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    LocalDate endDate = resultSet.getDate("endDate").toLocalDate();
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    Integer categoryId = resultSet.getObject("subCatId", Integer.class);
                    String category = resultSet.getString("catName");
                    String note = resultSet.getString("note");
                    String price = resultSet.getString("price");
                    String customerName = resultSet.getString("name");
                    String sended = resultSet.getString("sended");

                    Subscription sub = new Subscription(id, title, endDate, customerId, categoryId, price, note, sended);
                    sub.setCustomerName(customerName);
                    sub.setCategory(category);
                    subs.add(sub);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subs;
    }

    @Override
    public void deleteSub(int id) {
        String query = "DELETE FROM Subscriptions WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renewSub(int id, int yearsToAdd) {
        String updateQuery = "UPDATE Subscriptions SET endDate = DATEADD(YEAR, ?, endDate) WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setInt(1, yearsToAdd);
            stmt.setInt(2, id); // Το ID του συμβολαίου που ανανεώνεται

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Ημερομηνία λήξης ενημερώθηκε επιτυχώς!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSubSent(int id) {
        String query = "UPDATE Subscriptions SET sended = 'true' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}