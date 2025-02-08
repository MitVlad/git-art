package gitart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Map;

public class CommitManager {
    public static int commitGrid(boolean[][] grid, int rows, int cols, String mode) {
        int totalCommits = 0;
        LocalDate startDate;
        if ("Current Year".equals(mode)) {
            startDate = LocalDate.now().minusDays((cols * 7) - 1);
        } else {
            int selectedYear = Integer.parseInt(mode);
            LocalDate jan1 = LocalDate.of(selectedYear, 1, 1);
            startDate = jan1.with(DayOfWeek.SUNDAY);
            if (startDate.isBefore(jan1)) {
                startDate = startDate.plusWeeks(1);
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[r][c]) {
                    LocalDate commitDate = startDate.plusDays(c * 7 + r);
                    if (!"Current Year".equals(mode) && commitDate.getYear() != Integer.parseInt(mode))
                        continue;
                    if ("Current Year".equals(mode) && commitDate.isAfter(LocalDate.now()))
                        continue;
                    String commitMessage = "GitArt commit " + UUID.randomUUID().toString();
                    try (FileWriter fw = new FileWriter(new File("commit.txt"), true)) {
                        fw.write("Commit on " + commitDate.toString() + ": " + commitMessage + "\n");
                    } catch (IOException ex) {
                        continue;
                    }
                    boolean success = runGitCommit(commitMessage, commitDate.atTime(12, 0), formatter);
                    if (success) totalCommits++;
                }
            }
        }
        return totalCommits;
    }

    private static boolean runGitCommit(String commitMessage, LocalDateTime commitDateTime, DateTimeFormatter formatter) {
        String dateString = commitDateTime.format(formatter);
        try {
            ProcessBuilder addBuilder = new ProcessBuilder("git", "add", "commit.txt");
            Process addProcess = addBuilder.start();
            addProcess.waitFor();
            ProcessBuilder commitBuilder = new ProcessBuilder("git", "commit", "-m", commitMessage);
            Map<String, String> env = commitBuilder.environment();
            env.put("GIT_AUTHOR_DATE", dateString);
            env.put("GIT_COMMITTER_DATE", dateString);
            Process commitProcess = commitBuilder.start();
            commitProcess.waitFor();
            return commitProcess.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
