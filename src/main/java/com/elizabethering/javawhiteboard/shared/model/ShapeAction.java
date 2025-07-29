package com.elizabethering.javawhiteboard.shared.model;

import java.awt.Color;
import java.awt.Shape;

/**
 * 一个通用的形状绘制指令，可以表示线条、矩形、圆形等。
 */
public class ShapeAction implements Action {
    private static final long serialVersionUID = 2L; // 新的类，新的版本号

    private final ShapeType shapeType; // 形状类型
    private final Shape shape;         // 形状对象 (例如 Line2D, Rectangle2D)
    private final Color color;         // 颜色
    private final float strokeWidth;   // 粗细

    public ShapeAction(ShapeType shapeType, Shape shape, Color color, float strokeWidth) {
        this.shapeType = shapeType;
        this.shape = shape;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    // Getters for all fields
    public ShapeType getShapeType() { return shapeType; }
    public Shape getShape() { return shape; }
    public Color getColor() { return color; }
    public float getStrokeWidth() { return strokeWidth; }
}