package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.models.Customer;

import java.util.function.Function;

public enum CustomerFeature {
    TAXIS(
            "taxis",
            "Taxis",
            "taxisView.fxml",
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
            c -> DBHelper.getCustomerDao().hasDevice(c.getCode())
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
            c -> DBHelper.getCustomerDao().hasTask(c.getCode())
    ),
    SUBS(
            "subs",
            "Συνδρομές",
            "customerSubsView.fxml",
            c -> DBHelper.getCustomerDao().hasSub(c.getCode())
    ),
    OFFERS(
            "offers",
            "Προσφορές",
            "customerOffersView.fxml",
            c -> DBHelper.getCustomerDao().hasOffer(c.getCode())
    ),
    ORDERS(
            "orders",
            "Παραγγελίες",
            "ordersCustView.fxml",
            c -> DBHelper.getCustomerDao().hasOrders(c.getCode())
    ),
    CALLS(
        "calls",
        "Κλήσεις",
        "customerCallLogView.fxml",
        c -> DBHelper.getCallLogDao().hasCalls(c.getCode())
    ),
    EDPS(
            "edps",
            "EDPS",
            "edpsView.fxml",
            c -> DBHelper.getCustomerProjectDao().hasProjects(c.getCode(), 8)
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

    public boolean isGloballyEnabled() {
        return Features.isEnabled(featureFlag);
    }

    public boolean isPresentFor(Customer c) {
        if (c == null) return false;
        try {
            return hasData.apply(c);
        } catch (Exception e) {
            System.err.println("Error checking hasData for feature " + this.name() + ": " +
                    e.getMessage());
            return false;
        }
    }
}