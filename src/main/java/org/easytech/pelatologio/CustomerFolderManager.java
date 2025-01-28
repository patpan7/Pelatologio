package org.easytech.pelatologio;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class CustomerFolderManager {
    // Δημιουργία ή έλεγχος του φακέλου πελάτη
    public File customerFolder(String customerName, String afm) {
        String folderPath = AppSettings.loadSetting("datafolder") + customerName + "_" + afm;
        File folder = new File(folderPath);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Ο φάκελος δημιουργήθηκε: " + folderPath);
            } else {
                System.err.println("Αποτυχία δημιουργίας του φακέλου.");
                return null; // Επιστροφή null σε περίπτωση αποτυχίας
            }
        } else {
            System.out.println("Ο φάκελος υπάρχει ήδη: " + folderPath);
        }

        return folder; // Επιστροφή του φακέλου
    }

    // Δημιουργία ή άνοιγμα του φακέλου πελάτη
    public File createOrOpenCustomerFolder(String customerName, String afm) {
        File folder = customerFolder(customerName, afm);

        if (folder != null) {
            try {
                Desktop.getDesktop().open(folder);
                System.out.println("Ο φάκελος άνοιξε: " + folder.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Αποτυχία ανοίγματος του φακέλου.");
                e.printStackTrace();
            }
        }

        return folder; // Επιστροφή του φακέλου
    }
}
