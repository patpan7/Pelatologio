package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.ApplicationStepDao;
import org.easytech.pelatologio.models.ApplicationStep;
import org.easytech.pelatologio.helper.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationStepDaoImpl implements ApplicationStepDao {

    private final HikariDataSource dataSource;

    public ApplicationStepDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<ApplicationStep> getStepsForApplication(int applicationId) {
        List<ApplicationStep> steps = new ArrayList<>();
        String sql = "SELECT * FROM ApplicationSteps WHERE application_id = ? ORDER BY step_order ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ApplicationStep step = new ApplicationStep();
                step.setId(rs.getInt("id"));
                step.setApplicationId(rs.getInt("application_id"));
                step.setStepName(rs.getString("step_name"));
                step.setStepOrder(rs.getInt("step_order"));
                step.setActionType(rs.getString("action_type"));
                step.setActionConfigJson(rs.getString("action_config_json")); // Add this line
                steps.add(step);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return steps;
    }

    @Override
    public void addStep(ApplicationStep step) {
        String sql = "INSERT INTO ApplicationSteps (application_id, step_name, step_order, action_type, action_config_json) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, step.getApplicationId());
            pstmt.setString(2, step.getStepName());
            pstmt.setInt(3, step.getStepOrder());
            pstmt.setString(4, step.getActionType());
            pstmt.setString(5, step.getActionConfigJson()); // Add this line
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStep(ApplicationStep step) {
        String sql = "UPDATE ApplicationSteps SET step_name = ?, step_order = ?, action_type = ?, action_config_json = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, step.getStepName());
            pstmt.setInt(2, step.getStepOrder());
            pstmt.setString(3, step.getActionType());
            pstmt.setString(4, step.getActionConfigJson()); // Add this line
            pstmt.setInt(5, step.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteStep(int stepId) {
        String sql = "DELETE FROM ApplicationSteps WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stepId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
