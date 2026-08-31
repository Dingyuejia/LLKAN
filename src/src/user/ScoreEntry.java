package src.user;

import java.io.Serializable;

/**
 * 排行榜中的一条成绩记录（可序列化）。
 */
public class ScoreEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final int score;
    private final int difficulty;
    private final boolean win;
    private final long timestamp;

    /**
     * @param username   用户名
     * @param score      分数
     * @param difficulty 难度（0 简单 / 1 困难）
     * @param win        是否通关
     * @param timestamp  记录时间（毫秒时间戳）
     */
    public ScoreEntry(String username, int score, int difficulty, boolean win, long timestamp) {
        this.username = username;
        this.score = score;
        this.difficulty = difficulty;
        this.win = win;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public int getScore() { return score; }
    public int getDifficulty() { return difficulty; }
    public boolean isWin() { return win; }
    public long getTimestamp() { return timestamp; }
}
