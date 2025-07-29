package com.elizabethering.javawhiteboard.client.gui;

import com.elizabethering.javawhiteboard.client.ClientApp;
import com.elizabethering.javawhiteboard.shared.model.*;
import com.elizabethering.javawhiteboard.shared.model.Action;
import com.elizabethering.javawhiteboard.shared.model.ShapeAction;
import com.elizabethering.javawhiteboard.shared.model.TextAction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


/**
 * 绘图面板，用户在这里进行绘图操作。
 */
public class DrawingPanel extends JPanel {
    private final List<Action> actions = new ArrayList<>();
    private Point startPoint;
    private final MainFrame mainFrame;

    // 绘图属性
    private ShapeType currentTool = ShapeType.LINE;
    private Color currentColor = Color.BLACK;
    private float currentStroke = 2.0f;

    public DrawingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        setEnabled(false); // 初始时禁用，由MainFrame根据模式和权限启用

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!getClientApp().canDraw()) return; // 最终权限检查

                startPoint = e.getPoint();
                if (currentTool == ShapeType.TEXT) {
                    handleTextCreation(e.getPoint());
                    startPoint = null; // 文本工具不需要拖拽
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!getClientApp().canDraw() || startPoint == null) return;

                if (currentTool == ShapeType.LINE || currentTool == ShapeType.ERASER) {
                    handleContinuousDrawing(e.getPoint());
                    startPoint = e.getPoint(); // 更新起点以实现连续绘图
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!getClientApp().canDraw() || startPoint == null) return;

                if (currentTool == ShapeType.RECTANGLE || currentTool == ShapeType.OVAL || currentTool == ShapeType.TRIANGLE) {
                    handleShapeCreation(e.getPoint());
                }
                startPoint = null;
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    // --- 绘图逻辑处理 ---

    private void handleTextCreation(Point p) {
        String text = JOptionPane.showInputDialog(mainFrame, "请输入文本:", "添加文本", JOptionPane.PLAIN_MESSAGE);
        if (text != null && !text.trim().isEmpty()) {
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) currentStroke + 12);
            TextAction action = new TextAction(text, p.x, p.y, currentColor, font);
            processLocalAction(action);
        }
    }

    private void handleContinuousDrawing(Point endPoint) {
        Color color = (currentTool == ShapeType.ERASER) ? getBackground() : currentColor;
        float stroke = (currentTool == ShapeType.ERASER) ? currentStroke + 10 : currentStroke;
        Shape line = new Line2D.Float(startPoint, endPoint);
        ShapeAction action = new ShapeAction(currentTool, line, color, stroke);
        processLocalAction(action);
    }

    private void handleShapeCreation(Point endPoint) {
        Shape shape = createShape(startPoint, endPoint);
        if (shape != null) {
            ShapeAction action = new ShapeAction(currentTool, shape, currentColor, currentStroke);
            processLocalAction(action);
        }
    }

    private Shape createShape(Point start, Point end) {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);

        switch (currentTool) {
            case RECTANGLE:
                return new Rectangle2D.Float(x, y, width, height);
            case OVAL:
                return new Ellipse2D.Float(x, y, width, height);
            case TRIANGLE:
                return new Polygon(new int[]{start.x, end.x, (start.x + end.x) / 2}, new int[]{end.y, end.y, start.y}, 3);
            default:
                return null;
        }
    }

    private void processLocalAction(Action action) {
        addActionAndRepaint(action);
        if (getClientApp().getSessionMode() == SessionMode.SHARED) {
            getClientApp().sendAction(action);
        }
    }

    public void processServerAction(Action action) {
        if (action instanceof ClearAction) {
            clearCanvasInternal();
        } else {
            addActionAndRepaint(action);
        }
    }

    /**
     * 绘制所有图形和文本。
     * 使用经典的 instanceof 和类型转换以兼容 Java 8。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Action action : new ArrayList<>(actions)) {
            // ******** 这里是修正点 ********
            // 将新的 instanceof 模式匹配改写为经典的 if-else if 结构
            if (action instanceof ShapeAction) {
                ShapeAction sa = (ShapeAction) action; // 手动进行类型转换
                g2d.setColor(sa.getColor());
                g2d.setStroke(new BasicStroke(sa.getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(sa.getShape());
            } else if (action instanceof TextAction) {
                TextAction ta = (TextAction) action; // 手动进行类型转换
                g2d.setColor(ta.getColor());
                g2d.setFont(ta.getFont());
                g2d.drawString(ta.getText(), ta.getX(), ta.getY());
            }
        }
        g2d.dispose();
    }

    // --- 公共方法 ---

    public void clearCanvas() {
        clearCanvasInternal();
        if (getClientApp().getSessionMode() == SessionMode.SHARED && getClientApp().canDraw()) {
            getClientApp().sendAction(new ClearAction());
        }
    }

    private void clearCanvasInternal() {
        actions.clear();
        repaint();
    }

    public void setActions(List<Action> newActions) {
        actions.clear();
        actions.addAll(newActions);
        repaint();
    }

    private void addActionAndRepaint(Action action) {
        actions.add(action);
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }


    public List<Action> getActions() { return new ArrayList<>(actions); }
    private ClientApp getClientApp() { return mainFrame.getClientApp(); }
    public void setCurrentTool(ShapeType tool) { this.currentTool = tool; }
    public Color getCurrentColor() { return this.currentColor; }
    public void setCurrentColor(Color color) { this.currentColor = color; }
    public void setCurrentStroke(float stroke) { this.currentStroke = stroke; }
}