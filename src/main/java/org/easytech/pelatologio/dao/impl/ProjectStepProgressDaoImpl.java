package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.ProjectStepProgressDao;
import org.easytech.pelatologio.models.CustomerProjectSummary;
import org.easytech.pelatologio.models.GlobalProgressEntry;
import org.easytech.pelatologio.models.ProjectStepProgress;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectStepProgressDaoImpl implements ProjectStepProgressDao {

    private final HikariDataSource dataSource;

    public ProjectStepProgressDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<ProjectStepProgress> getProgressForProject(int projectId) {
        List<ProjectStepProgress> progressList = new ArrayList<>();
        String sql = "SELECT psp.*, aps.step_name, aps.action_type, aps.action_config_json FROM ProjectStepProgress psp " +
                     "JOIN ApplicationSteps aps ON psp.step_id = aps.id " +
                     "WHERE psp.project_id = ? ORDER BY aps.step_order ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectStepProgress progress = new ProjectStepProgress();
                progress.setId(rs.getInt("id"));
                progress.setProjectId(rs.getInt("project_id"));
                progress.setStepId(rs.getInt("step_id"));
                progress.setCompleted(rs.getBoolean("is_completed"));
                if (rs.getDate("completion_date") != null) {
                    progress.setCompletionDate(rs.getDate("completion_date").toLocalDate());
                }
                progress.setNotes(rs.getString("notes"));
                // Set transient fields
                progress.setStepName(rs.getString("step_name"));
                progress.setActionType(rs.getString("action_type"));
                progress.setActionConfigJson(rs.getString("action_config_json")); // Add this line
                progressList.add(progress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return progressList;
    }

    @Override
    public void updateProgress(ProjectStepProgress progress) {
        String sql = "UPDATE ProjectStepProgress SET is_completed = ?, completion_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, progress.isCompleted());
            if (progress.getCompletionDate() != null) {
                pstmt.setDate(2, Date.valueOf(progress.getCompletionDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setString(3, progress.getNotes());
            pstmt.setInt(4, progress.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createInitialProgressForProject(int projectId, int applicationId) {
        String sql = "INSERT INTO ProjectStepProgress (project_id, step_id) " +
                     "SELECT ?, id FROM ApplicationSteps WHERE application_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, applicationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<GlobalProgressEntry> getGlobalProgress(int applicationId) {
        List<GlobalProgressEntry> globalProgress = new ArrayList<>();
        String sql = """
            SELECT
                c.code AS customerId,
                c.name AS customerName,
                c.afm AS customerAfm,
                cp.id AS projectId,
                cp.project_name AS projectName,
                aps.id AS currentStepId,
                aps.step_name AS currentStepName,
                psp.is_completed AS isCompleted,
                psp.completion_date AS completionDate,
                psp.notes AS notes
            FROM
                CustomerProjects cp
            JOIN
                Customers c ON cp.customer_id = c.code
            JOIN
                ProjectStepProgress psp ON cp.id = psp.project_id
            JOIN
                ApplicationSteps aps ON psp.step_id = aps.id
            WHERE
                cp.application_id = ?
            ORDER BY
                c.name, cp.project_name, aps.step_order
            """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                GlobalProgressEntry entry = new GlobalProgressEntry();
                entry.setCustomerId(rs.getInt("customerId"));
                entry.setCustomerName(rs.getString("customerName"));
                entry.setCustomerAfm(rs.getString("customerAfm"));
                entry.setProjectId(rs.getInt("projectId"));
                entry.setProjectName(rs.getString("projectName"));
                entry.setCurrentStepId(rs.getInt("currentStepId"));
                entry.setCurrentStepName(rs.getString("currentStepName"));
                entry.setCompleted(rs.getBoolean("isCompleted"));
                if (rs.getDate("completionDate") != null) {
                    entry.setCompletionDate(rs.getDate("completionDate").toLocalDate());
                }
                entry.setNotes(rs.getString("notes"));
                globalProgress.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return globalProgress;
    }

    @Override
    public List<CustomerProjectSummary> getGlobalProgressSummary(int applicationId) {
        List<CustomerProjectSummary> summaries = new ArrayList<>();
        String sql = """
            WITH RankedSteps AS (
                SELECT 
                    psp.project_id,
                    aps.step_name,
                    psp.completion_date,
                    -- Rank pending steps by order, putting completed ones last
                    ROW_NUMBER() OVER (PARTITION BY psp.project_id ORDER BY CASE WHEN psp.is_completed = 0 THEN aps.step_order ELSE 2147483647 END ASC) as pending_rank,
                    -- Rank completed steps by date, putting pending ones last
                    ROW_NUMBER() OVER (PARTITION BY psp.project_id ORDER BY CASE WHEN psp.is_completed = 1 THEN psp.completion_date ELSE '1900-01-01' END DESC, aps.step_order DESC) as completed_rank
                FROM 
                    ProjectStepProgress psp
                JOIN 
                    ApplicationSteps aps ON psp.step_id = aps.id
            ),
            ProjectAggregates AS (
                SELECT
                    cp.id AS projectId,
                    cp.customer_id AS customerId,
                    c.name AS customerName,
                    c.afm AS customerAfm,
                    cp.project_name AS projectName,
                    COUNT(psp.id) AS totalSteps,
                    SUM(CASE WHEN psp.is_completed = 1 THEN 1 ELSE 0 END) AS completedSteps
                FROM
                    CustomerProjects cp
                JOIN
                    Customers c ON cp.customer_id = c.code
                JOIN
                    ProjectStepProgress psp ON cp.id = psp.project_id
                WHERE
                    cp.application_id = ?
                GROUP BY
                    cp.id, cp.customer_id, c.name, c.afm, cp.project_name
            )
            SELECT
                pa.customerId,
                pa.customerName,
                pa.customerAfm,
                pa.projectId,
                pa.projectName,
                pa.totalSteps,
                pa.completedSteps,
                next_step.step_name AS nextPendingStepName,
                last_step.step_name AS lastCompletedStepName,
                last_step.completion_date AS lastCompletionDate
            FROM
                ProjectAggregates pa
            LEFT JOIN
                RankedSteps next_step ON pa.projectId = next_step.project_id AND next_step.pending_rank = 1
            LEFT JOIN
                RankedSteps last_step ON pa.projectId = last_step.project_id AND last_step.completed_rank = 1
            ORDER BY
                pa.customerName, pa.projectName;
            """;
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, applicationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CustomerProjectSummary summary = new CustomerProjectSummary();
                summary.setCustomerId(rs.getInt("customerId"));
                summary.setCustomerName(rs.getString("customerName"));
                summary.setCustomerAfm(rs.getString("customerAfm"));
                summary.setProjectId(rs.getInt("projectId"));
                summary.setProjectName(rs.getString("projectName"));
                summary.setTotalSteps(rs.getInt("totalSteps"));
                summary.setCompletedSteps(rs.getInt("completedSteps"));
                summary.setNextPendingStepName(rs.getString("nextPendingStepName"));
                if (rs.getDate("lastCompletionDate") != null) {
                    summary.setLastCompletionDate(rs.getDate("lastCompletionDate").toLocalDate());
                }
                summary.setLastCompletedStepName(rs.getString("lastCompletedStepName"));
                summaries.add(summary);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}