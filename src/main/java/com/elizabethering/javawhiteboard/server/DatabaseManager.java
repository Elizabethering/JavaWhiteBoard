package com.elizabethering.javawhiteboard.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        try {
            // 连接到 SQLite 资料库，如果不存在，它会被自动创建
            connection = DriverManager.getConnection("jdbc:sqlite:whiteboard.db");
            System.out.println("成功連接到 SQLite 資料庫。");

            // 创建用户表（如果它不存在）
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL)");
            statement.close();
        } catch (SQLException e) {
            System.err.println("资料库连接失败: " + e.getMessage());
        }
    }

    public synchronized boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // 如果用戶名已存在 (UNIQUE constraint failed)，會拋出異常
            System.err.println("注册用户失败: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean loginUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return storedHash.equals(hashPassword(password));
            }
            return false; // 用戶不存在
        } catch (SQLException e) {
            System.err.println("登入查询失败: " + e.getMessage());
            return false;
        }
    }


    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}