package org.easytech.pelatologio;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                data.setNotes(resultSet.getString("notes"));
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

    public int insertCustomer(String name, String title, String job, String afm, String phone1,
                              String phone2, String mobile, String address,
                              String town, String postcode, String email, String manager, String managerPhone, String notes) {
        String insertQuery = "INSERT INTO Customers (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email, manager, managerPhone, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int newCustomerId = -1; // Default value for error handling
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

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
            pstmt.setString(12, manager);
            pstmt.setString(13, managerPhone);
            pstmt.setString(14, notes);

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

    public void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String manager, String managerPhone, String notes) {
        String sql = "UPDATE customers SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, postcode = ?, email = ?, manager = ?, managerPhone = ?, notes = ? WHERE code = ?";

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
            pstmt.setString(12, manager);
            pstmt.setString(13, managerPhone);
            pstmt.setString(14, notes);
            pstmt.setInt(15, code);

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

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(SQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertAdditionalAddress(int customerId, String address, String city, String postalCode) {
        String sql = "INSERT INTO CustomerAddresses (CustomerID, Address, Town, Postcode) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setString(2, address);
            statement.setString(3, city);
            statement.setString(4, postalCode);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

        } catch (SQLException e) {
            e.printStackTrace(); // Αν υπάρχει κάποιο σφάλμα, το εκτυπώνουμε
        }

        return customerAddresses;
    }

    public void addAddress(int code, CustomerAddress newCustomerAddress) {
        String sql = "INSERT INTO CustomerAddresses (CustomerID, Address, Town, Postcode, Store) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, code);
            statement.setString(2, newCustomerAddress.getAddress());
            statement.setString(3, newCustomerAddress.getTown());
            statement.setString(4, newCustomerAddress.getPostcode());
            statement.setString(5, newCustomerAddress.getStore());
            statement.executeUpdate();
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
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void customerDelete(int code) {
        String query = "DELETE FROM CustomerAddresses WHERE CustomerID = ?";
        String query2 = "DELETE FROM CustomerLogins WHERE CustomerID = ?";
        String query3 = "DELETE FROM Customers WHERE code = ?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt1 = conn.prepareStatement(query);
                 PreparedStatement pstmt2 = conn.prepareStatement(query2);
                 PreparedStatement pstmt3 = conn.prepareStatement(query3)) {

                pstmt1.setInt(1, code);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, code);
                pstmt2.executeUpdate();

                pstmt3.setInt(1, code);
                pstmt3.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCalendar(Calendars calendar) {
        String query = "INSERT INTO calendars (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, calendar.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                calendar.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Λήψη όλων των ημερολογίων
    public List<Calendars> getAllCalendars() {
        List<Calendars> calendars = new ArrayList<>();
        String query = "SELECT * FROM calendars";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                calendars.add(new Calendars(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return calendars;
    }

    public boolean saveAppointment(Appointment appointment) {
        String query = "INSERT INTO appointments (customerid, title, description, calendar_id, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appointment.getCustomerId());
            stmt.setString(2, appointment.getTitle());
            stmt.setString(3, appointment.getDescription());
            stmt.setInt(4,appointment.getCalendarId());
            stmt.setTimestamp(5, Timestamp.valueOf(appointment.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(appointment.getEndTime()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveAppointment(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM appointments";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int customerId = rs.getInt("customerid");
                String title = rs.getString("title");
                String description = rs.getString("description");
                int calendarId = rs.getInt("calendar_id");
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

                appointments.add(new Appointment(id, customerId, title, description, calendarId, startTime, endTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean updateAppointment(Appointment appointment) {
        String query = "UPDATE appointments SET title = ?, description = ?, calendar_id = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, appointment.getTitle());
            stmt.setString(2, appointment.getDescription());
            stmt.setInt(3, appointment.getCalendarId());
            stmt.setTimestamp(4, Timestamp.valueOf(appointment.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(appointment.getEndTime()));
            stmt.setInt(6, appointment.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveAppointment(appointment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteCalendar(int id) {
        String query = "DELETE FROM Calendars WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendra(Calendars updatedCalendar) {
        String query = "UPDATE Calendars SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedCalendar.getName());
            pstmt.setInt(2, updatedCalendar.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAppointment(int appointmentId) throws SQLException {
        String query = "DELETE FROM appointments WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, appointmentId);
            statement.executeUpdate();
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
                data.setManager(resultSet.getString("manager"));
                data.setManagerPhone(resultSet.getString("managerPhone"));
                data.setNotes(resultSet.getString("notes"));
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, c.name " +
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

                Task task = new Task(id, title, description, dueDate, isCompleted, category, customerId, customerName);
                tasks.add(task);
            }

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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean saveTask(Task task) {
        String query = "INSERT INTO Tasks (title, description, dueDate, is_completed, customerId, category) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDate(3, Date.valueOf(task.getDueDate()));
            stmt.setBoolean(4,task.getCompleted());
            if (task.getCustomerId() != null) {
                stmt.setInt(5, task.getCustomerId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, task.getCategory());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveTask(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTask(Task task) {
        String query = "UPDATE Tasks SET title = ?, description = ?, dueDate = ?, is_Completed = ?, category = ?, customerId = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(task.getDueDate()));
            stmt.setBoolean(4, task.getCompleted());
            stmt.setString(5, task.getCategory());
            if (task.getCustomerId() != null) {
                stmt.setInt(6, task.getCustomerId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setInt(7, task.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM Tasks WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
    }

}