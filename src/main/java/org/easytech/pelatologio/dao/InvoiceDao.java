package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Invoice;
import java.util.List;

public interface InvoiceDao {
    List<Invoice> getInvoices(String afm);
    List<Invoice> getInvoices1(String afm);
}