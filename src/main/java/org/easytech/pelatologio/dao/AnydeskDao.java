package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Anydesk;
import java.util.List;

public interface AnydeskDao {
    List<Anydesk> getAnydeskIdsForCustomer(int customerId);
    void addAnydeskId(Anydesk anydesk);
    void deleteAnydeskId(int id);
    void updateAnydeskId(Anydesk anydesk);
}
