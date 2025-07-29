package com.elizabethering.javawhiteboard.shared.model;

/**
 * 老师端发送给服务器，用于撤销某个学生的绘图权限。
 */
public class RevokePermissionAction implements Action {
    private static final long serialVersionUID = 14L;
    private final String studentUsername;

    public RevokePermissionAction(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentUsername() {
        return studentUsername;
    }
}