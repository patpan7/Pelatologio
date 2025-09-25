package org.easytech.pelatologio.helper;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.easytech.pelatologio.helper.EncryptionHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppSettings {
    private static final String FILE_NAME = "app.properties";

    public String server;
    public String dbUser;
    public String dbPass;
    public String db;
    public String myposLink;
    public String SimplyUser;
    public String SimplyPass;
    public String fanvilUser;
    public String fanvilPass;

    private AppSettings() {
        init();
    }

    public static synchronized AppSettings getInstance() {
        return new AppSettings();
    }

    private void init() {
        server = AppSettings.loadSetting("server");
        dbUser = AppSettings.loadSetting("dbUser");
        dbPass = AppSettings.loadSetting("dbPass");
        db = AppSettings.loadSetting("db");
        myposLink = AppSettings.loadSetting("myposlink");
        SimplyUser = AppSettings.loadSetting("simplyUser");
        SimplyPass = AppSettings.loadSetting("simplyPass");
        fanvilUser = AppSettings.loadSetting("fanvil.user");
        fanvilPass = AppSettings.loadSetting("fanvil.pass");

        // Add new email templates if they don't exist
        if (loadSetting("email.template.subject.simplyService") == null) {
            saveSetting("email.template.subject.simplyService", "Νέος Πελάτης Simply {logins.tag}");
        }
        if (loadSetting("email.template.body.simplyService") == null) {
            saveSetting("email.template.body.simplyService", "<b>Νέος Πελάτης Simply {logins.tag}</b><br><b>Επωνυμία:</b> {customer.name}<br><b>ΑΦΜ:</b> {customer.afm}<br><b>E-mail:</b> {logins.username}<br><b>Κωδικός:</b> {logins.password}<br><b>Κινητό:</b> {customer.mobile}<br>Έχει κάνει αποδοχή σύμβασης και εξουσιοδότηση<br>");
        }
        if (loadSetting("email.template.subject.simplyRenew") == null) {
            saveSetting("email.template.subject.simplyRenew", "Ανανέωση Πελάτη Simply {logins.tag}");
        }
        if (loadSetting("email.template.body.simplyRenew") == null) {
            saveSetting("email.template.body.simplyRenew", "<b>Ανανέωση Πελάτη Simply {logins.tag}</b><br><b>Επωνυμία:</b> {customer.name}<br><b>ΑΦΜ:</b> {customer.afm}<br><b>E-mail:</b> {logins.username}<br><b>Κωδικός:</b> {logins.password}<br><b>Κινητό:</b> {customer.mobile}<br>");
        }
        if (loadSetting("email.template.subject.subsReminder") == null) {
            saveSetting("email.template.subject.subsReminder", "Υπενθύμιση λήξης συνδρομής {subscription.title}");
        }
        if (loadSetting("email.template.body.subsReminder") == null) {
            saveSetting("email.template.body.subsReminder", "Αγαπητέ/ή {customer.name},<br>Σας υπενθυμίζουμε ότι η συνδρομή σας στο {subscription.title} λήγει στις {subscription.endDate}.<br>Για να συνεχίσετε να απολαμβάνετε τα προνόμια της συνδρομής σας, σας προσκαλούμε να την ανανεώσετε το συντομότερο δυνατόν.<br>Μπορείτε να ανανεώσετε τη συνδρομή σας εύκολα και γρήγορα κάνοντας κατάθεση του ποσού [{subscription.price}€ + φπα] = {calculatedPrice}€ στους παρακάτω τραπεζικούς λογαριασμούς.<br>Εναλλακτικά επισκεφθείτε  το κατάστημα μας για χρήση μετρητών για ποσά έως 500€ ή με χρήση τραπεζικής κάρτας.<br>Εάν έχετε οποιαδήποτε ερώτηση, μη διστάσετε να επικοινωνήσετε μαζί μας.<br><br><b>Τραπεζικοί Λογαριασμοί:</b><br><br><b>ΕΘΝΙΚΗ ΤΡΑΠΕΖΑ</b><br><b>Λογαριασμός:</b> 29700119679<br><b>Λογαριασμός (IBAN):</b> GR6201102970000029700119679<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ<br><b>EUROBANK</b><br><b>Λογαριασμός:</b> 0026.0451.27.0200083481<br><b>Λογαριασμός (IBAN):</b> GR7902604510000270200083481<br><b>Με Δικαιούχους:</b> ΓΚΟΥΜΑΣ ΔΗΜΗΤΡΙΟΣ ΑΠΟΣΤΟΛΟΣ<br><b>myPOS</b><br><b>ΑΡ.ΠΟΡΤΟΦΟΛΙΟΥ:</b> 40005794314<br><b>Όνομα δικαιούχου:</b> GKOUMAS DIMITRIOS <br><b>IBAN:</b> IE27MPOS99039012868261 <br><b>ΑΡΙΘΜΟΣ ΛΟΓΑΡΙΑΣΜΟΥ:</b> 12868261<br><b>myPOS Ltd</b><br><b>BIC: MPOSIE2D</b>");
        }
        if (loadSetting("email.template.subject.edpsProposal") == null) {
            saveSetting("email.template.subject.edpsProposal", "Νέα Πρόταση Σύμβασης - {customer.name}");
        }
        if (loadSetting("email.template.body.edpsProposal") == null) {
            saveSetting("email.template.body.edpsProposal", "Αγαπητή EDPS,<br><br>Παρακαλώ βρείτε παρακάτω τα στοιχεία της πρότασης για τον πελάτη: {customer.name} (ΑΦΜ: {customer.afm})<br><br><b>Προμήθεια:</b> {commission}%<br><b>Τιμή POS:</b> {posPrice}€<br><b>Μηνιαία Συνδρομή:</b> {monthlyFee}€<br><b>Τύπος Σύνδεσης:</b> {integrationType}<br><b>Όνομα ERP:</b> {erpName}<br><br>Συνημμένα θα βρείτε το υπογεγραμμένο έντυπο Α1.<br><br>Με εκτίμηση.");
        }
        if (loadSetting("email.template.subject.offer") == null) {
            saveSetting("email.template.subject.offer", "Προσφορά {offer.id}: {offer.customerName}");
        }
        if (loadSetting("email.template.body.offer") == null) {
            saveSetting("email.template.body.offer", "<h3>{offer.description}</h3><br><br><h3>Μπορείτε να την δείτε και να την αποδεχτείτε ή να την\n" +
                    "  απορρίψετε μέσω του παρακάτω συνδέσμου: </h3><a\n" +
                    "  href=http://dgou.dynns.com:8090/portal/offer.php?id={offer.id}><b><h2>Αποδοχή ή Απόρριψη προσφορά\n" +
                    "  {offer.id}</b><h2></a><br><br><h3>Μπορείτε δείτε τους τραπεζικούς μας λογαριασμούς </h3><a\n" +
                    "  href=http://dgou.dynns.com:8090/portal/bank_accounts.php><b><h2>Τραπεζικοί λογαριασμοί</b></h2></a><br><br><h3>Για\n" +
                    "  οποιαδήποτε διευκρίνιση, είμαστε στη διάθεσή σας.</h3>");
        }
        if (loadSetting("email.template.subject.erganiRegistration") == null) {
            saveSetting("email.template.subject.erganiRegistration", "Νέος πελάτης Εργάνη");
        }
        if (loadSetting("email.template.body.erganiRegistration") == null) {
            saveSetting("email.template.body.erganiRegistration", "<b>Νέος πελάτης Εργάνη</b><br><b>Επωνυμία:</b>\n" +
                    "  {customer.name}<br><b>ΑΦΜ:</b> {customer.afm}<br><b>E-mail:</b> {logins.username}<br><b>Κινητό:</b>\n" +
                    "  {logins.phone}<br><b>E-mail Λογιστή:</b> {erganiregistration.email}<br><b>Προγράμματα:</b>\n" +
                    "  {erganiregistration.program}<br><b>Σύνολο Ετών:</b> {erganiregistration.years}<br><b>Extra Είσοδος:</b>\n" +
                    "  {erganiregistration.entrance}");
        }
    }

    public static void saveSetting(String key, String value) {
        Properties prop = new Properties();
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                byte[] encryptedBytes = input.readAllBytes();
                if (encryptedBytes.length > 0) {
                    String encryptedSettings = new String(encryptedBytes, StandardCharsets.UTF_8);
                    String decryptedSettings = EncryptionHelper.decrypt(encryptedSettings);
                    prop.load(new StringReader(decryptedSettings));
                }
            } catch (Exception e) {
                System.err.println("Could not load or decrypt existing settings. A new file will be created.");
                e.printStackTrace();
            }
        }

        prop.setProperty(key, value);

        try (StringWriter stringWriter = new StringWriter()) {
            prop.store(stringWriter, null);
            String settingsString = stringWriter.toString();
            String encryptedSettings = EncryptionHelper.encrypt(settingsString);

            try (OutputStream output = new FileOutputStream(FILE_NAME)) {
                output.write(encryptedSettings.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την αποθήκευση.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    public static String loadSetting(String key) {
        Properties prop = getAllSettings();
        return prop.getProperty(key);
    }

    public static Properties getAllSettings() {
        Properties prop = new Properties();
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                byte[] encryptedBytes = input.readAllBytes();
                if (encryptedBytes.length > 0) {
                    String encryptedSettings = new String(encryptedBytes, StandardCharsets.UTF_8);
                    String decryptedSettings = EncryptionHelper.decrypt(encryptedSettings);
                    prop.load(new StringReader(decryptedSettings));
                }
            } catch (Exception e) {
                System.err.println("Could not load or decrypt existing settings. A new file will be created.");
                e.printStackTrace();
            }
        }
        return prop;
    }
}
