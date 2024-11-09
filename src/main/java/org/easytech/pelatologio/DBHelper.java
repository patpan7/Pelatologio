package org.easytech.pelatologio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DBHelper {
    String server;
    String user;
    String pass;

    public Connection getConnection() throws SQLException {
        try {
            // Φόρτωση του JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver not found");
        }
        server = AppSettings.getInstance().server;
        user = AppSettings.getInstance().dbUser;
        pass = AppSettings.getInstance().dbPass;

        String url = "jdbc:sqlserver://" + server + ";databaseName=Pelatologio;user=" + user + ";password=" + pass + ";encrypt=false;";
        return DriverManager.getConnection(url);
    }


    public static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Μέθοδος για να πάρεις δεδομένα
    public List<Customer> getCustomers() throws SQLException {
        List<Customer> dataList = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            String query = "SELECT * FROM customers order by code desc";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Customer data = new Customer();
                data.setCode(resultSet.getInt("code"));
                data.setName(resultSet.getString("name"));
                data.setTitle(resultSet.getString("title"));
                data.setJob(resultSet.getString("job"));
                data.setAfm(resultSet.getString("afm"));
                data.setPhone1(resultSet.getString("phone1"));
                data.setPhone2(resultSet.getString("phone2"));
                data.setMobile(resultSet.getString("mobile"));
                data.setAddress(resultSet.getString("address"));
                data.setTown(resultSet.getString("town"));
                data.setEmail(resultSet.getString("email"));
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                dataList.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public boolean isAfmExists(String afm) {
        String query = "SELECT COUNT(*) FROM Customers WHERE afm = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, afm);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertCustomer(String name, String title, String job, String afm, String phone1,
                               String phone2, String mobile, String address,
                               String town, String email, String manager, String managerPhone) {
        String insertQuery = "INSERT INTO Customers (name, title, job, afm, phone1, phone2, mobile, address, town, email, manager, managerPhone) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, job);
            pstmt.setString(4, afm);
            pstmt.setString(5, phone1);
            pstmt.setString(6, phone2);
            pstmt.setString(7, mobile);
            pstmt.setString(8, address);
            pstmt.setString(9, town);
            pstmt.setString(10, email);
            pstmt.setString(11, manager);
            pstmt.setString(12, managerPhone);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του πελάτη ήταν επιτυχής.");
                closeConnection(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String email, String manager, String managerPhone) {
        String sql = "UPDATE customers SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, email = ?, manager = ?, managerPhone = ? WHERE code = ?";

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, job);
            pstmt.setString(4, afm);
            pstmt.setString(5, phone1);
            pstmt.setString(6, phone2);
            pstmt.setString(7, mobile);
            pstmt.setString(8, address);
            pstmt.setString(9, town);
            pstmt.setString(10, email);
            pstmt.setString(11, manager);
            pstmt.setString(12, managerPhone);
            pstmt.setInt(13, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε πελάτης με αυτό το κωδικό.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String checkCustomerLock(int code, String appUser){
        String checkLockQuery = "SELECT locked_by FROM Customers WHERE code = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement checkLockStmt = conn.prepareStatement(checkLockQuery);
            checkLockStmt.setInt(1, code);
            ResultSet rs = checkLockStmt.executeQuery();

            if (rs.next()) {
                String lockedBy = rs.getString("locked_by");
                if (lockedBy != null && !lockedBy.equals(appUser)) {
                    // Εμφανίζεις μήνυμα ότι η εγγραφή είναι κλειδωμένη
                    //System.out.println("Η εγγραφή αυτή είναι ήδη κλειδωμένη από χρήστη "+lockedBy);
                    return "Η εγγραφή αυτή είναι ήδη κλειδωμένη από τον χρήστη "+lockedBy;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "unlocked";
    }

    public void customerLock(int code, String appUser){
        String checkLockQuery = "UPDATE Customers SET locked_by = ? WHERE code = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setString(1, appUser);
            lockStmt.setInt(2, code);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerUnlock(int code){
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE code = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setInt(1, code);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerUnlockAll(String appUser){
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE locked_by = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setString(1, appUser);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


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
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public void addLogin(int code, Logins newLogin, int i) {
        String sql = "INSERT INTO CustomerLogins (CustomerID, ApplicationID, username, password, tag, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, code);
            pstmt.setInt(2, i);
            pstmt.setString(3, newLogin.getUsername());
            pstmt.setString(4, newLogin.getPassword());
            pstmt.setString(5, newLogin.getTag());
            pstmt.setString(6, newLogin.getPhone());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogin(Logins updatedLogin) {
        String query = "UPDATE CustomerLogins SET username = ?, password = ?, tag = ?, phone = ? WHERE LoginID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedLogin.getUsername());
            pstmt.setString(2, updatedLogin.getPassword());
            pstmt.setString(3, updatedLogin.getTag());
            pstmt.setString(4, updatedLogin.getPhone());
            pstmt.setInt(5, updatedLogin.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLogin(int id) {
        String query = "DELETE FROM CustomerLogins WHERE LoginID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void syncMegasoft() {
        String SQL = "MERGE INTO [Pelatologio].[dbo].[Customers] AS target\n" +
                "USING (\n" +
                "    SELECT TOP 100 PERCENT\n" +
                "        COALESCE(BusinessTitle, '') AS BusinessTitle,\n" +
                "        COALESCE(Company, '') AS name, \n" +
                "        COALESCE(Business, '') AS job, \n" +
                "        COALESCE(Afm, '0000000000') AS afm,  -- Αντικαθιστούμε το κενό με placeholder\n" +
                "        COALESCE(Tel_1, '') AS phone1, \n" +
                "        COALESCE(Tel_2, '') AS phone2, \n" +
                "        COALESCE(Mobile, '') AS mobile, \n" +
                "        COALESCE(Address_1, '') AS address, \n" +
                "        COALESCE(City_1, '') AS city1, \n" +
                "        COALESCE(Email, '') AS mail1\n" +
                "    FROM Megasoft.dbo.E2_Emp065_24\n" +
                "    INNER JOIN Megasoft.dbo.E2_Emp001_24 ON Megasoft.dbo.E2_Emp001_24.pelid = Megasoft.dbo.E2_Emp065_24.pelid\n" +
                "    WHERE Afm IS NOT NULL AND Afm != ''\n" +
                "    ORDER BY Kwd  -- Ταξινόμηση βάσει του Kwd για διατήρηση της σειράς\n" +
                ") AS source\n" +
                "ON (target.afm = source.afm)\n" +
                "WHEN MATCHED THEN\n" +
                "    UPDATE SET\n" +
                "        target.name = source.name,\n" +
                "        target.title = source.BusinessTitle,\n" +
                "        target.job = source.job,\n" +
                "        target.phone1 = source.phone1,\n" +
                "        target.phone2 = source.phone2,\n" +
                "        target.mobile = source.mobile,\n" +
                "        target.address = source.address,\n" +
                "        target.town = source.city1,\n" +
                "        target.email = source.mail1\n" +
                "WHEN NOT MATCHED THEN\n" +
                "    INSERT (name, title, job, afm, phone1, phone2, mobile, address, town, email)\n" +
                "    VALUES (source.name, source.BusinessTitle, source.job, source.afm, source.phone1, source.phone2, source.mobile, source.address, source.city1, source.mail1);";

        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(SQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
