package cinema.ui.components;

import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Dark-styled text field with rounded border. */
public class CinemaField extends JTextField {

    public CinemaField(String placeholder) {
        setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.ACCENT_GOLD);
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setPreferredSize(new Dimension(260, 42));
        putClientProperty("placeholder", placeholder);
        // Auto-repaint on theme change so BG_INPUT and border colors update
        UITheme.addThemeListener(() -> {
            setForeground(UITheme.TEXT_PRIMARY);
            setCaretColor(UITheme.ACCENT_GOLD);
            repaint();
        });
        
        // Repaint on focus change to update border color correctly
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(UITheme.BG_INPUT);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.ARC, UITheme.ARC);

        // Border
        g2.setColor(hasFocus() ? UITheme.ACCENT_GOLD : UITheme.withAlpha(UITheme.TEXT_MUTED, 120));
        g2.setStroke(new BasicStroke(hasFocus() ? 1.8f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.ARC, UITheme.ARC);

        g2.dispose();

        // Placeholder
        if (getText().isEmpty() && !hasFocus()) {
            Graphics2D pg = (Graphics2D) g.create();
            pg.setFont(UITheme.FONT_BODY);
            pg.setColor(UITheme.TEXT_MUTED);
            Insets ins = getInsets();
            FontMetrics fm = pg.getFontMetrics();
            Object ph = getClientProperty("placeholder");
            pg.drawString(ph != null ? ph.toString() : "",
                    ins.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            pg.dispose();
        }

        super.paintComponent(g);
    }
}
