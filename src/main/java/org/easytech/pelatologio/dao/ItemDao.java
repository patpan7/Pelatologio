package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.Item;
import java.sql.SQLException;
import java.util.List;

public interface ItemDao {
    List<Item> getItems() throws SQLException;
    boolean isItemExists(String name);
    int insertItem(String name, String description, String category);
    void updateItem(int code, String name, String description, String category);
}