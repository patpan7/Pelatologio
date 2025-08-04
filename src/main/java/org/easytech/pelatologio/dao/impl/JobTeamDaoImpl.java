package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.JobTeamDao;
import org.easytech.pelatologio.models.JobTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public int getParentTeamIdBySubTeamId(int subTeamId) {
        String sql = "SELECT jobteamid FROM subjobteams WHERE id = ?"; // Προσαρμόστε τα ονόματα
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subTeamId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("jobteamid"); // Επιστρέφει το jobteamid
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Επιστρέφει 0 αν δεν βρεθεί
    }

    @Override
    public Map<String, Integer> getCustomerCountPerJobTeam() {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT jt.name, COUNT(c.code) as customer_count " +
                     "FROM JobTeams jt " +
                     "JOIN SubJobTeams sjt ON jt.id = sjt.jobteamid " +
                     "JOIN Customers c ON sjt.id = c.subjobteam " +
                     "GROUP BY jt.name";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("name"), rs.getInt("customer_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public Map<String, Integer> getCustomerCountPerSubJobTeam(int jobTeamId) {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT sjt.name, COUNT(c.code) as customer_count " +
                     "FROM SubJobTeams sjt " +
                     "JOIN Customers c ON sjt.id = c.subjobteam " +
                     "WHERE sjt.jobteamid = ? " +
                     "GROUP BY sjt.name";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobTeamId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("name"), rs.getInt("customer_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public int getJobTeamIdByName(String teamName) {
        String sql = "SELECT id FROM JobTeams WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teamName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
