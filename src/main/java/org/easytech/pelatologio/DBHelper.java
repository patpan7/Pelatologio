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

        String url = "jdbc:sqlserver://"+server+":1433;databaseName=Pelatologio;user="+user+";password="+pass+";encrypt=false;";
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

                String query = "SELECT * FROM customers";
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
                                   String town, String email) {
            String insertQuery = "INSERT INTO Customers (name, title, job, afm, phone1, phone2, mobile, address, town, email) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Η εισαγωγή του πελάτη ήταν επιτυχής.");
                    closeConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    public void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String email) {
        String sql = "UPDATE customers SET name = ?, title = ?, job = ?,afm = ?, phone1 = ?, " +
                "phone2 = ?, mobile = ?, address = ?, town = ?, email = ? WHERE code = ?";
        ;
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
            pstmt.setInt(11, code);

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
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public void addLogin(int code, Logins newLogin, int i) {
        String sql = "INSERT INTO CustomerLogins (CustomerID, ApplicationID, username, password, tag) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, code);
            pstmt.setInt(2, i);
            pstmt.setString(3, newLogin.getUsername());
            pstmt.setString(4, newLogin.getPassword());
            pstmt.setString(5, newLogin.getTag());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLogin(Logins updatedLogin) {
        String query = "UPDATE CustomerLogins SET username = ?, password = ?, tag = ? WHERE LoginID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedLogin.getUsername());
            pstmt.setString(2, updatedLogin.getPassword());
            pstmt.setString(3, updatedLogin.getTag());
            pstmt.setInt(4, updatedLogin.getId());
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


//
//    // Μέθοδος για εισαγωγή δεδομένων
//    public void insertData(YourDataModel data) {
//        String query = "INSERT INTO yourTableName (field1, field2) VALUES (?, ?)";
//        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
//
//            preparedStatement.setString(1, data.getField1());
//            preparedStatement.setString(2, data.getField2());
//            // Άλλα πεδία...
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Μέθοδος για ενημέρωση δεδομένων
//    public void updateData(YourDataModel data) {
//        String query = "UPDATE yourTableName SET field1 = ?, field2 = ? WHERE id = ?";
//        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
//
//            preparedStatement.setString(1, data.getField1());
//            preparedStatement.setString(2, data.getField2());
//            preparedStatement.setInt(3, data.getId()); // Υποθέτοντας ότι έχεις ένα id για το αντικείμενο
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
