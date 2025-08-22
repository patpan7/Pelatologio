package org.easytech.pelatologio.helper;


import org.easytech.pelatologio.models.Customer;

public interface CustomerTabController {
    /**
     * Καλείται αμέσως μετά το load() του FXML.
     */
    void setCustomer(Customer customer);

    /** Callback που καλείται όταν αποθηκευτούν δεδομένα στο tab */
    void setOnDataSaved(Runnable callback);
}