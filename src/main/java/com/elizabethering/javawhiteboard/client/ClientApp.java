package com.elizabethering.javawhiteboard.client;

import com.elizabethering.javawhiteboard.client.gui.LoginFrame;
import com.elizabethering.javawhiteboard.client.gui.MainFrame;
import com.elizabethering.javawhiteboard.client.gui.ModeSelectionFrame;
import com.elizabethering.javawhiteboard.shared.model.*;
import com.elizabethering.javawhiteboard.shared.model.Action;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 客户端主应用程序。
 */
public class ClientApp {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9999;

    private ObjectOutputStream oos;
    private Socket socket;

    private LoginFrame loginFrame;
    private ModeSelectionFrame modeSelectionFrame;
    private MainFrame mainFrame;

    private String username;
    private UserRole userRole;
    private SessionMode sessionMode;
    private volatile boolean canDraw = false;

    public static void main(String[] args) {
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        Font font = new Font("Microsoft YaHei UI", Font.PLAIN, 14);
        initGlobalFont(font);
        new ClientApp().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            SwingUtilities.invokeLater(() -> loginFrame = new LoginFrame(this));
            new Thread(() -> listenToServer(ois)).start();
        } catch (IOException e) {
            showErrorAndExit("无法连接到服务器: " + e.getMessage());
        }
    }

    private void listenToServer(ObjectInputStream ois) {
        try {
            while (!socket.isClosed()) {
                Action actionFromServer = (Action) ois.readObject();

                if (actionFromServer instanceof AuthResultAction) {
                    handleAuthResult((AuthResultAction) actionFromServer);
                } else if (actionFromServer instanceof SessionJoinResultAction) {
                    handleSessionJoinResult((SessionJoinResultAction) actionFromServer);
                } else if (actionFromServer instanceof PermissionStatusUpdateAction) {
                    handlePermissionStatusUpdate((PermissionStatusUpdateAction) actionFromServer);
                } else if (actionFromServer instanceof IncomingPermissionRequestAction) {
                    handleIncomingPermissionRequest((IncomingPermissionRequestAction) actionFromServer);
                } else if (actionFromServer instanceof UserListUpdateAction) {
                    handleUserListUpdate((UserListUpdateAction) actionFromServer);
                } else if (actionFromServer instanceof ShapeAction || actionFromServer instanceof TextAction || actionFromServer instanceof ClearAction) {
                    handleDrawingAction(actionFromServer);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!socket.isClosed()) {
                showErrorAndExit("与服务器的连接已断开。");
            }
        }
    }

    private void handleAuthResult(AuthResultAction result) {
        SwingUtilities.invokeLater(() -> {
            if (result.isSuccess()) {
                this.username = result.getUsername();
                if (loginFrame != null) loginFrame.dispose();
                modeSelectionFrame = new ModeSelectionFrame(this, this.username);
            } else {
                JOptionPane.showMessageDialog(loginFrame, result.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void handleSessionJoinResult(SessionJoinResultAction result) {
        this.userRole = result.getUserRole();
        this.canDraw = result.canDraw();

        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                mainFrame.configureForRole(this.userRole, this.canDraw);
            }
        });
    }

    private void handlePermissionStatusUpdate(PermissionStatusUpdateAction update) {
        this.canDraw = update.canDraw();
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                JOptionPane.showMessageDialog(mainFrame, update.getMessage(), "权限通知", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.updateDrawingPermission(this.canDraw);
            }
        });
    }

    private void handleIncomingPermissionRequest(IncomingPermissionRequestAction request) {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null && this.userRole == UserRole.TEACHER) {
                int response = JOptionPane.showConfirmDialog(
                        mainFrame,
                        "学生 '" + request.getStudentUsername() + "' 请求绘图权限。是否批准？",
                        "权限请求", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                );
                sendAction(new PermissionResponseAction(request.getStudentUsername(), response == JOptionPane.YES_OPTION));
            }
        });
    }

    private void handleUserListUpdate(UserListUpdateAction update) {
        if (sessionMode == SessionMode.SHARED && mainFrame != null) {
            SwingUtilities.invokeLater(() -> mainFrame.updateUserList(update.getUserStatuses()));
        }
    }

    private void handleDrawingAction(Action action) {
        if (sessionMode == SessionMode.SHARED && mainFrame != null) {
            mainFrame.getDrawingPanel().processServerAction(action);
        }
    }

    /**
     * 用户选择模式后的核心处理方法。
     * @param mode 用户选择的模式 (PRIVATE 或 SHARED)。
     */
    public void enterMode(SessionMode mode) {
        this.sessionMode = mode;

        // 直接在当前线程（Swing事件分发线程）中创建主窗口。
        // 这能确保在发送任何网络请求之前，mainFrame变量一定已经被赋值。
        mainFrame = new MainFrame(this);

        if (mode == SessionMode.PRIVATE) {
            // 私人模式下，客户端自己就可以决定角色和权限
            this.userRole = UserRole.TEACHER;
            this.canDraw = true;
            // 直接配置UI
            mainFrame.configureForRole(this.userRole, this.canDraw);
        } else { // 共享模式
            // 现在可以安全地发送请求，因为我们知道mainFrame已经准备好了
            sendAction(new StartSessionAction());
        }
    }

    public void showErrorAndExit(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "严重错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });
    }

    public void sendAction(Action action) {
        try {
            if (oos != null && !socket.isClosed()) {
                oos.writeObject(action);
                oos.flush();
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                showErrorAndExit("发送指令失败，连接可能已中断。");
            }
        }
    }

    public String getUsername() { return username; }
    public UserRole getUserRole() { return userRole; }
    public SessionMode getSessionMode() { return sessionMode; }
    public boolean canDraw() { return canDraw; }

    public void attemptLogin(String u, String p) { sendAction(new LoginAction(u, p)); }
    public void attemptRegister(String u, String p) { sendAction(new RegisterAction(u, p)); }

    private static void initGlobalFont(Font f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while(keys.hasMoreElements()){
            Object key = keys.nextElement();
            if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, new javax.swing.plaf.FontUIResource(f));
        }
    }
}