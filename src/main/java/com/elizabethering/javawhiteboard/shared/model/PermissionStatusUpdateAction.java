package com.elizabethering.javawhiteboard.shared.model;

/**
 * 服务器发送给学生端，用于更新其当前的绘图权限状态。
 */
public class PermissionStatusUpdateAction implements Action {
    private static final long serialVersionUID = 11L;
    private final boolean canDraw;
    private final String message;

    /**
     * 构造一个权限状态更新指令。
     * @param canDraw 当前是否可以绘图。
     * @param message 附带的消息，例如 "老师已授权" 或 "老师已拒绝"。
     */
    public PermissionStatusUpdateAction(boolean canDraw, String message) {
        this.canDraw = canDraw;
        this.message = message;
    }

    public boolean canDraw() {
        return canDraw;
    }

    public String getMessage() {
        return message;
    }
}