package org.easytech.pelatologio.helper;

import java.io.*;

public class Logger {
    public static void initLogging() {
        try {
            File logFile = new File("console_output.log");
            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);

            // Συνδυασμός δύο εξόδων: Κονσόλα & Αρχείο
            PrintStream consoleStream = System.out;  // Αποθηκεύει την αρχική κονσόλα
            PrintStream fileStream = new PrintStream(fileOutputStream);

            // Δημιουργία stream που γράφει και στα δύο
            PrintStream dualStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    consoleStream.write(b); // Εκτυπώνει στην κονσόλα
                    fileStream.write(b);    // Εκτυπώνει στο αρχείο
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    consoleStream.write(b, off, len);
                    fileStream.write(b, off, len);
                }
            });

            // Ανακατεύθυνση System.out & System.err
            System.setOut(dualStream);
            System.setErr(dualStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
