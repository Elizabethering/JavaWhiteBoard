package com.elizabethering.javawhiteboard.shared.model;

/**
 * 服务器发送给老师端，通知有一个来自学生的绘图权限请求。
 */
public class IncomingPermissionRequestAction implements Action {
    private static final long serialVersionUID = 9L;
    private final String studentUsername;

    public IncomingPermissionRequestAction(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentUsername() {
        return studentUsername;
    }
}
