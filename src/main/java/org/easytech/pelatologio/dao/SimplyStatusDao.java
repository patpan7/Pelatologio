package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.SimplyStatus;

import java.util.List;

public interface SimplyStatusDao {
    void updateSimplyStatus(int appLoginId, String columnName, boolean newVal);
    void updateSimplyStatusYears(int appLoginId, String selectedYear);
    boolean getSimpyStatus(int appLoginId, String columnName);
    String getSimplyYears(int appLoginId);
    List<SimplyStatus> getAllSimplyStatus();
    void addSimplySetupProgress(int loginId);
}