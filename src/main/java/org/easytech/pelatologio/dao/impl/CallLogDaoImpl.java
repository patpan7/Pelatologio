package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.CallLogDao;
import org.easytech.pelatologio.models.CallLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CallLogDaoImpl implements CallLogDao {

    private final HikariDataSource dataSource;

    public CallLogDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void insertCallLog(CallLog callLog) throws SQLException {
        String sql = "INSERT INTO CallLogs (customerId, callerNumber, callerName, callType, startTime, appUser) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, callLog.getCustomerId());
            pstmt.setString(2, callLog.getCallerNumber());
            pstmt.setString(3, callLog.getCallerName());
            pstmt.setString(4, callLog.getCallType());
            pstmt.setTimestamp(5, Timestamp.valueOf(callLog.getStartTime()));
            pstmt.setString(6, callLog.getAppUser());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    callLog.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void updateCallLog(CallLog callLog) throws SQLException {
        String sql = "UPDATE CallLogs SET callType = ?, endTime = ?, durationSeconds = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, callLog.getCallType());
            pstmt.setTimestamp(2, Timestamp.valueOf(callLog.getEndTime()));
            pstmt.setLong(3, callLog.getDurationSeconds());
            pstmt.setString(4, callLog.getNotes());
            pstmt.setInt(5, callLog.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<CallLog> getCallLogs() throws SQLException {
        List<CallLog> callLogs = new ArrayList<>();
        String sql = "SELECT * FROM CallLogs ORDER BY startTime DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CallLog callLog = new CallLog();
                    callLog.setId(rs.getInt("id"));
                    callLog.setCallerNumber(rs.getString("callerNumber"));
                    callLog.setCallerName(rs.getString("callerName"));
                    callLog.setCallType(rs.getString("callType"));
                    callLog.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
                    if (rs.getTimestamp("endTime") != null) {
                        callLog.setEndTime(rs.getTimestamp("endTime").toLocalDateTime());
                    }
                    callLog.setDurationSeconds(rs.getLong("durationSeconds"));
                    callLog.setCustomerId(rs.getInt("customerId"));
                    callLog.setNotes(rs.getString("notes"));
                    callLogs.add(callLog);
                }
            }
        }
        return callLogs;
    }

    @Override
    public List<CallLog> getCallLogsByCustomerId(int customerId) throws SQLException {
        List<CallLog> callLogs = new ArrayList<>();
        String sql = "SELECT * FROM CallLogs WHERE customerId = ? ORDER BY startTime DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CallLog callLog = new CallLog();
                    callLog.setId(rs.getInt("id"));
                    callLog.setCallerNumber(rs.getString("callerNumber"));
                    callLog.setCallerName(rs.getString("callerName"));
                    callLog.setCallType(rs.getString("callType"));
                    callLog.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
                    if (rs.getTimestamp("endTime") != null) {
                        callLog.setEndTime(rs.getTimestamp("endTime").toLocalDateTime());
                    }
                    callLog.setDurationSeconds(rs.getLong("durationSeconds"));
                    callLog.setCustomerId(rs.getInt("customerId"));
                    callLog.setNotes(rs.getString("notes"));
                    callLogs.add(callLog);
                }
            }
        }
        return callLogs;
    }

    @Override
    public void deleteCallLog(int callLogId) throws SQLException {
        String sql = "DELETE FROM CallLogs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, callLogId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<CallLog> getRecentCalls(int limit) {
        List<CallLog> callLogs = new ArrayList<>();
        String sql = "SELECT cl.id, cl.customerId, cl.callerNumber, cl.callerName, cl.callType, cl.startTime, cl.endTime, cl.durationSeconds, cl.notes, c.name AS customerName " +
                     "FROM CallLogs cl " +
                     "LEFT JOIN Customers c ON cl.customerId = c.code " +
                     "ORDER BY cl.startTime DESC " +
                     "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CallLog callLog = new CallLog();
                    callLog.setId(rs.getInt("id"));
                    callLog.setCustomerId(rs.getInt("customerId"));
                    callLog.setCallerNumber(rs.getString("callerNumber"));
                    callLog.setCallerName(rs.getString("callerName"));
                    callLog.setCallType(rs.getString("callType"));
                    callLog.setStartTime(rs.getTimestamp("startTime").toLocalDateTime());
                    if (rs.getTimestamp("endTime") != null) {
                        callLog.setEndTime(rs.getTimestamp("endTime").toLocalDateTime());
                    }
                    callLog.setDurationSeconds(rs.getLong("durationSeconds"));
                    callLog.setNotes(rs.getString("notes"));
                    // Set customer name from join
                    callLog.setCustomerName(rs.getString("customerName"));
                    callLogs.add(callLog);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return callLogs;
    }
}
