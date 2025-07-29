package com.elizabethering.javawhiteboard.shared.model;

import java.io.Serializable;

/**
 * 定义用户的角色（老师或学生）。
 * 这个枚举将决定用户在共享画板中的权限。
 */
public enum UserRole implements Serializable {
    /**
     * 老师角色，拥有完整的绘图和管理权限。
     */
    TEACHER,

    /**
     * 学生角色，默认没有绘图权限，需要向老师申请。
     */
    STUDENT
}