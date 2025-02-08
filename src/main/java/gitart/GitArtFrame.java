package gitart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GitArtFrame extends JFrame {
    public static final int CELL_SIZE = 20;
    public static final int GAP = 2;
    public static final int ROWS = 7;
    public static final int COLS = 53;

    private boolean[][] grid = new boolean[ROWS][COLS];
    private GridPanel gridPanel;
    private JButton commitButton;
    private JButton selectAllButton;
    private JButton selectWeekdaysButton;
    private JButton randomFillButton;
    private JButton clearAllButton;
    private JButton maxFillButton;
    private JLabel statusLabel;
    private JComboBox<String> yearComboBox;
    private String currentMode;
    private Map<String, boolean[][]> gridStates = new HashMap<>();
    private boolean randomFillActive = false;

    public static final Map<Character, String[]> LETTER_PATTERNS = new HashMap<>();
    static {
        LETTER_PATTERNS.put('A', new String[] {"01110","10001","10001","11111","10001","10001","10001"});
        LETTER_PATTERNS.put('B', new String[] {"11110","10001","10001","11110","10001","10001","11110"});
        LETTER_PATTERNS.put('C', new String[] {"01110","10001","10000","10000","10000","10001","01110"});
        LETTER_PATTERNS.put('D', new String[] {"11100","10010","10001","10001","10001","10010","11100"});
        LETTER_PATTERNS.put('E', new String[] {"11111","10000","10000","11111","10000","10000","11111"});
        LETTER_PATTERNS.put('F', new String[] {"11111","10000","10000","11111","10000","10000","10000"});
        LETTER_PATTERNS.put('G', new String[] {"01110","10001","10000","10111","10001","10001","01110"});
        LETTER_PATTERNS.put('H', new String[] {"10001","10001","10001","11111","10001","10001","10001"});
        LETTER_PATTERNS.put('I', new String[] {"01110","00100","00100","00100","00100","00100","01110"});
        LETTER_PATTERNS.put('J', new String[] {"00111","00010","00010","00010","10010","10010","01100"});
        LETTER_PATTERNS.put('K', new String[] {"10001","10010","10100","11000","10100","10010","10001"});
        LETTER_PATTERNS.put('L', new String[] {"10000","10000","10000","10000","10000","10000","11111"});
        LETTER_PATTERNS.put('M', new String[] {"10001","11011","10101","10101","10001","10001","10001"});
        LETTER_PATTERNS.put('N', new String[] {"10001","11001","10101","10011","10001","10001","10001"});
        LETTER_PATTERNS.put('O', new String[] {"01110","10001","10001","10001","10001","10001","01110"});
        LETTER_PATTERNS.put('P', new String[] {"11110","10001","10001","11110","10000","10000","10000"});
        LETTER_PATTERNS.put('Q', new String[] {"01110","10001","10001","10001","10101","10010","01101"});
        LETTER_PATTERNS.put('R', new String[] {"11110","10001","10001","11110","10100","10010","10001"});
        LETTER_PATTERNS.put('S', new String[] {"01111","10000","10000","01110","00001","00001","11110"});
        LETTER_PATTERNS.put('T', new String[] {"11111","00100","00100","00100","00100","00100","00100"});
        LETTER_PATTERNS.put('U', new String[] {"10001","10001","10001","10001","10001","10001","01110"});
        LETTER_PATTERNS.put('V', new String[] {"10001","10001","10001","10001","10001","01010","00100"});
        LETTER_PATTERNS.put('W', new String[] {"10001","10001","10001","10101","10101","10101","01010"});
        LETTER_PATTERNS.put('X', new String[] {"10001","10001","01010","00100","01010","10001","10001"});
        LETTER_PATTERNS.put('Y', new String[] {"10001","10001","01010","00100","00100","00100","00100"});
        LETTER_PATTERNS.put('Z', new String[] {"11111","00001","00010","00100","01000","10000","11111"});
    }

    public GitArtFrame() {
        super("GitHub Contributions Art Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel textLabel = new JLabel("Enter text:");
        JTextField textField = new JTextField(20);
        JButton renderTextButton = new JButton("Render Text");
        // Передаём текст без обрезки, чтобы сохранить ведущие пробелы
        renderTextButton.addActionListener(e -> renderText(textField.getText()));
        topPanel.add(textLabel);
        topPanel.add(textField);
        topPanel.add(renderTextButton);

        JLabel yearLabel = new JLabel("Select Year:");
        yearComboBox = new JComboBox<>();
        yearComboBox.addItem("Current Year");
        int currYear = LocalDate.now().getYear();
        for (int y = currYear - 5; y <= currYear; y++) {
            yearComboBox.addItem(String.valueOf(y));
        }
        yearComboBox.setSelectedItem("Current Year");
        currentMode = "Current Year";
        gridStates.put(currentMode, cloneGrid(grid));
        yearComboBox.addActionListener(e -> {
            String newMode = (String) yearComboBox.getSelectedItem();
            if (!newMode.equals(currentMode)) {
                gridStates.put(currentMode, cloneGrid(grid));
                if (gridStates.containsKey(newMode)) {
                    grid = cloneGrid(gridStates.get(newMode));
                } else {
                    grid = new boolean[ROWS][COLS];
                }
                currentMode = newMode;
                gridPanel.repaint();
            }
        });
        topPanel.add(yearLabel);
        topPanel.add(yearComboBox);
        add(topPanel, BorderLayout.NORTH);

        gridPanel = new GridPanel(this);
        gridPanel.setPreferredSize(new Dimension(COLS * (CELL_SIZE + GAP), ROWS * (CELL_SIZE + GAP)));
        add(gridPanel, BorderLayout.CENTER);

        commitButton = new JButton("COMMIT EVERYTHING");
        commitButton.addActionListener(e -> commitEverything());
        selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> toggleSelectAll());
        selectWeekdaysButton = new JButton("Select Weekdays");
        selectWeekdaysButton.addActionListener(e -> toggleSelectWeekdays());
        randomFillButton = new JButton("Random Fill");
        randomFillButton.addActionListener(e -> toggleRandomFill());
        clearAllButton = new JButton("Clear All");
        clearAllButton.addActionListener(e -> {
            for (int i = 0; i < yearComboBox.getItemCount(); i++) {
                String mode = yearComboBox.getItemAt(i);
                gridStates.put(mode, new boolean[ROWS][COLS]);
            }
            clearGrid();
            gridPanel.repaint();
        });
        maxFillButton = new JButton("Max Fill");
        maxFillButton.addActionListener(e -> {
            boolean[][] full = new boolean[ROWS][COLS];
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    full[i][j] = true;
                }
            }
            for (int i = 0; i < yearComboBox.getItemCount(); i++) {
                String mode = yearComboBox.getItemAt(i);
                gridStates.put(mode, cloneGrid(full));
            }
            grid = cloneGrid(full);
            gridPanel.repaint();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(commitButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(selectWeekdaysButton);
        buttonPanel.add(randomFillButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(maxFillButton);
        statusLabel = new JLabel("Click cells to toggle. Then press COMMIT EVERYTHING.");
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public void renderText(String text) {
        clearGrid();
        if (text == null || text.isEmpty()) {
            gridPanel.repaint();
            return;
        }
        text = text.toUpperCase();
        int offset = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == ' ') {
                offset += 2;
            } else {
                if (offset + 5 > COLS) break;
                String[] pattern = LETTER_PATTERNS.get(ch);
                if (pattern == null) pattern = LETTER_PATTERNS.get('?');
                for (int row = 0; row < 7; row++) {
                    if (row >= ROWS) break;
                    String line = pattern[row];
                    for (int col = 0; col < 5; col++) {
                        int gridCol = offset + col;
                        if (gridCol < COLS && line.charAt(col) == '1') {
                            grid[row][gridCol] = true;
                        }
                    }
                }
                offset += 6;
            }
            if (offset >= COLS) break;
        }
        gridPanel.repaint();
    }

    public void toggleSelectAll() {
        boolean allSelected = true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!grid[r][c]) {
                    allSelected = false;
                    break;
                }
            }
            if (!allSelected) break;
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = !allSelected;
            }
        }
        randomFillActive = false;
        gridPanel.repaint();
    }

    public void toggleSelectWeekdays() {
        boolean allWeekdaysSelected = true;
        for (int c = 0; c < COLS; c++) {
            for (int r = 1; r < 6; r++) {
                if (!grid[r][c]) {
                    allWeekdaysSelected = false;
                    break;
                }
            }
            if (!allWeekdaysSelected) break;
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 1; r < 6; r++) {
                grid[r][c] = !allWeekdaysSelected;
            }
        }
        randomFillActive = false;
        gridPanel.repaint();
    }

    public void toggleRandomFill() {
        if (!randomFillActive) {
            Random rand = new Random();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    grid[r][c] = rand.nextBoolean();
                }
            }
            randomFillActive = true;
        } else {
            clearGrid();
            randomFillActive = false;
        }
        gridPanel.repaint();
    }

    public void clearGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = false;
            }
        }
    }

    public void commitEverything() {
        int totalCommits = CommitManager.commitGrid(grid, ROWS, COLS, currentMode);
        JOptionPane.showMessageDialog(this, "Generated " + totalCommits + " commits!");
    }

    private boolean[][] cloneGrid(boolean[][] src) {
        boolean[][] dest = new boolean[src.length][];
        for (int i = 0; i < src.length; i++) {
            dest[i] = src[i].clone();
        }
        return dest;
    }
}
