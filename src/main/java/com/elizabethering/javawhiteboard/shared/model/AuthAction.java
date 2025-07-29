package com.elizabethering.javawhiteboard.shared.model;

public abstract class AuthAction implements Action {
    private static final long serialVersionUID = 3L;
    protected final String username;
    protected final String password;

    public AuthAction(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }
}