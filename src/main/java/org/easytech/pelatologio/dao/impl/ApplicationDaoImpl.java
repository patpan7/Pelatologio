package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.ApplicationDao;
import org.easytech.pelatologio.models.AppItem;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDaoImpl implements ApplicationDao {

    private final HikariDataSource dataSource;

    public ApplicationDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<AppItem> getAllApplications() {
        List<AppItem> applications = new ArrayList<>();
        String sql = "SELECT * FROM Applications ORDER BY ApplicationName ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                AppItem app = new AppItem();
                app.setId(rs.getInt("ApplicationID"));
                app.setName(rs.getString("ApplicationName"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }
}
