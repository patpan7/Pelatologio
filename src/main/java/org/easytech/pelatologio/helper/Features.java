package org.easytech.pelatologio.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Features {

    private static final Map<String, Boolean> FEATURE_FLAGS = new HashMap<>();
    private static final String PROPERTIES_FILE = "/features.properties";

    static {
        loadFeatureFlags();
    }

    public static void loadFeatureFlags() {
        try (InputStream input = Features.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + PROPERTIES_FILE);
                return;
            }
            Properties prop = new Properties();
            prop.load(input);
            for (String key : prop.stringPropertyNames()) {
                FEATURE_FLAGS.put(key, Boolean.parseBoolean(prop.getProperty(key)));
            }
            System.out.println("Feature flags loaded: " + FEATURE_FLAGS);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isEnabled(String featureName) {
        return FEATURE_FLAGS.getOrDefault(featureName, false); // Default to false if feature not found
    }
}