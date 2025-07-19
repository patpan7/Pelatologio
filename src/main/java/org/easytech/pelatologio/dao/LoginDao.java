package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Logins;
import java.sql.SQLException;
import java.util.List;

public interface LoginDao {
    List<Logins> getLogins(int customerId, int i);
    int addLogin(int code, Logins newLogin, int i);
    boolean updateLogin(Logins updatedLogin);
    void deleteLogin(int id);
    int getLoginsCount(int appId);
}