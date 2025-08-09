package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.Recommendation;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface CustomerDao {
    List<Customer> getCustomers() throws SQLException;
    void getCustomerDetails(Customer customer);
    boolean isAfmExists(String afm);
    boolean isAfmExistsMegasoft(String afm);
    int insertCustomer(String name, String title, String job, String afm, String phone1,
                       String phone2, String mobile, String address,
                       String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, int recommendation, String balance, String balanceReason, int jobTeam);
    void updateCustomer(int code, String name, String title, String job, String afm, String phone1, String phone2, String mobile, String address, String town, String postcode, String email, String email2, String manager, String managerPhone, String notes, int accId, String accName1, String accEmail1, int recommendation, String balance, String balanceReason, boolean isActive, int jobTeam);
    String checkCustomerLock(int code, String appUser);
    void customerLock(int code, String appUser);
    void customerUnlock(int code);
    void customerUnlockAll(String appUser);
    void customerDelete(int code);
    Customer getSelectedCustomer(int customerId);
    boolean hasSubAddress(int code);
    boolean hasApp(int code, int appId);
    boolean hasDevice(int code);
    boolean hasTask(int code);
    boolean hasSub(int code);
    boolean hasOffer(int code);
    boolean hasOrders(int code);
    boolean hasInvoices(String afm);
    List<Customer> getCustomersByAcc(int accId);
    List<Recommendation> getRecomedations();
    boolean hasAccountant(int customerId);

    void deactivateCustomer(Customer customer);

    void updateMyPosClientId(int customerId, String myposClientId);

    Customer getCustomerByPhoneNumber(String phoneNumber) throws SQLException;

    Map<String, Integer> getNewCustomersPerMonth();
    Map<String, Integer> getCustomersByRecommendation();

    Customer getCustomerByCode(int customerId);

    List<Customer> getCustomersWithBalance();
}