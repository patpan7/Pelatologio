package org.easytech.pelatologio.helper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
    private static RecommendationDao recommendationDao;
    private static JobTeamDao jobTeamDao;
    private static SubJobTeamDao subJobTeamDao;
    private static AppItemDao appItemDao;
    private static CustomerMyPosDetailsDao customerMyPosDetailsDao;
    private static CommissionDao commissionDao;
    private static SupplierPaymentDao supplierPaymentDao;
    private static PartnerEarningDao partnerEarningDao;
    private static PartnerDao partnerDao;
    private static ApplicationStepDao applicationStepDao;
    private static CustomerProjectDao customerProjectDao;
    private static ProjectStepProgressDao customerProjectTaskDao;
    private static ApplicationDao applicationDao;

    public static boolean initializeDatabase() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlserver://" + AppSettings.getInstance().server + ";databaseName="+AppSettings.getInstance().db+";encrypt=false;");
            config.setUsername(AppSettings.getInstance().dbUser);
            config.setPassword(AppSettings.getInstance().dbPass);
            config.setMaximumPoolSize(10); // Μέγιστο μέγεθος pool
            config.setMinimumIdle(5); // Ελάχιστες αδρανείς συνδέσεις
            config.setConnectionTimeout(3000); // 3 δευτερόλεπτα timeout
            config.setIdleTimeout(600000); // 10 λεπτά idle timeout
            config.setMaxLifetime(1800000); // 30 λεπτά μέγιστη διάρκεια ζωής σύνδεσης
            dataSource = new HikariDataSource(config);

            // --- Core DAOs (Always Initialized) ---
            customerDao = new CustomerDaoImpl(dataSource);
            supplierDao = new SupplierDaoImpl(dataSource);
            accountantDao = new AccountantDaoImpl(dataSource);
            addressDao = new AddressDaoImpl(dataSource);
            recommendationDao = new RecommendationDaoImpl(dataSource);
            jobTeamDao = new JobTeamDaoImpl(dataSource);
            subJobTeamDao = new SubJobTeamDaoImpl(dataSource);
            applicationDao = new ApplicationDaoImpl(dataSource); // Needed for feature management

            // --- Conditional DAOs based on Features ---
            if (Features.isEnabled("tasks")) {
                taskDao = new TaskDaoImpl(dataSource);
            }
            if (Features.isEnabled("devices")) {
                deviceDao = new DeviceDaoImpl(dataSource);
                itemDao = new ItemDaoImpl(dataSource);
                appItemDao = new AppItemDaoImpl(dataSource);
            }
            if (Features.isEnabled("subs")) {
                subscriptionDao = new SubscriptionDaoImpl(dataSource);
            }
            if (Features.isEnabled("offers")) {
                offerDao = new OfferDaoImpl(dataSource);
            }
            if (Features.isEnabled("megasoft")) {
                megasoftDao = new MegasoftDaoImpl(dataSource);
                invoiceDao = new InvoiceDaoImpl(dataSource);
            }
            if (Features.isEnabled("orders")) {
                orderDao = new OrderDaoImpl(dataSource);
            }
            if (Features.isEnabled("simply")) {
                simplyStatusDao = new SimplyStatusDaoImpl(dataSource);
            }
            if (Features.isEnabled("calls")) {
                callLogDao = new CallLogDaoImpl(dataSource);
            }
            if (Features.isEnabled("mypos")) {
                customerMyPosDetailsDao = new CustomerMyPosDetailsDaoImpl(dataSource);
            }
            if (Features.isEnabled("partners")) {
                partnerDao = new PartnerDaoImpl(dataSource);
                commissionDao = new CommissionDaoImpl(dataSource);
                supplierPaymentDao = new SupplierPaymentDaoImpl(dataSource);
                partnerEarningDao = new PartnerEarningDaoImpl(dataSource);
            }
            if (Features.isEnabled("edps")) { // Assuming 'edps' is the feature key
                applicationStepDao = new ApplicationStepDaoImpl(dataSource);
                customerProjectDao = new CustomerProjectDaoImpl(dataSource);
                customerProjectTaskDao = new ProjectStepProgressDaoImpl(dataSource);
            }

            // This DAO seems general purpose, might need to always be on
            trackingDao = new TrackingDaoImpl(dataSource);
            loginDao = new LoginDaoImpl(dataSource); // For app logins
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public static RecommendationDao getRecommendationDao() {
        return recommendationDao;
    }

    public static JobTeamDao getJobTeamDao() {
        return jobTeamDao;
    }

    public static SubJobTeamDao getSubJobTeamDao() {
        return subJobTeamDao;
    }

    public static AppItemDao getAppItemDao() {
        return appItemDao;
    }

    public static CustomerMyPosDetailsDao getCustomerMyPosDetailsDao() {
        return customerMyPosDetailsDao;
    }

    public static CommissionDao getCommissionDao() {
        return commissionDao;
    }

    public static SupplierPaymentDao getSupplierPaymentDao() {
        return supplierPaymentDao;
    }

    public static PartnerEarningDao getPartnerEarningDao() {
        return partnerEarningDao;
    }

    public static PartnerDao getPartnerDao() {
        return partnerDao;
    }

    public static ApplicationStepDao getApplicationStepDao() {
        return applicationStepDao;
    }
    public static CustomerProjectDao getCustomerProjectDao() {
        return customerProjectDao;
    }
    public static ProjectStepProgressDao getCustomerProjectTaskDao() {
        return customerProjectTaskDao;
    }
    public static ApplicationDao getApplicationDao() {
        return applicationDao;
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