package cinema.ui.components;

import cinema.model.Movie;
import cinema.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

/**
 * Movie card widget showing poster gradient + title + genre + duration.
 */
public class MovieCard extends JPanel {

    private static final int W = 190;
    private static final int H = 290;
    private static final Color[] PALETTE = {
            new Color(40, 10, 80), new Color(10, 30, 70), new Color(60, 10, 10),
            new Color(10, 50, 40), new Color(50, 30, 10), new Color(10, 10, 60)
    };

    private final Movie movie;
    private boolean hovered = false;
    private float hover = 0f;
    private Timer anim;

    private static final Map<Integer, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    public MovieCard(Movie movie, Consumer<Movie> onSelect) {
        this.movie = movie;
        setPreferredSize(new Dimension(W, H));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                startAnim();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                startAnim();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.accept(movie);
            }
        });

        if (!IMAGE_CACHE.containsKey(movie.getMovieId())) {
            new SwingWorker<Image, Void>() {
                @Override
                protected Image doInBackground() throws Exception {
                    String t = movie.getTitle().toLowerCase();
                    if (t.contains("doraemon")) {
                        java.net.URL url = getClass().getResource("/cinema/assets/doraemon_poster.jpg");
                        if (url != null) return ImageIO.read(url);
                    } else if (t.contains("lật mặt") || t.contains("lat mat")) {
                        java.net.URL url = getClass().getResource("/cinema/assets/latmat_poster.jpg");
                        if (url != null) return ImageIO.read(url);
                    }
                    String urlStr = "https://picsum.photos/seed/" + movie.getMovieId() + "/200/300";
                    return ImageIO.read(new URL(urlStr));
                }

                @Override
                protected void done() {
                    try {
                        IMAGE_CACHE.put(movie.getMovieId(), get());
                        repaint();
                    } catch (Exception ignored) {
                    }
                }
            }.execute();
        }
    }

    private void startAnim() {
        if (anim != null)
            anim.stop();
        anim = new Timer(16, e -> {
            float target = hovered ? 1f : 0f;
            hover += (target - hover) * 0.2f;
            if (Math.abs(hover - target) < 0.01f) {
                hover = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        anim.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int lift = (int) (hover * 8);
        int w = getWidth(), h = getHeight() - lift;
        int y0 = lift;

        // Shadow
        if (hover > 0.05f) {
            g2.setColor(new Color(0, 0, 0, (int) (hover * 80)));
            g2.fillRoundRect(6, y0 + 6, w - 6, h - 6, UITheme.ARC, UITheme.ARC);
        }

        // Background gradient (poster colour) or Image
        Image img = IMAGE_CACHE.get(movie.getMovieId());
        if (img != null) {
            Shape oldClip = g2.getClip();
            g2.clip(new RoundRectangle2D.Float(0, y0, w - 2, h, UITheme.ARC, UITheme.ARC));
            g2.drawImage(img, 0, y0, w - 2, h, null);
            g2.setClip(oldClip);
        } else {
            Color base = parsePosterColor(movie.getPosterColor());
            Color light = base.brighter();
            g2.setPaint(new GradientPaint(0, y0, light, 0, y0 + h, base));
            g2.fillRoundRect(0, y0, w - 2, h, UITheme.ARC, UITheme.ARC);

            // Subtle star pattern
            g2.setColor(new Color(255, 255, 255, 15));
            for (int i = 0; i < 30; i++) {
                int sx = (movie.getMovieId() * 17 + i * 37) % (w - 4);
                int sy = (movie.getMovieId() * 31 + i * 53) % (h / 2) + y0;
                g2.fillOval(sx, sy, 2, 2);
            }
        }

        // Age rating badge
        String rating = movie.getAgeRating() != null ? movie.getAgeRating() : "?";
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(w - 42, y0 + 8, 34, 20, 6, 6);
        g2.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD, 10f));
        g2.setColor(UITheme.ACCENT_GOLD);
        g2.drawString(rating, w - 40 + (30 - g2.getFontMetrics().stringWidth(rating)) / 2, y0 + 22);

        // Bottom gradient overlay for text
        int textH = 80;
        GradientPaint overlay = new GradientPaint(0, y0 + h - textH, new Color(0, 0, 0, 0),
                0, y0 + h, new Color(0, 0, 0, 220));
        g2.setPaint(overlay);
        g2.fillRoundRect(0, y0, w - 2, h, UITheme.ARC, UITheme.ARC);

        // Title
        g2.setFont(UITheme.FONT_HEADING);
        g2.setColor(UITheme.TEXT_PRIMARY);
        String title = movie.getTitle();
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(title) > w - 16)
            title = title.substring(0, 18) + "...";
        g2.drawString(title, 10, y0 + h - 46);

        // Genre chip
        g2.setFont(UITheme.FONT_SMALL);
        g2.setColor(UITheme.ACCENT_CYAN);
        String genre = movie.getGenre() != null ? movie.getGenre() : "";
        g2.drawString(genre, 10, y0 + h - 30);

        // Duration
        g2.setColor(UITheme.TEXT_SECONDARY);
        g2.drawString("⏱ " + movie.getDurationFormatted(), 10, y0 + h - 14);

        // Hover: glow border
        if (hover > 0.05f) {
            g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, (int) (hover * 180)));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, y0, w - 3, h - 1, UITheme.ARC, UITheme.ARC);
        }

        g2.dispose();
    }

    private Color parsePosterColor(String hex) {
        if (hex == null || hex.isBlank())
            return PALETTE[movie.getMovieId() % PALETTE.length];
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return PALETTE[0];
        }
    }
}
