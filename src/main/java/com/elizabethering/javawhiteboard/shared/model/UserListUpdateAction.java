package com.elizabethering.javawhiteboard.shared.model;

import java.util.List;

/**
 * 用于让服务器向客户端广播最新的用户列表和他们的状态。
 * 携带的是包含更丰富信息的UserStatus对象列表。
 */
public class UserListUpdateAction implements Action {
    private static final long serialVersionUID = 5L;

    // 将 String 列表修改为 UserStatus 列表
    private final List<UserStatus> userStatuses;

    public UserListUpdateAction(List<UserStatus> userStatuses) {
        this.userStatuses = userStatuses;
    }

    public List<UserStatus> getUserStatuses() {
        return userStatuses;
    }
}