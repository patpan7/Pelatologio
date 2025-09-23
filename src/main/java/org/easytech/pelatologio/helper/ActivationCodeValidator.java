package org.easytech.pelatologio.helper;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class ActivationCodeValidator {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final int MAX_SEARCH_DAYS = 731; // 2 years + 1 day buffer

    public static ValidationResult validate(String activationCode, String deviceId, String secretKey) {
        String formattedCode = activationCode.toUpperCase().replace("-", "");

        for (int i = 0; i < MAX_SEARCH_DAYS; i++) {
            String potentialDate = getDateAfterDays(i);
            String generatedCode = generateCodeForDate(deviceId, potentialDate, secretKey);

            if (generatedCode != null && generatedCode.equals(formattedCode)) {
                return new ValidationResult(true, potentialDate);
            }
        }
        return new ValidationResult(false, null);
    }

    private static String generateCodeForDate(String deviceId, String date, String secretKey) {
        try {
            String data = deviceId + "|" + date;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String hexString = toHexString(hmacBytes);
            return hexString.substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return null;
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

    private static String getDateAfterDays(int days) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, days);
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public record ValidationResult(boolean isValid, String expiryDate) {
    }
}