package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.TrackingDao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrackingDaoImpl implements TrackingDao {

    private final HikariDataSource dataSource;

    public TrackingDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void saveTrackingNumber(String tracking, LocalDate date, int customerId) {
        String query = "INSERT INTO CourierTracking (tracking_number, date, customerId) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tracking);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setInt(3, customerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getTrackingNumbers(int customerId) {
        List<String> trackingNumbers = new ArrayList<>();
        String query = "SELECT tracking_number, date FROM CourierTracking WHERE customerId = ? ORDER BY date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trackingNumbers.add("Αριθμός αποστολής: "+rs.getString("tracking_number") + " | Ημερομηνία: " + rs.getDate("date"));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trackingNumbers;
    }
}