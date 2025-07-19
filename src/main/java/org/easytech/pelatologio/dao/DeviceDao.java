package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Device;
import java.sql.SQLException;
import java.util.List;

public interface DeviceDao {
    List<Device> getAllDevices();
    boolean isSerialUnique(String serial);
    boolean saveDevice(Device newDevice);
    Boolean updateDevice(Device device);
    List<Device> getCustomerDevices(int customerId);
    Boolean recoverDevice(int id);
    Boolean deleteDevice(int id);
    boolean isSerialAssigned(String serial, int customerId);
    boolean assignDevice(String serial, int customerId);
    List<String> getRates();
}