package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.SimplyStatusDao;
import org.easytech.pelatologio.models.SimplyStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimplyStatusDaoImpl implements SimplyStatusDao {

    private final HikariDataSource dataSource;

    public SimplyStatusDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void updateSimplyStatus(int appLoginId, String columnName, boolean newVal) {
        String query = "UPDATE SimplySetupProgress SET " + columnName + " = ? WHERE app_login_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, newVal);
            pstmt.setInt(2, appLoginId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSimplyStatusYears(int appLoginId, String selectedYear) {
        String query = "UPDATE SimplySetupProgress SET years = ? WHERE app_login_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, selectedYear);
            pstmt.setInt(2, appLoginId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean getSimpyStatus(int appLoginId, String columnName) {
        String query = "SELECT " + columnName + " FROM SimplySetupProgress WHERE app_login_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appLoginId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getSimplyYears(int appLoginId) {
        String query = "SELECT years FROM SimplySetupProgress WHERE app_login_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appLoginId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("years");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public List<SimplyStatus> getAllSimplyStatus() {
        List<SimplyStatus> simplyStatuses = new ArrayList<>();
        String query = "SELECT " +
                "    s.id AS SetupID, " +
                "    s.app_login_id AS appLoginId, " +
                "    s.stock, " +
                "    s.register, " +
                "    s.auth, " +
                "    s.accept, " +
                "    s.mail, " +
                "    s.param, " +
                "    s.mydata, " +
                "    s.delivered, " +
                "    s.paid, " +
                "    s.years, " +
                "    cl.Username, " +
                "    c.name AS CustomerName, " +
                "    c.code AS CustomerCode " +
                "FROM " +
                "    SimplySetupProgress s " +
                "INNER JOIN " +
                "    CustomerLogins cl ON s.app_login_id = cl.LoginID " +
                "INNER JOIN " +
                "    Customers c ON cl.CustomerID = c.code " +
                "ORDER BY " +
                "    s.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("SetupID");
                int appLoginId = resultSet.getInt("appLoginId");
                boolean stock = resultSet.getBoolean("stock");
                boolean register = resultSet.getBoolean("register");
                boolean auth = resultSet.getBoolean("auth");
                boolean accept = resultSet.getBoolean("accept");
                boolean mail = resultSet.getBoolean("mail");
                boolean param = resultSet.getBoolean("param");
                boolean mydata = resultSet.getBoolean("mydata");
                boolean delivered = resultSet.getBoolean("delivered");
                boolean paid = resultSet.getBoolean("paid");
                String years = resultSet.getString("years");
                String username = resultSet.getString("Username");
                String customerName = resultSet.getString("CustomerName");
                int customerCode = resultSet.getInt("CustomerCode");

                SimplyStatus status = new SimplyStatus(id,appLoginId,stock, register,auth,accept, mail,param,mydata,delivered,paid,years,customerCode, customerName, username);
                simplyStatuses.add(status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return simplyStatuses;
    }

    @Override
    public void addSimplySetupProgress(int loginId) {
        String query = "INSERT INTO SimplySetupProgress (app_login_id, stock, register, auth, accept, mail, param, mydata, delivered, paid, years) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, loginId);
            pstmt.setBoolean(2, false);
            pstmt.setBoolean(3, false);
            pstmt.setBoolean(4, false);
            pstmt.setBoolean(5, false);
            pstmt.setBoolean(6, false);
            pstmt.setBoolean(7, false);
            pstmt.setBoolean(8, false);
            pstmt.setBoolean(9, false);
            pstmt.setBoolean(10, false);
            pstmt.setString(11, ""); // Default empty string for years
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}