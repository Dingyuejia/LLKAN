package src.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 音频管理器（背景音乐 + 音效）。
 *
 * <p>说明：</p>
 * <ul>
 *   <li>背景音乐：循环播放，使用单独线程（BGM）持续运行。</li>
 *   <li>音效：每次触发创建一个短线程播放一次。</li>
 *   <li>JLayer 依赖：为了让项目在缺少 jlayer.jar 时仍能运行，采用反射调用 AdvancedPlayer，
 *       通过 {@link #isJLayerAvailable()} 判断是否可用。</li>
 * </ul>
 *
 * <p>资源查找顺序：</p>
 * <ol>
 *   <li>classpath：{@code /music/*.mp3}</li>
 *   <li>文件系统：{@code resource/music/*.mp3}</li>
 * </ol>
 */
public class AudioManager {
    private volatile boolean bgmEnabled = true;
    private volatile boolean bgmRunning;
    private volatile Object bgmPlayer;
    private Thread bgmThread;

    /**
     * 启动背景音乐循环。
     *
     * <p>若 bgmEnabled=false 或运行环境缺少 JLayer，则不会启动。</p>
     */
    public void startBgmLoop() {
        if (!bgmEnabled) return;
        if (!isJLayerAvailable()) return;
        synchronized (this) {
            if (bgmRunning) return;
            bgmRunning = true;
            bgmThread = new Thread(this::bgmLoop, "BGM");
            bgmThread.setDaemon(true);
            bgmThread.start();
        }
    }

    /**
     * 停止背景音乐播放并释放播放器。
     */
    public void stopBgm() {
        synchronized (this) {
            bgmRunning = false;
            closePlayer(bgmPlayer);
            bgmPlayer = null;
        }
    }

    /**
     * 设置背景音乐开关。
     *
     * <p>开启后会尝试启动循环；关闭后会立即停止。</p>
     */
    public void setBgmEnabled(boolean enabled) {
        bgmEnabled = enabled;
        if (enabled) startBgmLoop();
        else stopBgm();
    }

    public boolean isBgmEnabled() {
        return bgmEnabled;
    }

    /**
     * 播放“点击”音效。
     */
    public void playClick() {
        playOnce("click.mp3");
    }

    /**
     * 播放“消除”音效。
     */
    public void playClear() {
        playOnce("clear.mp3");
    }

    private void bgmLoop() {
        while (bgmEnabled && bgmRunning) {
            try (InputStream in = openMusicStream("Kiss the rain.mp3")) {
                if (in == null) {
                    stopBgm();
                    return;
                }
                Object player = newAdvancedPlayer(in);
                synchronized (this) {
                    if (!bgmRunning) {
                        closePlayer(player);
                        return;
                    }
                    bgmPlayer = player;
                }
                invokePlay(player);
            } catch (Exception e) {
                stopBgm();
                return;
            } finally {
                synchronized (this) {
                    bgmPlayer = null;
                }
            }
        }
    }

    private void playOnce(String fileName) {
        if (!isJLayerAvailable()) return;
        Thread t = new Thread(() -> {
            try (InputStream in = openMusicStream(fileName)) {
                if (in == null) return;
                Object player = newAdvancedPlayer(in);
                invokePlay(player);
            } catch (Exception ignored) {
            }
        }, "SFX-" + fileName);
        t.setDaemon(true);
        t.start();
    }

    private InputStream openMusicStream(String fileName) {
        InputStream in = AudioManager.class.getResourceAsStream("/music/" + fileName);
        if (in != null) return new BufferedInputStream(in);
        try {
            return new BufferedInputStream(new FileInputStream("resource/music/" + fileName));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断运行环境是否存在 JLayer（AdvancedPlayer）。
     */
    private boolean isJLayerAvailable() {
        try {
            Class.forName("javazoom.jl.player.advanced.AdvancedPlayer");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 通过反射创建 AdvancedPlayer 实例。
     */
    private Object newAdvancedPlayer(InputStream in) throws Exception {
        Class<?> cls = Class.forName("javazoom.jl.player.advanced.AdvancedPlayer");
        return cls.getConstructor(InputStream.class).newInstance(in);
    }

    /**
     * 通过反射调用 player.play()（阻塞直到播放结束）。
     */
    private void invokePlay(Object player) throws Exception {
        player.getClass().getMethod("play").invoke(player);
    }

    /**
     * 安全关闭播放器（忽略异常）。
     */
    private void closePlayer(Object player) {
        if (player == null) return;
        try {
            player.getClass().getMethod("close").invoke(player);
        } catch (Exception ignored) {
        }
    }
}
