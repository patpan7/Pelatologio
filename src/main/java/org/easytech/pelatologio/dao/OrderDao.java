package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Order;
import java.util.List;
import java.sql.SQLException;

public interface OrderDao {
    List<Order> getPendingOrders();
    List<Order> getUnreceivedOrders();
    List<Order> getUndeliveredOrders();
    void saveOrder(Order newOrder);
    void updateOrder(Order order);
    void deleteOrder(int id);
    boolean completeOrder(int orderId, boolean complete);
    List<Order> getAllOrdersCust(int customerId);
    List<Order> getAllOrders();
    List<Order> getAllOrdersSup(int supplierId);
    List<Order> getOrdersBySupplier(int supplierId);
}