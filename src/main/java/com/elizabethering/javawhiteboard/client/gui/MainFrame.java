package com.elizabethering.javawhiteboard.client.gui;

import com.elizabethering.javawhiteboard.client.ClientApp;
import com.elizabethering.javawhiteboard.shared.model.*;
import com.elizabethering.javawhiteboard.shared.model.Action;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;



/**
 * 应用程序的主窗口框架。
 */
public class MainFrame extends JFrame {

    private final ClientApp clientApp;
    private final DrawingPanel drawingPanel;

    private JList<UserStatus> userJList;
    private DefaultListModel<UserStatus> userListModel;
    private JButton requestPermissionButton;
    private JLabel permissionStatusLabel;
    private JPanel userListPanel;

    public MainFrame(ClientApp clientApp) {
        super("协作画板");
        this.clientApp = clientApp;
        this.drawingPanel = new DrawingPanel(this);
        setupUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupUI() {
        JToolBar toolBar = createToolBar();
        userListPanel = createUserListPanel();

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(userListPanel, BorderLayout.EAST);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        // 绘图工具
        ButtonGroup toolGroup = new ButtonGroup();
        ShapeType[] tools = {ShapeType.LINE, ShapeType.RECTANGLE, ShapeType.OVAL, ShapeType.TRIANGLE, ShapeType.TEXT, ShapeType.ERASER};
        String[] toolNames = {"画线", "矩形", "圆形", "三角", "文本", "橡皮"};

        for (int i = 0; i < tools.length; i++) {
            JToggleButton btn = new JToggleButton(toolNames[i]);
            btn.setActionCommand(tools[i].name());
            btn.addActionListener(e -> drawingPanel.setCurrentTool(ShapeType.valueOf(e.getActionCommand())));
            toolGroup.add(btn);
            toolBar.add(btn);
        }
        ((JToggleButton)toolBar.getComponent(0)).setSelected(true);
        toolBar.addSeparator();

        // 属性控制
        JButton colorButton = new JButton("颜色");
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(this, "选择颜色", drawingPanel.getCurrentColor());
            if (chosenColor != null) drawingPanel.setCurrentColor(chosenColor);
        });
        toolBar.add(colorButton);
        toolBar.add(new JLabel(" 粗细:"));
        JSlider strokeSlider = new JSlider(1, 20, 2);
        strokeSlider.addChangeListener(e -> drawingPanel.setCurrentStroke(strokeSlider.getValue()));
        toolBar.add(strokeSlider);
        toolBar.addSeparator();

        // 文件和画布操作
        JButton clearButton = new JButton("清空");
        clearButton.addActionListener(e -> drawingPanel.clearCanvas());
        toolBar.add(clearButton);
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveCanvas());
        toolBar.add(saveButton);
        JButton loadButton = new JButton("加载");
        loadButton.addActionListener(e -> loadCanvas());
        toolBar.add(loadButton);
        toolBar.addSeparator();

        // 角色特定控件
        requestPermissionButton = new JButton("申请绘图权限");
        requestPermissionButton.addActionListener(e -> clientApp.sendAction(new PermissionRequestAction()));
        toolBar.add(requestPermissionButton);
        permissionStatusLabel = new JLabel("权限: 未授权");
        permissionStatusLabel.setForeground(Color.RED);
        toolBar.add(permissionStatusLabel);

        return toolBar;
    }

    /**
     * 创建用户列表面板，并为老师添加右键菜单功能。
     */
    private JPanel createUserListPanel() {
        userListModel = new DefaultListModel<>();
        userJList = new JList<>(userListModel);
        userJList.setCellRenderer(new UserStatusRenderer());
        userJList.setBorder(new TitledBorder("在线用户"));


        userJList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // 只在老师右键点击时触发
                if (SwingUtilities.isRightMouseButton(e) && clientApp.getUserRole() == UserRole.TEACHER) {
                    int index = userJList.locationToIndex(e.getPoint());
                    if (index == -1) return; // 如果没点在任何用户上，则返回

                    userJList.setSelectedIndex(index);
                    UserStatus selectedUser = userJList.getSelectedValue();

                    // 确保不操作老师自己，并且用户确实存在
                    if (selectedUser != null && !selectedUser.getUsername().contains("(老师)")) {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem item;
                        // 从显示文本（如 "Jerry (已授权)"）中提取出真实的用户名 "Jerry"
                        String realUsername = selectedUser.getUsername().replace(" (已授权)", "").trim();

                        // 根据学生当前是否有权限，动态创建不同的菜单项
                        if (selectedUser.hasPermission()) {
                            item = new JMenuItem("撤销绘图权限");
                            item.addActionListener(ae -> clientApp.sendAction(new RevokePermissionAction(realUsername)));
                        } else {
                            item = new JMenuItem("授予绘图权限");
                            item.addActionListener(ae -> clientApp.sendAction(new PermissionResponseAction(realUsername, true)));
                        }
                        menu.add(item);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(userJList), BorderLayout.CENTER);
        return panel;
    }

    public void configureForRole(UserRole role, boolean canDraw) {
        setTitle(createTitle());

        boolean isShared = (clientApp.getSessionMode() == SessionMode.SHARED);
        userListPanel.setVisible(isShared);

        if (!isShared) {
            requestPermissionButton.setVisible(false);
            permissionStatusLabel.setVisible(false);
        } else {
            if (role == UserRole.TEACHER) {
                requestPermissionButton.setVisible(false);
                permissionStatusLabel.setVisible(false);
            } else {
                requestPermissionButton.setVisible(true);
                permissionStatusLabel.setVisible(true);
            }
        }
        updateDrawingPermission(canDraw);
        revalidate();
        repaint();
    }

    public void updateDrawingPermission(boolean canDraw) {
        drawingPanel.setEnabled(canDraw);
        if (clientApp.getSessionMode() == SessionMode.SHARED && clientApp.getUserRole() == UserRole.STUDENT) {
            if (canDraw) {
                permissionStatusLabel.setText("权限: 已授权");
                permissionStatusLabel.setForeground(new Color(0, 128, 0));
                requestPermissionButton.setEnabled(false);
            } else {
                permissionStatusLabel.setText("权限: 未授权");
                permissionStatusLabel.setForeground(Color.RED);
                requestPermissionButton.setEnabled(true);
            }
        }
    }

    public void updateUserList(List<UserStatus> statuses) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (UserStatus status : statuses) {
                userListModel.addElement(status);
            }
        });
    }

    private void saveCanvas() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(drawingPanel.getActions());
                JOptionPane.showMessageDialog(this, "画布已成功保存！");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCanvas() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()))) {
                List<Action> loadedActions = (List<Action>) ois.readObject();
                drawingPanel.setActions(loadedActions);
                if (clientApp.getSessionMode() == SessionMode.SHARED && clientApp.getUserRole() == UserRole.TEACHER) {
                    clientApp.sendAction(new ClearAction());
                    for (Action action : loadedActions) {
                        clientApp.sendAction(action);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String createTitle() {
        SessionMode mode = clientApp.getSessionMode();
        UserRole role = clientApp.getUserRole();
        String modeStr = mode == SessionMode.PRIVATE ? "私人模式" : "共享模式";
        String roleStr = role != null ? " (" + role.toString().toLowerCase() + ")" : "";
        return "协作画板 - " + clientApp.getUsername() + " | " + modeStr + roleStr;
    }

    public DrawingPanel getDrawingPanel() { return drawingPanel; }
    public ClientApp getClientApp() { return clientApp; }
}

class UserStatusRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof UserStatus) {
            UserStatus status = (UserStatus) value;
            setText(status.toString());
            if (status.getUsername().contains("(老师)")) {
                setForeground(Color.BLUE);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (status.hasPermission()) {
                setForeground(new Color(0, 100, 0));
            } else {
                setForeground(Color.GRAY);
            }
        }
        return this;
    }
}