package com.elizabethering.javawhiteboard.shared.model;

/**
 * 当客户端成功加入一个共享会话后，服务器发送此指令。
 * 它明确告知客户端其被分配的角色和初始的绘图权限。
 */
public class SessionJoinResultAction implements Action {
    private static final long serialVersionUID = 15L; // 新的类，新的版本号

    private final UserRole userRole;
    private final boolean canDraw;

    public SessionJoinResultAction(UserRole userRole, boolean canDraw) {
        this.userRole = userRole;
        this.canDraw = canDraw;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public boolean canDraw() {
        return canDraw;
    }
}