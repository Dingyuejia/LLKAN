package src.ui;



import src.user.ScoreEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 排行榜弹窗。
 *
 * <p>UI 只负责展示传入的排行榜数据（entries），数据获取与排序由 LeaderboardManager 完成。</p>
 */
public class LeaderboardDialog extends JDialog {
    /**
     * @param owner   父窗口
     * @param entries 已排序的成绩列表（通常取 Top N）
     */
    public LeaderboardDialog(Frame owner, List<ScoreEntry> entries) {
        super(owner, "排行榜", true);
        setLayout(new BorderLayout());

        String[] columns = {"排名", "用户名", "分数", "模式", "时间"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rank = 1;
        for (ScoreEntry e : entries) {
            String mode = (e.getDifficulty() == 0) ? "简单" : "困难";
            String time = fmt.format(new Date(e.getTimestamp()));
            model.addRow(new Object[]{rank++, e.getUsername(), e.getScore(), mode, time});
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(ev -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(520, 420);
        setLocationRelativeTo(owner);
    }
}
