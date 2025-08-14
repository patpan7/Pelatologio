package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.PartnerEarning;

import java.sql.Connection;
import java.util.List;

public interface PartnerEarningDao {
    void addEarning(PartnerEarning earning);
    void updateEarningStatus(int earningId, String invoiceStatus, String paymentStatus, java.time.LocalDate paymentDate);
    List<PartnerEarning> getEarningsForPartner(int partnerId);
    List<PartnerEarning> getAllEarnings();
    boolean earningExists(int partnerId, int supplierPaymentId);
}
