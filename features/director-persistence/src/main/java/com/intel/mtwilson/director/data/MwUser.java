package com.intel.mtwilson.director.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_USER")
public class MwUser {

    @Id
    @UuidGenerator(name = "UUID")
    @GeneratedValue(generator = "UUID")
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "USER_NAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "DISPLAY_NAME")
    private String displayname;

    public MwUser(String id, String username, String email,
            String displayname) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayname = displayname;
    }

    public MwUser() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

}
