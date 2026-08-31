package src.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏控制面板（位于游戏界面底部）。
 *
 * <p>负责提供“重新开始 / 返回菜单 / 提示 / 保存存档”等操作入口。</p>
 */
public class ControlPanel extends JPanel {
    StatusPanel statusPanel;
    JButton restartButton;
    JButton backButton;
    JButton tipButton;
    JButton saveButton;
    private BoardPanel boardPanel;
    MainFrame mainFrame;

    /**
     * 注入 MainFrame（用于回到菜单、重新开始、播放音效、访问存档管理器等）。
     */
    public void setMainFrame(MainFrame mainFrame){
        this.mainFrame=mainFrame;
    }

    /**
     * 注入棋盘面板（用于提示、保存棋盘存档等）。
     */
    public void setBoardPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
    }

    /**
     * @param statusPanel 状态栏（用于弹窗定位与读取分数/时间等状态）
     */
    public ControlPanel(StatusPanel statusPanel) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        this.restartButton = new JButton("重新开始");
        this.backButton = new JButton("返回菜单");
        this.tipButton = new JButton("提示");
        this.saveButton =new JButton("保存存档");
        this.statusPanel = statusPanel;
        restartButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        tipButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        restartButton.setFocusPainted(false);
        Dimension btnSize = new Dimension(160, 50);
        restartButton.setPreferredSize(btnSize);
        backButton.setPreferredSize(btnSize);
        tipButton.setPreferredSize(btnSize);
        saveButton.setPreferredSize(btnSize);
        saveButton.setVisible(false);
        add(restartButton);
        add(backButton);
        add(tipButton);
        add(saveButton);


        tipButton.addActionListener(e -> {
            boardPanel.showTip();
            mainFrame.getAudioManager().playClick();
        });
        backButton.addActionListener(e -> {
            mainFrame.showMenu();
            mainFrame.getAudioManager().playClick();
        });
        // 约定：简单模式棋盘为 11x11，困难为 12x12；这里用 totalRow-11 推回 difficulty（0/1）。
        restartButton.addActionListener(e -> {
            int difficulty = this.boardPanel.totalRow - 11;
            String title = (difficulty == 0) ? "简单模式" : "困难模式";
            mainFrame.startGame(title, mainFrame.getWidth(), mainFrame.getHeight(), difficulty);
        });
        saveButton.addActionListener(e -> saveGame());
    }

    /**
     * 根据是否登录刷新按钮可见性。
     *
     * <p>当前实现：游客模式不允许存档，因此隐藏“保存存档”。</p>
     */
    public void refreshButton(){
        if (mainFrame.getCurrentUser() != null) {
            saveButton.setVisible(true);
        } else {
            saveButton.setVisible(false);
        }
        revalidate();
        repaint();
    }

    /**
     * 保存当前游戏进度（仅登录用户可用）。
     */
    private void saveGame() {
        if (mainFrame.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this, "游客模式无法存档");
            return;
        }
        String[] options = {"档案1", "档案2", "档案3"};
        int slot = JOptionPane.showOptionDialog(statusPanel, "选择存档槽位", "存档", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (slot >= 0) {
            mainFrame.getSaveManager().saveGame(mainFrame.getCurrentUser(), slot+1, this.boardPanel.gameBoard, this.statusPanel);
            JOptionPane.showMessageDialog(statusPanel, "存档成功");
        }
    }

}
