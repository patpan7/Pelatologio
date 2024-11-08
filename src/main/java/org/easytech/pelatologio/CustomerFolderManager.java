package org.easytech.pelatologio;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class CustomerFolderManager {
    public void createOrOpenCustomerFolder(String customerName, String afm) {
        String folderPath = AppSettings.loadSetting("datafolder") + customerName + "_" + afm;
        File folder = new File(folderPath);

        // Δημιουργία του φακέλου αν δεν υπάρχει
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Ο φάκελος δημιουργήθηκε: " + folderPath);
            } else {
                System.out.println("Αποτυχία δημιουργίας του φακέλου.");
                return;
            }
        } else {
            System.out.println("Ο φάκελος υπάρχει ήδη: " + folderPath);
        }

        // Άνοιγμα του φακέλου
        try {
            Desktop.getDesktop().open(folder);
            System.out.println("Ο φάκελος άνοιξε: " + folderPath);
        } catch (IOException e) {
            System.out.println("Αποτυχία ανοίγματος του φακέλου.");
            e.printStackTrace();
        }
    }
}

