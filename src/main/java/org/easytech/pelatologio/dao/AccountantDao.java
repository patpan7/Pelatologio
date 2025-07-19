package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Accountant;
import java.util.List;

public interface AccountantDao {
    List<Accountant> getAccountants();
    Accountant getSelectedAccountant(int accountantId);
    int insertAccountant(String name, String phone, String mobile, String email, String erganiEmail);
    void updateAccountant(int code, String name, String phone, String mobile, String email, String erganiEmail);
    void invalidateAccountantsCache();
}