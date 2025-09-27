package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.DeviceDao;
import org.easytech.pelatologio.models.Device;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceDaoImpl implements DeviceDao {

    private final HikariDataSource dataSource;

    public DeviceDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Device> getAllDevices() {
        List<Device> devices = new ArrayList<>();
        String query = "SELECT d.id, d.serial, d.description, d.rate, d.itemId, d.customerId, i.name AS itemName, c.name, d.is_active " +
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
                device.setActive(resultSet.getBoolean("is_active"));
                devices.add(device);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    @Override
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

    @Override
    public boolean saveDevice(Device newDevice) {
        String query = "INSERT INTO Devices (serial, description, rate, itemId, customerId, is_active) VALUES (?, ?, ?, ?, ?, ?)";
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
            stmt.setBoolean(6, newDevice.isActive());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Εισαγωγή επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία εισαγωγής
    }

    @Override
    public Boolean updateDevice(Device device) {
        String query = "UPDATE Devices SET serial = ?, description = ?, rate = ?, itemId = ?, customerId = ?, is_active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, device.getSerial());
            stmt.setString(2, device.getDescription());
            stmt.setString(3, device.getRate());
            stmt.setInt(4, device.getItemId());
            if (device.getCustomerId() != null) {
                stmt.setInt(5, device.getCustomerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setBoolean(6, device.isActive());
            stmt.setInt(7, device.getId());

            if (stmt.executeUpdate() > 0) {
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    @Override
    public List<Device> getCustomerDevices(int customerId) {
        List<Device> devices = new ArrayList<>();
        String query = "SELECT d.id, d.serial, d.description, d.rate, d.itemId, d.customerId, i.name AS itemName, d.is_active " +
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
                device.setActive(resultSet.getBoolean("is_active"));
                devices.add(device);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    @Override
    public Boolean recoverDevice(int id) {
        String query = "UPDATE Devices SET customerId = 0 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);

            if (stmt.executeUpdate() > 0) {
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    @Override
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

    @Override
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

    @Override
    public boolean assignDevice(String serial, int customerId) {
        String query = "UPDATE Devices SET customerId = ? WHERE serial = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, serial);

            if (stmt.executeUpdate() > 0) {
                return true; // Ενημέρωση επιτυχής
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Αποτυχία ενημέρωσης
    }

    @Override
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rates;
    }
}