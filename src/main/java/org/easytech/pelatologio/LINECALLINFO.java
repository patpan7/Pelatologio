package org.easytech.pelatologio;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class LINECALLINFO extends Structure {
    public int dwTotalSize;           // Το συνολικό μέγεθος της δομής
    public int dwNeededSize;
    public int dwUsedSize;
    public int dwLineDeviceID;
    public int dwAddressID;
    public int dwBearerMode;
    public int dwRate;
    public int dwMediaMode;
    public int dwAppSpecific;
    public int dwCallID;
    public int dwRelatedCallID;
    public int dwCallParamFlags;
    public int dwCallStates;
    public int dwMonitorDigitModes;
    public int dwMonitorMediaModes;
    public int dwOrigin;
    public int dwReason;
    public int dwCompletionID;
    public int dwNumberOfOwners;
    public int dwNumberOfMonitors;
    public int dwCountryCode;
    public int dwCallerIDFlags;
    public int dwCallerIDSize;
    public int dwCallerIDOffset;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("dwTotalSize", "dwNeededSize", "dwUsedSize",
                "dwLineDeviceID", "dwAddressID", "dwBearerMode",
                "dwRate", "dwMediaMode", "dwAppSpecific",
                "dwCallID", "dwRelatedCallID", "dwCallParamFlags",
                "dwCallStates", "dwMonitorDigitModes", "dwMonitorMediaModes",
                "dwOrigin", "dwReason", "dwCompletionID",
                "dwNumberOfOwners", "dwNumberOfMonitors", "dwCountryCode",
                "dwCallerIDFlags", "dwCallerIDSize", "dwCallerIDOffset");
    }
}
