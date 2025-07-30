package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.AppItemDao;
import org.easytech.pelatologio.models.AppItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppItemDaoImpl implements AppItemDao {
    private final HikariDataSource dataSource;

    public AppItemDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<AppItem> getApplications() {
        List<AppItem> appItems = new ArrayList<>();
        String query = "SELECT * FROM Applications ORDER BY ApplicationID";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                appItems.add(new AppItem(
                        rs.getInt("ApplicationID"),
                        rs.getString("ApplicationName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appItems;
    }
}
