package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.SubsCategory;
import org.easytech.pelatologio.models.Subscription;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionDao {
    List<SubsCategory> getAllSubsCategory();
    List<Subscription> getAllSubs(LocalDate fromDate, LocalDate toDate);
    void deleteSubsCategory(int id);
    void updateSubsCategory(SubsCategory updatedSubsCategory);
    void saveSubsCategory(SubsCategory newSubsCategory);
    boolean saveSub(Subscription newSub);
    boolean updateSub(Subscription sub);
    String getErganiEmail(int customerId);
    boolean updateErganiEmail(int customerId, String emailAcc);
    List<Subscription> getAllCustomerSubs(int customerCode);
    void deleteSub(int id);
    void renewSub(int id, int monthsToAdd);
    void updateSubSent(int id);
    List<Subscription> getExpiringSubscriptions(int days);
}