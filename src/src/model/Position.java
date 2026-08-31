package src.model;

/**
 * 棋盘坐标（行、列）。
 *
 * <p>约定：row/col 均为从 0 开始的下标，与二维数组 board[row][col] 的访问方式一致。</p>
 */
public class Position {
    private int row;
    private int col;

    /**
     * @param row 行下标（从 0 开始）
     * @param col 列下标（从 0 开始）
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return 行下标
     */
    public int getRow() {
        return row;
    }

    /**
     * @return 列下标
     */
    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        return result;
    }
}
