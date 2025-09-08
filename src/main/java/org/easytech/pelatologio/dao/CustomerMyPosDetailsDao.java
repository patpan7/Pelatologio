package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.CustomerMyPosDetails;

import java.util.List;

public interface CustomerMyPosDetailsDao {
    List<CustomerMyPosDetails> getByCustomerId(int customerId);
    void saveOrUpdate(CustomerMyPosDetails details);
    int countByVerificationStatus(String status);
    int countByAccountStatus(String status);
    int getTotalCount();
    void delete(int id);
    List<CustomerMyPosDetails> getAll();
}
