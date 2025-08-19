package org.easytech.pelatologio.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

    public static void setFeatureEnabled(String featureName, boolean enabled) {
        FEATURE_FLAGS.put(featureName, enabled);
        saveFeatureFlags();
    }

    private static void saveFeatureFlags() {
        try {
            Properties prop = new Properties();
            for (Map.Entry<String, Boolean> entry : FEATURE_FLAGS.entrySet()) {
                prop.setProperty(entry.getKey(), entry.getValue().toString());
            }

            // Get the path to the properties file
            java.net.URL url = Features.class.getResource(PROPERTIES_FILE);
            if (url == null) {
                System.err.println("Cannot save feature flags: properties file not found in classpath.");
                return;
            }
            java.io.File file = new java.io.File(url.toURI());
            try (java.io.OutputStream output = new java.io.FileOutputStream(file)) {
                prop.store(output, "Feature Flags");
                System.out.println("Feature flags saved.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}