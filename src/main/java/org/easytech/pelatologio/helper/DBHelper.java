package org.easytech.pelatologio.helper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.AppSettings;
import org.easytech.pelatologio.dao.*;
import org.easytech.pelatologio.dao.impl.*;
import org.easytech.pelatologio.dao.TrackingDao;

import java.sql.Connection;
import java.sql.SQLException;

public class DBHelper {
    private static HikariDataSource dataSource;
    private static CustomerDao customerDao;
    private static LoginDao loginDao;
    private static TaskDao taskDao;
    private static ItemDao itemDao;
    private static DeviceDao deviceDao;
    private static AccountantDao accountantDao;
    private static SubscriptionDao subscriptionDao;
    private static OfferDao offerDao;
    private static AddressDao addressDao;
    private static MegasoftDao megasoftDao;
    private static OrderDao orderDao;
    private static SupplierDao supplierDao;
    private static InvoiceDao invoiceDao;
    private static TrackingDao trackingDao;
    private static SimplyStatusDao simplyStatusDao;
    private static CallLogDao callLogDao;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlserver://" + AppSettings.getInstance().server + ";databaseName=Pelatologio;encrypt=false;");
            config.setUsername(AppSettings.getInstance().dbUser);
            config.setPassword(AppSettings.getInstance().dbPass);
            config.setMaximumPoolSize(10); // Μέγιστο μέγεθος pool
            config.setMinimumIdle(5); // Ελάχιστες αδρανείς συνδέσεις
            config.setConnectionTimeout(30000); // 30 δευτερόλεπτα timeout
            config.setIdleTimeout(600000); // 10 λεπτά idle timeout
            config.setMaxLifetime(1800000); // 30 λεπτά μέγιστη διάρκεια ζωής σύνδεσης
            dataSource = new HikariDataSource(config);

            // Initialize DAOs
            customerDao = new CustomerDaoImpl(dataSource);
            loginDao = new LoginDaoImpl(dataSource);
            taskDao = new TaskDaoImpl(dataSource);
            itemDao = new ItemDaoImpl(dataSource);
            deviceDao = new DeviceDaoImpl(dataSource);
            accountantDao = new AccountantDaoImpl(dataSource);
            subscriptionDao = new SubscriptionDaoImpl(dataSource);
            offerDao = new OfferDaoImpl(dataSource);
            addressDao = new AddressDaoImpl(dataSource);
            megasoftDao = new MegasoftDaoImpl(dataSource);
            orderDao = new OrderDaoImpl(dataSource);
            supplierDao = new SupplierDaoImpl(dataSource);
            invoiceDao = new InvoiceDaoImpl(dataSource);
            trackingDao = new TrackingDaoImpl(dataSource);
            simplyStatusDao = new SimplyStatusDaoImpl(dataSource);
            callLogDao = new CallLogDaoImpl(dataSource);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("SQL Server JDBC Driver not found.");
        }
    }

    public static CustomerDao getCustomerDao() {
        return customerDao;
    }

    public static LoginDao getLoginDao() {
        return loginDao;
    }

    public static TaskDao getTaskDao() {
        return taskDao;
    }

    public static ItemDao getItemDao() {
        return itemDao;
    }

    public static DeviceDao getDeviceDao() {
        return deviceDao;
    }

    public static AccountantDao getAccountantDao() {
        return accountantDao;
    }

    public static SubscriptionDao getSubscriptionDao() {
        return subscriptionDao;
    }

    public static OfferDao getOfferDao() {
        return offerDao;
    }

    public static AddressDao getAddressDao() {
        return addressDao;
    }

    public static MegasoftDao getMegasoftDao() {
        return megasoftDao;
    }

    public static OrderDao getOrderDao() {
        return orderDao;
    }

    public static SupplierDao getSupplierDao() {
        return supplierDao;
    }

    public static InvoiceDao getInvoiceDao() {
        return invoiceDao;
    }

    public static TrackingDao getTrackingDao() {
        return trackingDao;
    }

    public static SimplyStatusDao getSimplyStatusDao() {
        return simplyStatusDao;
    }

    public static CallLogDao getCallLogDao() {
        return callLogDao;
    }

    // This method is no longer needed as DAOs handle their own connections
    // but keeping it for compatibility if other parts of the code still use it directly.
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariPool-1 - Closed.");
        }
    }
}