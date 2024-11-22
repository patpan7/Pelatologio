package org.easytech.pelatologio;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class LINEDEVCAPS extends Structure {
    public int dwTotalSize;
    public int dwNeededSize;
    public int dwUsedSize;
    public int dwProviderInfoSize;
    public int dwProviderInfoOffset;
    public int dwSwitchInfoSize;
    public int dwSwitchInfoOffset;
    public int dwPermanentLineID;
    public int dwLineNameSize;
    public int dwLineNameOffset;
    public int dwStringFormat;
    public int dwAddressModes;
    public int dwNumAddresses;
    public int dwBearerModes;
    public int dwMaxRate;
    public int dwMediaModes;
    public int dwGenerateToneModes;
    public int dwGenerateToneMaxNumFreq;
    public int dwGenerateDigitModes;
    public int dwMonitorToneMaxNumFreq;
    public int dwMonitorToneMaxNumEntries;
    public int dwMonitorDigitModes;
    public int dwGatherDigitsMinTimeout;
    public int dwGatherDigitsMaxTimeout;
    public int dwMedCtlDigitMaxListSize;
    public int dwMedCtlMediaMaxListSize;
    public int dwMedCtlToneMaxListSize;
    public int dwMedCtlCallStateMaxListSize;
    public int dwDevCapFlags;
    public int dwMaxNumActiveCalls;
    public int dwAnswerMode;
    public int dwRingModes;
    public int dwLineStates;
    public int dwUUIAcceptSize;
    public int dwUUIAnswerSize;
    public int dwUUIMakeCallSize;
    public int dwUUIDropSize;
    public int dwUUISendUserUserInfoSize;
    public int dwUUICallInfoSize;
    public int dwMinRate;
    //public int dwMaxRate;
    public byte[] dwDeviceClassesSize = new byte[32];
    public byte[] dwDeviceClassesOffset = new byte[32];
    public byte[] dwPhoneCapsFlags = new byte[32];
    public byte[] dwTerminalsSize = new byte[32];
    public byte[] dwTerminalsOffset = new byte[32];
    public byte[] dwTerminalModes = new byte[32];

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "dwTotalSize", "dwNeededSize", "dwUsedSize", "dwProviderInfoSize", "dwProviderInfoOffset",
                "dwSwitchInfoSize", "dwSwitchInfoOffset", "dwPermanentLineID", "dwLineNameSize", "dwLineNameOffset",
                "dwStringFormat", "dwAddressModes", "dwNumAddresses", "dwBearerModes", "dwMaxRate",
                "dwMediaModes", "dwGenerateToneModes", "dwGenerateToneMaxNumFreq", "dwGenerateDigitModes",
                "dwMonitorToneMaxNumFreq", "dwMonitorToneMaxNumEntries", "dwMonitorDigitModes",
                "dwGatherDigitsMinTimeout", "dwGatherDigitsMaxTimeout", "dwMedCtlDigitMaxListSize",
                "dwMedCtlMediaMaxListSize", "dwMedCtlToneMaxListSize", "dwMedCtlCallStateMaxListSize",
                "dwDevCapFlags", "dwMaxNumActiveCalls", "dwAnswerMode", "dwRingModes", "dwLineStates",
                "dwUUIAcceptSize", "dwUUIAnswerSize", "dwUUIMakeCallSize", "dwUUIDropSize",
                "dwUUISendUserUserInfoSize", "dwUUICallInfoSize", "dwMinRate",
                "dwDeviceClassesSize", "dwDeviceClassesOffset", "dwPhoneCapsFlags",
                "dwTerminalsSize", "dwTerminalsOffset", "dwTerminalModes"
        );
    }
}
