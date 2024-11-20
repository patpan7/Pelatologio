package org.easytech.pelatologio;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import javax.swing.*;

public class TAPIListener {
    private PointerByReference hLineApp = new PointerByReference();
    private PointerByReference hLine = new PointerByReference();


    public void initTAPI() {
        IntByReference numDevs = new IntByReference();

        int result = TAPI.INSTANCE.lineInitialize(hLineApp, null, this::lineCallbackFunction, "Pelatologio TAPI Listener", numDevs);
        if (result != 0) {
            System.err.println("Failed to initialize TAPI. Error code: " + result);
            return;
        }
        System.out.println("TAPI Initialized. Number of devices: " + numDevs.getValue());

        // Άνοιγμα της γραμμής
        result = TAPI.INSTANCE.lineOpen(
                hLineApp.getValue(),
                0, // Χρησιμοποιούμε τη συσκευή με ID 0
                hLine,
                0x00020000, // API version
                0,
                null,
                0x00000001, // privileges
                0xFFFFFFFF, // media modes
                new LineCallParams()
        );
        if (result != 0) {
            System.err.println("Failed to open TAPI line. Error code: " + result);
            return;
        }
        System.out.println("TAPI Line Opened.");
    }

    // Νέα μέθοδος για να ξεκινήσεις να ακούς για κλήσεις
    public void startListening() {
        // Αρχικοποίηση και εκκίνηση του listener
        initTAPI();

        // Εδώ μπορείς να προσθέσεις τον κώδικα για να συνεχίζει το πρόγραμμα να ακούει για κλήσεις
        // για παράδειγμα, χρησιμοποιώντας έναν βρόχο (loop) ή μια άλλης μορφής διαχείριση της ροής
        // του προγράμματος που να κρατά το TAPI "ανοιχτό" και να ακούει.
        // Εδώ το κάνουμε απλά με ένα βρόχο, αλλά μπορείς να το προσαρμόσεις.
        while (true) {
            try {
                Thread.sleep(1000); // Κρατάει το πρόγραμμα ενεργό για να ακούει τις κλήσεις
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdownTAPI() {
        TAPI.INSTANCE.lineClose(hLine.getValue());
        TAPI.INSTANCE.lineShutdown(hLineApp.getValue());
        System.out.println("TAPI Shut Down.");
    }

    private void onLineEvent(int hDevice, int dwMessage, int dwCallbackInstance, int dwParam1, int dwParam2, int dwParam3) {
        System.out.println("onlineevent");
        if (dwMessage == TAPIConstants.LINE_CALLSTATE) {
            if (dwParam1 == TAPIConstants.LINECALLSTATE_OFFERING) {
                System.out.println("Incoming call detected.");

                // Λήψη πληροφοριών κλήσης
                LINECALLINFO callInfo = new LINECALLINFO();
                callInfo.dwTotalSize = callInfo.size();

                int result = TAPI.INSTANCE.lineGetCallInfo(new HANDLE(Pointer.createConstant(hDevice)), callInfo);
                if (result == 0) {
                    if (callInfo.dwCallerIDSize > 0) {
                        Pointer pointer = callInfo.getPointer();
                        String callerID = new String(pointer.getByteArray(callInfo.dwCallerIDOffset, callInfo.dwCallerIDSize));
                        System.out.println("Incoming call from: " + callerID);
                        showPopup(callerID);
                    } else {
                        System.out.println("No caller ID available");
                    }
                } else {
                    System.err.println("Failed to get call info, error code: " + result);
                }
            }
        }
    }

    public void lineCallbackFunction(long hDevice, long dwMsg, long dwCallbackInstance,
                                     long dwParam1, long dwParam2, long dwParam3) {
        System.out.println("lineCallbackFunction called: dwMsg=" + dwMsg +
                ", dwParam1=" + dwParam1 +
                ", dwParam2=" + dwParam2 +
                ", dwParam3=" + dwParam3);
        // Κλήση της onLineEvent για διαχείριση γεγονότων
        onLineEvent((int) hDevice, (int) dwMsg, (int) dwCallbackInstance, (int) dwParam1, (int) dwParam2, (int) dwParam3);

    }

    private void showPopup(String callerID) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, "Incoming call from: " + callerID);
        });
    }
}