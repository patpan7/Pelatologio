package org.easytech.pelatologio.helper;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Enumeration;

public class HardwareIdUtil {

    private static String cachedId; // Stores the full Base64 ID
    private static String cachedDisplayId; // Stores the user-friendly ID

    public static String getHardwareId() {
        if (cachedId != null) {
            return cachedId;
        }
        byte[] rawId = generateRawHardwareId();
        cachedId = Base64.getEncoder().encodeToString(rawId);
        return cachedId;
    }

    public static String getDisplayHardwareId() {
        if (cachedDisplayId != null) {
            return cachedDisplayId;
        }
        byte[] rawId = generateRawHardwareId();
        // Take the first 8 bytes (16 hex characters) for display
        byte[] displayBytes = new byte[8];
        System.arraycopy(rawId, 0, displayBytes, 0, 8);
        cachedDisplayId = toHexString(displayBytes).toUpperCase();
        return cachedDisplayId;
    }

    private static byte[] generateRawHardwareId() {
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
                    return hashString(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String fallback = System.getProperty("os.name") + System.getProperty("user.name") + System.getProperty("os.arch");
        return hashString(fallback);
    }

    private static byte[] hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
