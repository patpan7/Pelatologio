package org.easytech.pelatologio;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

            String query = "SELECT * FROM customers where code > 0 order by code desc";
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
                data.setPostcode(resultSet.getString("postcode"));
                data.setEmail(resultSet.getString("email"));
                data.setEmail2(resultSet.getString("email2"));
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                data.setNotes(resultSet.getString("notes") != null ? resultSet.getString("notes").trim() : "");
                data.setAccId(resultSet.getInt("accId"));
                data.setAccName1(resultSet.getString("accName1"));
                data.setAccEmail1(resultSet.getString("accEmail1"));
                data.setRecommendation(resultSet.getString("recommendation"));
                data.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                data.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                data.setActive(resultSet.getBoolean("isActive"));

                dataList.add(data);
            }
            closeConnection(conn);
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
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAfmExistsMegasoft(String afm) {
        String query = "SELECT COUNT(*) FROM Megasoft.dbo.E2_Emp001_25 WHERE afm = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, afm);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertCustomer(String name, String title, String job, String afm, String phone1,
                              String phone2, String mobile, String address,
                              String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, String recommendation, String balance, String balanceReason) {
        // Prepare the SQL query for inserting a new customer
        String insertQuery = "INSERT INTO Customers (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, recommendation, balance, balanceReason, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int newCustomerId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            Integer accId1 = null;
            if (accId != 0) {
                accId1 = accId;
            }
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, job);
            pstmt.setString(4, afm);
            pstmt.setString(5, phone1);
            pstmt.setString(6, phone2);
            pstmt.setString(7, mobile);
            pstmt.setString(8, address);
            pstmt.setString(9, town);
            pstmt.setString(10, postcode);
            pstmt.setString(11, email);
            pstmt.setString(12, email2);
            pstmt.setString(13, manager);
            pstmt.setString(14, managerPhone);
            pstmt.setString(15, notes);
            // Χειρισμός του accId (αν είναι null, βάζουμε SQL NULL)
            if (accId == 0) {
                pstmt.setNull(16, Types.INTEGER);
            } else {
                pstmt.setInt(16, accId);
            }
            pstmt.setString(17, accName1);
            pstmt.setString(18, accEmail1);
            pstmt.setString(19, recommendation);
            pstmt.setString(20, balance);
            pstmt.setString(21, balanceReason);
            pstmt.setBoolean(22, true); // Ορισμός του isActive σε true


            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του πελάτη ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCustomerId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του πελάτη απέτυχε.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του πελάτη: " + e.getMessage());
        }

        return newCustomerId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    public void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, String recommendation, String balance, String balanceReason, boolean isActive) {
        String sql = "UPDATE customers SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, postcode = ?, email = ?, email2 = ?,manager = ?, managerPhone = ?, notes = ?, accId = ?, accName1 = ?, accEmail1 = ?, recommendation = ?, balance = ?, balanceReason = ?, isActive = ? WHERE code = ?";

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
            pstmt.setString(10, postcode);
            pstmt.setString(11, email);
            pstmt.setString(12, email2);
            pstmt.setString(13, manager);
            pstmt.setString(14, managerPhone);
            pstmt.setString(15, notes);
            // Χειρισμός του accId (αν είναι null, βάζουμε SQL NULL)
            if (accId == 0) {
                pstmt.setNull(16, Types.INTEGER);
            } else {
                pstmt.setInt(16, accId);
            }
            pstmt.setString(17, accName1);
            pstmt.setString(18, accEmail1);
            pstmt.setString(19, recommendation);
            pstmt.setString(20, balance);
            pstmt.setString(21, balanceReason);
            pstmt.setBoolean(22, isActive); // Ορισμός του isActive σε true
            pstmt.setInt(23, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε πελάτης με αυτό το κωδικό.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public String checkCustomerLock(int code, String appUser) {
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
                    return "Η εγγραφή αυτή είναι ήδη κλειδωμένη από τον χρήστη " + lockedBy;
                }
            }
            closeConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "unlocked";
    }

    public void customerLock(int code, String appUser) {
        String checkLockQuery = "UPDATE Customers SET locked_by = ? WHERE code = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setString(1, appUser);
            lockStmt.setInt(2, code);
            lockStmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerUnlock(int code) {
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE code = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setInt(1, code);
            lockStmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerUnlockAll(String appUser) {
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE locked_by = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery);
            lockStmt.setString(1, appUser);
            lockStmt.executeUpdate();
            closeConnection(conn);
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
                data.setCustomerId(resultSet.getInt("CustomerID"));
                dataList.add(data);
            }
            closeConnection(conn);
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
            closeConnection(conn);
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
            closeConnection(conn);
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
            closeConnection(conn);
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
                "        COALESCE(zip_1, '') AS zip1, \n" +
                "        COALESCE(Email, '') AS mail1\n" +
                "    FROM Megasoft.dbo.E2_Emp065_25\n" +
                "    INNER JOIN Megasoft.dbo.E2_Emp001_25 ON Megasoft.dbo.E2_Emp001_25.pelid = Megasoft.dbo.E2_Emp065_25.pelid\n" +
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
                "        target.postcode = source.zip1,\n" +
                "        target.email = source.mail1\n" +
                "WHEN NOT MATCHED THEN\n" +
                "    INSERT (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email)\n" +
                "    VALUES (source.name, source.BusinessTitle, source.job, source.afm, source.phone1, source.phone2, source.mobile, source.address, source.city1, source.zip1, source.mail1);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CustomerAddress> getCustomerAddresses(int customerId) {
        String sql = "SELECT * FROM CustomerAddresses WHERE CustomerID = ?";
        List<CustomerAddress> customerAddresses = new ArrayList<>();


        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int addressId = rs.getInt("AddressID");
                String address = rs.getString("Address");
                String town = rs.getString("Town");
                String postcode = rs.getString("Postcode");
                String store = rs.getString("Store");

                CustomerAddress newCustomerAddress = new CustomerAddress();
                newCustomerAddress.setAddressId(addressId);
                newCustomerAddress.setAddress(address);
                newCustomerAddress.setTown(town);
                newCustomerAddress.setPostcode(postcode);
                newCustomerAddress.setStore(store);
                customerAddresses.add(newCustomerAddress);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace(); // Αν υπάρχει κάποιο σφάλμα, το εκτυπώνουμε
        }

        return customerAddresses;
    }

    public void addAddress(int code, CustomerAddress newCustomerAddress) {
        String sql = "INSERT INTO CustomerAddresses (CustomerID, Address, Town, Postcode, Store) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, code);
            statement.setString(2, newCustomerAddress.getAddress());
            statement.setString(3, newCustomerAddress.getTown());
            statement.setString(4, newCustomerAddress.getPostcode());
            statement.setString(5, newCustomerAddress.getStore());
            statement.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAddress(CustomerAddress updatedCustomerAddress) {
        String query = "UPDATE CustomerAddresses SET Address = ?, Town = ?, Postcode = ?, Store = ? WHERE AddressID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedCustomerAddress.getAddress());
            pstmt.setString(2, updatedCustomerAddress.getTown());
            pstmt.setString(3, updatedCustomerAddress.getPostcode());
            pstmt.setString(4, updatedCustomerAddress.getStore());
            pstmt.setInt(5, updatedCustomerAddress.getAddressId());
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAddress(int addressId) {
        String query = "DELETE FROM CustomerAddresses WHERE addressID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, addressId);
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasSubAddress(int code) {
        String query = "SELECT COUNT(*) FROM CustomerAddresses WHERE CustomerID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasApp(int code, int appId) {
        String query = "SELECT COUNT(*) FROM CustomerLogins WHERE CustomerID = ? and ApplicationID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.setInt(2, appId);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasDevice(int code) {
        String query = "SELECT COUNT(*) FROM Devices WHERE CustomerID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasTask(int code) {
        String query = "SELECT COUNT(*) FROM Tasks WHERE CustomerID = ? AND is_Completed = 0";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasSub(int code) {
        String query = "SELECT COUNT(*) FROM Subscriptions WHERE CustomerID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasOffer(int code) {
        String query = "SELECT COUNT(*) FROM Offers WHERE CustomerID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasOrders(int code) {
        String query = "SELECT COUNT(*) FROM Orders WHERE CustomerID = ? AND is_Completed = 0";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, code);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerDelete(int code) {
        String query = "DELETE FROM CustomerAddresses WHERE CustomerID = ?";
        String query2 = "DELETE FROM CustomerLogins WHERE CustomerID = ?";
        String query3 = "DELETE FROM Customers WHERE code = ?";
        String query4 = "UPDATE Devices set customerId = 0 WHERE customerId = ?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt1 = conn.prepareStatement(query);
                 PreparedStatement pstmt2 = conn.prepareStatement(query2);
                 PreparedStatement pstmt3 = conn.prepareStatement(query3);
                 PreparedStatement pstmt4 = conn.prepareStatement(query4)) {

                pstmt1.setInt(1, code);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, code);
                pstmt2.executeUpdate();

                pstmt3.setInt(1, code);
                pstmt3.executeUpdate();

                pstmt4.setInt(1, code);
                pstmt4.executeUpdate();
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Customer getSelectedCustomer(int customerId) {
        String query = "SELECT * FROM customers WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet resultSet = stmt.executeQuery();
            Customer data = null;
            if (resultSet.next()) {
                data = new Customer();
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
                data.setPostcode(resultSet.getString("postcode"));
                data.setEmail(resultSet.getString("email"));
                data.setEmail2(resultSet.getString("email2"));
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                data.setNotes(resultSet.getString("notes") != null ? resultSet.getString("notes").trim() : "");
                data.setAccId(resultSet.getInt("accId"));
                data.setAccName1(resultSet.getString("accName1"));
                data.setAccEmail1(resultSet.getString("accEmail1"));
                data.setRecommendation(resultSet.getString("recommendation"));
                data.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                data.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                data.setActive(resultSet.getBoolean("isActive"));
            }
            closeConnection(conn);
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Tasks> getAllTasks() {
        List<Tasks> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, t.is_ergent, t.is_wait, t.is_calendar, t.start_time, t.end_time, c.name " +
                "FROM Tasks t " +
                "LEFT JOIN Customers c ON t.customerId = c.code";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String category = resultSet.getString("category");
                String customerName = resultSet.getString("name");
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isCalendar = resultSet.getBoolean("is_calendar");
                LocalDateTime startTime = resultSet.getTimestamp("start_time") != null ? resultSet.getTimestamp("start_time").toLocalDateTime() : null;
                LocalDateTime endTime = resultSet.getTimestamp("end_time") != null ? resultSet.getTimestamp("end_time").toLocalDateTime() : null;


                Tasks task = new Tasks(id, title, description, dueDate, isCompleted, category, customerId, customerName, isErgent, isWait, isCalendar, startTime, endTime);
                tasks.add(task);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }


    public boolean completeTask(int taskId, boolean isCompleted) {
        String query = "UPDATE tasks SET is_completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isCompleted);
            stmt.setInt(2, taskId);
            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true;
            } else {
                closeConnection(conn);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean saveTask(Tasks tasks) {
        String query = "INSERT INTO Tasks (title, description, dueDate, is_completed, customerId, category, is_ergent, is_wait, is_calendar, start_time, end_time, snooze) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tasks.getTitle());
            stmt.setString(2, tasks.getDescription());
            stmt.setDate(3, Date.valueOf(tasks.getDueDate()));
            stmt.setBoolean(4, false);
            if (tasks.getCustomerId() != null) {
                stmt.setInt(5, tasks.getCustomerId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, tasks.getCategory());
            stmt.setBoolean(7, tasks.getErgent());
            stmt.setBoolean(8, tasks.getWait());
            stmt.setBoolean(9, tasks.getIsCalendar());
            stmt.setTimestamp(10, Timestamp.valueOf(tasks.getStartTime()));
            stmt.setTimestamp(11, Timestamp.valueOf(tasks.getEndTime()));
            stmt.setBoolean(12, false);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveTask(tasks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTask(Tasks tasks) {
        String query = "UPDATE Tasks SET title = ?, description = ?, dueDate = ?, is_Completed = ?, category = ?, customerId = ?, is_ergent = ?, is_wait = ?, is_calendar = ?, start_time = ?, end_time = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tasks.getTitle());
            stmt.setString(2, tasks.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(tasks.getDueDate()));
            stmt.setBoolean(4, tasks.getCompleted());
            stmt.setString(5, tasks.getCategory());
            if (tasks.getCustomerId() != null) {
                stmt.setInt(6, tasks.getCustomerId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setBoolean(7, tasks.getErgent());
            stmt.setBoolean(8, tasks.getWait());
            stmt.setBoolean(9, tasks.getIsCalendar());
            stmt.setTimestamp(10, Timestamp.valueOf(tasks.getStartTime()));
            stmt.setTimestamp(11, Timestamp.valueOf(tasks.getEndTime()));
            stmt.setInt(12, tasks.getId());


            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true;
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateTaskCalendar(Tasks tasks) {
        String query = "UPDATE Tasks SET is_calendar = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, tasks.getIsCalendar());
            stmt.setInt(2, tasks.getId());


            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM Tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
            closeConnection(conn);
        }
    }

    public void saveTaskCategory(TaskCategory newTaskCategory) {
        String query = "INSERT INTO TaskCategories (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, newTaskCategory.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                newTaskCategory.setId(rs.getInt(1));
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTaskCategory(int id) {
        String query = "DELETE FROM TaskCategories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTaskCategory(TaskCategory updatedCategory) {
        String query = "UPDATE TaskCategories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedCategory.getName());
            pstmt.setInt(2, updatedCategory.getId());
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TaskCategory> getAllTaskCategory() {
        List<TaskCategory> taskCategories = new ArrayList<>();
        String query = "SELECT * FROM TaskCategories";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                taskCategories.add(new TaskCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskCategories;
    }

    public int getTasksCount() {
        String query = "SELECT COUNT(*) FROM Tasks where is_Completed = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAppointmentsCount() {
        String query = "SELECT COUNT(*) AS total_appointments " +
                "FROM appointments " +
                "WHERE CAST(start_time AS DATE) = CAST(GETDATE() AS DATE);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Item> getItems() throws SQLException {
        List<Item> dataList = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            String query = "SELECT * FROM Items where id > 0 order by id desc";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Item item = new Item();
                item.setId(resultSet.getInt("id"));
                item.setName(resultSet.getString("name"));
                item.setDescription(resultSet.getString("description"));
                dataList.add(item);
            }
            closeConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public boolean isItemExists(String name) {
        String query = "SELECT COUNT(*) FROM Items WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertItem(String name, String description) {
        String insertQuery = "INSERT INTO Items (name, description) "
                + "VALUES (?, ?)";
        int newItemId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του είδους ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newItemId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του είδους απέτυχε.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του είδους: " + e.getMessage());
        }

        return newItemId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    public void updateItem(int code, String name, String description) {
        String sql = "UPDATE items SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε είδος με αυτό το κωδικό.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Device> getAllDevices() {
        List<Device> devices = new ArrayList<>();
        String query = "SELECT d.id, d.serial, d.description, d.rate, d.itemId, d.customerId, i.name AS itemName, c.name " +
                "FROM Devices d " +
                "LEFT JOIN Customers c ON d.customerId = c.code " +
                "LEFT JOIN Items i ON d.itemId = i.id " +
                "ORDER BY d.id DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String serial = resultSet.getString("serial").trim();
                String description = resultSet.getString("description").trim();
                String rate = resultSet.getString("rate").trim();
                Integer itemId = resultSet.getInt("itemId");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String item = resultSet.getString("itemName");
                String customerName = resultSet.getString("name");

                Device device = new Device(id, serial, description, rate, itemId, customerId, item, customerName);
                devices.add(device);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public boolean isSerialUnique(String serial) {
        String query = "SELECT COUNT(*) FROM Devices WHERE serial = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, serial);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Αν το COUNT είναι 0, ο σειριακός είναι μοναδικός
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Σε περίπτωση σφάλματος, θεωρούμε ότι δεν είναι μοναδικός
    }

    public boolean saveDevice(Device newDevice) {
        String query = "INSERT INTO Devices (serial, description, rate, itemId, customerId) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newDevice.getSerial());
            stmt.setString(2, newDevice.getDescription());
            stmt.setString(3, newDevice.getRate());
            stmt.setInt(4, newDevice.getItemId());
            if (newDevice.getCustomerId() != null) {
                stmt.setInt(5, newDevice.getCustomerId());
            } else {
                stmt.setInt(5, 0);
            }
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                closeConnection(conn);
                return true; // Εισαγωγή επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία εισαγωγής
    }

    public Boolean updateDevice(Device device) {
        String query = "UPDATE Devices SET serial = ?, description = ?, rate = ?, itemId = ?, customerId = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, device.getSerial());
            stmt.setString(2, device.getDescription());
            stmt.setString(3, device.getRate());
            stmt.setInt(4, device.getItemId());
            if (device.getCustomerId() != null) {
                stmt.setInt(5, device.getCustomerId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, device.getId());

            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    public List<Device> getCustomerDevices(int customerId) {
        List<Device> devices = new ArrayList<>();
        String query = "SELECT d.id, d.serial, d.description, d.rate, d.itemId, d.customerId, i.name AS itemName " +
                "FROM Devices d " +
                "LEFT JOIN Items i ON d.itemId = i.id " +
                "WHERE d.customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String serial = resultSet.getString("serial").trim();
                String description = resultSet.getString("description").trim();
                String rate = resultSet.getString("rate").trim();
                Integer itemId = resultSet.getInt("itemId");
                String item = resultSet.getString("itemName");

                Device device = new Device(id, serial, description, rate, itemId, customerId, item);
                devices.add(device);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public Boolean recoverDevice(int id) {
        String query = "UPDATE Devices SET customerId = 0 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);

            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    public Boolean deleteDevice(int id) {
        String checkQuery = "SELECT customerId FROM devices WHERE id = ?";
        String deleteQuery = "DELETE FROM devices WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {

            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("customerId");
                if (customerId == 0) {
                    deleteStmt.setInt(1, id);
                    deleteStmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSerialAssigned(String serial, int customerId) {
        String query = "SELECT customerId FROM Devices WHERE serial = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, serial);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int existingCustomer = rs.getInt("customerId");
                if (existingCustomer != 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean assignDevice(String serial, int customerId) {
        String query = "UPDATE Devices SET customerId = ? WHERE serial = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, serial);

            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    public List<String> getRates() {
        List<String> rates = new ArrayList<>();
        String query = "SELECT DISTINCT(rate) FROM Devices ORDER BY rate ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String rate = resultSet.getString("RATE").trim();
                rates.add(rate);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rates;
    }

    public int getLoginsCount(int appId) {
        String query = "SELECT COUNT(DISTINCT customerId) FROM CustomerLogins WHERE ApplicationID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            closeConnection(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Tasks> getAllCustomerTasks(int customerCode) {
        List<Tasks> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, t.is_ergent, t.is_wait, t.is_calendar, t.start_time, t.end_time, c.name " +
                "FROM Tasks t " +
                "LEFT JOIN Customers c ON t.customerId = c.code " +
                "WHERE t.customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String category = resultSet.getString("category");
                boolean isErgent = resultSet.getBoolean("is_ergent");
                boolean isWait = resultSet.getBoolean("is_wait");
                String customerName = resultSet.getString("name");
                boolean isCalendar = resultSet.getBoolean("is_calendar");
                LocalDateTime startTime = resultSet.getObject("start_time", LocalDateTime.class);
                LocalDateTime endTime = resultSet.getObject("end_time", LocalDateTime.class);

                Tasks task = new Tasks(id, title, description, dueDate, isCompleted, category, customerId, customerName, isErgent, isWait, isCalendar, startTime, endTime);
                tasks.add(task);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public List<Accountant> getAccountants() {
        List<Accountant> accountants = new ArrayList<>();
        String query = "SELECT * FROM Accountants";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String mobile = rs.getString("mobile");
                String email = rs.getString("email");
                String erganiEmail = rs.getString("erganiEmail");
                Accountant accountant = new Accountant(id, name, phone, mobile, email, erganiEmail);
                accountants.add(accountant);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountants;
    }

    public Accountant getSelectedAccountant(int accountantId) {
        String query = "SELECT * FROM accountants WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accountantId);
            ResultSet resultSet = stmt.executeQuery();
            Accountant data = null;
            if (resultSet.next()) {
                data = new Accountant();
                data.setId(resultSet.getInt("id"));
                data.setName(resultSet.getString("name"));
                data.setPhone(resultSet.getString("phone"));
                data.setMobile(resultSet.getString("mobile"));
                data.setEmail(resultSet.getString("email"));
            }
            closeConnection(conn);
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertAccountant(String name, String phone, String mobile, String email, String erganiEmail) {
        String insertQuery = "INSERT INTO Accountants (name, phone, mobile, email, erganiEmail) "
                + "VALUES (?, ?, ?, ?, ?)";
        int newCustomerId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, mobile);
            pstmt.setString(4, email);
            pstmt.setString(5, erganiEmail);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του λογιστή ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCustomerId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του λογιστή απέτυχε.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του λογιστή: " + e.getMessage());
        }

        return newCustomerId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    public void updateAccountant(int code, String name, String phone, String mobile, String email, String erganiEmail) {
        String sql = "UPDATE accountants SET name = ?, phone = ?, mobile = ?, email = ?, erganiEmail = ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, mobile);
            pstmt.setString(4, email);
            pstmt.setString(5, erganiEmail);
            pstmt.setInt(6, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε πελάτης με αυτό το κωδικό.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Customer> getCustomersByAcc(int accId) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE accId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accId);
            ResultSet resultSet = stmt.executeQuery();
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
                data.setPostcode(resultSet.getString("postcode"));
                data.setEmail(resultSet.getString("email"));
                data.setEmail2(resultSet.getString("email2"));
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                data.setNotes(resultSet.getString("notes"));
                data.setAccId(resultSet.getInt("accId"));
                data.setAccName1(resultSet.getString("accName1"));
                data.setAccEmail1(resultSet.getString("accEmail1"));
                data.setRecommendation(resultSet.getString("recommendation"));
                data.setBalance(resultSet.getString("balance"));
                data.setBalanceReason(resultSet.getString("balanceReason"));
                customers.add(data);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public List<String> getRecomedations() {
        List<String> recommendations = new ArrayList<>();
        String query = "SELECT DISTINCT(recommendation) FROM Customers WHERE recommendation IS NOT NULL ORDER BY recommendation ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                recommendations.add(resultSet.getString("recommendation"));
            }
            closeConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recommendations;
    }

    public List<SubsCategory> getAllSubsCategory() {
        List<SubsCategory> subsCategories = new ArrayList<>();
        String query = "SELECT * FROM subsCategories";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                subsCategories.add(new SubsCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subsCategories;

    }

    public List<Subscription> getAllSubs(LocalDate fromDate, LocalDate toDate) {
        List<Subscription> subs = new ArrayList<>();
        String query = "SELECT s.id, s.title, s.endDate, s.customerId, s.subCatId, i.name AS catName, s.note, s.price, s.sended, c.name  " +
                "FROM Subscriptions s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "LEFT JOIN SubsCategories i ON s.subCatId = i.id " +
                "WHERE s.endDate BETWEEN ? AND ? " +
                "ORDER BY s.endDate ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                LocalDate endDate = resultSet.getDate("endDate").toLocalDate();
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer categoryId = resultSet.getObject("subCatId", Integer.class);
                String category = resultSet.getString("catName");
                String note = resultSet.getString("note");
                String price = resultSet.getString("price");
                String customerName = resultSet.getString("name");
                String sended = resultSet.getString("sended");

                Subscription sub = new Subscription(id, title, endDate, customerId, categoryId, price, note, sended);
                sub.setCustomerName(customerName);
                sub.setCategory(category);
                subs.add(sub);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subs;

    }

    public void deleteSubsCategory(int id) {
        String query = "DELETE FROM SubsCategories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubsCategory(SubsCategory updatedSubsCategory) {
        String query = "UPDATE SubsCategories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedSubsCategory.getName());
            pstmt.setInt(2, updatedSubsCategory.getId());
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveSubsCategory(SubsCategory newSubsCategory) {
        String query = "INSERT INTO SubsCategories (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, newSubsCategory.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                newSubsCategory.setId(rs.getInt(1));
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean saveSub(Subscription newSub) {
        String query = "INSERT INTO Subscriptions (title, endDate, note, subCatId, customerId, price, sended) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newSub.getTitle());
            stmt.setDate(2, Date.valueOf(newSub.getEndDate()));
            stmt.setString(3, newSub.getNote());
            stmt.setInt(4, newSub.getCategoryId());
            stmt.setInt(5, newSub.getCustomerId());
            stmt.setString(6, newSub.getPrice());
            stmt.setString(7, newSub.getSended());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveSub(newSub);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateSub(Subscription sub) {
        String query = "UPDATE Subscriptions SET title = ?, endDate = ?, note = ?, subCatId = ?, customerId = ?, price = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, sub.getTitle());
            stmt.setDate(2, Date.valueOf(sub.getEndDate()));
            stmt.setString(3, sub.getNote());
            stmt.setInt(4, sub.getCategoryId());
            stmt.setInt(5, sub.getCustomerId());
            stmt.setString(6, sub.getPrice());
            stmt.setInt(7, sub.getId());

            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getErganiEmail(int customerId) {
        String query = "SELECT a.erganiEmail " +
                "FROM customers c " +
                "JOIN accountants a ON c.accid = a.id " +
                "WHERE c.code = ?";
        String title = "";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                title = resultSet.getString("erganiEmail");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return title;
    }

    public boolean updateErganiEmail(int customerId, String emailAcc) {
        String query = "UPDATE ac " +
                "SET ac.erganiEmail = ? " +
                "FROM accountants ac " +
                "INNER JOIN customers c ON c.accid = ac.id " +
                "WHERE c.code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, emailAcc);
            stmt.setInt(2, customerId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // Επιστρέφει true αν ενημερώθηκε τουλάχιστον 1 γραμμή

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Σε περίπτωση σφάλματος
    }

    public boolean hasAccountant(int customerId) {
        String query = "SELECT accid FROM Customers WHERE code = ? ";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            closeConnection(conn);
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Subscription> getAllCustomerSubs(int customerCode) {
        List<Subscription> subs = new ArrayList<>();
        String query = "SELECT s.id, s.title, s.endDate, s.customerId, s.subCatId, i.name AS catName, s.note, s.price, s.sended, c.name  " +
                "FROM Subscriptions s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "LEFT JOIN SubsCategories i ON s.subCatId = i.id " +
                "WHERE s.customerId = ? " +
                "ORDER BY s.endDate ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                LocalDate endDate = resultSet.getDate("endDate").toLocalDate();
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer categoryId = resultSet.getObject("subCatId", Integer.class);
                String category = resultSet.getString("catName");
                String note = resultSet.getString("note");
                String price = resultSet.getString("price");
                String customerName = resultSet.getString("name");
                String sended = resultSet.getString("sended");

                Subscription sub = new Subscription(id, title, endDate, customerId, categoryId, price, note, sended);
                sub.setCustomerName(customerName);
                sub.setCategory(category);
                subs.add(sub);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subs;
    }

    public void deleteSub(int id) {
        String query = "DELETE FROM Subscriptions WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void renewSub(int id, int yearsToAdd) {
        String updateQuery = "UPDATE Subscriptions SET endDate = DATEADD(YEAR, ?, endDate) WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setInt(1, yearsToAdd);
            stmt.setInt(2, id); // Το ID του συμβολαίου που ανανεώνεται

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Ημερομηνία λήξης ενημερώθηκε επιτυχώς!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Offer> getAllOffers() {
        List<Offer> offers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, s.is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                String description = resultSet.getString("description").trim();
                String hours = resultSet.getString("hours").trim();
                String status = resultSet.getString("status").trim();
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Date responseSqlDate = resultSet.getDate("response_date");
                LocalDate responseDate = (responseSqlDate != null) ? responseSqlDate.toLocalDate() : null;
                String customerName = resultSet.getString("name");
                String paths = resultSet.getObject("offer_file_paths") != null ? resultSet.getString("offer_file_paths").trim() : "";
                String sended = resultSet.getString("sended");
                Boolean isArchived = resultSet.getBoolean("is_archived");

                Offer offer = new Offer(id, offerDate, description, hours, status, customerId, responseDate, customerName, paths, sended, isArchived);
                offers.add(offer);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offers;
    }


    public void deleteOffer(int id) {
        String query = "DELETE FROM Offers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean saveOffer(Offer newOffer) {
        String query = "INSERT INTO Offers (offerDate, description, hours, status, customerId, offer_file_paths, sended) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(newOffer.getOfferDate()));
            stmt.setString(2, newOffer.getDescription().trim());
            stmt.setString(3, newOffer.getHours().trim());
            stmt.setString(4, newOffer.getStatus().trim());
            stmt.setInt(5, newOffer.getCustomerId());
            stmt.setString(6, newOffer.getPaths());
            stmt.setString(7, newOffer.getSended());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveOffer(newOffer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean updateOffer(Offer offer) {
        String query = "UPDATE Offers SET offerDate = ?, description = ?, hours = ?, status = ?, customerId = ?, offer_file_paths = ?, is_archived = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(offer.getOfferDate()));
            stmt.setString(2, offer.getDescription().trim());
            stmt.setString(3, offer.getHours().trim());
            stmt.setString(4, offer.getStatus().trim());
            stmt.setInt(5, offer.getCustomerId());
            stmt.setString(6, offer.getPaths());
            stmt.setBoolean(7, offer.getArchived());
            stmt.setInt(8, offer.getId());

            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Offer> getAllCustomerOffers(int customerCode) {
        List<Offer> offers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code " +
                "WHERE s.customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                String description = resultSet.getString("description").trim();
                String hours = resultSet.getString("hours").trim();
                String status = resultSet.getString("status").trim();
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Date responseSqlDate = resultSet.getDate("response_date");
                LocalDate responseDate = (responseSqlDate != null) ? responseSqlDate.toLocalDate() : null;
                String customerName = resultSet.getString("name").trim();
                String paths = resultSet.getString("offer_file_paths");
                String sended = resultSet.getString("sended");
                Boolean isArchived = resultSet.getBoolean("is_archived");

                Offer offer = new Offer(id, offerDate, description, hours, status, customerId, responseDate, customerName, paths, sended, isArchived);
                offers.add(offer);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offers;
    }

    public List<Offer> getUpdatedOffers(LocalDateTime lastCheck) {
        List<Offer> updatedOffers = new ArrayList<>();
        String query = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, s.customerId, s.response_date, s.offer_file_paths, s.sended, is_archived, c.name " +
                "FROM Offers s " +
                "LEFT JOIN Customers c ON s.customerId = c.code WHERE last_updated > ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(lastCheck)); // Ορίζουμε το timestamp
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate offerDate = resultSet.getDate("offerDate").toLocalDate();
                String description = resultSet.getString("description");
                String hours = resultSet.getString("hours");
                String status = resultSet.getString("status");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                LocalDate response_date = resultSet.getDate("response_date") != null ?
                        resultSet.getDate("response_date").toLocalDate() : null;
                String customerName = resultSet.getString("name");
                String paths = resultSet.getObject("offer_file_paths") != null ? resultSet.getString("offer_file_paths").trim() : "";
                String sended = resultSet.getString("sended");
                Boolean isArchived = resultSet.getBoolean("is_archived");

                updatedOffers.add(new Offer(id, offerDate, description, hours, status, customerId, response_date, customerName, paths, sended, isArchived));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedOffers;
    }

    public boolean updateOfferStatus(int offerId, String newStatus) {
        String query = "UPDATE Offers SET status = ?, last_updated = GETDATE() WHERE id = ? AND status <> ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, offerId);
            stmt.setString(3, newStatus); // Δεν αλλάζουμε αν το status είναι ίδιο

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Η προσφορά #" + offerId + " ενημερώθηκε σε " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOfferStatusManual(int offerId, String newStatus) {
        String query = "UPDATE Offers SET status = ?, response_date = GETDATE(), last_updated = GETDATE() WHERE id = ? AND status <> ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, offerId);
            stmt.setString(3, newStatus); // Δεν αλλάζουμε αν το status είναι ίδιο

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Η προσφορά #" + offerId + " ενημερώθηκε σε " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateOfferSent(int id) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String formattedDate = "Ναί " + myDateObj.format(myFormatObj);
        String query = "UPDATE Offers SET sended = '" + formattedDate + "' WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubSent(int id) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String formattedDate = "Ναί " + myDateObj.format(myFormatObj);
        String query = "UPDATE Subscriptions SET sended = '" + formattedDate + "' WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean completeAppointment(int id) {
        String query = "UPDATE appointments SET completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, true);
            stmt.setInt(2, id);
            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true;
            } else {
                closeConnection(conn);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Tasks> getUpcomingAppointments(LocalDateTime checkTime) {
        List<Tasks> appointments = new ArrayList<>();

        // Στρογγυλοποιούμε την checkTime για να αφαιρέσουμε τη νανοδευτερόλεπτη ακρίβεια
        checkTime = checkTime.truncatedTo(ChronoUnit.SECONDS);
        // Ερώτημα SQL για να βρούμε τα ραντεβού που ξεκινούν σε απόσταση 15 λεπτών από την τρέχουσα ώρα
        String query = "SELECT id, customerId, title, description, start_time, end_time FROM Tasks " +
                "WHERE start_time BETWEEN ? AND ? AND is_completed = 0 AND snooze = 0";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Βάζουμε το χρονικό παράθυρο για τα ραντεβού (από τώρα μέχρι 15 λεπτά μετά)
            LocalDateTime startRange = checkTime;
            LocalDateTime endRange = checkTime.plusMinutes(30);

            stmt.setTimestamp(1, Timestamp.valueOf(startRange));
            stmt.setTimestamp(2, Timestamp.valueOf(endRange));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                    int customerId = rs.getInt("customerId");

                    // Δημιουργούμε το αντικείμενο Appointment και το προσθέτουμε στη λίστα
                    Tasks appointment = new Tasks();
                    appointment.setId(id);
                    appointment.setTitle(title);
                    appointment.setStartTime(startTime);
                    appointment.setCustomerId(customerId);
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    public void snoozeAppointment(Integer id) {
        String query = "UPDATE Tasks SET snooze = 1 WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);


            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Supplier> getSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers where Id > 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String title = rs.getString("title");
                String phone = rs.getString("phone");
                String mobile = rs.getString("mobile");
                String contact = rs.getString("contact");
                String email = rs.getString("email");
                String site = rs.getNString("site");
                Supplier supplier = new Supplier(id, name, title, phone, mobile, contact, email, site);
                suppliers.add(supplier);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public int insertSupplier(String name, String title, String phone, String mobile, String contact, String email, String site) {
        String insertQuery = "INSERT INTO Suppliers (name, title, phone, mobile, email, contact, site) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int newCustomerId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, phone);
            pstmt.setString(4, mobile);
            pstmt.setString(5, contact);
            pstmt.setString(6, email);
            pstmt.setString(7, site);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Η εισαγωγή του προμηθευτή ήταν επιτυχής.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCustomerId = generatedKeys.getInt(1);
                    }
                }
            } else {
                System.out.println("Η εισαγωγή του προμηθευτή απέτυχε.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του λογιστή: " + e.getMessage());
        }

        return newCustomerId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    public void updateSupplier(int code, String name, String title, String phone, String mobile, String contact, String email, String site) {
        String sql = "UPDATE suppliers SET name = ?, title = ?, phone = ?, mobile = ?, contact = ?, email = ?, site = ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, title);
            pstmt.setString(3, phone);
            pstmt.setString(4, mobile);
            pstmt.setString(5, contact);
            pstmt.setString(6, email);
            pstmt.setString(7, site);
            pstmt.setInt(8, code);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Η ενημέρωση ήταν επιτυχής!");
                // Μπορείς να προσθέσεις εδώ και μια ενημέρωση της λίστας πελατών στην κύρια σκηνή.
            } else {
                System.out.println("Δεν βρέθηκε πελάτης με αυτό το κωδικό.");
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Supplier getSelectedSupplier(int accountantId) {
        String query = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accountantId);
            ResultSet resultSet = stmt.executeQuery();
            Supplier data = null;
            if (resultSet.next()) {
                data = new Supplier();
                data.setId(resultSet.getInt("id"));
                data.setName(resultSet.getString("name"));
                data.setTitle(resultSet.getString("title"));
                data.setPhone(resultSet.getString("phone"));
                data.setMobile(resultSet.getString("mobile"));
                data.setMobile(resultSet.getString("contact"));
                data.setEmail(resultSet.getString("email"));
                data.setMobile(resultSet.getString("site"));
            }
            closeConnection(conn);
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer supplierId = resultSet.getObject("supplierId", Integer.class);
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isReceived = resultSet.getBoolean("is_received");
                Boolean isDelivered = resultSet.getBoolean("is_delivered");
                String customerName = resultSet.getString("customerName");
                String supplierName = resultSet.getString("supplierName");


                Order order = new Order(id, title, description, dueDate, isCompleted, customerId, supplierId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                orders.add(order);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public boolean completeOrder(int orderId, boolean isCompleted) {
        String query = "UPDATE orders SET is_completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isCompleted);
            stmt.setInt(2, orderId);
            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true;
            } else {
                closeConnection(conn);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteOrder(int orderId) throws SQLException {
        String query = "DELETE FROM Orders WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
            closeConnection(conn);
        }
    }

    public boolean saveOrder(Order order) {
        String query = "INSERT INTO Orders (title, description, dueDate, is_completed, customerId, supplierId, is_ergent, is_wait) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, order.getTitle());
            stmt.setString(2, order.getDescription());
            stmt.setDate(3, Date.valueOf(order.getDueDate()));
            stmt.setBoolean(4, false);
            if (order.getCustomerId() != null) {
                stmt.setInt(5, order.getCustomerId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            if (order.getSupplierId() != null) {
                stmt.setInt(6, order.getSupplierId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setBoolean(7, order.getErgent());
            stmt.setBoolean(8, order.getWait());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                closeConnection(conn);
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveOrder(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrder(Order order) {
        String query = "UPDATE Orders SET title = ?, description = ?, dueDate = ?, is_Completed = ?, customerId = ?, supplierId = ?, is_ergent = ?, is_wait = ?, is_received = ?, is_delivered = ?  WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, order.getTitle());
            stmt.setString(2, order.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(order.getDueDate()));
            stmt.setBoolean(4, order.getCompleted());
            if (order.getCustomerId() != null) {
                stmt.setInt(5, order.getCustomerId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            if (order.getSupplierId() != null) {
                stmt.setInt(6, order.getSupplierId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setBoolean(7, order.getErgent());
            stmt.setBoolean(8, order.getWait());
            stmt.setBoolean(9, order.getReceived());
            stmt.setBoolean(10, order.getDelivered());
            stmt.setInt(11, order.getId());


            if (stmt.executeUpdate() > 0) {
                closeConnection(conn);
                return true;
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Supplier> getSuppliersFromOrders() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT DISTINCT s.id, s.name, s.title, s.phone, s.mobile, s.contact, s.email, s.site  " +
                "FROM Suppliers s " +
                "JOIN Orders o ON s.id = o.supplierId " +
                "ORDER BY s.name;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String title = rs.getString("title");
                String phone = rs.getString("phone");
                String mobile = rs.getString("mobile");
                String contact = rs.getString("contact");
                String email = rs.getString("email");
                String site = rs.getNString("site");
                Supplier supplier = new Supplier(id, name, title, phone, mobile, contact, email, site);
                suppliers.add(supplier);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.is_completed = 0 " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer supplierId = resultSet.getObject("supplierId", Integer.class);
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isReceived = resultSet.getBoolean("is_received");
                Boolean isDelivered = resultSet.getBoolean("is_delivered");
                String customerName = resultSet.getString("customerName");
                String supplierName = resultSet.getString("supplierName");


                Order order = new Order(id, title, description, dueDate, isCompleted, customerId, supplierId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                orders.add(order);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getUnreceivedOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.is_completed = 1 AND o.is_received = 0 " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer supplierId = resultSet.getObject("supplierId", Integer.class);
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isReceived = resultSet.getBoolean("is_received");
                Boolean isDelivered = resultSet.getBoolean("is_delivered");
                String customerName = resultSet.getString("customerName");
                String supplierName = resultSet.getString("supplierName");


                Order order = new Order(id, title, description, dueDate, isCompleted, customerId, supplierId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                orders.add(order);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getUndeliveredOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.is_completed = 1 AND o.is_received = 1 AND o.is_delivered = 0 " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                Integer supplierId = resultSet.getObject("supplierId", Integer.class);
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isReceived = resultSet.getBoolean("is_received");
                Boolean isDelivered = resultSet.getBoolean("is_delivered");
                String customerName = resultSet.getString("customerName");
                String supplierName = resultSet.getString("supplierName");


                Order order = new Order(id, title, description, dueDate, isCompleted, customerId, supplierId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                orders.add(order);
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getAllOrdersSup(int supplierId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.supplierId = ? " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                    boolean isCompleted = resultSet.getBoolean("is_Completed");
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    Integer suppId = resultSet.getObject("supplierId", Integer.class);
                    Boolean isErgent = resultSet.getBoolean("is_ergent");
                    Boolean isWait = resultSet.getBoolean("is_wait");
                    Boolean isReceived = resultSet.getBoolean("is_received");
                    Boolean isDelivered = resultSet.getBoolean("is_delivered");
                    String customerName = resultSet.getString("customerName");
                    String supplierName = resultSet.getString("supplierName");

                    Order order = new Order(id, title, description, dueDate, isCompleted, customerId, suppId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                    orders.add(order);
                }
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getAllOrdersCust(int custId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT " +
                "o.id," +
                "o.title," +
                "o.description," +
                "o.dueDate," +
                "o.is_completed," +
                "o.customerId," +
                "o.supplierId," +
                "o.is_ergent," +
                "o.is_wait," +
                "o.is_received," +
                "o.is_delivered," +
                "c.name AS customerName," +
                "s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.customerId = ? " +
                "ORDER BY o.dueDate DESC;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, custId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                    boolean isCompleted = resultSet.getBoolean("is_Completed");
                    Integer customerId = resultSet.getObject("customerId", Integer.class);
                    Integer suppId = resultSet.getObject("supplierId", Integer.class);
                    Boolean isErgent = resultSet.getBoolean("is_ergent");
                    Boolean isWait = resultSet.getBoolean("is_wait");
                    Boolean isReceived = resultSet.getBoolean("is_received");
                    Boolean isDelivered = resultSet.getBoolean("is_delivered");
                    String customerName = resultSet.getString("customerName");
                    String supplierName = resultSet.getString("supplierName");

                    Order order = new Order(id, title, description, dueDate, isCompleted, customerId, suppId, isErgent, isWait, isReceived, isDelivered, customerName, supplierName);
                    orders.add(order);
                }
            }
            closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public boolean updateOfferArchived(int id) {
        String query = "UPDATE Offers SET is_archived = 1 WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}