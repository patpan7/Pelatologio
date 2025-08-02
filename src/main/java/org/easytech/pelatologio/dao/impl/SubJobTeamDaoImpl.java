package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.SubJobTeamDao;
import org.easytech.pelatologio.models.SubJobTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubJobTeamDaoImpl implements SubJobTeamDao {

    private final HikariDataSource dataSource;

    public SubJobTeamDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<SubJobTeam> getSubJobTeams(int id) {
        List<SubJobTeam> jobTeams = new ArrayList<>();
        String query = "SELECT * FROM SubJobTeams WHERE jobTeamId = ? ORDER BY name ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id); // ✅ Τώρα εκτός try(...) declaration block

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobTeams.add(new SubJobTeam(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("jobTeamId")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jobTeams;
    }


    @Override
    public void saveSubJobTeam(SubJobTeam subJobTeam) {
        String query = "INSERT INTO SubJobTeams (name) VALUES (?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, subJobTeam.getName());
            stmt.setInt (2, subJobTeam.getJobTeamId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSubJobTeam(SubJobTeam subJobTeam) {
        String query = "UPDATE SubJobTeams SET name = ?, jobTeamId = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, subJobTeam.getName());
            stmt.setInt(2, subJobTeam.getJobTeamId());
            stmt.setInt(3, subJobTeam.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubJobTeam(int id) {
        String query = "DELETE FROM SubJobTeams WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
