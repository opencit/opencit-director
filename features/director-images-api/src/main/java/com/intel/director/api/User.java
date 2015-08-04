package com.intel.director.api;

public class User {

    private String id;

    private String displayname;

    private String username;

    private String email;

    public User() {

    }

    public User(String id, String displayname, String username, String email) {
        super();
        this.id = id;
        this.displayname = displayname;
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
