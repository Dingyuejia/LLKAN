package src.utils;

import src.model.Cell;
import src.model.GameBoard;
import src.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * 连连看核心算法工具集。
 *
 * <p>核心问题：判断两个棋子（posA、posB）是否能在“空格通道”中用不超过 2 次拐弯连接。</p>
 *
 * <p>连线规则约定：</p>
 * <ul>
 *   <li>连线只能水平/垂直移动</li>
 *   <li>路径经过的位置必须是空格（isEmpty==true）</li>
 *   <li>允许 0 折（直线）、1 折（一个拐点）、2 折（两个拐点）</li>
 * </ul>
 */
public class Utils {
    /**
     * 从 posA 出发，在上下左右四个方向上一直走到遇到非空格为止，收集沿途所有可达空格 Cell。
     *
     * <p>该方法常用于 2 折连线判定：先枚举 A 可达的空格作为中间拐点候选。</p>
     */
    public static List<Cell> getReachablePointsInFourDirections(GameBoard gameBoard, Position posA) {
        List<Cell> res = new ArrayList<>();
        for (int i = posA.getRow() + 1; i < gameBoard.getRowCnt(); i++) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        for (int i = posA.getRow() - 1; i >= 0; i--) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        for (int i = posA.getCol() + 1; i < gameBoard.getColCnt(); i++) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        for (int i = posA.getCol() - 1; i >= 0; i--) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        return res;
    }

    /**
     * 0 折（直线）连通判定。
     *
     * <p>要求 A、B 在同一行或同一列，并且中间经过的所有格子都为空。</p>
     */
    public static boolean findZeroTurn(GameBoard gameBoard, Position posA, Position posB) {
        boolean tmpRes0 = true;
        if (posA.getCol() == posB.getCol()) {
            int smallLine = Math.min(posA.getRow(), posB.getRow());
            int largeLine = Math.max(posA.getRow(), posB.getRow());
            for (int i = smallLine + 1; i < largeLine ; i++) {
                if (!gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                    tmpRes0 = false;
                    break;
                }
            }
            if (tmpRes0) {
                return true;
            }
        }
        if (posA.getRow() == posB.getRow()) {
            int smallCol = Math.min(posA.getCol(), posB.getCol());
            int largeCol = Math.max(posA.getCol(), posB.getCol());
            for (int i = smallCol + 1; i < largeCol ; i++) {
                if (!gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                    tmpRes0 = false;
                    break;
                }
            }
            if (tmpRes0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1 折（一个拐点）连通判定。
     *
     * <p>拐点只可能是 (A.row, B.col) 或 (B.row, A.col)，要求：</p>
     * <ul>
     *   <li>A -> corner 直线可达</li>
     *   <li>B -> corner 直线可达</li>
     *   <li>corner 必须是空格</li>
     * </ul>
     */
    public static boolean findOneTurn(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getCol() != posB.getCol() && posA.getRow() != posB.getRow()) {
            Position cornerPoint1 = new Position(posA.getRow(), posB.getCol());
            Position cornerPoint2 = new Position(posB.getRow(), posA.getCol());
            if (findZeroTurn(gameBoard, posA, cornerPoint1) && findZeroTurn(gameBoard, posB, cornerPoint1)&&
            gameBoard.getCell(cornerPoint1.getRow(),cornerPoint1.getCol()).isEmpty()) {
                return true;
            }
            return findZeroTurn(gameBoard, posA, cornerPoint2) && findZeroTurn(gameBoard, posB, cornerPoint2) &&
                    gameBoard.getCell(cornerPoint2.getRow(), cornerPoint2.getCol()).isEmpty();
        }
        return false;
    }

    /**
     * 2 折（两个拐点）连通判定。
     *
     * <p>实现方式：先枚举 A 在四个方向上可达的空格 c，再判断 c 与 B 是否满足 1 折连通。</p>
     */
    public static boolean findTwoTurn(GameBoard gameBoard, Position posA, Position posB) {
        List<Cell> reachablePoints = getReachablePointsInFourDirections(gameBoard, posA);
        for (Cell c: reachablePoints) {
            if (findOneTurn(gameBoard, c.getPos(), posB)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 综合判定：是否存在不超过 2 折的连线路径连接 A、B。
     */
    public static boolean canLinkAB(GameBoard gameBoard, Position posA, Position posB){
        if (findZeroTurn(gameBoard, posA, posB)) {
            return true;
        }
        if (findOneTurn(gameBoard, posA, posB)) {
            return true;
        }
        if (findTwoTurn(gameBoard, posA, posB)) {
            return true;
        }
        return false;
    }

    /**
     * 提示算法：在棋盘中找出任意一对“同图案且可连通”的棋子。
     *
     * <p>返回值为长度 2 的 Position 数组；若不存在可消除对则返回 null。</p>
     *
     * <p>复杂度：双重遍历所有格子对（O(n^4)）并做连通判定，棋盘规模较小（11/12）时可接受。</p>
     */
    public static Position[] getMatchableCells(GameBoard gameBoard) {
        Position[] matchablePair = new Position[2];
        for (int i = 0; i < gameBoard.getRowCnt(); i++) {
            for (int j = 0; j < gameBoard.getColCnt(); j++) {
                Cell cell1 = gameBoard.getCell(i, j);
                if (cell1.isEmpty()) {
                    continue;
                }
                for (int x = 0; x < gameBoard.getRowCnt(); x++) {
                    for (int y = 0; y < gameBoard.getColCnt(); y++) {
                        Cell cell2 = gameBoard.getCell(x, y);
                        if (cell2.isEmpty()) {
                            continue;
                        }
                        if (cell1 == cell2) {
                            continue;
                        }
                        if (canLinkAB(gameBoard, cell1.getPos(), cell2.getPos())
                                && cell2.getIconIndex() == cell1.getIconIndex()) {
                            matchablePair[0] = cell1.getPos();
                            matchablePair[1] = cell2.getPos();
                            return matchablePair;
                        }
                    }
                }
            }
        }
        return null;
    }

}
