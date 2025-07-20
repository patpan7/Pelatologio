package org.easytech.pelatologio.helper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ActiveCallState {
    private static String pendingCallerId = null;
    private static final PropertyChangeSupport support = new PropertyChangeSupport(ActiveCallState.class);

    public static String getPendingCallerId() {
        return pendingCallerId;
    }

    public static boolean hasPendingCall() {
        return pendingCallerId != null;
    }

    public static void setPendingCall(String callerId) {
        String oldCallerId = ActiveCallState.pendingCallerId;
        ActiveCallState.pendingCallerId = callerId;
        support.firePropertyChange("pendingCall", oldCallerId, callerId);
    }

    public static void clearPendingCall() {
        setPendingCall(null);
    }

    public static void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public static void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}