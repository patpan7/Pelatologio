package org.easytech.pelatologio;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.*;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class PrismaWinAutomation {

    public static boolean isProcessRunning(String processName) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains(processName)) {
                    return true; // Βρέθηκε
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Δεν βρέθηκε
    }

    public class WindowUtils {
        interface User32Library extends Library {
            User32Library INSTANCE = Native.load("user32", User32Library.class);
            boolean SetForegroundWindow(HWND hWnd);
        }

        public static void bringToFront(String windowTitle) {
            HWND hWnd = User32.INSTANCE.FindWindow(null, windowTitle);
            if (hWnd != null) {
                User32Library.INSTANCE.SetForegroundWindow(hWnd);
            }
        }

        public static void removeAlwaysOnTop(String windowTitle) {
            HWND hWnd = User32.INSTANCE.FindWindow(null, windowTitle);
            if (hWnd != null) {
                User32.INSTANCE.SetWindowPos(hWnd, new HWND(new WinDef.INT_PTR(0).toPointer()), 0, 0, 0, 0,
                        WinUser.SWP_NOSIZE | WinUser.SWP_NOMOVE | WinUser.SWP_NOACTIVATE);
            }
        }

        public static void minimizeWindow(String windowTitle) {
            HWND hWnd = User32.INSTANCE.FindWindow(null, windowTitle);
            if (hWnd != null) {
                User32.INSTANCE.ShowWindow(hWnd, User32.SW_RESTORE); // Πρώτα αποκατάσταση
                //User32.INSTANCE.ShowWindow(hWnd, User32.SW_MINIMIZE); // Μετά ελαχιστοποίηση
            }
        }
    }

    public static void maximizeWindow(String windowTitle) {
        User32 user32 = User32.INSTANCE;
        HWND hWnd = user32.FindWindow(null, windowTitle);

        if (hWnd != null) {
            User32.INSTANCE.ShowWindow(hWnd, User32.SW_RESTORE);  // Επαναφορά παραθύρου
            User32.INSTANCE.ShowWindow(hWnd, User32.SW_MAXIMIZE); // Μεγιστοποίηση
            System.out.println("Το Prisma Win μεγιστοποιήθηκε.");
        } else {
            System.out.println("Δεν βρέθηκε το παράθυρο του Prisma Win.");
        }
    }


    public static void addCustomer(Customer customer) {
        try {
            String processName = "Prisma.exe";
            String windowTitle = "Megasoft PRISMA Win - Εμπορική Διαχείριση";
            Screen screen = new Screen();

            // Έλεγχος αν το Prisma Win είναι ανοιχτό
            if (!isProcessRunning(processName)) {
                System.out.println("Το Prisma Win δεν είναι ανοιχτό. Εκκίνηση...");
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\Megasoft\\PRISMA Win\\Prisma.exe");
                waitForSeconds(5); // Περιμένει να ανοίξει
            } else {
                System.out.println("Το Prisma Win είναι ήδη ανοιχτό.");
                WindowUtils.bringToFront("Megasoft PRISMA Win - Εμπορική Διαχείριση");
                maximizeWindow(windowTitle);
            }

            // Δοκιμή εύρεσης και κλικ σε εικόνες χωρίς σταθερό Thread.sleep
            waitForImageAndClick(screen, "pelates0.png", 10);
            waitForImageAndClick(screen, "neos.png", 10);
            waitForImageAndPaste(screen, "afm.png", customer.getAfm(), 10);
            waitForImageAndClick(screen, "afmbtn.png", 10);
            waitForImageAndClick(screen, "aadecopy.png", 10);

            if (customer.getPhone1() != null) {
                waitForImageAndPaste(screen, "til1.png", customer.getPhone1(), 10);
            }
            if (customer.getPhone2() != null) {
                waitForImageAndPaste(screen, "til2.png", customer.getPhone2(), 10);
            }
            if (customer.getMobile() != null) {
                waitForImageAndPaste(screen, "kin.png", customer.getMobile(), 10);
            }
            if (customer.getEmail() != null) {
                waitForImageAndPaste(screen, "mail.png", customer.getEmail(), 10);
            }
            if (customer.getManager() != null) {
                waitForImageAndPaste(screen, "manager.png", customer.getManager(), 10);
            }
            waitForImageAndClick(screen, "note.png", 10);
            DBHelper dbHelper = new DBHelper();
            List<Logins> myposLogins = dbHelper.getLogins(customer.getCode(), 1);
            if (!myposLogins.isEmpty()) {
                for (Logins login : myposLogins) {
                    String loginstr = "\n\nmyPOS"+
                            "\nEmail: " + login.getUsername() +
                            "\nΚωδικός: " + login.getPassword() +
                            "\nΚινητό: " + login.getPhone();
                    waitForImageAndPaste(screen, "note2.png", loginstr, 10);
                }
            }
            List<Logins> simplyLogins = dbHelper.getLogins(customer.getCode(), 2);
            if (!simplyLogins.isEmpty()) {
                for (Logins login : simplyLogins) {
                    System.out.println(login.getTag());
                    String loginstr = "\n\nSimply "+ login.getTag() +
                            "\nEmail: " + login.getUsername() +
                            "\nΚωδικός: " + login.getPassword() +
                            "\nΚινητό: " + login.getPhone();
                    waitForImageAndPaste(screen, "note2.png", loginstr, 10);
                }
            }
            List<Logins> emblemLogins = dbHelper.getLogins(customer.getCode(), 4);
            if (!emblemLogins.isEmpty()) {
                for (Logins login : emblemLogins) {
                    String loginstr = "\n\nEmblem"+
                            "\nEmail: " + login.getUsername() +
                            "\nΚωδικός: " + login.getPassword() +
                            "\nΚινητό: " + login.getPhone();
                    waitForImageAndPaste(screen, "note2.png", loginstr, 10);
                }
            }
            

            System.out.println("Εισαγωγή ολοκληρώθηκε!");
            Notifications notifications = Notifications.create()
                    .title("Ολοκλήρωση")
                    .text("Εισαγωγή ολοκληρώθηκε")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showInformation();
            WindowUtils.removeAlwaysOnTop("Megasoft PRISMA Win - Εμπορική Διαχείριση");
            screen = null;  // Ελευθέρωση της μνήμης του SikuliX
            System.gc();  // Κλήση garbage collection
            WindowUtils.minimizeWindow("Megasoft PRISMA Win - Εμπορική Διαχείριση");
        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    public static void showCustomer(Customer customer) {
        try {
            String processName = "Prisma.exe";
            String windowTitle = "Megasoft PRISMA Win - Εμπορική Διαχείριση";
            Screen screen = new Screen();

            // Έλεγχος αν το Prisma Win είναι ανοιχτό
            if (!isProcessRunning(processName)) {
                System.out.println("Το Prisma Win δεν είναι ανοιχτό. Εκκίνηση...");
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\Megasoft\\PRISMA Win\\Prisma.exe");
                waitForSeconds(5); // Περιμένει να ανοίξει
            } else {
                System.out.println("Το Prisma Win είναι ήδη ανοιχτό.");
                WindowUtils.bringToFront("Megasoft PRISMA Win - Εμπορική Διαχείριση");
                maximizeWindow(windowTitle);
            }

            // Δοκιμή εύρεσης και κλικ σε εικόνες χωρίς σταθερό Thread.sleep
            waitForImageAndClick(screen, "pelates0.png", 10);
            waitForImageAndClick(screen, "afm2.png", 10);
            screen.type(Key.F7);
            screen.paste(customer.getAfm());
            waitForImageAndClick(screen, "select.png", 10);

            System.out.println("Εισαγωγή ολοκληρώθηκε!");
            Notifications notifications = Notifications.create()
                    .title("Ολοκλήρωση")
                    .text("Η εμφάνιση ολοκληρώθηκε")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showInformation();
            WindowUtils.removeAlwaysOnTop("Megasoft PRISMA Win - Εμπορική Διαχείριση");
            screen = null;  // Ελευθέρωση της μνήμης του SikuliX
            System.gc();  // Κλήση garbage collection
            WindowUtils.minimizeWindow("Megasoft PRISMA Win - Εμπορική Διαχείριση");
        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    // 📌 Περιμένει να εμφανιστεί μια εικόνα και κάνει κλικ
    private static void waitForImageAndClick(Screen screen, String imagePath, int timeoutSeconds) {
        try {
            String fullPath = getImagePath(imagePath);
            if (fullPath == null) return; // Αν η εικόνα δεν υπάρχει, σταματά

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
                if (screen.exists(fullPath, 0.5) != null) {
                    screen.click(fullPath);
                    System.out.println("✅ Βρέθηκε και πάτησε το: " + imagePath);
                    return;
                }
                Thread.sleep(500); // Προσθήκη μικρής καθυστέρησης
            }
            System.out.println("❌ Δεν βρέθηκε η εικόνα: " + imagePath);
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("❌ Δεν βρέθηκε η εικόνα: " + imagePath)
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showWarning();
        } catch (Exception e) {
            System.out.println("⚠ Σφάλμα στο waitForImageAndClick: " + e.getMessage());
            Notifications notifications = Notifications.create()
                    .title("Σφάλμα")
                    .text("⚠ Σφάλμα στο waitForImageAndClick: " + e.getMessage())
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
        }
    }


    // 📌 Περιμένει να εμφανιστεί μια εικόνα και πληκτρολογεί κείμενο
    private static void waitForImageAndPaste(Screen screen, String imagePath, String text, int timeoutSeconds) {
        try {
            String fullPath = getImagePath(imagePath);
            if (fullPath == null) return;

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
                if (screen.exists(fullPath, 0.5) != null) {
                    screen.paste(fullPath, text);
                    System.out.println("✅ Βρέθηκε και πληκτρολόγησε στο: " + imagePath);
                    return;
                }
                Thread.sleep(500);
            }
            System.out.println("❌ Δεν βρέθηκε η εικόνα: " + imagePath);
            Notifications notifications = Notifications.create()
                    .title("Προσοχή")
                    .text("❌ Δεν βρέθηκε η εικόνα: " + imagePath)
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showWarning();
        } catch (Exception e) {
            System.out.println("⚠ Σφάλμα στο waitForImageAndPaste: " + e.getMessage());
            Notifications notifications = Notifications.create()
                    .title("Σφάλμα")
                    .text("⚠ Σφάλμα στο waitForImageAndPaste: " + e.getMessage())
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
        }
    }


    // 📌 Μικρή καθυστέρηση χωρίς Thread.sleep()
    private static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String getImagePath(String imageName) {
        // Παίρνει τον φάκελο που τρέχει η εφαρμογή (ο φάκελος του .exe)
        String currentDir = System.getProperty("user.dir");
        String fullPath = currentDir + File.separator + "images" + File.separator + imageName;

        // Έλεγχος αν υπάρχει το αρχείο
        File imageFile = new File(fullPath);
        if (!imageFile.exists()) {
            System.out.println("❌ Δεν βρέθηκε η εικόνα: " + fullPath);
            Notifications notifications = Notifications.create()
                    .title("Σφάλμα")
                    .text("❌ Δεν βρέθηκε η εικόνα: " + fullPath)
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
            notifications.showError();
            return null;
        }

        return fullPath;
    }

}
