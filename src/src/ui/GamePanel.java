package src.ui;
import src.model.Cell;
import src.model.GameBoard;
import src.model.Position;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static src.utils.Utils.canLinkAB;

public class GamePanel extends JPanel{
    StatusPanel statusPanel;
    ControlPanel controlPanel;
    BoardPanel boardPanel;
    MainFrame mainFrame;
    private final Random random = new Random();

    private static void fillNullAsEmpty(Cell[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    board[i][j] = new Cell(new Position(i, j), true, 0);
                }
            }
        }
    }

    public GamePanel(MainFrame mainFrame, int width, int height, int difficulty) {
        this.mainFrame=mainFrame;
        Cell[][] board = null;

        if (difficulty==0) {
            board = new Cell[11][11];
            for (int i = 0; i < 11; i++) {
                for (int j = 0; j < 11; j++) {
                    if (i == 0 || i == 10 || j == 0 || j == 10) {
                        board[i][j] = new Cell(new Position(i, j), true, 0);
                    }
                }
            }
            for (int i = 1; i < 6; i++) {
                for (int j = 5; j < 10; j++) {
                    board[i][j] = new Cell(new Position(i, j), true, 0);
                }
            }
            for (int i = 5; i < 10; i++) {
                for (int j = 1; j < 6; j++) {
                    board[i][j] = new Cell(new Position(i, j), true, 0);
                }
            }

            List<Integer> tiles = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                for (int j = 1; j <= 4; j++) {
                    tiles.add(i * 10 + j);
                }
            }
            for (int i = 6; i <= 9; i++) {
                for (int j = 6; j <= 9; j++) {
                    tiles.add(i * 10 + j);
                }
            }
            Collections.shuffle(tiles, random);
            Integer[] arr = tiles.toArray(new Integer[0]);

            for (int k = 0;k<10 ; k ++){
                int m = arr[k]/10;
                int n = arr[k]%10;
                board[m][n] = new Cell(new Position(m, n), false, (k+2)/2);
            }
            for (int k = 10;k<32 ; k=k+2){
                int r = random.nextInt(5) + 1;
                int m = arr[k]/10;
                int n = arr[k]%10;
                board[m][n] = new Cell(new Position(m, n), false, r);
                m = arr[k+1]/10;
                n = arr[k+1]%10;
                board[m][n] = new Cell(new Position(m, n), false, r);
            }

            fillNullAsEmpty(board);
            ensureSolvable(board); // ✅ 只在这里检查一次（正确位置）
            boardPanel = new BoardPanel(new GameBoard(11, 11, board), 0, 0, 800, 800);

        } else if (difficulty==1) {
            board = new Cell[12][12];
            for (int i = 0; i < 12; i++) {
                for (int j = 0; j < 12; j++) {
                    if (i == 0 || i == 11 || j == 0 || j == 11) {
                        board[i][j] = new Cell(new Position(i, j), true, 0);
                    }
                }
            }
            List<Integer> tiles = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                for (int j = 1; j <= 10; j++) {
                    tiles.add(i * 100 + j);
                }
            }
            Collections.shuffle(tiles, random);
            Integer[] arr = tiles.toArray(new Integer[0]);

            for (int k = 0;k<24 ; k ++){
                int m = arr[k]/100;
                int n = arr[k]%100;
                board[m][n] = new Cell(new Position(m, n), false, (k+2)/2);
            }
            for (int k = 24;k<100 ; k=k+2){
                int r = random.nextInt(12) + 1;
                int m = arr[k]/100;
                int n = arr[k]%100;
                board[m][n] = new Cell(new Position(m, n), false, r);
                m = arr[k+1]/100;
                n = arr[k+1]%100;
                board[m][n] = new Cell(new Position(m, n), false, r);
            }

            fillNullAsEmpty(board);
            ensureSolvable(board); // ✅ 只在这里检查一次
            boardPanel = new BoardPanel(new GameBoard(12, 12, board), 0, 0, 800, 800);
        }

        setLayout(new BorderLayout());
        setVisible(true);

        statusPanel = new StatusPanel();
        statusPanel.setPreferredSize(new Dimension(0, 100));
        controlPanel = new ControlPanel(statusPanel);
        controlPanel.setPreferredSize(new Dimension(0, 100));

        JPanel boardContainer = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        boardContainer.add(boardPanel, gbc);
        boardContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int size = Math.min(boardContainer.getWidth(), boardContainer.getHeight());
                if (size <= 0) return;
                Dimension d = new Dimension(size, size);
                if (!d.equals(boardPanel.getPreferredSize())) {
                    boardPanel.setPreferredSize(d);
                    boardContainer.revalidate();
                }
            }
        });

        add(statusPanel, BorderLayout.NORTH);
        add(boardContainer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        controlPanel.setMainFrame(mainFrame);
        controlPanel.setBoardPanel(boardPanel);
        boardPanel.setMainFrame(mainFrame);
        boardPanel.setStatusPanel(statusPanel);
        statusPanel.setBoardPanel(boardPanel);
        statusPanel.startTimer();
        controlPanel.refreshButton();
    }

    public void setMainFrame(MainFrame mainFrame){
        this.mainFrame=mainFrame;
        this.controlPanel.setMainFrame(mainFrame);
        this.boardPanel.setMainFrame(mainFrame);
    }

    // ✅ 修复：棋盘生成完再检查可解性（最多重洗 100 次确保有可消除对）
    private void ensureSolvable(Cell[][] board) {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (hasAnyMatch(board)) {
                return;
            }
            shuffleBoard(board);
        }
    }

    // ✅ 修复：cols 获取正确
    public boolean hasAnyMatch(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        for (int r1 = 0; r1 < rows; r1++) {
            for (int c1 = 0; c1 < cols; c1++) {
                if (board[r1][c1] == null || board[r1][c1].isEmpty()) continue;

                for (int r2 = 0; r2 < rows; r2++) {
                    for (int c2 = 0; c2 < cols; c2++) {
                        if (r1 == r2 && c1 == c2) continue;
                        if (board[r2][c2] == null || board[r2][c2].isEmpty()) continue;

                        if (board[r1][c1].getIconIndex() == board[r2][c2].getIconIndex()) {
                            if (canLinkAB(new GameBoard(rows, cols, board),
                                    board[r1][c1].getPos(),
                                    board[r2][c2].getPos())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // ✅ 修复：直接操作 board 数组，不依赖 boardPanel（彻底解决空指针）
    public void shuffleBoard(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        List<int[]> positions = new ArrayList<>();
        List<Integer> iconIndices = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board[r][c];
                if (cell != null && !cell.isEmpty()) {
                    positions.add(new int[]{r, c});
                    iconIndices.add(cell.getIconIndex());
                }
            }
        }

        Collections.shuffle(iconIndices, random);

        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            int r = pos[0];
            int c = pos[1];
            int newIcon = iconIndices.get(i);
            board[r][c] = new Cell(new Position(r, c), false, newIcon);
        }
    }
}