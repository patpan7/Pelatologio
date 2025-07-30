package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.JobTeamDao;
import org.easytech.pelatologio.models.JobTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JobTeamDaoImpl implements JobTeamDao {

    private final HikariDataSource dataSource;

    public JobTeamDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<JobTeam> getJobTeams() {
        List<JobTeam> jobTeams = new ArrayList<>();
        String query = "SELECT * FROM JobTeams ORDER BY name ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                jobTeams.add(new JobTeam(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobTeams;
    }

    @Override
    public void saveJobTeam(JobTeam jobTeam) {
        String query = "INSERT INTO JobTeams (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, jobTeam.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateJobTeam(JobTeam jobTeam) {
        String query = "UPDATE JobTeams SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, jobTeam.getName());
            stmt.setInt(2, jobTeam.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteJobTeam(int id) {
        String query = "DELETE FROM JobTeams WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
