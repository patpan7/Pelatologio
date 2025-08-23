package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Partner;
import org.easytech.pelatologio.models.PartnerCustomer;

import java.util.List;

public interface PartnerDao {
    Partner findById(int id);
    List<Partner> findAll();
    void insert(Partner partner);
    void update(Partner partner);
    void delete(int id);
    List<PartnerCustomer> getByPartnerId(int partnerId);
}