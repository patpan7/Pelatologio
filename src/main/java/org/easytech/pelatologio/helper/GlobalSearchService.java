package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.models.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GlobalSearchService {

    public List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();
        String greekQuery = AppUtils.toGreek(query).toUpperCase();
        String englishQuery = AppUtils.toEnglish(query).toUpperCase();

        // Search Customers
        try {
            for (Customer customer : DBHelper.getCustomerDao().getCustomers()) {
                if (customer.getName().toUpperCase().contains(greekQuery) || customer.getName().toUpperCase().contains(englishQuery) ||
                    customer.getAfm().toUpperCase().contains(englishQuery) ||
                    (customer.getPhone1() != null && customer.getPhone1().contains(query)) ||
                    (customer.getMobile() != null && customer.getMobile().contains(query)) ||
                    (customer.getEmail() != null && customer.getEmail().toUpperCase().contains(englishQuery))) {
                    results.add(new SearchResult(customer.getName() + " (Πελάτης)", SearchResult.SearchResultType.CUSTOMER, customer.getCode()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Search Devices
        for (Device device : DBHelper.getDeviceDao().getAllDevices()) {
            if (device.getSerial().toUpperCase().contains(englishQuery) ||
                (device.getItemName() != null && (device.getItemName().toUpperCase().contains(greekQuery) || device.getItemName().toUpperCase().contains(englishQuery))) ||
                (device.getDescription() != null && (device.getDescription().toUpperCase().contains(greekQuery) || device.getDescription().toUpperCase().contains(englishQuery)))) {
                results.add(new SearchResult(device.getSerial() + " (" + device.getItemName() + ") (Συσκευή)", SearchResult.SearchResultType.DEVICE, device.getId()));
            }
        }

        // Search Subscriptions
        // Assuming you have a method to get all subscriptions
        // for (Subscription sub : DBHelper.getSubscriptionDao().getAllSubscriptions()) {
        //     if (sub.getTitle().toUpperCase().contains(greekQuery) || sub.getTitle().toUpperCase().contains(englishQuery)) {
        //         results.add(new SearchResult(sub.getTitle() + " (Συνδρομή)", SearchResult.SearchResultType.SUBSCRIPTION, sub.getId()));
        //     }
        // }

        // Search Suppliers
        for (Supplier supplier : DBHelper.getSupplierDao().getSuppliers()) {
            if (supplier.getName().toUpperCase().contains(greekQuery) || supplier.getName().toUpperCase().contains(englishQuery)) {
                results.add(new SearchResult(supplier.getName() + " (Προμηθευτής)", SearchResult.SearchResultType.SUPPLIER, supplier.getId()));
            }
        }

        // Search Accountants
        for (Accountant accountant : DBHelper.getAccountantDao().getAccountants()) {
            if (accountant.getName().toUpperCase().contains(greekQuery) || accountant.getName().toUpperCase().contains(englishQuery)) {
                results.add(new SearchResult(accountant.getName() + " (Λογιστής)", SearchResult.SearchResultType.ACCOUNTANT, accountant.getId()));
            }
        }

        return results;
    }
}
