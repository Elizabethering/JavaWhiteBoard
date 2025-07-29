package com.elizabethering.javawhiteboard.shared.model;

import java.io.Serializable;

/**
 * 定义用户可以选择的工作模式。
 */
public enum SessionMode implements Serializable {
    /**
     * 私人模式，用户独立在自己的画板上工作，不受他人影响。
     */
    PRIVATE,
    /**
     * 共享模式，用户参与到一个多人的协作画板中。
     */
    SHARED
}