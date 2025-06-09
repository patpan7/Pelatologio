package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.settings.AppSettings;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class CustomerFolderManager {

    // Εύρεση φακέλου με βάση το ΑΦΜ
    private File findFolderByAfm(String afm) {
        String dataFolderPath = AppSettings.loadSetting("datafolder");
        File dataFolder = new File(dataFolderPath);

        if (dataFolder.exists() && dataFolder.isDirectory()) {
            File[] folders = dataFolder.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    if (folder.getName().endsWith("_" + afm)) {
                        return folder; // Επιστροφή υπάρχοντος φακέλου
                    }
                }
            }
        }
        return null; // Αν δεν βρέθηκε φακέλος με το συγκεκριμένο ΑΦΜ
    }

    // Δημιουργία ή έλεγχος φακέλου πελάτη
    public File customerFolder(String customerName, String afm) {
        File existingFolder = findFolderByAfm(afm);

        if (existingFolder != null) {
            System.out.println("Βρέθηκε φάκελος για το ΑΦΜ: " + existingFolder.getAbsolutePath());
            return existingFolder;
        }

        String folderPath = AppSettings.loadSetting("datafolder") + customerName + "_" + afm;
        File folder = new File(folderPath);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Ο φάκελος δημιουργήθηκε: " + folderPath);
            } else {
                System.err.println("Αποτυχία δημιουργίας του φακέλου.");
                return null;
            }
        } else {
            System.out.println("Ο φάκελος υπάρχει ήδη: " + folderPath);
        }

        return folder;
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

        return folder;
    }

    public File createCustomerOfferFolder(String customerName, String afm) {
        File folder = customerFolder(customerName, afm);
        String offerFolderPath = folder + "\\Προσφορές";
        File folderOffer = new File(offerFolderPath);

        if (!folderOffer.exists()) {
            if (folderOffer.mkdirs()) {
                System.out.println("Ο φάκελος δημιουργήθηκε: " + folderOffer);
            } else {
                System.err.println("Αποτυχία δημιουργίας του φακέλου.");
                return null;
            }
        } else {
            System.out.println("Ο φάκελος υπάρχει ήδη: " + folderOffer);
        }
        return folderOffer;
    }

    public File openCustomerOfferFolder(String customerName, String afm) {
        File folder = createCustomerOfferFolder(customerName, afm);

        if (folder != null) {
            try {
                Desktop.getDesktop().open(folder);
                System.out.println("Ο φάκελος άνοιξε: " + folder.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Αποτυχία ανοίγματος του φακέλου.");
                e.printStackTrace();
            }
        }

        return folder;
    }
}
