package com.coffee.cfdemo.model;

public class User {

    private String userId;
    private String name;
    private boolean available;

    public User() {
    }

    public User(String userId, String name, boolean available) {
        this.userId = userId;
        this.name = name;
        this.available = available;
    }

    public static User unavailable() {
        return new User(null, "UNAVAILABLE", false);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
