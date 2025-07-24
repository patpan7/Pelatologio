package org.easytech.pelatologio.models;

public class Anydesk {
    private int id;
    private int customerId;
    private String anydeskId;
    private String description;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAnydeskId() {
        return anydeskId;
    }

    public void setAnydeskId(String anydeskId) {
        this.anydeskId = anydeskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
