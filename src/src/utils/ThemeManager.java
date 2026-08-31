package src.utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主题管理器（背景图 + 棋子图）。
 *
 * <p>主题由一个全局 themeId 控制：</p>
 * <ul>
 *   <li>{@link #THEME_NUMBERS}：主题1（默认）</li>
 *   <li>{@link #THEME_THEME2}：主题2</li>
 * </ul>
 *
 * <p>Theme2 的棋子编号映射约定：</p>
 * <ul>
 *   <li>type 1..12 -&gt; 图片 13..24（即 +12）</li>
 *   <li>type &lt;=0 视为“空态值”，映射为 0（但当前棋盘渲染不会绘制空格图片）</li>
 * </ul>
 *
 * <p>资源查找顺序（棋子与背景一致）：</p>
 * <ol>
 *   <li>classpath（打包资源）：/images/*</li>
 *   <li>文件系统（新目录）：resources/images/*</li>
 *   <li>文件系统（兼容旧目录）：resource/images/*</li>
 * </ol>
 *
 * <p>缓存策略：</p>
 * <ul>
 *   <li>棋子缓存 key：themeId + ":" + type</li>
 *   <li>背景缓存 key：themeId</li>
 * </ul>
 */
public final class ThemeManager {
    public static final int THEME_NUMBERS = 0;
    public static final int THEME_THEME2 = 1;
    private static final int THEME2_TILE_OFFSET = 12;

    private static volatile int themeId = THEME_NUMBERS;
    private static final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();
    private static final Map<Integer, Image> backgroundCache = new ConcurrentHashMap<>();

    private ThemeManager() {}

    /**
     * @return 当前主题 ID
     */
    public static int getTheme() {
        return themeId;
    }

    /**
     * 设置主题 ID（仅允许 THEME_NUMBERS/THEME_THEME2）。
     */
    public static void setTheme(int id) {
        themeId = (id == THEME_THEME2) ? THEME_THEME2 : THEME_NUMBERS;
    }

    /**
     * @return 主题名称（用于 UI 显示）
     */
    public static String getThemeName(int id) {
        return (id == THEME_THEME2) ? "主题2" : "主题1";
    }

    /**
     * 获取棋子图片（带缓存）。
     *
     * @param type 棋子类型编号（由 Cell.iconIndex 提供）
     */
    public static ImageIcon getIcon(int type) {
        int id = themeId;
        int fileNo;
        if (type <= 0) {
            fileNo = 0;
        } else {
            fileNo = (id == THEME_THEME2) ? (type + THEME2_TILE_OFFSET) : type;
        }
        String key = id + ":" + type;
        return cache.computeIfAbsent(key, k -> loadIcon(fileNo));
    }

    /**
     * 获取当前主题背景图（带缓存）。
     */
    public static Image getBackgroundImage() {
        int id = themeId;
        return backgroundCache.computeIfAbsent(id, ThemeManager::loadBackgroundImage);
    }

    private static ImageIcon loadIcon(int fileNo) {
        String resourcePath = "/" + "images/" + fileNo + ".png";

        URL url = ThemeManager.class.getResource(resourcePath);
        if (url != null) {
            return new ImageIcon(url);
        }

        Path p1 = Paths.get("resources", "images", fileNo + ".png");
        if (Files.isRegularFile(p1)) {
            return new ImageIcon(p1.toString());
        }

        Path p2 = Paths.get("resource", "images", fileNo + ".png");
        if (Files.isRegularFile(p2)) {
            return new ImageIcon(p2.toString());
        }

        throw new IllegalStateException("找不到图片资源: " + resourcePath + " 或 " + p1 + " 或 " + p2);
    }

    private static Image loadBackgroundImage(int id) {
        String fileName = (id == THEME_THEME2) ? "theme2.png" : "theme1.png";
        String resourcePath = "/images/" + fileName;
        URL url = ThemeManager.class.getResource(resourcePath);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        Path p1 = Paths.get("resources", "images", fileName);
        if (Files.isRegularFile(p1)) {
            return new ImageIcon(p1.toString()).getImage();
        }

        Path p2 = Paths.get("resource", "images", fileName);
        if (Files.isRegularFile(p2)) {
            return new ImageIcon(p2.toString()).getImage();
        }

        throw new IllegalStateException("找不到背景资源: " + resourcePath + " 或 " + p1 + " 或 " + p2);
    }
}
