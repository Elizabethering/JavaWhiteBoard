package com.elizabethering.javawhiteboard.client.gui;

import com.elizabethering.javawhiteboard.client.ClientApp;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final ClientApp clientApp;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame(ClientApp clientApp) {
        this.clientApp = clientApp;
        setTitle("登录 / 注册");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("用户名:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(e -> onAuthAction(true));
        registerButton.addActionListener(e -> onAuthAction(false));

        setVisible(true);
    }

    private void onAuthAction(boolean isLogin) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(isLogin) {
            clientApp.attemptLogin(username, password);
        } else {
            clientApp.attemptRegister(username, password);
        }
    }
}