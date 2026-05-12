import javax.swing.*;

/**
 * Entry point for PRMS — Placement Records Management System
 * CS621 Database Systems | IIIT Guwahati
 *
 * Run: java -cp "out;lib/*" Main
 */
public class Main {
    public static void main(String[] args) {
        // Use native OS look-and-feel (Windows / macOS / Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* fall back to cross-platform L&F */ }

        SwingUtilities.invokeLater(() -> new ui.LoginFrame().setVisible(true));
    }
}
