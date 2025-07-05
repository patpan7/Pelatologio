package org.easytech.pelatologio.models;

public class AppItem {
    private final int id;
    private final String name;

    public AppItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
