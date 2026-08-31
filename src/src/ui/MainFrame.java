package src.ui;



import src.model.Cell;
import src.model.Position;
import src.user.LeaderboardManager;
import src.user.SaveManager;
import src.user.User;
import src.user.UserManager;
import src.utils.AudioManager;
import src.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * 应用主窗口（入口 Frame）。
 *
 * <p>职责：</p>
 * <ul>
 *   <li>使用 CardLayout 在菜单（MENU）与游戏（GAME）界面之间切换</li>
 *   <li>维护全局单例：用户管理（UserManager）、存档（SaveManager）、排行榜（LeaderboardManager）、音频（AudioManager）</li>
 *   <li>维护当前用户与当前难度，并提供通关结算写榜入口 {@link #onGameWin(int)}</li>
 * </ul>
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private UserManager userManager;
    private SaveManager saveManager;
    private LeaderboardManager leaderboardManager;
   private AudioManager audioManager;
    private User currentUser;  // null为游客
    private int currentDifficulty;

    public MainFrame() {
        setTitle("连连看");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 1000);
        setMinimumSize(new Dimension(800, 1000));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        userManager = new UserManager();
        saveManager = new SaveManager();
        leaderboardManager = new LeaderboardManager();


        audioManager = new AudioManager();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuPanel = new MenuPanel(this);
        mainPanel.add(menuPanel, "MENU");

        add(mainPanel);
        cardLayout.show(mainPanel, "MENU");

        // 菜单栏或设置
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("用户");
        JMenuItem loginItem = new JMenuItem("登录/注册");
        loginItem.addActionListener(e -> showLoginDialog());
        JMenuItem logoutItem = new JMenuItem("登出");
        logoutItem.addActionListener(e -> { currentUser = null; refreshUserUI(); });
        userMenu.add(loginItem);
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        refreshUserUI();


        audioManager.startBgmLoop();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                audioManager.stopBgm();
            }
        });


    }

    private void refreshUserUI() {
        menuPanel.refresh();
        menuPanel.revalidate();
        menuPanel.repaint();
    }



    public void startGame(String title, int width, int height, int difficulty) {
        this.currentDifficulty = difficulty;
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
        }
        gamePanel = new GamePanel( this, width, height, currentDifficulty);
        gamePanel.setMainFrame(this);
        mainPanel.add(gamePanel, "GAME");
        if (currentDifficulty==0){
            gamePanel.statusPanel.setRemainPairs(16);
        }else {
            gamePanel.statusPanel.setRemainPairs(50);
        }
        cardLayout.show(mainPanel, "GAME");
        gamePanel.requestFocus();
    }
    public void startGameWithSave(SaveManager.SaveData data) {

        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
        }
        // 读档时直接用一个标准初始尺寸创建 GamePanel，随后用存档数据覆盖棋盘与状态。
        gamePanel = new GamePanel(this,800, 1000, data.difficulty);

        for (int i = 0; i < data.rows; i++) {//构造空单元格
            for (int j = 0; j < data.cols; j++) {
                    gamePanel.boardPanel.gameBoard.board[i][j] = new Cell(new Position(i, j), data.ifEmpty[i][j], data.cellIndex[i][j]);
            }
        }

        gamePanel.statusPanel.totalSeconds = data.timeRemained;
        gamePanel.statusPanel.setScore(data.score);
        gamePanel.statusPanel.setCombo(data.combo);
        gamePanel.statusPanel.setRemainPairs(data.pairsRemained);
        // 刷新状态面板显示

        gamePanel.statusPanel.scoreLabel.setText("当前分数: " + data.score);
        gamePanel.statusPanel.comboLabel.setText("连消: " + data.combo);
        gamePanel.statusPanel.statusLabel.setText("剩余对数: "+data.pairsRemained);
        // 恢复计时器状态
        gamePanel.statusPanel.timer.restart();
        gamePanel.setMainFrame(this);
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");
        gamePanel.requestFocus();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");

        refreshUserUI();
    }
    public AudioManager getAudioManager() {
        return audioManager;
    }

    void showLoginDialog() {
        LoginDialog dialog = new LoginDialog(this, userManager);
        dialog.setVisible(true);
        if (dialog.getLoggedUser() != null) {
            currentUser = dialog.getLoggedUser();
            // 主题已在 LoginDialog 内按用户偏好恢复，这里不再覆盖
            refreshUserUI();
        }
    }
    public User getCurrentUser() { return currentUser; }
    public SaveManager getSaveManager() { return saveManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public int getCurrentDifficulty() { return currentDifficulty; }

    /**
     * 通关结算入口：写入排行榜。
     *
     * <p>由 BoardPanel 在剩余对数归零时触发；MainFrame 统一管理排行榜实例，
     * 这样菜单中查看排行榜可以立刻读到最新数据。</p>
     */
    public void onGameWin(int score) {
        String username = (currentUser != null) ? currentUser.getUsername() : "游客";
        leaderboardManager.record(username, score, currentDifficulty, true);
        JOptionPane.showMessageDialog(this,
                "恭喜通关！\n用户名：" + username + "\n分数：" + score,
                "通关", JOptionPane.INFORMATION_MESSAGE);
    }


    public void setCurrentUserTheme(int themeId) {
        if (currentUser == null) return;
        userManager.setUserTheme(currentUser, themeId);
        refreshUserUI();
    }



}
