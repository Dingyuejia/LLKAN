package src.ui;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import src.user.SaveManager;
import src.user.User;
import src.utils.ThemeManager;

/**
 * 主菜单面板（MENU）。
 *
 * <p>提供入口：</p>
 * <ul>
 *   <li>开始游戏（简单/困难）</li>
 *   <li>登录/注册</li>
 *   <li>读取存档（登录后可用）</li>
 *   <li>排行榜</li>
 *   <li>主题切换</li>
 *   <li>背景音乐开关</li>
 * </ul>
 *
 * <p>背景图由 {@link ThemeManager#getBackgroundImage()} 提供，并随主题切换变化。</p>
 */
public class MenuPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel userLabel;
    private JButton loadButton;
    private JButton bgmButton;
    private JButton themeButton;

    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // 标题
        JLabel title = new JLabel("连连看");
        title.setFont(new Font("楷体", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        themeButton = new JButton();
        themeButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        themeButton.addActionListener(e -> toggleTheme());
        add(themeButton, gbc);

        // 难度选择
        JButton easyBtn = new JButton("简单模式");
        easyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        easyBtn.addActionListener(e -> {mainFrame.startGame("简单模式",800, 1000, 0);
        mainFrame.getAudioManager().playClick();}
        );
        add(easyBtn, gbc);

        JButton hardBtn = new JButton("困难模式");
        hardBtn.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        hardBtn.addActionListener(e -> {mainFrame.startGame("困难模式",800, 1000, 1);
            mainFrame.getAudioManager().playClick();}
        );
        add(hardBtn, gbc);

        // 用户信息/登录
        userLabel = new JLabel("当前：游客模式");
        userLabel.setFont(new Font("宋体", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        add(userLabel, gbc);

        JButton loginBtn = new JButton("登录 / 注册");
        loginBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loginBtn.addActionListener(e -> mainFrame.showLoginDialog());
        add(loginBtn, gbc);

        // 读档按钮（仅登录后可用）
        loadButton = new JButton("读取存档");
        loadButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loadButton.setVisible(false);
        loadButton.addActionListener(this::loadGame);
        add(loadButton, gbc);

        JButton leaderboardBtn = new JButton("排行榜");
        leaderboardBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
       leaderboardBtn.addActionListener(e -> showLeaderboard());
        add(leaderboardBtn, gbc);

        bgmButton = new JButton();
        bgmButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        bgmButton.addActionListener(e -> toggleBgm());
        add(bgmButton, gbc);

        // 退出
        JButton exitBtn = new JButton("退出游戏");
        exitBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        exitBtn.addActionListener(e ->{mainFrame.getAudioManager().playClick();
            System.exit(0);
        });
        add(exitBtn, gbc);

        refresh();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image bg = ThemeManager.getBackgroundImage();
        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * 根据当前用户状态刷新界面（由 MainFrame 在登录/登出/切换菜单时调用）。
     */
    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user != null) {
            userLabel.setText("当前用户：" + user.getUsername());
            loadButton.setVisible(true);
        } else {
            userLabel.setText("当前：游客模式");
            loadButton.setVisible(false);
        }
        if (bgmButton != null) {
            bgmButton.setText(mainFrame.getAudioManager().isBgmEnabled() ? "背景音乐：开" : "背景音乐：关");
        }
        if (themeButton != null) {
            themeButton.setText("主题：" + ThemeManager.getThemeName(ThemeManager.getTheme()));
        }
    }

    /**
     * 读取存档并进入游戏（仅登录用户可用）。
     */
    private void loadGame(ActionEvent e) {
        User user = mainFrame.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "游客无法读档，请先登录。");
            return;
        }
        String[] options = {"档案1", "档案2", "档案3"};
        int slot = JOptionPane.showOptionDialog(
                this, "请选择要读取的存档", "读档",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if (slot < 0) return;

        try {
            SaveManager.SaveData data = mainFrame.getSaveManager().loadGame(user, slot + 1);
            mainFrame.startGameWithSave(data);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "存档无效！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 打开排行榜弹窗。
     */
    private void showLeaderboard() {
        LeaderboardDialog dialog = new LeaderboardDialog(mainFrame, mainFrame.getLeaderboardManager().getTop(20));
        dialog.setVisible(true);
    }

    /**
     * 切换背景音乐开关（只影响是否播放，不影响资源文件本身）。
     */
    private void toggleBgm() {
        boolean enabled = !mainFrame.getAudioManager().isBgmEnabled();
        mainFrame.getAudioManager().setBgmEnabled(enabled);
        refresh();
    }

    /**
     * 在主题 1/主题 2 之间切换，并把当前主题写入用户偏好（登录用户）。
     */
    private void toggleTheme() {
        int next = (ThemeManager.getTheme() == ThemeManager.THEME_THEME2) ? ThemeManager.THEME_NUMBERS : ThemeManager.THEME_THEME2;
        ThemeManager.setTheme(next);
        if (mainFrame.getCurrentUser() != null) {
            mainFrame.setCurrentUserTheme(next);
        }
        refresh();
        repaint();
    }




}
