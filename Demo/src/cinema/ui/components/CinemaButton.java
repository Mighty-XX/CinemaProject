package cinema.ui.components;

import cinema.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Rounded gradient button with hover animation.
 */
public class CinemaButton extends JButton {

    public enum Style { GOLD, PURPLE, GHOST, DANGER }

    private final Style style;
    private float hoverAlpha = 0f;
    private Timer hoverTimer;

    public CinemaButton(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(UITheme.FONT_BUTTON);
        setForeground(UITheme.TEXT_PRIMARY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(160, 42));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { animateTo(1f); }
            @Override public void mouseExited(MouseEvent e)  { animateTo(0f); }
        });
    }

    private void animateTo(float target) {
        if (hoverTimer != null) hoverTimer.stop();
        hoverTimer = new Timer(16, null);
        hoverTimer.addActionListener(e -> {
            hoverAlpha += (target - hoverAlpha) * 0.25f;
            if (Math.abs(hoverAlpha - target) < 0.01f) {
                hoverAlpha = target;
                hoverTimer.stop();
            }
            repaint();
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, UITheme.ARC, UITheme.ARC);
        g2.setClip(shape);

        switch (style) {
            case GOLD:
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(255, 200, 40), w, h, new Color(255, 100, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                // Hover overlay
                g2.setColor(new Color(255, 255, 255, (int)(hoverAlpha * 40)));
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                g2.setColor(Color.BLACK);
                break;

            case PURPLE:
                g2.setPaint(new GradientPaint(0, 0, new Color(138, 43, 226),
                                              w, h, new Color(75, 0, 130)));
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                g2.setColor(new Color(255, 255, 255, (int)(hoverAlpha * 40)));
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                g2.setColor(UITheme.TEXT_PRIMARY);
                break;

            case GHOST:
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, (int)(30 + hoverAlpha * 40)));
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                g2.setColor(UITheme.ACCENT_GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, UITheme.ARC, UITheme.ARC);
                g2.setColor(UITheme.ACCENT_GOLD);
                break;

            case DANGER:
                g2.setColor(new Color(180, 30, 50, (int)(200 + hoverAlpha * 55)));
                g2.fillRoundRect(0, 0, w, h, UITheme.ARC, UITheme.ARC);
                g2.setColor(UITheme.TEXT_PRIMARY);
                break;
        }

        // Text
        FontMetrics fm = g2.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setFont(getFont());
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
