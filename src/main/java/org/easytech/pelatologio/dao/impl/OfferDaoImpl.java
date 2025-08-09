package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.OfferDao;
import org.easytech.pelatologio.models.Offer;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OfferDaoImpl implements OfferDao {

    private final HikariDataSource dataSource;

    public OfferDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Offer> getAllOffers() {
        List<Offer> offers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, s.is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                String description = resultSet.getString("description").trim();
                String hours = resultSet.getString("hours").trim();
                String status = resultSet.getString("status").trim();
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Date responseSqlDate = resultSet.getDate("response_date");
                LocalDate responseDate = (responseSqlDate != null) ? responseSqlDate.toLocalDate() : null;
                String customerName = resultSet.getString("name");
                String paths = resultSet.getObject("offer_file_paths") != null ? resultSet.getString("offer_file_paths").trim() : "";
                String sended = resultSet.getString("sended");
                Boolean isArchived = resultSet.getBoolean("is_archived");

                Offer offer = new Offer(id, offerDate, description, hours, status, customerId, responseDate, customerName, paths, sended, isArchived);
                offers.add(offer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offers;
    }

    @Override
    public void deleteOffer(int id) {
        String query = "DELETE FROM Offers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean saveOffer(Offer newOffer) {
        String query = "INSERT INTO Offers (offerDate, description, hours, status, customerId, offer_file_paths, sended) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(newOffer.getOfferDate()));
            stmt.setString(2, newOffer.getDescription().trim());
            stmt.setString(3, newOffer.getHours().trim());
            stmt.setString(4, newOffer.getStatus().trim());
            stmt.setInt(5, newOffer.getCustomerId());
            stmt.setString(6, newOffer.getPaths());
            stmt.setString(7, newOffer.getSended());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveOffer(newOffer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean updateOffer(Offer offer) {
        String query = "UPDATE Offers SET offerDate = ?, description = ?, hours = ?, status = ?, customerId = ?, offer_file_paths = ?, is_archived = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(offer.getOfferDate()));
            stmt.setString(2, offer.getDescription().trim());
            stmt.setString(3, offer.getHours().trim());
            stmt.setString(4, offer.getStatus().trim());
            stmt.setInt(5, offer.getCustomerId());
            stmt.setString(6, offer.getPaths());
            stmt.setBoolean(7, offer.getArchived());
            stmt.setInt(8, offer.getId());

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
    public List<Offer> getAllCustomerOffers(int customerCode) {
        List<Offer> offers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "WHERE s.customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                    String description = resultSet.getString("description").trim();
                    String hours = resultSet.getString("hours").trim();
                    String status = resultSet.getString("status").trim();
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    Date responseSqlDate = resultSet.getDate("response_date");
                    LocalDate responseDate = (responseSqlDate != null) ? responseSqlDate.toLocalDate() : null;
                    String customerName = resultSet.getString("name").trim();
                    String paths = resultSet.getString("offer_file_paths");
                    String sended = resultSet.getString("sended");
                    Boolean isArchived = resultSet.getBoolean("is_archived");

                    Offer offer = new Offer(id, offerDate, description, hours, status, customerId, responseDate, customerName, paths, sended, isArchived);
                    offers.add(offer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offers;
    }

    @Override
    public List<Offer> getUpdatedOffers(LocalDateTime lastCheck) {
        List<Offer> updatedOffers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code WHERE last_updated > ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(lastCheck)); // Ορίζουμε το timestamp
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                    String description = resultSet.getString("description");
                    String hours = resultSet.getString("hours");
                    String status = resultSet.getString("status");
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    LocalDate response_date = resultSet.getDate("response_date") != null ?
                            resultSet.getDate("response_date").toLocalDate() : null;
                    String customerName = resultSet.getString("name");
                    String paths = resultSet.getObject("offer_file_paths") != null ? resultSet.getString("offer_file_paths").trim() : "";
                    String sended = resultSet.getString("sended");
                    Boolean isArchived = resultSet.getBoolean("is_archived");

                    Offer offer = new Offer(id, offerDate, description, hours, status, customerId, response_date, customerName, paths, sended, isArchived);
                    updatedOffers.add(offer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return updatedOffers;
    }

    @Override
    public void updateOfferSent(int id) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = "Ναί " + myDateObj.format(myFormatObj);
        String query = "UPDATE Offers SET sended = '" + formattedDate + "' WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateOfferStatusManual(int id, String status) {
        String query = "UPDATE Offers SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateOfferStatus(int id, String status) {
        String query = "UPDATE Offers SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateOfferArchived(int id) {
        String query = "UPDATE Offers SET is_archived = 1 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}