package org.easytech.pelatologio.helper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ActiveCallState {

    private static String pendingCallNumber;
    private static final PropertyChangeSupport support = new PropertyChangeSupport(ActiveCallState.class);

    public static boolean hasPendingCall() {
        return pendingCallNumber != null;
    }

    public static String getPendingCallNumber() {
        return pendingCallNumber;
    }

    public static void setPendingCall(String callerId) {
        String oldNumber = pendingCallNumber;
        pendingCallNumber = callerId;
        support.firePropertyChange("pendingCallNumber", oldNumber, pendingCallNumber);
    }
    
    public static void clearPendingCall() {
        String oldNumber = pendingCallNumber;
        pendingCallNumber = null;
        support.firePropertyChange("pendingCallNumber", oldNumber, null);
    }

    public static void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public static void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
