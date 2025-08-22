package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Commission;

import java.sql.SQLException;
import java.util.List;

public interface CommissionDao {
    void addCommission(Commission commission);
    void updateCommission(Commission commission);
    void deleteCommission(int commissionId) throws SQLException;
    Commission findCommission(int customerId, int supplierId);
    List<Commission> getCommissionsForPartner(int partnerId);
    List<Commission> getAllCommissions();
}
