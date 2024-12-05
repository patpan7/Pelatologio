package org.easytech.pelatologio;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class SipTapiHandler implements Runnable {

    private ActiveXComponent siptapi;
    private boolean running;
    private PointerByReference hLineApp = new PointerByReference();
    private PointerByReference hLine = new PointerByReference();

    public SipTapiHandler() {
        // Δημιουργία αντικειμένου TAPI
        siptapi = new ActiveXComponent("TAPI.TAPI.1");
        running = false;
    }

    /**
     * Αρχικοποιεί το TAPI.
     */
    public void initialize() {
        try {
            Dispatch.call(siptapi, "Initialize");
            System.out.println("TAPI initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize TAPI: " + e.getMessage());
        }
        IntByReference numDevs = new IntByReference();
        int result = TAPI.INSTANCE.lineInitialize(hLineApp, null, this::lineCallbackFunction, "Pelatologio TAPI Listener", numDevs);

        if (result != 0) {
            System.err.println("Failed to initialize TAPI. Error code: " + result);
            return;
        }
        System.out.println("TAPI Initialized. Number of devices: " + numDevs.getValue());
        result = TAPI.INSTANCE.lineOpen(
                hLineApp.getValue(),
                0, // Χρησιμοποιούμε τη συσκευή με ID 0
                hLine,
                0x00020000, // API version
                0,
                null,
                0x00000001, // privileges
                0x00000004, // media modes
                new LineCallParams()
        );
        if (result != 0) {
            System.err.println("Failed to open TAPI line. Error code: " + result);
            return;
        }
        System.out.println("TAPI Line Opened.");
    }
    public void lineCallbackFunction(long hDevice, long dwMsg, long dwCallbackInstance,
                                     long dwParam1, long dwParam2, long dwParam3) {
        System.out.println("Callback received: dwMsg=" + dwMsg + ", dwParam1=" + dwParam1 +
                ", dwParam2=" + dwParam2 + ", dwParam3=" + dwParam3);

    }

    /**
     * Εγγράφει ειδοποιήσεις για εισερχόμενες κλήσεις.
     */
    public void registerCallNotifications() {
        try {
            Variant addresses = Dispatch.get(siptapi, "Addresses");
            if (addresses.isNull()) {
                System.err.println("No addresses available to register notifications.");
                return;
            }

            Dispatch addressCollection = addresses.toDispatch();
            int addressCount = Dispatch.call(addressCollection, "Count").getInt();
            System.out.println("Number of addresses available: " + addressCount);

            // Εγγραφή ειδοποιήσεων για την πρώτη διεύθυνση
            if (addressCount > 0) {
                Dispatch address = Dispatch.call(addressCollection, "Item", 1).toDispatch();
                Dispatch.call(siptapi, "RegisterCallNotifications", address, true, true, 0, 0);
                System.out.println("Call notifications registered successfully for address 1.");
            }
        } catch (Exception e) {
            System.err.println("Failed to register call notifications: " + e.getMessage());
        }
    }


    /**
     * Λήψη αριθμού καλούντος από ενεργές κλήσεις.
     */
    public String getCallerId() {
        try {
            // Λήψη όλων των CallHubs
            Variant callHubs = Dispatch.get(siptapi, "CallHubs");
            if (callHubs.isNull()) {
                System.err.println("No CallHubs available.");
                return null;
            }

            Dispatch callHubsDispatch = callHubs.toDispatch();
            int hubCount = Dispatch.call(callHubsDispatch, "Count").getInt();
            System.out.println("Number of CallHubs: " + hubCount);

            // Λήψη του πρώτου CallHub
            Variant firstCallHub = Dispatch.call(callHubsDispatch, "Item", 1);
            if (firstCallHub.isNull()) {
                System.err.println("No CallHub found.");
                return null;
            }

            Dispatch firstCallHubDispatch = firstCallHub.toDispatch();

            // Λήψη CallerId από το CallHub
            Variant callerId = Dispatch.get(firstCallHubDispatch, "CallerId");
            if (callerId.isNull()) {
                System.err.println("Caller ID is null.");
                return null;
            }

            return callerId.toString();
        } catch (Exception e) {
            System.err.println("Failed to get Caller ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Διακοπή παρακολούθησης και τερματισμός του TAPI.
     */
    public void shutdown() {
        running = false;
        try {
            Dispatch.call(siptapi, "Shutdown");
            System.out.println("TAPI shut down successfully.");
        } catch (Exception e) {
            System.err.println("Failed to shut down TAPI: " + e.getMessage());
        }
    }

    /**
     * Ελέγχει για εισερχόμενες κλήσεις και επιστρέφει τον αριθμό καλούντος.
     */
    public String monitorIncomingCalls() {
        try {
            // Απόκτησε τις διευθύνσεις (Addresses) που είναι συνδεδεμένες στο TAPI
            Variant addresses = Dispatch.get(siptapi, "Addresses");
            Dispatch addressCollection = addresses.toDispatch();
            int count = Dispatch.call(addressCollection, "Count").getInt();

            for (int i = 1; i <= count; i++) {
                // Λάβε την τρέχουσα διεύθυνση
                Dispatch address = Dispatch.call(addressCollection, "Item", i).toDispatch();

                // Ελέγξτε τις κλήσεις σε αυτή τη διεύθυνση
                Variant calls = Dispatch.get(address, "Calls");
                Dispatch callCollection = calls.toDispatch();
                int callCount = Dispatch.call(callCollection, "Count").getInt();

                for (int j = 1; j <= callCount; j++) {
                    // Πάρε την τρέχουσα κλήση
                    Dispatch call = Dispatch.call(callCollection, "Item", j).toDispatch();

                    // Ελέγξε αν η κλήση είναι εισερχόμενη
                    String state = Dispatch.get(call, "CallState").toString(); // π.χ., "CS_INCOMING"
                    if ("CS_INCOMING".equals(state)) {
                        // Λάβε τον αριθμό καλούντος
                        String callerId = Dispatch.get(call, "CallerId").toString();
                        return callerId;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to monitor incoming calls: " + e.getMessage());
        }
        return null;
    }

    public void monitorAddresses() {
        try {
            Variant addresses = Dispatch.get(siptapi, "Addresses");
            if (addresses.isNull()) {
                System.err.println("No addresses found.");
                return;
            }

            Dispatch addressCollection = addresses.toDispatch();
            int addressCount = Dispatch.call(addressCollection, "Count").getInt();
            System.out.println("Number of addresses: " + addressCount);

            for (int i = 1; i <= addressCount; i++) {
                Dispatch address = Dispatch.call(addressCollection, "Item", i).toDispatch();
                System.out.println("Checking address " + i);

                Variant calls = Dispatch.get(address, "Calls");
                if (calls.isNull()) {
                    System.out.println("No calls on address " + i);
                    continue;
                }

                Dispatch callCollection = calls.toDispatch();
                int callCount = Dispatch.call(callCollection, "Count").getInt();
                System.out.println("Number of calls on address " + i + ": " + callCount);

                for (int j = 1; j <= callCount; j++) {
                    Dispatch call = Dispatch.call(callCollection, "Item", j).toDispatch();
                    String state = Dispatch.get(call, "CallState").toString();
                    System.out.println("Call " + j + " state: " + state);

                    if ("CS_INCOMING".equals(state)) {
                        String callerId = Dispatch.get(call, "CallerId").toString();
                        System.out.println("Incoming call on address " + i + " from: " + callerId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error monitoring addresses: " + e.getMessage());
        }
    }


    /**
     * Παράλληλη παρακολούθηση κλήσεων σε thread.
     */
    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                monitorAddresses();
                String callerId = monitorIncomingCalls();
                if (callerId != null) {
                    System.out.println("Incoming call from: " + callerId);
                }

                // Περίμενε λίγο πριν τον επόμενο έλεγχο
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }
        System.out.println("Call monitoring thread stopped.");
    }

}
