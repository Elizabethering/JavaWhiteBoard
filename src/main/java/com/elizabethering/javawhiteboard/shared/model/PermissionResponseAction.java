package com.elizabethering.javawhiteboard.shared.model;

/**
 * 老师端发送给服务器，用于响应学生绘图权限请求的指令。
 */
public class PermissionResponseAction implements Action {
    private static final long serialVersionUID = 10L;
    private final String studentUsername;
    private final boolean granted;

    /**
     * 构造一个权限响应指令。
     * @param studentUsername 目标学生用户名。
     * @param granted 是否同意授权。
     */
    public PermissionResponseAction(String studentUsername, boolean granted) {
        this.studentUsername = studentUsername;
        this.granted = granted;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public boolean isGranted() {
        return granted;
    }
}