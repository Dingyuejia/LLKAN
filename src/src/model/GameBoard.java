package src.model;

import src.user.SaveManager;

/**
 * 游戏棋盘数据结构。
 *
 * <p>该类只负责保存“棋盘是什么样”，不包含 UI 绘制，也不直接包含消除算法。
 * UI（如 BoardPanel）会通过 {@link #getCell(int, int)} 获取格子状态进行绘制与交互。</p>
 *
 * <p>约定：board[row][col] 通常应为非 null 的 Cell；为了兼容历史数据或异常情况，
 * 部分方法（如 {@link #clearAllChosen()}）仍保留 null 防御。</p>
 */
public class GameBoard {
    int rowCnt;
    int colCnt;
    public Cell[][] board;

    /**
     * 使用外部构造好的二维数组创建棋盘。
     *
     * @param rowCnt 行数
     * @param colCnt 列数
     * @param board  棋盘二维数组（下标按 row/col 访问）
     */
    public GameBoard(int rowCnt, int colCnt, Cell[][] board) {
        this.rowCnt = rowCnt;
        this.colCnt = colCnt;
        this.board = board;
    }

    /**
     * 从存档数据恢复棋盘。
     *
     * <p>SaveData 中存的是每个格子的 iconIndex 与是否为空的标记，这里会重新创建 Cell 对象。</p>
     */
    public GameBoard(SaveManager.SaveData data) {
        this.rowCnt = data.rows;
        this.colCnt = data.cols;
        this.board = new Cell[rowCnt][colCnt];

        for (int r = 0; r < data.rows; r++) {
            for (int c = 0; c < data.cols; c++) {
                board[r][c] = new Cell(new Position(r, c), data.ifEmpty[r][c], data.cellIndex[r][c]);
            }
        }

    }

    /**
     * @return 棋盘行数
     */
    public int getRowCnt() {
        return rowCnt;
    }

    /**
     * @return 棋盘列数
     */
    public int getColCnt() {
        return colCnt;
    }

    /**
     * 获取指定坐标的格子。
     *
     * @param row 行
     * @param col 列
     * @return 对应 Cell（理论上不应为 null）
     */
    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    /**
     * 清空所有格子的“选中”状态。
     *
     * <p>UI 在每次开始新一次点击选择前会调用该方法，避免出现多个格子同时处于选中态。</p>
     */
    public void clearAllChosen() {
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                if (board[i][j] != null) {
                    board[i][j].setChosen(false);
                }
            }
        }
    }

}
