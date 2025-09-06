package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.CallLog;
import java.sql.SQLException;
import java.util.List;

public interface CallLogDao {
    void insertCallLog(CallLog callLog) throws SQLException;
    void updateCallLog(CallLog callLog) throws SQLException;
    List<CallLog> getCallLogs() throws SQLException;
    List<CallLog> getCallLogsByCustomerId(int customerId) throws SQLException;
    void deleteCallLog(int callLogId) throws SQLException;
    List<CallLog> getRecentCalls(int limit);

    boolean hasCalls(int code);
}
