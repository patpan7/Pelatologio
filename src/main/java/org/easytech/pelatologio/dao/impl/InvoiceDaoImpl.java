package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.InvoiceDao;
import org.easytech.pelatologio.models.Invoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDaoImpl implements InvoiceDao {

    private final HikariDataSource dataSource;

    public InvoiceDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Invoice> getInvoices(String afm) {
        List<Invoice> invoiceList = new ArrayList<>();

        try (Connection conn = getConnection()) {
            // 1. Βρίσκουμε όλα τα ονόματα πινάκων που ταιριάζουν με το pattern
            List<String> tables = new ArrayList<>();
            String tableQuery = "SELECT TABLE_NAME " +
                    "FROM MEGASOFT.INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME LIKE 'E2_Emp016_%'";

            try (PreparedStatement pstmt = conn.prepareStatement(tableQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            if (tables.isEmpty()) {
                return invoiceList; // δεν υπάρχουν πίνακες που να ταιριάζουν
            }

            // 2. Φτιάχνουμε δυναμικό SQL με UNION ALL
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < tables.size(); i++) {
                if (i > 0) queryBuilder.append(" UNION ALL ");
                queryBuilder.append("SELECT DatePar, PerigrafhPar, SeiraPar, ArPar, SynTeliko, PerPlhromis, Parat1 ")
                        .append("FROM MEGASOFT.dbo.").append(tables.get(i))
                        .append(" WHERE PelProm = 1 AND AfmPel = ?");
            }
            queryBuilder.append(" ORDER BY DatePar DESC, ArPar DESC");

            String finalQuery = queryBuilder.toString();

            // 3. Προετοιμάζουμε PreparedStatement (ένα ? για κάθε πίνακα)
            try (PreparedStatement pstmt = conn.prepareStatement(finalQuery)) {
                for (int i = 0; i < tables.size(); i++) {
                    pstmt.setString(i + 1, afm);
                }

                // 4. Εκτελούμε query
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Date sqlDate = rs.getDate("DatePar");
                        String date = (sqlDate != null) ? String.valueOf(sqlDate.toLocalDate()) : null;

                        String description = rs.getString("PerigrafhPar");
                        String series = rs.getString("SeiraPar");
                        String number = rs.getString("ArPar");
                        String total = rs.getString("SynTeliko");
                        String paid = rs.getString("PerPlhromis");
                        String note = rs.getString("Parat1");

                        String seriesAndNumber = (series != null ? series : "") + " " + number;

                        Invoice invoice = new Invoice(date, description, seriesAndNumber, total, paid, note);
                        invoiceList.add(invoice);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoiceList;
    }


    @Override
    public List<Invoice> getInvoices1(String afm) {
        List<Invoice> invoiceList = new ArrayList<>();

        try (Connection conn = getConnection()) {
            // 1. Βρίσκουμε όλα τα ονόματα πινάκων που ταιριάζουν με το pattern
            List<String> tables = new ArrayList<>();
            String tableQuery = "SELECT TABLE_NAME " +
                    "FROM MEGASOFT.INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME LIKE 'E12_Emp016_%'";

            try (PreparedStatement pstmt = conn.prepareStatement(tableQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            if (tables.isEmpty()) {
                return invoiceList; // δεν υπάρχουν πίνακες που να ταιριάζουν
            }

            // 2. Φτιάχνουμε δυναμικό SQL με UNION ALL
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < tables.size(); i++) {
                if (i > 0) queryBuilder.append(" UNION ALL ");
                queryBuilder.append("SELECT DatePar, PerigrafhPar, SeiraPar, ArPar, SynTeliko, PerPlhromis, Parat1 ")
                        .append("FROM MEGASOFT.dbo.").append(tables.get(i))
                        .append(" WHERE PelProm = 1 AND AfmPel = ?");
            }
            queryBuilder.append(" ORDER BY DatePar DESC, ArPar DESC");

            String finalQuery = queryBuilder.toString();

            // 3. Προετοιμάζουμε PreparedStatement (ένα ? για κάθε πίνακα)
            try (PreparedStatement pstmt = conn.prepareStatement(finalQuery)) {
                for (int i = 0; i < tables.size(); i++) {
                    pstmt.setString(i + 1, afm);
                }

                // 4. Εκτελούμε query
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Date sqlDate = rs.getDate("DatePar");
                        String date = (sqlDate != null) ? String.valueOf(sqlDate.toLocalDate()) : null;

                        String description = rs.getString("PerigrafhPar");
                        String series = rs.getString("SeiraPar");
                        String number = rs.getString("ArPar");
                        String total = rs.getString("SynTeliko");
                        String paid = rs.getString("PerPlhromis");
                        String note = rs.getString("Parat1");

                        String seriesAndNumber = (series != null ? series : "") + " " + number;

                        Invoice invoice = new Invoice(date, description, seriesAndNumber, total, paid, note);
                        invoiceList.add(invoice);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoiceList;
    }
}