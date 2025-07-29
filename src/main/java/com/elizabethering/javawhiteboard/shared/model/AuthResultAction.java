package com.elizabethering.javawhiteboard.shared.model;

import java.util.List;

/**
 * 服务器在处理完登录或注册请求后，返回给客户端的结果指令。
 * 登录成功后，客户端将进入模式选择界面。
 */
public class AuthResultAction implements Action {
    private static final long serialVersionUID = 4L;
    private final boolean success;
    private final String message;
    private final String username;

    public AuthResultAction(boolean success, String message, String username) {
        this.success = success;
        this.message = message;
        this.username = username;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUsername() { return username; }
}