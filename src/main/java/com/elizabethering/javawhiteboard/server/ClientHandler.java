package com.elizabethering.javawhiteboard.server;

import com.elizabethering.javawhiteboard.shared.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * 为每个连接的客户端创建一个独立的线程进行处理。
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ServerApp server;
    private final DatabaseManager dbManager;
    private final SessionManager sessionManager;
    private ObjectOutputStream oos;
    private volatile String username = null;
    private volatile UserRole role = null;
    private volatile boolean inSharedMode = false;

    public ClientHandler(Socket socket, ServerApp server, DatabaseManager dbManager, SessionManager sessionManager) {
        this.clientSocket = socket;
        this.server = server;
        this.dbManager = dbManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                Action receivedAction = (Action) ois.readObject();
                handleAction(receivedAction);
            }
        } catch (IOException | ClassNotFoundException e) {
            // 客户端断开
        } finally {
            server.removeClient(this);
            try {
                if (!clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }

    private void handleAction(Action action) throws IOException {
        if (username == null) {
            // 处理登录和注册
            if (action instanceof RegisterAction) {
                RegisterAction ra = (RegisterAction) action;
                boolean success = dbManager.registerUser(ra.getUsername(), ra.getPassword());
                sendAction(new AuthResultAction(success, success ? "注册成功！请登录。" : "注册失败，用户名可能已存在。", null));
            } else if (action instanceof LoginAction) {
                LoginAction la = (LoginAction) action;
                boolean success = dbManager.loginUser(la.getUsername(), la.getPassword());
                if (success) {
                    this.username = la.getUsername();
                    System.out.println("用户 " + this.username + " 已登录。");
                    sendAction(new AuthResultAction(true, "登录成功！", this.username));
                } else {
                    sendAction(new AuthResultAction(false, "登录失败，用户名或密码错误。", null));
                }
            }
            return;
        }


        if (action instanceof StartSessionAction) {
            this.inSharedMode = true;
            this.role = sessionManager.addUserToSession(this); // 加入会话并获取角色
            boolean canDraw = sessionManager.canDraw(this);   // 获取初始权限

            System.out.println("用户 " + this.username + " 已进入共享模式，角色为: " + this.role + "，初始权限: " + canDraw);

            // 1. 立即向该用户发送其角色和权限信息
            sendAction(new SessionJoinResultAction(this.role, canDraw));

            // 2. 接着发送绘图历史
            List<Action> history = server.getDrawingHistory();
            for (Action histAction : history) {
                sendAction(histAction);
            }

            // 3. 最后，向会话中的所有人（包括刚加入的）广播更新后的用户列表
            server.updateAndBroadcastUserList();

        } else if (action instanceof PermissionRequestAction) {
            sessionManager.handlePermissionRequest(this);
        } else if (action instanceof PermissionResponseAction) {
            PermissionResponseAction pra = (PermissionResponseAction) action;
            sessionManager.handlePermissionResponse(pra.getStudentUsername(), pra.isGranted());
            server.updateAndBroadcastUserList();
        } else if (action instanceof RevokePermissionAction) {
            RevokePermissionAction rpa = (RevokePermissionAction) action;
            sessionManager.handleRevokePermission(rpa.getStudentUsername());
            server.updateAndBroadcastUserList();
        } else if (action instanceof LogoutAction) {
            try { clientSocket.close(); } catch (IOException e) {}
        } else {
            // 其他绘图指令
            if (inSharedMode) {
                server.processAndBroadcastSharedAction(action, this);
            }
        }
    }

    public void sendAction(Action action) throws IOException {
        if (oos != null && !clientSocket.isClosed()) {
            oos.writeObject(action);
            oos.flush();
        }
    }

    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public boolean isInSharedMode() { return inSharedMode; }
}