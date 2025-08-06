package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.OrderDao;
import org.easytech.pelatologio.models.Order;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {

    private final HikariDataSource dataSource;

    public OrderDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
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
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
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
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
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
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public void saveOrder(Order newOrder) {
        String query = "INSERT INTO Orders (title, description, dueDate, is_completed, customerId, supplierId, is_ergent, is_wait, is_received, is_delivered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newOrder.getTitle());
            stmt.setString(2, newOrder.getDescription());
            stmt.setDate(3, Date.valueOf(newOrder.getDueDate()));
            stmt.setBoolean(4, newOrder.getCompleted() == null ? false : newOrder.getCompleted());
            stmt.setInt(5, newOrder.getCustomerId());
            stmt.setInt(6, newOrder.getSupplierId());
            stmt.setBoolean(7, newOrder.getErgent() == null ? false : newOrder.getErgent());
            stmt.setBoolean(8, newOrder.getWait() == null ? false : newOrder.getWait());
            stmt.setBoolean(9, newOrder.getReceived() == null ? false : newOrder.getReceived());
            stmt.setBoolean(10, newOrder.getDelivered() == null ? false : newOrder.getDelivered());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateOrder(Order order) {
        String query = "UPDATE Orders SET title = ?, description = ?, dueDate = ?, is_completed = ?, customerId = ?, supplierId = ?, is_ergent = ?, is_wait = ?, is_received = ?, is_delivered = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, order.getTitle());
            stmt.setString(2, order.getDescription());
            stmt.setDate(3, Date.valueOf(order.getDueDate()));
            stmt.setBoolean(4, order.getCompleted());
            stmt.setInt(5, order.getCustomerId());
            stmt.setInt(6, order.getSupplierId());
            stmt.setBoolean(7, order.getErgent());
            stmt.setBoolean(8, order.getWait());
            stmt.setBoolean(9, order.getReceived());
            stmt.setBoolean(10, order.getDelivered());
            stmt.setInt(11, order.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteOrder(int id) {
        String query = "DELETE FROM Orders WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean completeOrder(int orderId, boolean complete) {
        String query = "UPDATE Orders SET is_completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, complete);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Order> getAllOrdersCust(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.id, o.title, o.description, o.dueDate, o.is_completed, o.customerId, o.supplierId, o.is_ergent, o.is_wait, o.is_received, o.is_delivered, c.name AS customerName, s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.customerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.id, o.title, o.description, o.dueDate, o.is_completed, o.customerId, o.supplierId, o.is_ergent, o.is_wait, o.is_received, o.is_delivered, c.name AS customerName, s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrdersSup(int supplierId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.id, o.title, o.description, o.dueDate, o.is_completed, o.customerId, o.supplierId, o.is_ergent, o.is_wait, o.is_received, o.is_delivered, c.name AS customerName, s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.supplierId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersBySupplier(int supplierId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.id, o.title, o.description, o.dueDate, o.is_completed, o.customerId, o.supplierId, o.is_ergent, o.is_wait, o.is_received, o.is_delivered, c.name AS customerName, s.name AS supplierName " +
                "FROM Orders o " +
                "LEFT JOIN Customers c ON o.customerId = c.code " +
                "LEFT JOIN Suppliers s ON o.supplierId = s.id " +
                "WHERE o.supplierId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setTitle(rs.getString("title"));
        order.setDescription(rs.getString("description"));
        order.setDueDate(rs.getDate("dueDate").toLocalDate());
        order.setCompleted(rs.getBoolean("is_completed"));
        order.setCustomerId(rs.getInt("customerId"));
        order.setSupplierId(rs.getInt("supplierId"));
        order.setErgent(rs.getBoolean("is_ergent"));
        order.setWait(rs.getBoolean("is_wait"));
        order.setReceived(rs.getBoolean("is_received"));
        order.setDelivered(rs.getBoolean("is_delivered"));
        order.setCustomerName(rs.getString("customerName"));
        order.setSupplierName(rs.getString("supplierName"));
        return order;
    }
}