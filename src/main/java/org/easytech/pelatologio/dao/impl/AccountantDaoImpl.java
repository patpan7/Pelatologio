package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.AccountantDao;
import org.easytech.pelatologio.models.Accountant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountantDaoImpl implements AccountantDao {

    private final HikariDataSource dataSource;
    private static List<Accountant> accountantsCache; // Cache for accountants

    public AccountantDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Accountant> getAccountants() {
        List<Accountant> accountants = new ArrayList<>();
        String query = "SELECT * FROM Accountants";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String mobile = rs.getString("mobile");
                String email = rs.getString("email");
                String erganiEmail = rs.getString("erganiEmail");
                Accountant accountant = new Accountant(id, name, phone, mobile, email, erganiEmail);
                accountants.add(accountant);
            }
            accountantsCache = accountants; // Cache the results
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountants;
    }

    @Override
    public Accountant getSelectedAccountant(int accountantId) {
        String query = "SELECT * FROM accountants WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accountantId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                Accountant data = null;
                if (resultSet.next()) {
                    data = new Accountant();
                    data.setId(resultSet.getInt("id"));
                    data.setName(resultSet.getString("name"));
                    data.setPhone(resultSet.getString("phone"));
                    data.setMobile(resultSet.getString("mobile"));
                    data.setEmail(resultSet.getString("email"));
                }
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insertAccountant(String name, String phone, String mobile, String email, String erganiEmail) {
        String insertQuery = "INSERT INTO Accountants (name, phone, mobile, email, erganiEmail) "
                + "VALUES (?, ?, ?, ?, ?)";
        int newCustomerId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, mobile);
            pstmt.setString(4, email);
            pstmt.setString(5, erganiEmail);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του λογιστή ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCustomerId = generatedKeys.getInt(1);
                    }
                }
                invalidateAccountantsCache(); // Invalidate cache
            } else {
                System.out.println("Η εισαγωγή του λογιστή απέτυχε.");
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του λογιστή: " + e.getMessage());
        }

        return newCustomerId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    @Override
    public void updateAccountant(int code, String name, String phone, String mobile, String email, String erganiEmail) {
        String sql = "UPDATE accountants SET name = ?, phone = ?, mobile = ?, email = ?, erganiEmail = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, mobile);
            pstmt.setString(4, email);
            pstmt.setString(5, erganiEmail);
            pstmt.setInt(6, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε πελάτης με αυτό το κωδικό.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void invalidateAccountantsCache() {
        accountantsCache = null; // Clear the cache
    }
}