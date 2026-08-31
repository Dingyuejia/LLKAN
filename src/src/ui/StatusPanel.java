package src.ui;

import javax.swing.*;
import java.awt.*;
import src.ui.BoardPanel;

/**
 * 顶部状态栏（计时/分数/连消/剩余对数）。
 *
 * <p>该面板本身不处理点击消除逻辑，主要由 {@link BoardPanel} 在消除成功后调用：</p>
 * <ul>
 *   <li>{@link #remainPairsminus()}：剩余对数 -1</li>
 *   <li>{@link #checkCombo()}：根据上一次消除时间更新连消</li>
 *   <li>{@link #addScore()}：按连消计算分数</li>
 *   <li>{@link #updateRemainPairs()}：刷新文本，并在通关时停止计时器</li>
 * </ul>
 */
public class StatusPanel extends JPanel {
    JLabel statusLabel;
    BoardPanel boardPanel;
    JLabel timeLabel;
    JLabel scoreLabel;
    JLabel comboLabel;
    Timer timer;
    int seconds;
    int minutes;
    /**
     * 剩余时间（秒），会在计时器 tick 时递减。
     */
    int totalSeconds;
    /**
     * 剩余可消除对数；为 0 时判定通关。
     */
    int remainPairs;
    private int combo = 0;
    private long lastClearTime = 0;
    private int score=0;

    /**
     * 按当前连消增加分数。
     *
     * <p>当前计分规则：每次消除加 {@code combo * 10} 分。</p>
     */
    public void addScore(){
        score=score+combo*10;
        scoreLabel.setText("当前分数: "+score);
    }

    /**
     * 剩余对数 -1（由 BoardPanel 在成功消除一对后调用）。
     */
    public void remainPairsminus(){
        remainPairs--;
    }

    /**
     * @return 当前分数
     */
    public int getScore(){
        return score;
    }
    public void setScore(int score){
        this.score=score;
    }
    public void setCombo(int combo){
        this.combo=combo;
    }
    public int getCombo(){
        return combo;
    }

    /**
     * 更新连消数。
     *
     * <p>规则：两次消除间隔 &lt; 3 秒则连消 +1，否则重置为 1。</p>
     */
    public void checkCombo(){
        long nowTime = System.currentTimeMillis();
        if (lastClearTime==0){
            combo=1;
        }else if (nowTime-lastClearTime<3000){
            combo++;
        }else {
            combo=1;
        }
        comboLabel.setText("连消: "+combo);
        lastClearTime=nowTime;
    }

    /**
     * 启动倒计时（GamePanel 创建 UI 并注入 boardPanel 后调用）。
     */
    public void startTimer() {
        if (timer != null) {
            timer.start();
        }
    }

    public int getRemainPairs(){
        return remainPairs;
    }

    /**
     * 初始化/设置剩余对数，并立即刷新显示。
     */
    public void setRemainPairs(int remainPairs){
        this.remainPairs=remainPairs;
        statusLabel.setText("剩余对数：" + remainPairs);
        if (remainPairs == 0) {
            statusLabel.setText("恭喜通关！剩余对数：0");
        }
    }
    public void setBoardPanel(BoardPanel boardPanel){
        this.boardPanel=boardPanel;
    }

    /**
     * 创建状态栏，并初始化倒计时（默认 180 秒）。
     */
    public StatusPanel() {
        setLayout(new GridLayout(2, 2));
        statusLabel = new JLabel("剩余对数："+remainPairs);
        timeLabel = new JLabel("READY!!!");
        scoreLabel=new JLabel("当前分数: "+score);
        comboLabel=new JLabel("连消: "+combo);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        comboLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalSeconds=180;
        timer = new Timer(1000, e -> {
            totalSeconds--;
            if (totalSeconds>0) {
                minutes = totalSeconds / 60;
                seconds = totalSeconds % 60;
                timeLabel.setText(String.format("剩余时间: %02d:%02d", minutes, seconds));
            }else {
                timeLabel.setText("时间到!!!");
                if (boardPanel != null) {
                    boardPanel.setAllEmpty();
                }
                // 倒计时结束后停止计时器，避免继续递减
                timer.stop();
            }

        });

        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        comboLabel.setFont(new Font("微软雅黑", Font.BOLD, 30));
        add(statusLabel);
        add(timeLabel);
        add(scoreLabel);
        add(comboLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
        repaint();
    }

    /**
     * 刷新“剩余对数”的显示，并在通关时停止计时器。
     */
    public void updateRemainPairs() {
        if (boardPanel != null) {
            statusLabel.setText("剩余对数：" + this.getRemainPairs());
            if (this.getRemainPairs() == 0) {
                statusLabel.setText("恭喜通关！剩余对数：0");
                timer.stop();
            }
        }
        repaint();
    }

    public int getTotalSeconds(){
        return totalSeconds;
    }

}
