package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Offer;

import java.time.LocalDateTime;
import java.util.List;

public interface OfferDao {
    List<Offer> getAllOffers();
    void deleteOffer(int id);
    Boolean saveOffer(Offer newOffer);
    Boolean updateOffer(Offer offer);
    List<Offer> getAllCustomerOffers(int customerCode);
    List<Offer> getUpdatedOffers(LocalDateTime lastCheck);
    void updateOfferSent(int id);
    boolean updateOfferStatusManual(int id, String status);
    void updateOfferStatus(int id, String status);
    boolean updateOfferArchived(int id);
}