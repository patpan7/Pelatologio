package org.easytech.pelatologio;

public class TAPIConstants {

    // Κατάστασεις κλήσεων
    public static final int LINE_CALLSTATE = 0x0400;
    public static final int LINECALLSTATE_OFFERING = 0x00000001;
    public static final int LINECALLSTATE_CONNECTED = 0x00000002;
    public static final int LINECALLSTATE_DISCONNECTED = 0x00000008;

    // Επιλογές για τα media modes
    public static final int LINEMEDIAMODE_INTERACTIVEVOICE = 0x00000001;

    // Επιλογές για τη γραμμή
    public static final int LINEBEARERMODE_VOICE = 0x00000001;

    // Άλλα constants, π.χ. δικαιώματα κλήσεων
    public static final int LINECALLPRIVILEGE_OWNER = 0x00000001;
}
