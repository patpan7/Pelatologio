package org.easytech.pelatologio;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.sikuli.script.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    public static void bringToFront(String windowTitle) {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hWnd = user32.FindWindow(null, windowTitle);

        if (hWnd != null) {
            int foregroundThread = user32.GetWindowThreadProcessId(user32.GetForegroundWindow(), null);
            int targetThread = user32.GetWindowThreadProcessId(hWnd, null);

            // Συνδέουμε τα δύο threads για να παρακάμψουμε περιορισμούς
            if (foregroundThread != targetThread) {
                user32.AttachThreadInput(new WinDef.DWORD(foregroundThread), new WinDef.DWORD(targetThread), true);
            }

            user32.ShowWindow(hWnd, User32.SW_RESTORE);  // Επαναφορά αν είναι minimized
            user32.SetForegroundWindow(hWnd);           // Φέρνει το Prisma μπροστά
            //.user32.BringWindowToTop(hWnd);              // Extra εντολή ενεργοποίησης
            System.out.println("Το Prisma Win ήρθε στο προσκήνιο.");

            // Αποσύνδεση thread input για να μη δημιουργήσει πρόβλημα
            if (foregroundThread != targetThread) {
                user32.AttachThreadInput(new WinDef.DWORD(foregroundThread), new WinDef.DWORD(targetThread), false);
            }

        } else {
            System.out.println("Δεν βρέθηκε το παράθυρο του Prisma Win.");
        }
    }

    public static void maximizeWindow(String windowTitle) {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hWnd = user32.FindWindow(null, windowTitle);

        if (hWnd != null) {
            user32.ShowWindow(hWnd, User32.SW_MAXIMIZE);
            System.out.println("Το Prisma Win μεγιστοποιήθηκε.");
        } else {
            System.out.println("Δεν βρέθηκε το παράθυρο του Prisma Win.");
        }
    }


    public static void run(Customer customer) {
        try {
            String processName = "Prisma.exe";
            String windowTitle = "Megasoft PRISMA Win - Εμπορική Διαχείριση";
            // Έλεγχος αν το Prisma Win είναι ανοιχτό
            if (!isProcessRunning(processName)) {
                System.out.println("Το Prisma Win δεν είναι ανοιχτό. Εκκίνηση...");
                Runtime.getRuntime().exec("C:\\Program Files (x86)\\Megasoft\\PRISMA Win\\Prisma.exe");
                Thread.sleep(5000); // Αναμονή για φόρτωμα
            } else {
                System.out.println("Το Prisma Win είναι ήδη ανοιχτό.");
                bringToFront(windowTitle);
                maximizeWindow(windowTitle);
            }

            // Εκτέλεση SikuliX script
            try {
                Screen screen = new Screen();
                String imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/pelates.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                screen.click(imagePath);
                Thread.sleep(2000); // Αναμονή για φόρτωμα
                imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/neos.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                screen.click(imagePath);
                Thread.sleep(1000); // Αναμονή για φόρτωμα
                imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/afm.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                screen.paste(imagePath, customer.getAfm());
                Thread.sleep(500); // Αναμονή για φόρτωμα
                imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/afmbtn.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                screen.click(imagePath);
                Thread.sleep(500); // Αναμονή για φόρτωμα
                imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/aadecopy.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                screen.click(imagePath);
                Thread.sleep(500); // Αναμονή για φόρτωμα
                if (customer.getPhone1() != null) {
                    imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/til1.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                    screen.paste(imagePath, customer.getPhone1() == null ? "" : customer.getPhone1());
                    Thread.sleep(500);
                }
                if (customer.getPhone2() != null) {
                    imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/til2.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                    screen.paste(imagePath, customer.getPhone2() == null ? "" : customer.getPhone2());
                    Thread.sleep(500);
                }
                if (customer.getMobile() != null) {
                    imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/kin.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                    screen.paste(imagePath, customer.getMobile() == null ? "" : customer.getMobile());
                    Thread.sleep(500);
                }
                if (customer.getEmail() != null) {
                    imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/mail.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                    //screen.type(imagePath, customer.getEmail() == null ? "" : customer.getEmail());
                    screen.paste(imagePath, customer.getEmail() == null ? "" : customer.getEmail());
                }
                if (customer.getManager() != null) {
                    imagePath = PrismaWinAutomation.class.getResource("/org/easytech/pelatologio/megasoft/manager.png").getPath(); // Αν η εικόνα είναι στο resources/images/
                    screen.paste(imagePath, customer.getManager() == null ? "" : customer.getManager());
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Δεν βρέθηκε η εικόνα!");
            }

            System.out.println("Εισαγωγή ολοκληρώθηκε!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
