package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.MegasoftDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MegasoftDaoImpl implements MegasoftDao {

    private final HikariDataSource dataSource;

    public MegasoftDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void syncMegasoft() {
        String SQL = "MERGE INTO [Pelatologio].[dbo].[Customers] AS target\n" +
                "USING (\n" +
                "    SELECT TOP 100 PERCENT\n" +
                "        COALESCE(BusinessTitle, '') AS BusinessTitle,\n" +
                "        COALESCE(Company, '') AS name, \n" +
                "        COALESCE(Business, '') AS job, \n" +
                "        COALESCE(Afm, '0000000000') AS afm,  -- Αντικαθιστούμε το κενό με placeholder\n" +
                "        COALESCE(Tel_1, '') AS phone1, \n" +
                "        COALESCE(Tel_2, '') AS phone2, \n" +
                "        COALESCE(Mobile, '') AS mobile, \n" +
                "        COALESCE(Address_1, '') AS address, \n" +
                "        COALESCE(City_1, '') AS city1, \n" +
                "        COALESCE(zip_1, '') AS zip1, \n" +
                "        COALESCE(Email, '') AS mail1\n" +
                "    FROM Megasoft.dbo.E2_Emp065_25\n" +
                "    INNER JOIN Megasoft.dbo.E2_Emp001_25 ON Megasoft.dbo.E2_Emp001_25.pelid = Megasoft.dbo.E2_Emp065_25.pelid\n" +
                "    WHERE Afm IS NOT NULL AND Afm != ''\n" +
                "    ORDER BY Kwd  -- Ταξινόμηση βάσει του Kwd για διατήρηση της σειράς\n" +
                ") AS source\n" +
                "ON (target.afm = source.afm)\n" +
                "WHEN MATCHED THEN\n" +
                "    UPDATE SET\n" +
                "        target.name = source.name,\n" +
                "        target.title = source.BusinessTitle,\n" +
                "        target.job = source.job,\n" +
                "        target.phone1 = source.phone1,\n" +
                "        target.phone2 = source.phone2,\n" +
                "        target.mobile = source.mobile,\n" +
                "        target.address = source.address,\n" +
                "        target.town = source.city1,\n" +
                "        target.postcode = source.zip1,\n" +
                "        target.email = source.mail1\n" +
                "WHEN NOT MATCHED THEN\n" +
                "    INSERT (name, title, job, afm, phone1, phone2, mobile, address, town, postcode, email)\n" +
                "    VALUES (source.name, source.BusinessTitle, source.job, source.afm, source.phone1, source.phone2, source.mobile, source.address, source.city1, source.zip1, source.mail1);";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getMegasoftBalance(String afm) {
        String query = "SELECT ypol FROM Megasoft.dbo.E2_Emp001_25 WHERE Afm = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, afm);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ypol");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getMegasoftBalance1(String afm) {
        String query = "SELECT ypol FROM Megasoft.dbo.E12_Emp001_27 WHERE Afm = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, afm);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ypol");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}