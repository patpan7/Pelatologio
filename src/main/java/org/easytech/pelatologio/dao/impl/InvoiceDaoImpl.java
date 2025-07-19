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
        List<Invoice> invoceList = new ArrayList<>();
        String query = "SELECT DatePar, PerigrafhPar, SeiraPar, ArPar, SynTeliko, PerPlhromis, Parat1 " +
                "FROM MEGASOFT.dbo.E2_Emp016_25 " +
                "WHERE PelProm = 1 AND AfmPel = ? " +
                "ORDER BY DatePar DESC, ArPar DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, afm);
            ResultSet rs = pstmt.executeQuery();
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
                invoceList.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoceList;
    }
}