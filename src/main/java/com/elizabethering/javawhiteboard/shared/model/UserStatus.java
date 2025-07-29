package com.elizabethering.javawhiteboard.shared.model;

import java.io.Serializable;

/**
 * 封装了用户在共享会话中的状态信息，主要用于老师端的用户列表显示。
 */
public class UserStatus implements Serializable {
    private final String username;
    private final boolean hasPermission;

    public UserStatus(String username, boolean hasPermission) {
        this.username = username;
        this.hasPermission = hasPermission;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasPermission() {
        return hasPermission;
    }

    @Override
    public String toString() {
        // 这个方法将决定JList中如何显示每个用户
        return username + (hasPermission ? " (已授权)" : "");
    }
}