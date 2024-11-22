package org.easytech.pelatologio;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
//import com.sun.jna.win32.StdCallCallback;

public interface TAPI extends StdCallLibrary {
    TAPI INSTANCE = Native.load("tapi32", TAPI.class);

    // Αρχικοποίηση TAPI
    int lineInitialize(PointerByReference hLineApp, Pointer hInstance, LineCallback lpfnCallback, String appName, IntByReference numDevs);

    // Τερματισμός TAPI
    int lineShutdown(Pointer hLineApp);

    // Άνοιγμα γραμμής
    int lineOpen(Pointer hLineApp, int deviceID, PointerByReference hLine, int apiVersion, int extVersion, Pointer hCallbackInstance, int privileges, int mediaModes, LineCallParams params);

    // Κλείσιμο γραμμής
    int lineClose(Pointer hLine);

    // Απάντηση σε κλήση
    int lineAnswer(Pointer hCall, byte[] userUserInfo, int size);

    // Λήψη πληροφοριών κλήσης
    int lineGetCallInfo(HANDLE hCall, LINECALLINFO callInfo);

    // Λήψη δυνατοτήτων συσκευής
    int lineGetDevCaps(Pointer hLineApp, int deviceID, int apiVersion, int extVersion, LINEDEVCAPS devCaps);

    // Callback interface για TAPI γεγονότα
    interface LineCallback extends StdCallCallback {
        void callback(int hDevice, int dwMessage, int dwCallbackInstance, int dwParam1, int dwParam2, int dwParam3);
    }
}
