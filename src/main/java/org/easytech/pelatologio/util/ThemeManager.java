package org.easytech.pelatologio.util;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.scene.Scene;
import org.easytech.pelatologio.helper.AppSettings;
import org.easytech.pelatologio.util.SoftBlueTheme;

import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static final String THEME_PREF_KEY = "app.theme";
    private static final String DARK_MODE_PREF_KEY = "app.darkMode";
    private static final Map<String, Theme> THEMES = new HashMap<>();

    static {
        // Initialize available themes
        THEMES.put("Nord Light", new NordLight());
        THEMES.put("Nord Dark", new NordDark());
        THEMES.put("Primer Light", new PrimerLight());
        THEMES.put("Primer Dark", new PrimerDark());
        THEMES.put("Cupertino Light", new CupertinoLight());
        THEMES.put("Cupertino Dark", new CupertinoDark());
        THEMES.put("Dracula", new Dracula());
        THEMES.put("Soft Blue", new SoftBlueTheme());
        THEMES.put("Soft Red", new SoftRedTheme());
        THEMES.put("Soft Green", new SoftGreenTheme());
    }

    public static String[] getAvailableThemes() {
        return THEMES.keySet().toArray(new String[0]);
    }

    public static void applyTheme(String themeName, Scene scene) {
        Theme theme = THEMES.get(themeName);
        if (theme != null) {
            Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
            if (scene != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(ThemeManager.class.getResource("/org/easytech/pelatologio/custom-atlantafx.css").toExternalForm());
            }
            AppSettings.saveSetting(THEME_PREF_KEY, themeName);
        }
    }

    public static void setDarkMode(boolean darkMode) {
        AppSettings.saveSetting(DARK_MODE_PREF_KEY, String.valueOf(darkMode));
        // Apply dark mode specific styles if needed
        // This can be expanded based on your theming needs
    }

    public static boolean isDarkMode() {
        String darkMode = AppSettings.loadSetting(DARK_MODE_PREF_KEY);
        return Boolean.parseBoolean(darkMode);
    }

    public static String getCurrentTheme() {
        return AppSettings.loadSetting(THEME_PREF_KEY);
    }

    public static void applySavedTheme(Scene scene) {
        String savedTheme = getCurrentTheme();
        if (savedTheme != null && !savedTheme.isEmpty()) {
            applyTheme(savedTheme, scene);
        } else {
            // Default theme
            applyTheme("Nord Light", scene);
        }
    }
}
