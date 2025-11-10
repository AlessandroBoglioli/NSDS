package com.lab.evaluation25;

public class StoreMsg {

    private String name;
    private String email;
    private Boolean primary;

    public StoreMsg (String name, String email, Boolean primary) {
        this.name = name;
        this.email = email;
        this.primary = primary;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getPrimary() {
        return primary;
    }
}
