package org.easytech.pelatologio.helper;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Enumeration;

public class HardwareIdUtil {

    private static String cachedId;

    public static String getHardwareId() {
        if (cachedId != null) {
            return cachedId;
        }

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && ni.getHardwareAddress() != null) {
                    byte[] mac = ni.getHardwareAddress();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    cachedId = hashString(sb.toString());
                    return cachedId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String fallback = System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("os.arch");
        cachedId = hashString(fallback);
        return cachedId;
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static String getFormattedHardwareId() {
        String rawId = getHardwareId();
        if (rawId == null || rawId.length() < 16) {
            return rawId; // Return as is if too short or null
        }
        // Format: XXXXXXXX-XXXXXXXX-XXXXXXXX-XXXXXXXX
        return rawId.substring(0, 8) + "-" +
               rawId.substring(8, 16) + "-" +
               rawId.substring(16, 24) + "-" +
               rawId.substring(24, 32);
    }
}
