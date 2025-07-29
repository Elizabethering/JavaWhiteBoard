package com.elizabethering.javawhiteboard.server;

import com.elizabethering.javawhiteboard.shared.model.Action;
import com.elizabethering.javawhiteboard.shared.model.ClearAction;
import com.elizabethering.javawhiteboard.shared.model.UserListUpdateAction;
import com.elizabethering.javawhiteboard.shared.model.UserStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 服务器主应用程序类。
 * 修改后，它将依赖 SessionManager 来处理会话逻辑。
 */
public class ServerApp {

    public static final int PORT = 9999;
    // 这个列表现在只跟踪所有连接的客户端，无论他们处于何种模式
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    // 绘图历史现在只用于共享画板
    private final List<Action> drawingHistory = new CopyOnWriteArrayList<>();
    private final DatabaseManager dbManager;
    private final SessionManager sessionManager;

    public ServerApp() {
        this.dbManager = new DatabaseManager();
        this.sessionManager = new SessionManager();
    }

    public static void main(String[] args) {
        new ServerApp().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("多功能协作画板服务器已启动，正在监听 " + PORT + " 端口...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, dbManager, sessionManager);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理来自客户端的指令，并根据权限决定是否广播。
     * 这个方法现在只处理共享画板中的绘图指令。
     * @param action 客户端发送的指令。
     * @param sender 发送指令的客户端处理器。
     */
    public synchronized void processAndBroadcastSharedAction(Action action, ClientHandler sender) {
        if (sender.getUsername() == null) return;


        if (!sessionManager.canDraw(sender)) {
            System.out.println("用户 " + sender.getUsername() + " 没有共享画板的绘图权限，操作被拒绝。");
            return;
        }

        // 用户有权限，正常处理和广播
        if (action instanceof ClearAction) {
            drawingHistory.clear();
            System.out.println("用户 " + sender.getUsername() + " 清空了共享画板。");
        } else {
            // 只有形状和文本指令才被添加到历史记录中
            if (action instanceof com.elizabethering.javawhiteboard.shared.model.ShapeAction || action instanceof com.elizabethering.javawhiteboard.shared.model.TextAction) {
                drawingHistory.add(action);
            }
        }
        // 将操作广播给所有在共享会话中的用户
        broadcastToSharedSession(action);
    }

    /**
     * 将一个指令广播给所有在共享会话中的用户。
     * @param action 要广播的指令。
     */
    public void broadcastToSharedSession(Action action) {
        clients.stream()
                .filter(ClientHandler::isInSharedMode) // 只选择处于共享模式的客户端
                .forEach(client -> {
                    try {
                        client.sendAction(action);
                    } catch (IOException e) {
                        removeClient(client);
                    }
                });
    }

    /**
     * 从服务器移除一个客户端。
     * @param client 要移除的客户端处理器。
     */
    public synchronized void removeClient(ClientHandler client) {
        if (clients.remove(client)) {
            System.out.println("用户 " + (client.getUsername() == null ? "[未登录]" : client.getUsername()) + " 已断开连接。");
            // 如果用户在共享模式下，需要将他从会话中移除并更新列表
            if(client.isInSharedMode()){
                sessionManager.removeUserFromSession(client);
                updateAndBroadcastUserList();
            }
        }
    }

    /**
     * 向共享会话中的所有客户端广播最新的用户列表和他们的状态。
     */
    public void updateAndBroadcastUserList() {
        List<UserStatus> statuses = sessionManager.getAllUserStatuses();
        UserListUpdateAction updateAction = new UserListUpdateAction(statuses);
        System.out.println("更新共享会话用户列表: " + statuses.stream().map(UserStatus::getUsername).collect(Collectors.toList()));
        broadcastToSharedSession(updateAction);
    }

    /**
     * 获取共享画板的完整绘图历史。
     * @return 包含所有绘图指令的列表。
     */
    public List<Action> getDrawingHistory() {
        return drawingHistory;
    }
}