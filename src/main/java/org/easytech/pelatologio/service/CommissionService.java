package org.easytech.pelatologio.service;

import org.easytech.pelatologio.dao.CommissionDao;
import org.easytech.pelatologio.dao.PartnerEarningDao;
import org.easytech.pelatologio.dao.SupplierPaymentDao;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Commission;
import org.easytech.pelatologio.models.PartnerEarning;
import org.easytech.pelatologio.models.SupplierPayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CommissionService {

    private final CommissionDao commissionDao;
    private final SupplierPaymentDao supplierPaymentDao;
    private final PartnerEarningDao partnerEarningDao;

    public CommissionService() {
        this.commissionDao = DBHelper.getCommissionDao();
        this.supplierPaymentDao = DBHelper.getSupplierPaymentDao();
        this.partnerEarningDao = DBHelper.getPartnerEarningDao();
    }

    public CalculationResult calculatePartnerEarnings() throws SQLException {
        CalculationResult result = new CalculationResult();
        List<SupplierPayment> uncalculatedPayments = supplierPaymentDao.getUncalculatedPayments();
        
        if (uncalculatedPayments.isEmpty()) {
            result.setMessage("No new supplier payments to process.");
            return result;
        }

        for (SupplierPayment payment : uncalculatedPayments) {
            // Find the active commission for this customer and supplier
            Commission commission = commissionDao.findCommission(payment.getCustomerId(), payment.getSupplierId());

            if (commission == null) {
                result.addSkippedPayment(payment.getId(), "No active commission found.");
                continue; // Skip to the next payment
            }

            // Check if an earning for this specific payment has already been calculated
            if (partnerEarningDao.earningExists(commission.getPartnerId(), payment.getId())) {
                result.addSkippedPayment(payment.getId(), "Earning already exists.");
                // Mark as calculated to avoid processing it again
                supplierPaymentDao.markAsCalculated(payment.getId());
                continue;
            }

            // Calculate the earning amount
            BigDecimal paymentAmount = payment.getAmount();
            BigDecimal rate = BigDecimal.valueOf(commission.getRate());
            BigDecimal earningAmount = paymentAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Create the new earning record
            PartnerEarning newEarning = new PartnerEarning();
            newEarning.setPartnerId(commission.getPartnerId());
            newEarning.setSupplierPaymentId(payment.getId());
            newEarning.setCustomerId(payment.getCustomerId());
            newEarning.setCommissionId(commission.getId());
            newEarning.setEarningDate(LocalDate.now());
            newEarning.setEarningAmount(earningAmount);
            newEarning.setPartnerInvoiceStatus("Pending"); // Explicitly set default
            newEarning.setPaymentToPartnerStatus("Unpaid"); // Explicitly set default

            // Save the new earning and mark the payment as calculated within a transaction
            try {
                saveEarningAndUpdatePayment(newEarning, payment.getId());
                result.addCreatedEarning(newEarning);
            } catch (SQLException e) {
                result.addSkippedPayment(payment.getId(), "Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        result.setMessage("Calculation process finished.");
        return result;
    }

    private void saveEarningAndUpdatePayment(PartnerEarning earning, int paymentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Using the DAOs which will use the provided connection
            partnerEarningDao.addEarning(earning);
            supplierPaymentDao.markAsCalculated(paymentId);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e; // Re-throw the exception to be handled by the caller
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Helper class to return results
    public static class CalculationResult {
        private int createdCount = 0;
        private int skippedCount = 0;
        private String message;
        private final StringBuilder details = new StringBuilder();

        public void addCreatedEarning(PartnerEarning earning) {
            createdCount++;
            details.append(String.format("Created: Earning of %s for Partner ID %d.\n", earning.getEarningAmount(), earning.getPartnerId()));
        }

        public void addSkippedPayment(int paymentId, String reason) {
            skippedCount++;
            details.append(String.format("Skipped: Payment ID %d. Reason: %s\n", paymentId, reason));
        }

        // Getters and Setters
        public int getCreatedCount() { return createdCount; }
        public int getSkippedCount() { return skippedCount; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getDetails() { return details.toString(); }
    }
}
