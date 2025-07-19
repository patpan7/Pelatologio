package org.easytech.pelatologio.dao;

import java.time.LocalDate;
import java.util.List;

public interface TrackingDao {
    void saveTrackingNumber(String tracking, LocalDate date, int customerId);
    List<String> getTrackingNumbers(int customerId);
}