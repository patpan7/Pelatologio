package org.easytech.pelatologio;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.*;

public interface TAPI extends StdCallLibrary {
    TAPI INSTANCE = Native.load("tapi32", TAPI.class);

    // Αντί να περνάμε Pointer στην μέθοδο lineGetCallInfo, χρησιμοποιούμε long.
    int lineInitialize(PointerByReference hLineApp, Pointer hInstance, LineCallback lpfnCallback, String appName, IntByReference numDevs);

    int lineShutdown(Pointer hLineApp);
    int lineOpen(Pointer hLineApp, int deviceID, PointerByReference hLine, int apiVersion, int extVersion, Pointer hCallbackInstance, int privileges, int mediaModes, LineCallParams params);
    int lineClose(Pointer hLine);
    int lineAnswer(Pointer hCall, byte[] userUserInfo, int size);
    int lineGetCallInfo(long hCall, LINECALLINFO callInfo);  // Χρησιμοποιούμε long εδώ αντί για HANDLE.

    // Callback interface για την γραμμή TAPI
    interface LineCallback extends StdCallCallback {
        void callback(int hDevice, int dwMessage, int dwCallbackInstance, int dwParam1, int dwParam2, int dwParam3);
    }
}
