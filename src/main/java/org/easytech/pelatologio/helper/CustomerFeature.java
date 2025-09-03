package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.models.Customer;

import java.util.function.Function;

public enum CustomerFeature {
    TAXIS(
            "taxis",                // feature flag name (Features.isEnabled("taxis"))
            "Taxis",                // τίτλος tab
            "taxisView.fxml", // FXML path (προτίμησε absolute με /)
            // Πότε θεωρούμε ότι "υπάρχουν δεδομένα" ώστε να εμφανιστεί το tab μόνιμα
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 3)
    ),
    MY_POS(
            "mypos",
            "MyPOS",
            "myposView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 1)
    ),
    SIMPLY(
            "simply",
            "Simply",
            "simplyView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 2)
    ),
    EMBLEM(
            "emblem",
            "Emblem",
            "emblemView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 4)
    ),
    ERGANI(
            "ergani",
            "Εργάνη",
            "erganiView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 5)
    ),
    PELATOLOGIO(
            "pelatologio",
            "Πελατολόγιο App",
            "pelatologioView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 6)
    ),
    NINEPOS(
            "ninepos",
            "NinePOS",
            "nineposView.fxml",
            c -> DBHelper.getCustomerDao().hasApp(c.getCode(), 7)
    ),
    DEVICES(
            "devices",
            "Συσκευές",
            "customerDevicesView.fxml",
            c -> true // Always show if feature is enabled
    ),
    INVOICES(
            "megasoft",
            "Τιμολόγια",
            "invoicesView.fxml",
            c -> DBHelper.getCustomerDao().hasInvoices(c.getAfm())
    ),
    TASKS(
            "tasks",
            "Εργασίες",
            "customerTasksView.fxml",
            c -> true
    ),
    SUBS(
            "subs",
            "Συνδρομές",
            "customerSubsView.fxml",
            c -> true
    ),
    OFFERS(
            "offers",
            "Προσφορές",
            "customerOffersView.fxml",
            c -> true
    ),
    ORDERS(
            "orders",
            "Παραγγελίες",
            "ordersCustView.fxml",
            c -> true
    ),
    CALLS(
            "calls",
            "Κλήσεις",
            "customerCallLogView.fxml",
            c -> true
    ),
    NOTES(
            "notes",
            "Σημειώσεις",
            "notesView.fxml",
            c -> true
    ),
    EDPS(
            "edps",
            "EDPS",
            "edpsView.fxml",
            c -> DBHelper.getCustomerProjectDao().hasProjects(c.getCode(), 8) // Assuming 1 is the ApplicationID for EDPS
    );

    public final String featureFlag;
    public final String title;
    public final String fxml;
    public final Function<Customer, Boolean> hasData;

    CustomerFeature(String featureFlag, String title, String fxml, Function<Customer, Boolean> hasData) {
        this.featureFlag = featureFlag;
        this.title = title;
        this.fxml = fxml;
        this.hasData = hasData;
    }

    /**
     * Είναι ενεργό συνολικά; (global feature flag)
     */
    public boolean isGloballyEnabled() {
        return Features.isEnabled(featureFlag);
    }

    /**
     * Υπάρχουν δεδομένα για αυτό τον πελάτη;
     */
    public boolean isPresentFor(Customer c) {
        try {
            return hasData.apply(c);
        } catch (Exception e) {
            return false;
        }
    }
}