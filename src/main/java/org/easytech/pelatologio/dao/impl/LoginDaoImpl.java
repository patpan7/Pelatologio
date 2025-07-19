package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.LoginDao;
import org.easytech.pelatologio.models.Logins;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginDaoImpl implements LoginDao {

    private final HikariDataSource dataSource;

    public LoginDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Logins> getLogins(int customerId, int i) {
        List<Logins> dataList = new ArrayList<>();
        String query = "SELECT * FROM CustomerLogins WHERE CustomerID = ? and ApplicationID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, i);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                Logins data = new Logins();
                data.setId(resultSet.getInt("LoginID"));
                data.setUsername(resultSet.getString("Username"));
                data.setPassword(resultSet.getString("Password"));
                data.setTag(resultSet.getString("Tag"));
                data.setPhone(resultSet.getString("Phone"));
                data.setCustomerId(resultSet.getInt("CustomerID"));
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    @Override
    public int addLogin(int code, Logins newLogin, int i) {
        int loginId = 0;
        String sql = "INSERT INTO CustomerLogins (CustomerID, ApplicationID, username, password, tag, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, code);
            pstmt.setInt(2, i);
            pstmt.setString(3, newLogin.getUsername());
            pstmt.setString(4, newLogin.getPassword());
            pstmt.setString(5, newLogin.getTag());
            pstmt.setString(6, newLogin.getPhone());

            int rowsInserted = pstmt.executeUpdate(); // ✅ μόνο μία φορά

            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του login ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        loginId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του login απέτυχε.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loginId;
    }

    @Override
    public boolean updateLogin(Logins updatedLogin) {
        String query = "UPDATE CustomerLogins SET username = ?, password = ?, tag = ?, phone = ? WHERE LoginID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedLogin.getUsername());
            pstmt.setString(2, updatedLogin.getPassword());
            pstmt.setString(3, updatedLogin.getTag());
            pstmt.setString(4, updatedLogin.getPhone());
            pstmt.setInt(5, updatedLogin.getId());
            pstmt.executeUpdate();
            if( pstmt.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void deleteLogin(int id) {
        String query1 = "DELETE FROM SimplySetupProgress WHERE app_login_id = ?";
        String query2 = "DELETE FROM CustomerLogins WHERE LoginID = ?";
        try (Connection conn = getConnection()){
            try (
                PreparedStatement pstmt1 = conn.prepareStatement(query1);
                PreparedStatement pstmt2 = conn.prepareStatement(query2);

            ){
                pstmt1.setInt(1, id);
                pstmt1.executeUpdate();
                pstmt2.setInt(1, id);
                pstmt2.executeUpdate();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLoginsCount(int appId) {
        String query = "SELECT COUNT(DISTINCT customerId) FROM CustomerLogins WHERE ApplicationID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}