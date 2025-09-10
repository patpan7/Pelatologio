package org.easytech.pelatologio.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseSetup {

    public static boolean checkDatabaseExists(String dbName) throws Exception {
        String connectionUrl = "jdbc:sqlserver://" + AppSettings.getInstance().server + ";encrypt=false;";
        try (Connection con = DriverManager.getConnection(connectionUrl, AppSettings.getInstance().dbUser, AppSettings.getInstance().dbPass);
             Statement stmt = con.createStatement()) {
            String sql = "SELECT name FROM sys.databases WHERE name = N'" + dbName + "'";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next();
            }
        }
    }

    public static void runSetupScript(String dbName) throws Exception {
        // Connect to master to create the database
        String masterConnectionUrl = "jdbc:sqlserver://" + AppSettings.getInstance().server + ";encrypt=false;";
        try (Connection con = DriverManager.getConnection(masterConnectionUrl, AppSettings.getInstance().dbUser, AppSettings.getInstance().dbPass);
             Statement stmt = con.createStatement()) {
            String sql = "CREATE DATABASE [" + dbName + "]";
            stmt.executeUpdate(sql);
        }

        // Connect to the newly created database to create tables
        String dbConnectionUrl = "jdbc:sqlserver://" + AppSettings.getInstance().server + ";databaseName=" + dbName + ";encrypt=false;";
        try (Connection con = DriverManager.getConnection(dbConnectionUrl, AppSettings.getInstance().dbUser, AppSettings.getInstance().dbPass);
             Statement stmt = con.createStatement()) {

            InputStream inputStream = DatabaseSetup.class.getResourceAsStream("/setup.sql");
            if (inputStream == null) {
                throw new Exception("setup.sql not found in resources");
            }

            String scriptContent = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Split by "GO" statements
            String[] commands = scriptContent.split("\\s+GO\\s*");

            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
        }
    }
}

