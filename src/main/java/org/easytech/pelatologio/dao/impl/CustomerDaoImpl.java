package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.CustomerDao;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.Recommendation;

import java.sql.*;
import java.util.*;

public class CustomerDaoImpl implements CustomerDao {

    private final HikariDataSource dataSource;

    public CustomerDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
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
                data.setRecommendation(resultSet.getInt("recommendation"));
                data.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                data.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                data.setActive(resultSet.getBoolean("isActive"));
                data.setSubJobTeam(resultSet.getInt("subJobTeam"));

                dataList.add(data);
            }
            query = "SELECT CustomerID, ApplicationID FROM CustomerLogins";  // Προσαρμόζεις στα δικά σου δεδομένα
            PreparedStatement ps = conn.prepareStatement(query);
            resultSet = ps.executeQuery();

            Map<Integer, List<Integer>> appMap = new HashMap<>();
            while (resultSet.next()) {
                int customerId = resultSet.getInt("CustomerID");
                int appId = resultSet.getInt("ApplicationID");
                appMap.computeIfAbsent(customerId, k -> new ArrayList<>()).add(appId);
            }
            for (Customer c : dataList) {
                if (appMap.containsKey(c.getCode())) {
                    c.setApps(appMap.get(c.getCode()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    @Override
    public void getCustomerDetails(Customer customer) {
        // This method was not in the original DBHelper.java
        // It was added during the refactoring. We can leave it as is or remove it.
        // For now, I will leave it.
        if (customer == null) return;
        String query = "SELECT job, address, postcode, email2, manager, managerPhone, notes, accId, accName1, accEmail1, recommendation, balanceReason, subJobTeam FROM customers WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customer.getCode());
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    customer.setJob(resultSet.getString("job"));
                    customer.setAddress(resultSet.getString("address"));
                    customer.setPostcode(resultSet.getString("postcode"));
                    customer.setEmail2(resultSet.getString("email2"));
                    customer.setManager(resultSet.getString("manager"));
                    customer.setManagerPhone(resultSet.getString("managerPhone"));
                    customer.setNotes(resultSet.getString("notes") != null ? resultSet.getString("notes").trim() : "");
                    customer.setAccId(resultSet.getInt("accId"));
                    customer.setAccName1(resultSet.getString("accName1"));
                    customer.setAccEmail1(resultSet.getString("accEmail1"));
                    customer.setRecommendation(resultSet.getInt("recommendation"));
                    customer.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                    customer.setSubJobTeam(resultSet.getInt("subJobTeam"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
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

    @Override
    public boolean isAfmExistsMegasoft(String afm) {
        String query = "SELECT COUNT(*) FROM Megasoft.dbo.E2_Emp001_25 WHERE afm = ?";
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

    @Override
    public int insertCustomer(String name, String title, String job, String afm, String phone1,
                              String phone2, String mobile, String address,
                              String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, int recommendation, String balance, String balanceReason, int subJobTeam) {
        // Prepare the SQL query for inserting a new customer
        String insertQuery = "INSERT INTO Customers (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email, email2, manager, managerPhone, notes, accId, accName1, accEmail1, recommendation, balance, balanceReason, isActive, subJobTeam) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setInt(19, recommendation);
            pstmt.setString(20, balance);
            pstmt.setString(21, balanceReason);
            pstmt.setBoolean(22, true); // Ορισμός του isActive σε true
            pstmt.setInt(23, subJobTeam); // Ορισμός του subJobTeam σε 0


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
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του πελάτη: " + e.getMessage());
        }

        return newCustomerId; // Επιστρέφει το CustomerID ή -1 αν υπήρξε σφάλμα
    }

    @Override
    public void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, int recommendation, String balance, String balanceReason, boolean isActive, int subJobTeam) {
        String sql = "UPDATE customers SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, postcode = ?, email = ?, email2 = ?,manager = ?, managerPhone = ?, notes = ?, accId = ?, accName1 = ?, accEmail1 = ?, recommendation = ?, balance = ?, balanceReason = ?, isActive = ?, subJobTeam = ? WHERE code = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            pstmt.setInt(19, recommendation);
            pstmt.setString(20, balance);
            pstmt.setString(21, balanceReason);
            pstmt.setBoolean(22, isActive); // Ορισμός του isActive σε true
            pstmt.setInt(23, subJobTeam); // Ορισμός του subJobTeam σε 0
            pstmt.setInt(24, code);

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

    @Override
    public String checkCustomerLock(int code, String appUser) {
        String checkLockQuery = "SELECT locked_by FROM Customers WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkLockStmt = conn.prepareStatement(checkLockQuery)) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "unlocked";
    }

    @Override
    public void customerLock(int code, String appUser) {
        String checkLockQuery = "UPDATE Customers SET locked_by = ? WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery)) {
            lockStmt.setString(1, appUser);
            lockStmt.setInt(2, code);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void customerUnlock(int code) {
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery)) {
            lockStmt.setInt(1, code);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void customerUnlockAll(String appUser) {
        String checkLockQuery = "UPDATE Customers SET locked_by = NULL WHERE locked_by = ?";
        try (Connection conn = getConnection();
             PreparedStatement lockStmt = conn.prepareStatement(checkLockQuery)) {
            lockStmt.setString(1, appUser);
            lockStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void customerDelete(int code) {
        String deleteProgress = "DELETE FROM SimplySetupProgress WHERE app_login_id IN (SELECT id FROM CustomerLogins WHERE CustomerID = ?)";
        String deleteAddresses = "DELETE FROM CustomerAddresses WHERE CustomerID = ?";
        String deleteLogins = "DELETE FROM CustomerLogins WHERE CustomerID = ?";
        String deleteCustomer = "DELETE FROM Customers WHERE code = ?";
        String releaseDevices = "UPDATE Devices SET customerId = 0 WHERE customerId = ?";

        try (Connection conn = getConnection()) {
            try (
                    PreparedStatement pstmt0 = conn.prepareStatement(deleteProgress);
                    PreparedStatement pstmt1 = conn.prepareStatement(deleteAddresses);
                    PreparedStatement pstmt2 = conn.prepareStatement(deleteLogins);
                    PreparedStatement pstmt3 = conn.prepareStatement(deleteCustomer);
                    PreparedStatement pstmt4 = conn.prepareStatement(releaseDevices)
            ) {
                pstmt0.setInt(1, code);
                pstmt0.executeUpdate();

                pstmt1.setInt(1, code);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, code);
                pstmt2.executeUpdate();

                pstmt3.setInt(1, code);
                pstmt3.executeUpdate();

                pstmt4.setInt(1, code);
                pstmt4.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
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
                data.setRecommendation(resultSet.getInt("recommendation"));
                data.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                data.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                data.setActive(resultSet.getBoolean("isActive"));
                data.setSubJobTeam(resultSet.getInt("subJobTeam"));
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasInvoices(String afm) {
        String query = "SELECT COUNT(*) FROM  [MEGASOFT].[dbo].[E2_Emp016_25] WHERE AfmPel = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, afm);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
                data.setRecommendation(resultSet.getInt("recommendation"));
                data.setBalance(resultSet.getString("balance"));
                data.setBalanceReason(resultSet.getString("balanceReason"));
                data.setSubJobTeam(resultSet.getInt("subJobTeam"));
                customers.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public List<Recommendation> getRecomedations() {
        List<Recommendation> recommendations = new ArrayList<>();
        String query = "SELECT * FROM Recommendations ORDER BY name ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                recommendations.add(new Recommendation(resultSet.getInt("id"), resultSet.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recommendations;
    }

    @Override
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deactivateCustomer(Customer customer) {
        String query = "UPDATE Customers SET active = 0 WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customer.getCode());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Customer getCustomerByPhoneNumber(String phoneNumber) throws SQLException {
        String query = "SELECT * FROM customers WHERE phone1 = ? OR phone2 = ? OR mobile = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            stmt.setString(2, phoneNumber);
            stmt.setString(3, phoneNumber);
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
                data.setRecommendation(resultSet.getInt("recommendation"));
                data.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                data.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                data.setActive(resultSet.getBoolean("isActive"));
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception after logging
        }
    }

    @Override
    public Map<String, Integer> getNewCustomersPerMonth() {
        Map<String, Integer> monthlyData = new LinkedHashMap<>();
        // Query for the last 12 months
        String query = "SELECT FORMAT(created_at, 'yyyy-MM') AS month, COUNT(*) AS count " +
                "FROM Customers " +
                "WHERE created_at >= DATEADD(year, -1, GETDATE()) " +
                "GROUP BY FORMAT(created_at, 'yyyy-MM') " +
                "ORDER BY month;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                monthlyData.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlyData;
    }

    @Override
    public Map<String, Integer> getCustomersByRecommendation() {
        Map<String, Integer> recommendationData = new LinkedHashMap<>();
        String query = "SELECT R.name AS recommendation_name,  COUNT(*) AS count " +
                "FROM Customers C " +
                "JOIN Recommendations R ON C.recommendation = R.id " +
                "WHERE C.recommendation <> 0 " +
                "GROUP BY R.name " +
                "ORDER BY R.name;";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                recommendationData.put(rs.getString("recommendation_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendationData;
    }

    @Override
    public Customer getCustomerByCode(int customerId) {
        Customer customer = null;
        String query = "SELECT * FROM customers where code = ?";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                customer = new Customer();
                customer.setCode(resultSet.getInt("code"));
                customer.setName(resultSet.getString("name"));
                customer.setTitle(resultSet.getString("title"));
                customer.setJob(resultSet.getString("job"));
                customer.setAfm(resultSet.getString("afm"));
                customer.setPhone1(resultSet.getString("phone1"));
                customer.setPhone2(resultSet.getString("phone2"));
                customer.setMobile(resultSet.getString("mobile"));
                customer.setAddress(resultSet.getString("address"));
                customer.setTown(resultSet.getString("town"));
                customer.setPostcode(resultSet.getString("postcode"));
                customer.setEmail(resultSet.getString("email"));
                customer.setEmail2(resultSet.getString("email2"));
                customer.setManager(resultSet.getString("manager"));
                customer.setManagerPhone(resultSet.getString("managerPhone"));
                customer.setNotes(resultSet.getString("notes") != null ? resultSet.getString("notes").trim() : "");
                customer.setAccId(resultSet.getInt("accId"));
                customer.setAccName1(resultSet.getString("accName1"));
                customer.setAccEmail1(resultSet.getString("accEmail1"));
                customer.setRecommendation(resultSet.getInt("recommendation"));
                customer.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                customer.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                customer.setActive(resultSet.getBoolean("isActive"));
                customer.setSubJobTeam(resultSet.getInt("subJobTeam"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customer;
    }

    @Override
    public List<Customer> getCustomersWithBalance() {
        List<Customer> customerList = new ArrayList<>();
        Customer customer = null;
        String query = "SELECT * FROM customers where balance <> ''";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                customer = new Customer();
                customer.setCode(resultSet.getInt("code"));
                customer.setName(resultSet.getString("name"));
                customer.setTitle(resultSet.getString("title"));
                customer.setJob(resultSet.getString("job"));
                customer.setAfm(resultSet.getString("afm"));
                customer.setPhone1(resultSet.getString("phone1"));
                customer.setPhone2(resultSet.getString("phone2"));
                customer.setMobile(resultSet.getString("mobile"));
                customer.setAddress(resultSet.getString("address"));
                customer.setTown(resultSet.getString("town"));
                customer.setPostcode(resultSet.getString("postcode"));
                customer.setEmail(resultSet.getString("email"));
                customer.setEmail2(resultSet.getString("email2"));
                customer.setManager(resultSet.getString("manager"));
                customer.setManagerPhone(resultSet.getString("managerPhone"));
                customer.setNotes(resultSet.getString("notes") != null ? resultSet.getString("notes").trim() : "");
                customer.setAccId(resultSet.getInt("accId"));
                customer.setAccName1(resultSet.getString("accName1"));
                customer.setAccEmail1(resultSet.getString("accEmail1"));
                customer.setRecommendation(resultSet.getInt("recommendation"));
                customer.setBalance(resultSet.getObject("balance") != null ? resultSet.getString("balance").trim() : "");
                customer.setBalanceReason(resultSet.getString("balanceReason") != null ? resultSet.getString("balanceReason").trim() : "");
                customer.setActive(resultSet.getBoolean("isActive"));
                customer.setSubJobTeam(resultSet.getInt("subJobTeam"));
                customerList.add(customer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerList;
    }
}
