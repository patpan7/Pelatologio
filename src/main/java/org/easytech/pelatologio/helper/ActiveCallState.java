package org.easytech.pelatologio.helper;

public class ActiveCallState {

    private static String currentCallerId = null;

    public static synchronized String getCurrentCallerId() {
        return currentCallerId;
    }

    public static synchronized void setCurrentCallerId(String callerId) {
        currentCallerId = callerId;
    }

    public static synchronized boolean isCallActive() {
        return currentCallerId != null;
    }

    public static synchronized void clearCall() {
        currentCallerId = null;
    }
}
