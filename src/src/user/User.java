package src.user;

import java.io.Serializable;

/**
 * 用户数据（可序列化）。
 *
 * <p>当前项目将用户数据序列化保存到 users.dat 中，用于实现简单的“登录/注册 + 主题偏好”。</p>
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    /**
     * 演示用途的密码哈希。
     *
     * <p>注意：这里使用 {@code String.hashCode()} 只是为了课程/演示方便，不适用于任何真实生产环境。
     * 真实项目应使用带盐的强哈希（如 bcrypt/scrypt/Argon2 等）。</p>
     */
    private String passwordHash;
    private int themeId;

    /**
     * @param username 用户名
     * @param password 明文密码（仅在创建时参与计算 passwordHash）
     */
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = Integer.toHexString(password.hashCode());
        this.themeId = 0;
    }

    /**
     * 校验用户输入的密码是否正确。
     */
    public boolean checkPassword(String password) {
        return passwordHash.equals(Integer.toHexString(password.hashCode()));
    }

    public String getUsername() { return username; }
    public int getThemeId() { return themeId; }
    public void setThemeId(int themeId) { this.themeId = themeId; }
}
