package org.easytech.pelatologio.helper;

public class SearchResult {
    private final String display_text;
    private final SearchResultType type;
    private final int object_id;

    public SearchResult(String display_text, SearchResultType type, int object_id) {
        this.display_text = display_text;
        this.type = type;
        this.object_id = object_id;
    }

    public String getDisplayText() {
        return display_text;
    }

    public SearchResultType getType() {
        return type;
    }

    public int getObjectId() {
        return object_id;
    }

    @Override
    public String toString() {
        return display_text;
    }

    public enum SearchResultType {
        CUSTOMER, DEVICE, SUBSCRIPTION, SUPPLIER, ACCOUNTANT
    }
}