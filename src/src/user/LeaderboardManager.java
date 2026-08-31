package src.user;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 排行榜管理器（本地持久化）。
 *
 * <p>数据会序列化存储到 {@code leaderboard.dat}，程序启动时会自动加载。</p>
 *
 * <p>排序规则：</p>
 * <ul>
 *   <li>分数从高到低</li>
 *   <li>同分时按时间戳从早到晚（更早的成绩排在前）</li>
 * </ul>
 *
 * <p>线程安全：对外方法使用 synchronized，避免在 UI 多线程环境下读写同一份 entries 发生并发问题。</p>
 */
public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.dat";
    private final int maxEntries;
    private List<ScoreEntry> entries;

    /**
     * 默认最多保留 50 条成绩。
     */
    public LeaderboardManager() {
        this(50);
    }

    /**
     * @param maxEntries 最多保留的成绩条数（超过后会裁剪）
     */
    public LeaderboardManager(int maxEntries) {
        this.maxEntries = maxEntries;
        this.entries = new ArrayList<>();
        load();
    }

    /**
     * 记录一条成绩（并立即落盘）。
     *
     * @param username    用户名（空/空白会被替换为“游客”）
     * @param score       分数
     * @param difficulty  难度（0 简单 / 1 困难）
     * @param win         是否通关（当前 UI 仅在通关时写榜，但字段保留以便未来扩展）
     */
    public synchronized void record(String username, int score, int difficulty, boolean win) {
        if (username == null || username.isBlank()) username = "游客";
        entries.add(new ScoreEntry(username, score, difficulty, win, System.currentTimeMillis()));
        sortAndTrim();
        save();
    }

    /**
     * 获取前 N 名（按当前排序规则）。
     */
    public synchronized List<ScoreEntry> getTop(int n) {
        int size = Math.min(n, entries.size());
        return new ArrayList<>(entries.subList(0, size));
    }

    /**
     * 获取全部成绩（会返回拷贝，避免外部修改内部列表）。
     */
    public synchronized List<ScoreEntry> getAll() {
        return new ArrayList<>(entries);
    }

    private void sortAndTrim() {
        entries.sort(Comparator
                .comparingInt(ScoreEntry::getScore).reversed()
                .thenComparingLong(ScoreEntry::getTimestamp));
        if (entries.size() > maxEntries) {
            entries = new ArrayList<>(entries.subList(0, maxEntries));
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                entries = (List<ScoreEntry>) obj;
                sortAndTrim();
            }
        } catch (FileNotFoundException e) {
            entries = new ArrayList<>();
        } catch (Exception e) {
            entries = new ArrayList<>();
        }
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(entries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
