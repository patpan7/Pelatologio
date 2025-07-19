package org.easytech.pelatologio.dao;

import java.sql.SQLException;

public interface MegasoftDao {
    void syncMegasoft();
    String getMegasoftBalance(String afm);
}