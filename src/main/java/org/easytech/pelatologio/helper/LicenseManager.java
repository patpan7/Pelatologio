package org.easytech.pelatologio.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.prefs.Preferences;

/**
 * Manages the entire lifecycle of the license on the client-side for DESKTOP.
 * Includes activation, validation, and clock tampering detection.
 */
public class LicenseManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(LicenseManager.class);

    private static final String KEY_ACTIVATION_CODE = "act_code";
    private static final String KEY_SECRET = "app_sec";
    private static final String KEY_EXPIRY_DATE = "exp_date";
    private static final String KEY_LAST_SEEN_DATE = "last_seen";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

    static {
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC+2"));
    }

    public boolean activate(String activationCode, String secretKey) {
        String deviceId = HardwareIdUtil.getDisplayHardwareId();
        ActivationCodeValidator.ValidationResult result = ActivationCodeValidator.validate(activationCode, deviceId, secretKey);

        if (result.isValid()) {
            System.out.println("LicenseManager: Activation successful. Expiry Date: " + result.getExpiryDate());
            prefs.put(KEY_ACTIVATION_CODE, activationCode);
            prefs.put(KEY_SECRET, secretKey);
            prefs.put(KEY_EXPIRY_DATE, result.getExpiryDate());
            prefs.put(KEY_LAST_SEEN_DATE, DATE_FORMATTER.format(new Date()));
            return true;
        }
        System.out.println("LicenseManager: Activation failed.");
        return false;
    }

    public boolean isLicenseValid() {
        String expiryDateStr = prefs.get(KEY_EXPIRY_DATE, null);
        String lastSeenDateStr = prefs.get(KEY_LAST_SEEN_DATE, null);
        System.out.println("LicenseManager: Checking validity. Expiry: " + expiryDateStr + ", Last Seen: " + lastSeenDateStr);

        if (expiryDateStr == null || lastSeenDateStr == null) {
            System.out.println("LicenseManager: No expiry or last seen date found. Invalid.");
            return false;
        }

        try {
            Date expiryDate = DATE_FORMATTER.parse(expiryDateStr);
            Date lastSeenDate = DATE_FORMATTER.parse(lastSeenDateStr);
            Date currentDate = new Date();
            System.out.println("LicenseManager: Parsed dates - Expiry: " + expiryDate + ", Last Seen: " + lastSeenDate + ", Current: " + currentDate);

            if (currentDate.after(expiryDate)) {
                System.out.println("LicenseManager: Current date is after expiry date. Invalid.");
                return false;
            }

            if (currentDate.before(lastSeenDate)) {
                System.out.println("LicenseManager: Clock tampering detected (current date before last seen). Invalid.");
                return false;
            }

            prefs.put(KEY_LAST_SEEN_DATE, DATE_FORMATTER.format(currentDate));
            System.out.println("LicenseManager: License is valid. Updated last seen date.");
            return true;

        } catch (ParseException e) {
            System.err.println("LicenseManager: Date parsing error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getExpiryDate() {
        return prefs.get(KEY_EXPIRY_DATE, null);
    }

    public long getRemainingDays() {
        String expiryDateStr = getExpiryDate();
        if (expiryDateStr == null) {
            return 0;
        }
        try {
            Date expiryDate = DATE_FORMATTER.parse(expiryDateStr);
            Date currentDate = new Date();
            long diff = expiryDate.getTime() - currentDate.getTime();
            return diff / (24 * 60 * 60 * 1000);
        } catch (ParseException e) {
            return 0;
        }
    }
}
