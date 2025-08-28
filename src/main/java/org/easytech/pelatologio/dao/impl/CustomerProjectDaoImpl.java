package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.CustomerProjectDao;
import org.easytech.pelatologio.models.CustomerProject;
import org.easytech.pelatologio.helper.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerProjectDaoImpl implements CustomerProjectDao {

    private final HikariDataSource dataSource;

    public CustomerProjectDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<CustomerProject> getProjectsForCustomer(int customerId) {
        List<CustomerProject> projects = new ArrayList<>();
        String sql = "SELECT * FROM CustomerProjects WHERE customer_id = ? ORDER BY start_date DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CustomerProject project = new CustomerProject();
                project.setId(rs.getInt("id"));
                project.setCustomerId(rs.getInt("customer_id"));
                project.setApplicationId(rs.getInt("application_id"));
                project.setProjectName(rs.getString("project_name"));
                project.setStartDate(rs.getDate("start_date").toLocalDate());
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    @Override
    public void addProjectForCustomer(CustomerProject project) {
        String sql = "INSERT INTO CustomerProjects (customer_id, application_id, project_name, start_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, project.getCustomerId());
            pstmt.setInt(2, project.getApplicationId());
            pstmt.setString(3, project.getProjectName());
            pstmt.setDate(4, Date.valueOf(project.getStartDate()));
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                project.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasProjects(int customerId, int applicationId) {
        String sql = "SELECT COUNT(*) FROM CustomerProjects WHERE customer_id = ? AND application_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, applicationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
