package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.AppItem;

import java.util.List;

public interface AppItemDao {
    List<AppItem> getApplications();
}
