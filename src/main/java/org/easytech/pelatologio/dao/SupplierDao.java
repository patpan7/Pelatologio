package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Supplier;
import java.util.List;

public interface SupplierDao {
    List<Supplier> getSuppliersFromOrders();
    List<Supplier> getSuppliers();
    Supplier getSelectedSupplier(int supplierId);
    int insertSupplier(String name, String title, String afm, String phone, String mobile, String contact, String email, String email2, String site, String notes);
    void updateSupplier(int code, String name, String title, String afm, String phone, String mobile, String contact, String email, String email2, String site, String notes);
}