package src.ui;


import src.utils.ThemeManager;
import src.user.User;
import src.user.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 登录/注册对话框。
 *
 * <p>通过 {@link UserManager} 完成注册与登录校验；成功后将登录用户保存到 {@link #loggedUser}，
 * 供 {@link MainFrame} 读取并设置为 currentUser。</p>
 */
public class LoginDialog extends JDialog {
    private UserManager userManager;
    private JTextField userField;
    private JPasswordField passField;
    private User loggedUser;

    /**
     * @param owner       父窗口
     * @param userManager 用户管理器（负责注册/登录/持久化）
     */
    public LoginDialog(Frame owner, UserManager userManager) {
        super(owner, "用户登录", true);
        this.userManager = userManager;
        this.loggedUser = null;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("用户名："), gbc);
        userField = new JTextField(12);
        gbc.gridx = 1;
        add(userField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("密码："), gbc);
        passField = new JPasswordField(12);
        gbc.gridx = 1;
        add(passField, gbc);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("登录");
        loginBtn.addActionListener(this::login);
        JButton registerBtn = new JButton("注册");
        registerBtn.addActionListener(this::register);

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(btnPanel, gbc);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    /**
     * 登录流程：校验输入 -> UserManager.login -> 成功则写入 loggedUser 并关闭弹窗。
     */
    private void login(ActionEvent e) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
            return;
        }
        User user = userManager.login(username, password);
        if (user != null) {
            // 登录时恢复该用户保存的主题偏好（而不是用当前主题覆盖它）
            ThemeManager.setTheme(user.getThemeId());
            loggedUser = user;
            JOptionPane.showMessageDialog(this, "登录成功！");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 注册流程：校验输入 -> UserManager.register（带当前主题）-> 成功则自动登录并关闭弹窗。
     */
    private void register(ActionEvent e) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
            return;
        }
        User newUser = userManager.register(username, password, ThemeManager.getTheme());
        if (newUser != null) {
            loggedUser = newUser;
            JOptionPane.showMessageDialog(this, "注册成功！已自动登录。");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "用户名已被注册！", "注册失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return 登录成功后的用户；若未登录则为 null
     */
    public User getLoggedUser() {
        return loggedUser;
    }
}
