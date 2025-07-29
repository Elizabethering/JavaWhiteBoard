package com.elizabethering.javawhiteboard.server;

import com.elizabethering.javawhiteboard.shared.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 管理共享画板会话、用户角色和权限的核心类。
 */
public class SessionManager {

    private ClientHandler teacherHandler = null;
    private final Map<String, ClientHandler> students = new ConcurrentHashMap<>();
    private final Set<ClientHandler> authorizedStudents = ConcurrentHashMap.newKeySet();

    public synchronized UserRole addUserToSession(ClientHandler userHandler) {
        if (teacherHandler == null) {
            teacherHandler = userHandler;
            System.out.println("用户 " + userHandler.getUsername() + " 已成为老师，并创建了新会话。");
            return UserRole.TEACHER;
        } else {
            students.put(userHandler.getUsername(), userHandler);
            System.out.println("用户 " + userHandler.getUsername() + " 已作为学生加入会话。");
            return UserRole.STUDENT;
        }
    }

    public synchronized void removeUserFromSession(ClientHandler userHandler) {
        if (userHandler == teacherHandler) {
            System.out.println("老师 " + userHandler.getUsername() + " 已离开，会话结束。");
            teacherHandler = null;
            students.clear();
            authorizedStudents.clear();
        } else {
            if (students.remove(userHandler.getUsername()) != null) {
                authorizedStudents.remove(userHandler);
                System.out.println("学生 " + userHandler.getUsername() + " 已离开会话。");
            }
        }
    }

    public boolean canDraw(ClientHandler userHandler) {
        if (userHandler == null) return false;
        if (userHandler.getRole() == UserRole.TEACHER) {
            return true;
        }
        return authorizedStudents.contains(userHandler);
    }

    public void handlePermissionRequest(ClientHandler studentHandler) {
        if (teacherHandler != null) {
            try {
                Action notification = new IncomingPermissionRequestAction(studentHandler.getUsername());
                teacherHandler.sendAction(notification);
                System.out.println("已将学生 " + studentHandler.getUsername() + " 的权限请求转发给老师。");
            } catch (IOException e) {
                System.err.println("向老师转发权限请求时出错: " + e.getMessage());
            }
        }
    }

    public void handlePermissionResponse(String studentUsername, boolean granted) {
        ClientHandler studentHandler = students.get(studentUsername);
        if (studentHandler == null) {
            System.err.println("处理权限响应失败：找不到学生 " + studentUsername);
            return;
        }
        updateStudentPermission(studentHandler, granted, granted ? "老师已授予您绘图权限。" : "老师拒绝了您的绘图请求。");
    }

    /**
     * 处理来自老师的权限撤销操作。
     * @param studentUsername 目标学生的用户名。
     */
    public void handleRevokePermission(String studentUsername) {
        ClientHandler studentHandler = students.get(studentUsername);
        if (studentHandler == null) {
            System.err.println("处理权限撤销失败：找不到学生 " + studentUsername);
            return;
        }
        updateStudentPermission(studentHandler, false, "老师已撤销您的绘图权限。");
    }

    private void updateStudentPermission(ClientHandler studentHandler, boolean isAuthorized, String message) {
        if (isAuthorized) {
            authorizedStudents.add(studentHandler);
        } else {
            authorizedStudents.remove(studentHandler);
        }

        try {
            Action statusUpdate = new PermissionStatusUpdateAction(isAuthorized, message);
            studentHandler.sendAction(statusUpdate);
            System.out.println("已将权限状态更新 (" + message + ") 发送给学生 " + studentHandler.getUsername());
        } catch (IOException e) {
            System.err.println("向学生 " + studentHandler.getUsername() + " 发送权限状态更新时出错: " + e.getMessage());
        }
    }

    public List<UserStatus> getAllUserStatuses() {
        List<UserStatus> statuses = students.values().stream()
                .map(student -> new UserStatus(student.getUsername(), authorizedStudents.contains(student)))
                .collect(Collectors.toList());

        if (teacherHandler != null) {
            statuses.add(0, new UserStatus(teacherHandler.getUsername() + " (老师)", true));
        }
        return statuses;
    }
}