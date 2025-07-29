package com.elizabethering.javawhiteboard.client.gui;

import com.elizabethering.javawhiteboard.client.ClientApp;
import com.elizabethering.javawhiteboard.shared.model.SessionMode;

import javax.swing.*;
import java.awt.*;

/**
 * 登录成功后，用户选择进入“私人画板”或“共享会话”的窗口。
 */
public class ModeSelectionFrame extends JFrame {

    private final ClientApp clientApp;

    public ModeSelectionFrame(ClientApp clientApp, String username) {
        this.clientApp = clientApp;
        setTitle("欢迎, " + username + " - 请选择模式");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板和欢迎标签
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel welcomeLabel = new JLabel("请选择您的工作模式:", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        JButton privateButton = new JButton("私人画板");
        JButton sharedButton = new JButton("共享会话");

        // 设置按钮样式
        Font buttonFont = new Font("Microsoft YaHei UI", Font.PLAIN, 14);
        privateButton.setFont(buttonFont);
        sharedButton.setFont(buttonFont);

        // 添加按钮点击事件
        privateButton.addActionListener(e -> {
            clientApp.enterMode(SessionMode.PRIVATE);
            dispose();
        });

        sharedButton.addActionListener(e -> {
            clientApp.enterMode(SessionMode.SHARED);
            dispose();
        });

        buttonPanel.add(privateButton);
        buttonPanel.add(sharedButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }
}