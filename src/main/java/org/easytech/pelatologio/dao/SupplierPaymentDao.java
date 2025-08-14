package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.SupplierPayment;

import java.sql.Connection;
import java.util.List;

public interface SupplierPaymentDao {
    List<SupplierPayment> getPaymentsForSupplier(int supplierId);
    void addPayment(SupplierPayment payment);
    void updatePayment(SupplierPayment payment);
    void deletePayment(int paymentId);
    List<SupplierPayment> getUncalculatedPayments();
    void markAsCalculated(int paymentId) throws java.sql.SQLException;

}
