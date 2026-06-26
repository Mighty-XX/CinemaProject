package cinema.app;

import cinema.ui.CinemaFrame;

import javax.swing.*;

public class CinemaMain {

    public static void main(String[] args) {
        // ── Look & Feel ──────────────────────────────────────────
        try {
            // Use Nimbus as a clean base, then override with our dark theme
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fall back to default
        }

        // Apply global dark overrides
        UIManager.put("Panel.background", new java.awt.Color(8, 8, 22));
        UIManager.put("OptionPane.background", new java.awt.Color(22, 22, 48));
        UIManager.put("OptionPane.messageForeground", new java.awt.Color(240, 240, 255));
        UIManager.put("Button.background", new java.awt.Color(30, 30, 60));
        UIManager.put("Button.foreground", new java.awt.Color(240, 240, 255));
        UIManager.put("Label.foreground", new java.awt.Color(240, 240, 255));
        UIManager.put("TextField.background", new java.awt.Color(20, 20, 46));
        UIManager.put("TextField.foreground", new java.awt.Color(240, 240, 255));
        UIManager.put("TextField.caretForeground", new java.awt.Color(255, 190, 50));
        UIManager.put("PasswordField.background", new java.awt.Color(20, 20, 46));
        UIManager.put("PasswordField.foreground", new java.awt.Color(240, 240, 255));
        UIManager.put("RadioButton.background", new java.awt.Color(22, 22, 48));
        UIManager.put("RadioButton.foreground", new java.awt.Color(240, 240, 255));
        UIManager.put("CheckBox.background", new java.awt.Color(22, 22, 48));
        UIManager.put("ScrollPane.background", new java.awt.Color(8, 8, 22));
        UIManager.put("Viewport.background", new java.awt.Color(8, 8, 22));
        UIManager.put("ScrollBar.thumb", new java.awt.Color(100, 80, 0));
        UIManager.put("ScrollBar.track", new java.awt.Color(15, 15, 35));

        // ── Launch ───────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            CinemaFrame frame = new CinemaFrame();
            frame.setVisible(true);
        });
    }
}
