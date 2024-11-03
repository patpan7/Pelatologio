package org.easytech.pelatologio;

public class Logins {
    int id;
    private String username;
    private String password;
    private String tag;

    public Logins() {
    }

    public Logins(String username, String password, String tag) {
        this.username = username;
        this.password = password;
        this.tag = tag;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
