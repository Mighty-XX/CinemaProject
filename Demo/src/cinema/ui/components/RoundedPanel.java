package cinema.ui.components;

import cinema.util.UITheme;

import javax.swing.*;
import java.awt.*;

/** Rounded card panel with dark background. */
public class RoundedPanel extends JPanel {

    private Color bg;
    private int arc;
    private boolean hasBorder;
    private Color borderColor;

    public RoundedPanel() { this(UITheme.BG_CARD, UITheme.ARC, false, null); }

    public RoundedPanel(Color bg) { this(bg, UITheme.ARC, false, null); }

    public RoundedPanel(Color bg, int arc, boolean hasBorder, Color borderColor) {
        this.bg = bg;
        this.arc = arc;
        this.hasBorder   = hasBorder;
        this.borderColor = borderColor;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void setBgColor(Color c) { this.bg = c; repaint(); }
    public void setBorderHighlight(boolean v, Color c) { hasBorder = v; borderColor = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        if (hasBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
