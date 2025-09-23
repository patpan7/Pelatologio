package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import org.easytech.pelatologio.EmailDialogController;
import org.easytech.pelatologio.models.Customer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppUtils {

    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();
    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('A', 'Α');
        ENGLISH_TO_GREEK.put('B', 'Β');
        ENGLISH_TO_GREEK.put('C', 'Ψ');
        ENGLISH_TO_GREEK.put('D', 'Δ');
        ENGLISH_TO_GREEK.put('E', 'Ε');
        ENGLISH_TO_GREEK.put('F', 'Φ');
        ENGLISH_TO_GREEK.put('G', 'Γ');
        ENGLISH_TO_GREEK.put('H', 'Η');
        ENGLISH_TO_GREEK.put('I', 'Ι');
        ENGLISH_TO_GREEK.put('J', 'Ξ');
        ENGLISH_TO_GREEK.put('K', 'Κ');
        ENGLISH_TO_GREEK.put('L', 'Λ');
        ENGLISH_TO_GREEK.put('M', 'Μ');
        ENGLISH_TO_GREEK.put('N', 'Ν');
        ENGLISH_TO_GREEK.put('O', 'Ο');
        ENGLISH_TO_GREEK.put('P', 'Π');
        ENGLISH_TO_GREEK.put('Q', 'Κ');
        ENGLISH_TO_GREEK.put('R', 'Ρ');
        ENGLISH_TO_GREEK.put('S', 'Σ');
        ENGLISH_TO_GREEK.put('T', 'Τ');
        ENGLISH_TO_GREEK.put('U', 'Θ');
        ENGLISH_TO_GREEK.put('V', 'Ω');
        ENGLISH_TO_GREEK.put('W', 'Σ');
        ENGLISH_TO_GREEK.put('X', 'Χ');
        ENGLISH_TO_GREEK.put('Y', 'Υ');
        ENGLISH_TO_GREEK.put('Z', 'Ζ');

        GREEK_TO_ENGLISH.put('Α', 'A');
        GREEK_TO_ENGLISH.put('Β', 'B');
        GREEK_TO_ENGLISH.put('Ψ', 'C');
        GREEK_TO_ENGLISH.put('Δ', 'D');
        GREEK_TO_ENGLISH.put('Ε', 'E');
        GREEK_TO_ENGLISH.put('Φ', 'F');
        GREEK_TO_ENGLISH.put('Γ', 'G');
        GREEK_TO_ENGLISH.put('Η', 'H');
        GREEK_TO_ENGLISH.put('Ι', 'I');
        GREEK_TO_ENGLISH.put('Ξ', 'J');
        GREEK_TO_ENGLISH.put('Κ', 'K');
        GREEK_TO_ENGLISH.put('Λ', 'L');
        GREEK_TO_ENGLISH.put('Μ', 'M');
        GREEK_TO_ENGLISH.put('Ν', 'N');
        GREEK_TO_ENGLISH.put('Ο', 'O');
        GREEK_TO_ENGLISH.put('Π', 'P');
        GREEK_TO_ENGLISH.put('Ρ', 'R');
        GREEK_TO_ENGLISH.put('Σ', 'S');
        GREEK_TO_ENGLISH.put('Τ', 'T');
        GREEK_TO_ENGLISH.put('Θ', 'U');
        GREEK_TO_ENGLISH.put('Ω', 'V');
        GREEK_TO_ENGLISH.put('Χ', 'X');
        GREEK_TO_ENGLISH.put('Υ', 'Y');
        GREEK_TO_ENGLISH.put('Ζ', 'Z');
    }

    public static String toGreek(String input) {
        if (input == null) {
            return "";
        }
        char[] chars = input.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character repl = ENGLISH_TO_GREEK.get(chars[i]);
            if (repl != null) {
                chars[i] = repl;
            }
        }
        return new String(chars);
    }

    public static String toEnglish(String input) {
        if (input == null) {
            return "";
        }
        char[] chars = input.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Character repl = GREEK_TO_ENGLISH.get(chars[i]);
            if (repl != null) {
                chars[i] = repl;
            }
        }
        return new String(chars);
    }

    public static void pasteText(javafx.scene.control.TextInputControl textField) {
        if (textField != null) {
            textField.paste();
        }
    }

    public static void clearText(javafx.scene.control.TextInputControl textField) {
        if (textField != null) {
            textField.clear();
        }
    }

    public static void copyTextToClipboard(String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    // Μέθοδος αποστολής με viber
    public static void viberComunicate(String phone) {
        if (phone != null && !phone.isEmpty()) {
            try {
                File viberPath = new File(System.getenv("LOCALAPPDATA") + "\\Viber\\Viber.exe");
                Desktop.getDesktop().open(viberPath);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(phone);  // Replace with the desired text
                clipboard.setContent(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CustomNotification.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ εισάγετε ένα έγκυρο τηλέφωνο")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showError();
        }
    }

    public static void sendTestEmail(String email, ProgressIndicator progressIndicator) {
        if (email != null && !email.isEmpty()) {
            // Εμφάνιση του progress indicator
            progressIndicator.setVisible(true);

            // Δημιουργία και αποστολή email σε ξεχωριστό thread για να μην κολλήσει το UI
            Thread emailThread = new Thread(() -> {
                try {
                    String subject = "Δοκιμή Email";
                    String body = "Δοκιμή email.";
                    EmailSender emailSender = new EmailSender(AppSettings.loadSetting("smtp"), AppSettings.loadSetting("smtpport"), AppSettings.loadSetting("email"), AppSettings.loadSetting("emailPass"));
                    emailSender.sendEmail(email, subject, body);

                    // Ενημερώνουμε το UI όταν ολοκληρωθεί η αποστολή του email
                    Platform.runLater(() -> {
                        CustomNotification.create()
                                .title("Επιτυχία")
                                .text("Το email στάλθηκε με επιτυχία.")
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT)
                                .showConfirmation();
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertDialogHelper.showDialog("Σφάλμα", "Υπήρξε πρόβλημα με την αποστολή του email.", e.getMessage(), Alert.AlertType.ERROR);
                        progressIndicator.setVisible(false); // Απόκρυψη του progress indicator
                    });
                    Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποστολή email.", e.getMessage(), Alert.AlertType.ERROR));
                }
            });
            emailThread.setDaemon(true);
            emailThread.start(); // Ξεκινάμε το thread για την αποστολή του email
        } else {
            //showAlert("Προσοχή", "Παρακαλώ εισάγετε ένα έγκυρο email.");
            CustomNotification.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ εισάγετε ένα έγκυρο email.")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showError();
        }
    }


    public static void setupPhoneButton(javafx.scene.control.Button button, TextField textField) {
        button.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                PhoneCall.callHandle(textField.getText());
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                PhoneCall.callHandle2(textField.getText());
            }
        });
    }


}
