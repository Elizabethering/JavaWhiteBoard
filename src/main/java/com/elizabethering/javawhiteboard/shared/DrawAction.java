package com.elizabethering.javawhiteboard.shared;

import java.awt.Color;
import java.io.Serializable;

/**
 * 代表一个绘图动作的数据传输对象 (DTO - Data Transfer Object)。
 * 实现了 Serializable 接口，使其可以被序列化并通过网络流进行传输。
 */
public class DrawAction implements Serializable {

    // 为了确保序列化和反序列化的兼容性，最好显式声明一个serialVersionUID
    private static final long serialVersionUID = 1L;

    private final int x1; // 起点x坐标
    private final int y1; // 起点y坐标
    private final int x2; // 终点x坐标
    private final int y2; // 终点y坐标
    private final Color color; // 颜色
    private final float strokeWidth; // 画笔粗细

    public DrawAction(int x1, int y1, int x2, int y2, Color color, float strokeWidth) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    // 提供getter方法以供外部访问数据，但没有setter，使其成为一个不可变对象，更安全
    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public Color getColor() {
        return color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    // 重写toString()方法，这在调试时非常有用，可以直接打印对象内容
    @Override
    public String toString() {
        return "DrawAction{" +
                "from=(" + x1 + "," + y1 +
                "), to=(" + x2 + "," + y2 +
                "), color=" + color.getRGB() +
                ", strokeWidth=" + strokeWidth +
                '}';
    }
}