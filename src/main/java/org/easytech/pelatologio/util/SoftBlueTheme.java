package org.easytech.pelatologio.util;

import atlantafx.base.theme.Theme;

public class SoftBlueTheme implements Theme {

    @Override
    public String getName() {
        return "Soft Blue";
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("/org/easytech/pelatologio/soft-blue.css").toExternalForm();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }
}
