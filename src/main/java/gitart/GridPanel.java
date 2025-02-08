package gitart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GridPanel extends JPanel {
    private GitArtFrame parent;

    public GridPanel(GitArtFrame parent) {
        this.parent = parent;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
                int row = e.getY() / (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
                if (row >= 0 && row < GitArtFrame.ROWS && col >= 0 && col < GitArtFrame.COLS) {
                    parent.getGrid()[row][col] = !parent.getGrid()[row][col];
                    repaint();
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        boolean[][] grid = parent.getGrid();
        for (int c = 0; c < GitArtFrame.COLS; c++) {
            for (int r = 0; r < GitArtFrame.ROWS; r++) {
                g2.setColor(grid[r][c] ? new Color(0, 200, 0) : new Color(235, 237, 240));
                int x = c * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
                int y = r * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
                g2.fillRoundRect(x, y, GitArtFrame.CELL_SIZE, GitArtFrame.CELL_SIZE, 5, 5);
            }
        }
        g2.setColor(Color.LIGHT_GRAY);
        for (int c = 0; c <= GitArtFrame.COLS; c++) {
            int x = c * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
            g2.drawLine(x, 0, x, GitArtFrame.ROWS * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP));
        }
        for (int r = 0; r <= GitArtFrame.ROWS; r++) {
            int y = r * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP);
            g2.drawLine(0, y, GitArtFrame.COLS * (GitArtFrame.CELL_SIZE + GitArtFrame.GAP), y);
        }
    }
}
