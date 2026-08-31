package src.user;

import src.model.Cell;
import src.model.GameBoard;
import src.ui.StatusPanel;

import java.io.*;

/**
 * 存档管理器。
 *
 * <p>负责把当前棋盘与状态栏数据（时间/分数/连消/剩余对数/难度）序列化到本地文件，
 * 并在读档时反序列化为 {@link SaveData} 供 UI 恢复游戏。</p>
 *
 * <p>存档文件位置：{@code saves/{username}_slot{slot}.sav}</p>
 */
public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    static {
        new File(SAVE_DIR).mkdirs();
    }

    /**
     * 获取存档文件路径（相对项目运行目录）。
     *
     * @param username 用户名
     * @param slot     槽位（从 1 开始）
     */
    public String getSaveFilePath(String username, int slot) {
        return SAVE_DIR + username + "_slot" + slot + ".sav";
    }

    /**
     * 保存游戏。
     *
     * <p>序列化内容由 {@link SaveData} 定义；其中棋盘信息按二维数组逐格保存：</p>
     * <ul>
     *   <li>cellIndex：每个格子的棋子类型编号</li>
     *   <li>ifEmpty：每个格子是否为空</li>
     * </ul>
     */
    public void saveGame(User user, int slot, GameBoard board, StatusPanel statusPanel) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFilePath(user.getUsername(), slot)))) {
            SaveData data = new SaveData();
            data.rows = board.getRowCnt();
            data.cols = board.getColCnt();
            data.cellIndex = new int[data.rows][data.cols];
            data.ifEmpty = new boolean[data.rows][data.cols];
            for (int r = 0; r < data.rows; r++) {
                for (int c = 0; c < data.cols; c++) {
                    Cell cell = board.getCell(r, c);
                    if (cell != null) {
                        data.cellIndex[r][c] = cell.getIconIndex();
                        data.ifEmpty[r][c] = cell.isEmpty();
                    } else {
                        // 理论上棋盘格子不应为 null；这里保留兜底，避免异常数据导致存档失败。
                        data.cellIndex[r][c] = -1;
                    }
                }
            }
            data.score = statusPanel.getScore();
            data.timeRemained = statusPanel.getTotalSeconds();
            data.combo=statusPanel.getCombo();
            data.pairsRemained=statusPanel.getRemainPairs();
            if(data.cols==11){
                // 约定：简单模式为 11x11
                data.difficulty=0;
            }else {
                // 约定：困难模式为 12x12
                data.difficulty=1;
            }
            oos.writeObject(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取存档。
     *
     * @return SaveData（由 MainFrame/GamePanel 负责把数据恢复到棋盘与 UI）
     * @throws Exception 存档不存在或反序列化失败时抛出“存档无效”
     */
    public SaveData loadGame(User user, int slot) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSaveFilePath(user.getUsername(), slot)))) {
            return (SaveData) ois.readObject();
        } catch (Exception e) {
            throw new Exception("存档无效");
        }
    }

    /**
     * 存档数据结构（可序列化）。
     *
     * <p>字段说明：</p>
     * <ul>
     *   <li>rows/cols：棋盘尺寸</li>
     *   <li>cellIndex：每个格子的棋子类型编号</li>
     *   <li>ifEmpty：每个格子是否为空</li>
     *   <li>score：当前分数</li>
     *   <li>timeRemained：剩余时间（秒）</li>
     *   <li>difficulty：难度（0 简单 / 1 困难）</li>
     *   <li>combo：当前连消数</li>
     *   <li>pairsRemained：剩余可消除对数</li>
     * </ul>
     */
    public static class SaveData implements Serializable {
        public int rows, cols;
        public int[][] cellIndex;
        public boolean[][] ifEmpty;
        public int score;
        public int timeRemained;
        public int difficulty;
        public int combo;
        public int pairsRemained;
    }
}
