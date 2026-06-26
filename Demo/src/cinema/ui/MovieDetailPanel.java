package cinema.ui;

import cinema.dao.MovieShowDAO;
import cinema.model.Movie;
import cinema.model.MovieShow;
import cinema.ui.components.CinemaButton;
import cinema.ui.components.RoundedPanel;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;

public class MovieDetailPanel extends JPanel {

    private final CinemaFrame frame;
    private final MovieShowDAO showDAO = new MovieShowDAO();

    private Movie movie;

    public MovieDetailPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void loadMovie(Movie m) {
        this.movie = m;
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));

        CinemaButton btnBack = new CinemaButton("← Trang chủ", CinemaButton.Style.GHOST);
        btnBack.setPreferredSize(new Dimension(150, 36));
        btnBack.addActionListener(e -> frame.showPanel("HOME"));
        topBar.add(btnBack, BorderLayout.WEST);

        // Add Delete Button if manager
        if (frame.getCurrentAccount() != null && frame.getCurrentAccount().isManager()) {
            CinemaButton btnDelete = new CinemaButton("Xóa Phim", CinemaButton.Style.GOLD);
            btnDelete.setPreferredSize(new Dimension(120, 36));
            btnDelete.addActionListener(e -> {
                if (frame.showConfirmDialog("Bạn có chắc chắn muốn xóa phim này?")) {
                    if (new cinema.dao.MovieDAO().deleteMovie(movie.getMovieId())) {
                        frame.showMessage("Xóa phim thành công!", false);
                        frame.showPanel("HOME");
                    } else {
                        frame.showMessage("Không thể xóa phim này do đã có người đặt vé.", true);
                    }
                }
            });
            topBar.add(btnDelete, BorderLayout.EAST);
        }

        add(topBar, BorderLayout.NORTH);

        // ── Content ───────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Poster panel
        JPanel poster = buildPoster();
        JPanel posterWrapper = new JPanel(new BorderLayout());
        posterWrapper.setOpaque(false);
        posterWrapper.setBorder(new EmptyBorder(8, 0, 0, 0));
        posterWrapper.add(poster, BorderLayout.NORTH);
        content.add(posterWrapper, BorderLayout.WEST);

        // Info + showtimes
        JPanel info = buildInfo();
        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(info, BorderLayout.NORTH);
        content.add(infoWrapper, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getVerticalScrollBar().setUI(new HomePanel.DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildPoster() {
        JPanel p = new JPanel() {
            private Image posterImg = null;
            {
                new SwingWorker<Image, Void>() {
                    @Override
                    protected Image doInBackground() throws Exception {
                        String t = movie.getTitle().toLowerCase();
                        if (t.contains("doraemon")) {
                            java.net.URL url = getClass().getResource("/cinema/assets/doraemon_poster.jpg");
                            if (url != null)
                                return ImageIO.read(url);
                        } else if (t.contains("lật mặt") || t.contains("lat mat")) {
                            java.net.URL url = getClass().getResource("/cinema/assets/latmat_poster.jpg");
                            if (url != null)
                                return ImageIO.read(url);
                        }
                        String urlStr = "https://picsum.photos/seed/" + movie.getMovieId() + "/600/900";
                        return ImageIO.read(new URL(urlStr));
                    }

                    @Override
                    protected void done() {
                        try {
                            posterImg = get();
                            repaint();
                        } catch (Exception ignored) {
                        }
                    }
                }.execute();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (posterImg != null) {
                    Shape oldClip = g2.getClip();
                    g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.drawImage(posterImg, 0, 0, getWidth(), getHeight(), null);
                    g2.setClip(oldClip);
                } else {
                    Color base = parsePosterColor(movie.getPosterColor());
                    g2.setPaint(new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base.darker()));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    // Decorative shapes
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillOval(-30, -30, 220, 220);
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.fillOval(50, getHeight() - 150, 200, 200);

                    // Film emoji
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
                    FontMetrics fm = g2.getFontMetrics();
                    String em = " ";
                    g2.drawString(em, (getWidth() - fm.stringWidth(em)) / 2, getHeight() / 2 - 10);
                    // Title over poster
                    g2.setFont(UITheme.FONT_HEADING.deriveFont(Font.BOLD, 16f));
                    g2.setColor(UITheme.TEXT_PRIMARY);
                    String t = movie.getTitle();
                    fm = g2.getFontMetrics();
                    g2.drawString(t.length() > 18 ? t.substring(0, 18) + "…" : t,
                            (getWidth() - fm.stringWidth(t.length() > 18 ? t.substring(0, 18) + "…" : t)) / 2,
                            getHeight() / 2 + 60);
                }
                // Age rating
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(getWidth() - 52, 12, 40, 22, 8, 8);
                g2.setColor(UITheme.ACCENT_GOLD);
                g2.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
                String rat = movie.getAgeRating() != null ? movie.getAgeRating() : "?";
                g2.drawString(rat, getWidth() - 52 + (40 - g2.getFontMetrics().stringWidth(rat)) / 2, 27);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(600, 900));
        p.setOpaque(false);
        return p;
    }

    private JPanel buildInfo() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 10, 0, 10));

        // Title
        JLabel title = new JLabel(movie.getTitle());
        title.setFont(UITheme.FONT_TITLE.deriveFont(40f));
        title.setForeground(UITheme.ACCENT_GOLD);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Meta row
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(LEFT_ALIGNMENT);

        addChip(meta, "Thể loại: " + nvl(movie.getGenre()), UITheme.ACCENT_CYAN);
        addChip(meta, "Thời lượng: " + movie.getDurationFormatted(), UITheme.TEXT_SECONDARY);
        addChip(meta, "Ngôn ngữ: " + nvl(movie.getLanguage()), UITheme.TEXT_SECONDARY);
        if (movie.getReleaseDate() != null)
            addChip(meta, "Khởi chiếu: " + movie.getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    UITheme.TEXT_SECONDARY);

        // Director / Cast
        JLabel dirLabel = infoLabel("Đạo diễn: ", UITheme.TEXT_SECONDARY);
        JLabel dirVal = infoLabel(nvl(movie.getDirector()), UITheme.TEXT_PRIMARY);

        JLabel castLabel = infoLabel("Diễn viên: ", UITheme.TEXT_SECONDARY);
        JLabel castVal = infoLabel(movie.getCastMembersDisplay(), UITheme.TEXT_PRIMARY);
        castVal.setMaximumSize(new Dimension(500, 60));

        // Synopsis
        JLabel synTitle = infoLabel("Nội dung phim", UITheme.ACCENT_GOLD);
        synTitle.setFont(UITheme.FONT_HEADING.deriveFont(20f));

        JTextArea synText = new JTextArea(nvl(movie.getSynopsis()));
        synText.setFont(UITheme.FONT_BODY.deriveFont(16f));
        synText.setForeground(UITheme.TEXT_SECONDARY);
        synText.setBackground(UITheme.BG_CARD);
        synText.setLineWrap(true);
        synText.setWrapStyleWord(true);
        synText.setEditable(false);
        synText.setBorder(new EmptyBorder(10, 12, 10, 12));
        synText.setMaximumSize(new Dimension(600, 100));
        synText.setAlignmentX(LEFT_ALIGNMENT);

        // Showtimes
        JLabel stTitle = infoLabel("Chọn suất chiếu", UITheme.ACCENT_GOLD);
        stTitle.setFont(UITheme.FONT_HEADING.deriveFont(20f));
        stTitle.setAlignmentX(LEFT_ALIGNMENT);

        JPanel stGrid = buildShowtimeGrid();
        stGrid.setAlignmentX(LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(8));
        p.add(meta);
        p.add(Box.createVerticalStrut(12));
        addRow(p, dirLabel, dirVal);
        addRow(p, castLabel, castVal);
        p.add(Box.createVerticalStrut(12));
        p.add(synTitle);
        p.add(Box.createVerticalStrut(6));
        p.add(synText);
        p.add(Box.createVerticalStrut(20));
        p.add(stTitle);
        p.add(Box.createVerticalStrut(10));
        p.add(stGrid);

        return p;
    }

    private JPanel buildShowtimeGrid() {
        List<MovieShow> shows = showDAO.getByMovie(movie.getMovieId());
        JPanel grid = new JPanel(new HomePanel.WrapLayout(FlowLayout.LEFT, 10, 10));
        grid.setOpaque(false);

        if (shows.isEmpty()) {
            JLabel empty = new JLabel("Hiện chưa có suất chiếu.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            grid.add(empty);
        } else {
            for (MovieShow ms : shows) {
                ms.setMovie(movie);
                JButton btn = buildShowtimeBtn(ms);
                grid.add(btn);
            }
        }
        return grid;
    }

    private JButton buildShowtimeBtn(MovieShow ms) {
        String line1 = ms.getShowtime().getShowDate()
                .format(DateTimeFormatter.ofPattern("dd/MM"));
        String line2 = ms.getShowtime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String line3 = ms.getRoom() != null ? ms.getRoom().getRoomName() : "";

        JButton btn = new JButton(
                "<html><center><font size='4'>" + line1 + "</font><br><font size='6'><b>" + line2 + "</b></font><br>" +
                        "<font size='3'>" + line3 + "</font></center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (getModel().isRollover()) {
                    g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), getHeight()));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(UITheme.FONT_BODY.deriveFont(15f));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 95));
        btn.addActionListener(e -> {
            if (frame.getCurrentAccount() == null) {
                frame.showMessage("Vui lòng đăng nhập để đặt vé!", true);
                frame.showPanel("LOGIN");
            } else {
                frame.showSeatSelection(ms);
            }
        });
        return btn;
    }

    private void addChip(JPanel p, String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD, 14f));
        l.setForeground(c);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.withAlpha(c, 80), 1, true),
                new EmptyBorder(3, 8, 3, 8)));
        p.add(l);
    }

    private void addRow(JPanel p, JLabel lbl, JLabel val) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(lbl);
        row.add(val);
        p.add(row);
    }

    private JLabel infoLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY.deriveFont(16f));
        l.setForeground(color);
        return l;
    }

    private String nvl(String s) {
        return s != null ? s : "—";
    }

    private Color parsePosterColor(String hex) {
        if (hex == null || hex.isBlank())
            return new Color(40, 10, 80);
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return new Color(40, 10, 80);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
