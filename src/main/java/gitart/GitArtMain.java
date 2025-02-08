package gitart;

import javax.swing.SwingUtilities;

public class GitArtMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GitArtFrame());
    }
}
