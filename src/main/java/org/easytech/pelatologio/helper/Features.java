package org.easytech.pelatologio.helper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Features {

    private static final Map<String, Boolean> FEATURE_FLAGS = new HashMap<>();
    private static final String INTERNAL_PROPERTIES_FILE = "/features.properties"; // Defaults inside JAR
    private static final String EXTERNAL_PROPERTIES_FILE = "features.properties"; // User settings outside JAR

    static {
        loadFeatureFlags();
    }

    public static void loadFeatureFlags() {
        Properties props = new Properties();

        // 1. Load default features from inside the JAR
        try (InputStream input = Features.class.getResourceAsStream(INTERNAL_PROPERTIES_FILE)) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("Default properties file not found: " + INTERNAL_PROPERTIES_FILE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // 2. Load user-defined features from external file (overwrites defaults)
        try (InputStream externalInput = new FileInputStream(EXTERNAL_PROPERTIES_FILE)) {
            props.load(externalInput);
        } catch (FileNotFoundException e) {
            System.out.println("No external features file found. Using defaults.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // 3. Populate the map
        for (String key : props.stringPropertyNames()) {
            FEATURE_FLAGS.put(key, Boolean.parseBoolean(props.getProperty(key)));
        }
        System.out.println("Feature flags loaded: " + FEATURE_FLAGS);
    }

    public static boolean isEnabled(String featureName) {
        return FEATURE_FLAGS.getOrDefault(featureName, false); // Default to false if feature not found
    }

    public static void setFeatureEnabled(String featureName, boolean enabled) {
        FEATURE_FLAGS.put(featureName, enabled);
        saveFeatureFlags();
    }

    private static void saveFeatureFlags() {
        Properties prop = new Properties();
        for (Map.Entry<String, Boolean> entry : FEATURE_FLAGS.entrySet()) {
            prop.setProperty(entry.getKey(), entry.getValue().toString());
        }

        // Write to the external file in the same directory as the application
        try (OutputStream output = new FileOutputStream(EXTERNAL_PROPERTIES_FILE)) {
            prop.store(output, "User-defined Feature Flags");
            System.out.println("Feature flags saved to " + EXTERNAL_PROPERTIES_FILE);
        } catch (IOException io) {
            io.printStackTrace();
            System.err.println("Failed to save feature flags to external file.");
        }
    }
}
