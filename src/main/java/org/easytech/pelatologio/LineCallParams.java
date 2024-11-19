package org.easytech.pelatologio;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class LineCallParams extends Structure {
    public int dwTotalSize;       // Το συνολικό μέγεθος της δομής
    public int dwBearerMode;      // Τρόπος μετάδοσης (π.χ., voice, data)
    public int dwMinRate;         // Ελάχιστο bitrate
    public int dwMaxRate;         // Μέγιστο bitrate
    public int dwMediaMode;       // Τρόπος media
    public int dwCallParamFlags;  // Flags για τη διαχείριση κλήσης
    public int dwAddressMode;     // Τρόπος διαχείρισης διεύθυνσης
    public int dwAddressID;       // Αναγνωριστικό διεύθυνσης

    // Άλλα πεδία που μπορεί να απαιτούνται...

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("dwTotalSize", "dwBearerMode", "dwMinRate", "dwMaxRate",
                "dwMediaMode", "dwCallParamFlags", "dwAddressMode", "dwAddressID");
    }

    // Δημιουργία του default constructor
    public LineCallParams() {
        this.dwTotalSize = size();  // Αυτόματα ορίζει το μέγεθος της δομής
        this.dwMediaMode = 0x00000008; // LINE_MEDIAMODE_INTERACTIVEVOICE
    }
}
