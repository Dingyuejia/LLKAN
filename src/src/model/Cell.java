package src.model;

/**
 * 棋盘上的一个格子（Cell）。
 *
 * <p>一个格子由三个核心状态组成：</p>
 * <ul>
 *   <li>pos：在棋盘上的坐标（行/列）。</li>
 *   <li>isEmpty：是否为空格。空格不可被点击消除。</li>
 *   <li>iconIndex：棋子类型编号。仅在非空格时有意义，用于匹配相同图案的棋子。</li>
 * </ul>
 *
 * <p>此外 isChosen 用于 UI 选中态（绘制红框），不参与消除规则本身。</p>
 */
public class Cell {
    Position pos;
    boolean isEmpty;
    int iconIndex;
    boolean isChosen;


    /**
     * 创建一个格子。
     *
     * @param pos      格子坐标
     * @param isEmpty  是否为空格
     * @param iconIndex 棋子类型编号（空格时该值通常为 0）
     */
    public Cell(Position pos, boolean isEmpty, int iconIndex) {
        this.pos = pos;
        this.isEmpty = isEmpty;
        this.iconIndex = iconIndex;
    }

    /**
     * @return 当前格子是否处于 UI 选中状态
     */
    public boolean getIsChosen() {
        return isChosen;
    }

    /**
     * 设置 UI 选中状态。
     */
    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    /**
     * @return 格子在棋盘上的坐标
     */
    public Position getPos() {
        return pos;
    }

    /**
     * @return 是否为空格
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * @return 棋子类型编号（仅非空格时有意义）
     */
    public int getIconIndex() {
        return iconIndex;
    }

    /**
     * 设置空格状态。
     *
     * <p>历史实现中会把空格的 iconIndex 统一设置为 0 作为“空态值”。当前渲染逻辑不会把空格绘制成 0.png，
     * 但保留该约定有助于存档/调试时对空态的表达一致。</p>
     */
    public void setEmpty(boolean empty) {
        isEmpty = empty;
        iconIndex = 0;
    }


}
