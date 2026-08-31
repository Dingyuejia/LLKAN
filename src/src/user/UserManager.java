package src.user;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理器（本地持久化）。
 *
 * <p>职责：</p>
 * <ul>
 *   <li>注册：创建新用户并写入 users.dat</li>
 *   <li>登录：校验用户名/密码</li>
 *   <li>偏好：保存用户主题（themeId）</li>
 * </ul>
 *
 * <p>用户数据序列化存储在 {@code users.dat}。如果文件损坏，当前实现会重建为空（相当于丢弃旧数据）。</p>
 */
public class UserManager {
    private static final String USER_FILE = "users.dat";
    private Map<String, User> users = new HashMap<>();

    public UserManager() {
        loadUsers();
    }

    /**
     * 注册用户（默认主题）。
     *
     * @return 注册成功返回 User；用户名已存在则返回 null
     */
    public User register(String username, String password) {
        if (users.containsKey(username)) return null;
        User user = new User(username, password);
        users.put(username, user);
        saveUsers();
        return user;
    }

    /**
     * 注册用户（指定主题）。
     *
     * <p>该重载用于 UI 注册时把当前主题一并保存，避免下次登录主题丢失。</p>
     *
     * @return 注册成功返回 User；用户名已存在则返回 null
     */
    public User register(String username, String password, int themeId) {
        if (users.containsKey(username)) return null;
        User user = new User(username, password);
        user.setThemeId(themeId);
        users.put(username, user);
        saveUsers();
        return user;
    }

    /**
     * 登录校验。
     *
     * @return 成功返回 User，失败返回 null
     */
    public User login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        return null;
    }

    /**
     * 更新用户主题并落盘。
     */
    public void setUserTheme(User user, int themeId) {
        if (user == null) return;
        user.setThemeId(themeId);
        users.put(user.getUsername(), user);
        saveUsers();
    }

    /**
     * 从 users.dat 加载用户表。
     *
     * <p>若文件不存在则保持为空用户表；若文件损坏则会输出提示并重建为空用户表。</p>
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (FileNotFoundException e) {
            // 首次运行，无文件
        } catch (Exception e) {
            System.err.println("用户文件损坏，将重建。");
            users = new HashMap<>();
        }
    }

    /**
     * 把当前用户表写回 users.dat。
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
