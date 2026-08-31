package src.model;

import java.awt.*;

/**
 * UI 绘制用矩形（位置 + 尺寸）。
 *
 * <p>该类与 AWT/Swing 的 {@link java.awt.Rectangle} 不同，这里只保留项目需要的字段与方法，
 * 并提供中心点计算用于连线绘制。</p>
 */
public class Rectangle {
    int x;
    int y;
    int width;
    int height;

    /**
     * @param x 左上角 x
     * @param y 左上角 y
     * @param width 宽
     * @param height 高
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @return 矩形中心点（用于画连线）
     */
    public Point getCenterPosition() {
        return new Point(x + width / 2, y + height / 2);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
