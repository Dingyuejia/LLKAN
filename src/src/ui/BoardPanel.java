package src.ui;

import src.model.GameBoard;
import src.model.Line;
import src.model.Position;
import src.model.Rectangle;
import src.model.Cell;
import src.utils.Utils;
import src.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static src.utils.Utils.*;

/**
 * 棋盘面板（核心交互层）。
 *
 * <p>职责：</p>
 * <ul>
 *   <li>根据 {@link GameBoard} 绘制棋子、格子边框、选中框</li>
 *   <li>处理鼠标点击：两次选择 -> 判断是否可连通 -> 展示连线 -> 播放音效 -> 执行消除动画</li>
 *   <li>与 {@link StatusPanel} 联动：更新剩余对数/连消/分数，并在通关时触发写入排行榜</li>
 *   <li>提示功能：高亮一对可消除棋子</li>
 * </ul>
 */
public class BoardPanel extends JPanel {
    int offSetX;
    int offSetY;

    boolean eliminate=false;

    GameBoard gameBoard;
    List<Line> lineList = new ArrayList<>();
    int totalRow;
    int totalCol;
    boolean lineVisible;
    int width;
    int height;
    int cellWidth;
    int cellHeight;
    /**
     * 第一次点击选中的格子。
     */
    Position firstSelected = null;
    /**
     * 第二次点击选中的格子（只在一次匹配尝试周期内有效）。
     */
    Position secondSelected = null;
    /**
     * 消除动画进行中，期间忽略点击以避免状态错乱。
     */
    boolean animating = false;
    StatusPanel statusPanel;
    /**
     * 提示用的一对可消除坐标（长度为 2）。为 null 时表示不显示提示。
     */
    private Position[] tip;
    private Timer tiptimer;
    MainFrame mainFrame;
    /**
     * 通关结算幂等标记：remainPairs == 0 时只写榜一次。
     */
    private boolean gameEnded = false;
    public void setStatusPanel(StatusPanel statusPanel){
        this.statusPanel=statusPanel;
    }

    private void updateCellSize() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || totalCol <= 0 || totalRow <= 0) {
            cellWidth = 0;
            cellHeight = 0;
            return;
        }
        cellWidth = Math.max(1, w / totalCol);
        cellHeight = Math.max(1, h / totalRow);
    }

    public Position getPositionByPoint(int x, int y) {
        updateCellSize();
        if (cellWidth <= 0 || cellHeight <= 0) return null;

        int col = x / cellWidth;
        int row = y / cellHeight;
        if (row < 0 || row >= totalRow || col < 0 || col >= totalCol) {
            return null;
        }
        return new Position(row, col);
    }

    /**
     * 显示直线连线（0 折）。
     */
    public void showLine(Cell c1, Cell c2) {
        lineList.clear();
        lineList.add(new Line(c1, c2));
        lineVisible = true;
        repaint();
    }
    public void showzheLine(Cell c1, Cell c2 ,Cell c3) {
        lineList.clear();
        lineList.add(new Line(c1, c2));
        lineList.add(new Line(c2, c3));
        lineVisible = true;
        repaint();
    }
    public void showwanLine(Cell c1, Cell c2 ,Cell c3,Cell c4) {
        lineList.clear();
        lineList.add(new Line(c1, c2));
        lineList.add(new Line(c2, c3));
        lineList.add(new Line(c3, c4));
        lineVisible = true;
        repaint();
    }
    public void setMainFrame(MainFrame mainFrame){
        this.mainFrame=mainFrame;
    }

    /**
     * 创建棋盘面板。
     *
     * <p>该构造器保留 width/height 与 setBounds 的历史写法，当前 GamePanel 会通过布局管理来控制其最终显示大小。</p>
     */
    public BoardPanel(GameBoard gameBoard, int offSetX, int offSetY,int width, int height) {
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.setBounds(offSetX, offSetY, width, height);
        this.totalRow = gameBoard.getRowCnt();
        this.totalCol = gameBoard.getColCnt();
        this.width = width;
        this.height = height;
        this.gameBoard = gameBoard;
        this.setPreferredSize(new Dimension(this.width, this.height));
        updateCellSize();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX() , e.getY());
            }
        });
    }

    /**
     * 处理一次鼠标点击。
     *
     * <p>两次点击构成一次“尝试消除”：</p>
     * <ul>
     *   <li>第一次点击：记录 firstSelected，并将该格子置为选中态</li>
     *   <li>第二次点击：若两格图案相同且可连通，则展示连线并执行消除动画；否则把第二格当作新的 firstSelected</li>
     * </ul>
     */
    public void handleClick(int x, int y) {
        mainFrame.getAudioManager().playClick();
        if (animating) {
            return;
        }

        Position pos = getPositionByPoint(x, y);
        if (pos == null) {
            return;
        }

        Cell clickedCell = gameBoard.getCell(pos.getRow(), pos.getCol());
        if (clickedCell == null || clickedCell.isEmpty()) {
            return;
        }

        if (firstSelected == null) {
            gameBoard.clearAllChosen();
            clickedCell.setChosen(true);
            firstSelected = pos;
            repaint();
            return;
        }

        if (firstSelected.equals(pos)) {
            clickedCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            repaint();
            return;
        }

        secondSelected = pos;
        Cell secondCell = gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol());

        secondCell.setChosen(true);
        repaint();
        if (canLinkAB( gameBoard, firstSelected, secondSelected)&&
        gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol()).getIconIndex()==
                gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol()).getIconIndex()) {
            animating = true;

            if (findZeroTurn(gameBoard, firstSelected, secondSelected)){
                showLine(
                        gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol()),
                        gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol())
                );
                eliminate=true;

            } else if (findOneTurn(gameBoard, firstSelected, secondSelected)) {
                Position cornerPoint1 = new Position(firstSelected.getRow(), secondSelected.getCol());
                Position cornerPoint2 = new Position(secondSelected.getRow(), firstSelected.getCol());
                if (findZeroTurn(gameBoard, firstSelected, cornerPoint1) && findZeroTurn(gameBoard, secondSelected, cornerPoint1)&&
                        gameBoard.getCell(cornerPoint1.getRow(),cornerPoint1.getCol()).isEmpty()) {
                    showzheLine(
                            gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol()),
                            gameBoard.getCell(cornerPoint1.getRow(), cornerPoint1.getCol()),
                            gameBoard.getCell(secondSelected.getRow(),secondSelected.getCol())
                    );
                    eliminate=true;
                } else if (findZeroTurn(gameBoard, firstSelected, cornerPoint2) && findZeroTurn(gameBoard, secondSelected, cornerPoint2)&&
                        gameBoard.getCell(cornerPoint2.getRow(),cornerPoint2.getCol()).isEmpty()) {
                    showzheLine(
                            gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol()),
                            gameBoard.getCell(cornerPoint2.getRow(), cornerPoint2.getCol()),
                            gameBoard.getCell(secondSelected.getRow(),secondSelected.getCol())
                    );
                    eliminate=true;
                }
            } else if (findTwoTurn(gameBoard, firstSelected, secondSelected)) {
                List<Cell> reachablePointsof1 = getReachablePointsInFourDirections(gameBoard, firstSelected);
                for (Cell c: reachablePointsof1) {
                    if (findOneTurn(gameBoard, c.getPos(), secondSelected)) {
                        List<Cell> reachablePointsofc = getReachablePointsInFourDirections(gameBoard, c.getPos());
                        for (Cell e: reachablePointsofc){
                            if (findZeroTurn(gameBoard, e.getPos(), secondSelected)){
                                showwanLine(gameBoard.getCell(firstSelected.getRow(),firstSelected.getCol()),
                                        c,e,
                                        gameBoard.getCell(secondSelected.getRow(),secondSelected.getCol()));
                                eliminate=true;
                            }
                        }
                    }
                }
            }
            if(eliminate){
                statusPanel.remainPairsminus();
                mainFrame.getAudioManager().playClear();
                eliminate=false;
                statusPanel.checkCombo();
                statusPanel.addScore();
                this.statusPanel.updateRemainPairs();
                if (!gameEnded && statusPanel.getRemainPairs() == 0) {
                    gameEnded = true;
                    mainFrame.onGameWin(statusPanel.getScore());
                }
            }


            Timer timer = new Timer(300, e -> {
                Cell c1 = gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol());
                Cell c2 = gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol());
                c1.setEmpty(true);
                c2.setEmpty(true);
                c1.setChosen(false);
                c2.setChosen(false);
                lineVisible = false;
                lineList.clear();
                firstSelected = null;
                secondSelected = null;
                animating = false;
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            gameBoard.clearAllChosen();
            secondCell.setChosen(true);
            firstSelected = secondSelected;
            secondSelected = null;
            repaint();
        }
    }
    public Rectangle getRectangle(Position position) {
        updateCellSize();
        int x = position.getCol() * cellWidth;
        int y = position.getRow() * cellHeight;
        return new Rectangle(x, y, cellWidth, cellHeight);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateCellSize();
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < gameBoard.getRowCnt(); i++) {
            for (int j = 0; j < gameBoard.getColCnt(); j++) {
                Rectangle rec = getRectangle(new Position(i, j));
                Cell cell = gameBoard.getCell(i, j);
                if (cell != null && !cell.isEmpty()) {
                    g2.drawImage(
                            ThemeManager.getIcon(cell.getIconIndex()).getImage(),
                            rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(),
                            this
                    );
                }
                if (cell != null && cell.getIsChosen()) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(
                            rec.getX() + 1,
                            rec.getY() + 1,
                            rec.getWidth() - 3,
                            rec.getHeight() - 3
                    );
                } else {
                    g2.setColor(Color.GRAY);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(
                            rec.getX(),
                            rec.getY(),
                            rec.getWidth() - 1,
                            rec.getHeight() - 1
                    );
                }
            }
        }
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));
        if (lineVisible) {
            for (Line line: lineList) {
                Rectangle rec1 = getRectangle(line.getCell1().getPos());
                Rectangle rec2 = getRectangle(line.getCell2().getPos());
                g.drawLine((int) rec1.getCenterPosition().getX(), (int) rec1.getCenterPosition().getY(), (int) rec2.getCenterPosition().getX(), (int) rec2.getCenterPosition().getY());
            }
        }
        if(tip!=null){
            Rectangle rec1 = getRectangle(tip[0]);
            Rectangle rec2 = getRectangle(tip[1]);
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(
                    rec1.getX(),
                    rec1.getY(),
                    rec1.getWidth() - 1,
                    rec1.getHeight() - 1
            );
            g2.drawRect(
                    rec2.getX(),
                    rec2.getY(),
                    rec2.getWidth() - 1,
                    rec2.getHeight() - 1
            );
        }
    }


    public void showTip(){
        if(tiptimer !=null){
            tiptimer.stop();
        }
        tip = Utils.getMatchableCells(gameBoard);
        repaint();
        tiptimer = new Timer(2000, e -> {
            tip = null;
            repaint();
            tiptimer.stop();
        });
        tiptimer.setRepeats(false);
        tiptimer.start();
    }

    /**
     * 将棋盘上所有格子置为空（用于“时间到”直接结束游戏）。
     */
    public void setAllEmpty(){
        for (int i = 0; i < totalRow; i++) {
            for (int j = 0; j < totalCol; j++) {
                gameBoard.board[i][j].setEmpty(true);
            }
        }
        // 清理选中/连线/动画状态，避免“时间到”后界面残留
        firstSelected = null;
        secondSelected = null;
        lineVisible = false;
        lineList.clear();
        animating = false;
        gameBoard.clearAllChosen();
        repaint();
    }

}
