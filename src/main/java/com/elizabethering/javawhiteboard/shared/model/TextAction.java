package com.elizabethering.javawhiteboard.shared.model;

import java.awt.Color;
import java.awt.Font;

public class TextAction implements Action {
    private static final long serialVersionUID = 6L; // 新的类，新的版本号

    private final String text;
    private final int x;
    private final int y;
    private final Color color;
    private final Font font; // 让指令可以携带字体信息

    public TextAction(String text, int x, int y, Color color, Font font) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.font = font;
    }

    // 为所有字段提供 Getters
    public String getText() { return text; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getColor() { return color; }
    public Font getFont() { return font; }
}