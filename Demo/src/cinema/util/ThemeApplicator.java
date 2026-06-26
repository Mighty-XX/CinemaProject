package cinema.util;

import cinema.ui.components.CinemaButton;
import cinema.ui.components.CinemaField;

import javax.swing.*;
import java.awt.*;

/**
 * Walks the Swing component tree and re-applies theme colors
 * to all standard components. Call after UITheme.applyDark/Light().
 */
public class ThemeApplicator {

    /**
     * Apply current UITheme colors to all components under root.
     * Custom-painted components (using UITheme.* in paintComponent) will
     * repaint automatically. Standard Swing components need explicit updates.
     */
    public static void apply(Component root) {
        applyRecursive(root);
        if (root instanceof JComponent) ((JComponent) root).repaint();
    }

    private static void applyRecursive(Component c) {
        // ── JLabel ──────────────────────────────────────────────────
        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            Color fg = l.getForeground();
            // Map old theme colors to new ones
            l.setForeground(remapTextColor(fg));
            // If label is non-opaque, background doesn't matter
        }

        // ── JTextField / CinemaField ─────────────────────────────
        else if (c instanceof CinemaField) {
            CinemaField f = (CinemaField) c;
            f.setForeground(UITheme.TEXT_PRIMARY);
            f.setCaretColor(UITheme.ACCENT_GOLD);
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setForeground(UITheme.TEXT_PRIMARY);
            tf.setBackground(UITheme.BG_INPUT);
            tf.setCaretColor(UITheme.ACCENT_GOLD);
        }

        // ── JPasswordField ───────────────────────────────────────
        else if (c instanceof JPasswordField) {
            JPasswordField pf = (JPasswordField) c;
            pf.setForeground(UITheme.TEXT_PRIMARY);
            pf.setBackground(UITheme.BG_INPUT);
            pf.setCaretColor(UITheme.ACCENT_GOLD);
        }

        // ── JRadioButton / JCheckBox ─────────────────────────────
        else if (c instanceof JRadioButton) {
            ((JRadioButton) c).setForeground(UITheme.TEXT_PRIMARY);
            ((JRadioButton) c).setBackground(new Color(0, 0, 0, 0));
        }

        // ── Opaque JPanel ────────────────────────────────────────
        // Don't touch non-opaque panels; custom paintComponent handles them
        else if (c instanceof JPanel && ((JPanel) c).isOpaque()) {
            ((JPanel) c).setBackground(UITheme.BG_CARD);
        }

        // ── JScrollPane ──────────────────────────────────────────
        else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setBackground(UITheme.BG_PRIMARY);
            sp.getViewport().setBackground(UITheme.BG_PRIMARY);
        }

        // ── JSeparator ───────────────────────────────────────────
        else if (c instanceof JSeparator) {
            ((JSeparator) c).setForeground(UITheme.withAlpha(UITheme.TEXT_MUTED, 80));
        }

        // Repaint this component then recurse into children
        c.repaint();

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                applyRecursive(child);
            }
        }
    }

    /**
     * Map a color that was previously set to a dark-theme value
     * to the equivalent light-theme value (or vice-versa).
     * We match by proximity rather than exact equality.
     */
    private static Color remapTextColor(Color c) {
        if (c == null) return UITheme.TEXT_PRIMARY;

        // Check brightness: was this a "bright/primary" text color?
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float brightness = hsb[2];
        float saturation = hsb[1];

        // Gold / accent colors → remap to theme gold (already correct)
        if (isNear(c, UITheme.ACCENT_GOLD, 80) || isNear(c, new Color(255,200,50), 80)
                || isNear(c, new Color(180,110,0), 80)) {
            return UITheme.accentGold();
        }
        if (isNear(c, UITheme.ACCENT_CYAN, 60)) return UITheme.ACCENT_CYAN;
        if (isNear(c, UITheme.ACCENT_GREEN, 60)) return UITheme.ACCENT_GREEN;
        if (isNear(c, UITheme.ACCENT_PURPLE, 60)) return UITheme.ACCENT_PURPLE;
        if (isNear(c, UITheme.ACCENT_PINK, 60)) return UITheme.ACCENT_PINK;

        // Bright text → primary
        if (brightness > 0.85f && saturation < 0.3f) return UITheme.TEXT_PRIMARY;
        // Medium text → secondary
        if (brightness > 0.55f && saturation < 0.4f) return UITheme.TEXT_SECONDARY;
        // Dim text → muted
        if (brightness > 0.3f) return UITheme.TEXT_MUTED;

        // Very dark (like Color.BLACK in dark mode) → primary on light
        if (brightness < 0.2f) return UITheme.TEXT_PRIMARY;

        return UITheme.TEXT_PRIMARY;
    }

    private static boolean isNear(Color a, Color b, int tolerance) {
        return Math.abs(a.getRed()   - b.getRed())   <= tolerance
            && Math.abs(a.getGreen() - b.getGreen()) <= tolerance
            && Math.abs(a.getBlue()  - b.getBlue())  <= tolerance;
    }
}
