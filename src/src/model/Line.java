package src.model;

/**
 * 两个格子之间的连线段。
 *
 * <p>用于 UI 绘制消除路径（直线/折线/拐弯），不包含路径搜索逻辑。</p>
 */
public class Line {
    Cell cell1;
    Cell cell2;

    /**
     * @param cell1 线段起点格子
     * @param cell2 线段终点格子
     */
    public Line(Cell cell1, Cell cell2) {
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    /**
     * @return 起点格子
     */
    public Cell getCell1() {
        return cell1;
    }

    /**
     * @return 终点格子
     */
    public Cell getCell2() {
        return cell2;
    }
}
