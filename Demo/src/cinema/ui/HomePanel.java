package cinema.ui;

import cinema.dao.MovieDAO;
import cinema.model.Movie;
import cinema.model.MovieAccount;
import cinema.ui.components.CinemaButton;
import cinema.ui.components.CinemaField;
import cinema.ui.components.MovieCard;
import cinema.ui.components.RoundedPanel;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomePanel extends JPanel {

    private final CinemaFrame frame;
    private final MovieDAO movieDAO = new MovieDAO();

    private JPanel movieGrid;
    private CinemaField tfSearch;
    private JPanel genreBar;
    private String activeGenre = "Tất cả";
    private JLabel lblUser;
    private CinemaButton btnAddMovie;
    private JWindow notificationPopup;
    private long lastPopupCloseTime = 0;

    private static final String[] GENRES = {
            "Tất cả", "Hành động", "Viễn tưởng", "Kinh dị", "Hài hước", "Tình cảm"
    };

    public HomePanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Top Nav ───────────────────────────────────────────
        JPanel nav = buildNav();
        add(nav, BorderLayout.NORTH);

        // ── Body ──────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Hero banner
        JPanel hero = buildHero();
        body.add(hero, BorderLayout.NORTH);

        // Genre filter
        genreBar = buildGenreBar();
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(genreBar, BorderLayout.NORTH);

        // Movie grid
        movieGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
        movieGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(movieGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        center.add(scroll, BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        loadMovies("Tất cả");
    }

    private JPanel buildNav() {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.navBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.withAlpha(UITheme.accentGold(), 80));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(10, 24, 10, 24));

        // Logo
        JLabel logo = new JLabel("G09 Cinema");
        logo.setFont(UITheme.FONT_SUBTITLE);
        logo.setForeground(UITheme.accentGold());
        nav.add(logo, BorderLayout.WEST);

        // ── Search (icon + field) ─────────────────
        JPanel searchWrap = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.ARC, UITheme.ARC);

                boolean focused = tfSearch != null && tfSearch.hasFocus();
                g2.setColor(focused ? UITheme.ACCENT_GOLD : UITheme.withAlpha(UITheme.TEXT_MUTED, 100));
                g2.setStroke(new BasicStroke(focused ? 1.8f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.ARC, UITheme.ARC);
                g2.dispose();
            }
        };
        searchWrap.setOpaque(false);
        searchWrap.setPreferredSize(new Dimension(300, 38));
        UITheme.addThemeListener(searchWrap::repaint);

        JLabel iconSearch = new JLabel("  🔍");
        iconSearch.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        iconSearch.setForeground(UITheme.TEXT_MUTED);
        iconSearch.setBorder(new EmptyBorder(0, 8, 0, 0));

        tfSearch = new CinemaField("") {
            @Override
            protected void paintComponent(Graphics g) {
                // Skip the custom border and background of CinemaField
                super.paintComponent(g);
                // Draw placeholder
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
            }
        };
        tfSearch.setBorder(new EmptyBorder(4, 6, 4, 8));
        tfSearch.setPreferredSize(null);
        tfSearch.putClientProperty("placeholder", "Tìm phim...");
        tfSearch.addActionListener(e -> loadMovies(activeGenre));
        tfSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                searchWrap.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                searchWrap.repaint();
            }
        });

        searchWrap.add(iconSearch, BorderLayout.WEST);
        searchWrap.add(tfSearch, BorderLayout.CENTER);

        JPanel searchCenter = new JPanel(new GridBagLayout());
        searchCenter.setOpaque(false);
        searchCenter.add(searchWrap);
        nav.add(searchCenter, BorderLayout.CENTER);

        // ── Right area: notification + user + logout ─────
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userArea.setOpaque(false);

        lblUser = new JLabel("");
        lblUser.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        lblUser.setForeground(UITheme.TEXT_PRIMARY);

        btnAddMovie = new CinemaButton("Nhập phim mới", CinemaButton.Style.GOLD);
        btnAddMovie.setPreferredSize(new Dimension(130, 34));
        btnAddMovie.setVisible(false);
        btnAddMovie.addActionListener(e -> showAddMovieDialog());

        // ── Bell notification button (JLabel custom to properly render emoji) ──
        JLabel[] bellRef = new JLabel[1];
        JLabel btnBell = new JLabel("🔔", SwingConstants.CENTER) {
            private boolean hov = false;
            {
                bellRef[0] = this;
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hov = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hov = false;
                        repaint();
                    }

                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (System.currentTimeMillis() - lastPopupCloseTime < 100) {
                            return; // It was just closed by this click due to focus lost
                        }
                        if (notificationPopup != null && notificationPopup.isVisible()) {
                            notificationPopup.dispose();
                            notificationPopup = null;
                        } else {
                            showNotificationPopup(bellRef[0]);
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hov) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btnBell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        btnBell.setForeground(UITheme.ACCENT_GOLD);
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBell.setPreferredSize(new Dimension(46, 40));
        btnBell.setToolTipText("Phim mới ra mắt");

        CinemaButton btnLogout = new CinemaButton("Đăng xuất", CinemaButton.Style.GHOST);
        btnLogout.setPreferredSize(new Dimension(110, 34));
        btnLogout.addActionListener(e -> frame.showPanel("LOGIN"));

        // ── Theme toggle button ──────────────────────────────────
        JLabel[] themeRef = new JLabel[1];
        JLabel btnTheme = new JLabel(UITheme.IS_DARK ? "☀️" : "🌙", SwingConstants.CENTER) {
            private boolean hov = false;
            {
                themeRef[0] = this;
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hov = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hov = false;
                        repaint();
                    }

                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (UITheme.IS_DARK)
                            UITheme.applyLight();
                        else
                            UITheme.applyDark();
                        themeRef[0].setText(UITheme.IS_DARK ? "☀️" : "🌙");
                        UITheme.notifyListeners();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (hov) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btnTheme.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btnTheme.setForeground(UITheme.ACCENT_GOLD);
        btnTheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTheme.setPreferredSize(new Dimension(40, 38));
        btnTheme.setToolTipText("Chuyển giao diện Sáng / Tối");

        userArea.add(lblUser);
        userArea.add(btnAddMovie);
        userArea.add(btnBell);
        userArea.add(btnTheme);
        userArea.add(btnLogout);
        nav.add(userArea, BorderLayout.EAST);

        return nav;
    }

    private void showNotificationPopup(Component anchor) {
        java.util.List<cinema.model.Movie> newest = movieDAO.getByGenre("Tất cả");
        int count = Math.min(newest.size(), 5);

        // Build popup panel
        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
        popupPanel.setBackground(new Color(18, 18, 38));
        popupPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.withAlpha(UITheme.ACCENT_GOLD, 160), 2),
                new EmptyBorder(0, 0, 6, 0)));

        // Header row
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerRow.setOpaque(false);
        JLabel bellLbl = new JLabel("🔔");
        bellLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        bellLbl.setForeground(UITheme.ACCENT_GOLD);
        JLabel titleLbl = new JLabel("Phim mới ra mắt");
        titleLbl.setFont(UITheme.FONT_HEADING.deriveFont(Font.BOLD, 16f));
        titleLbl.setForeground(UITheme.ACCENT_GOLD);
        headerRow.add(bellLbl);
        headerRow.add(titleLbl);
        popupPanel.add(headerRow);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UITheme.withAlpha(UITheme.ACCENT_GOLD, 80));
        popupPanel.add(sep);

        if (count == 0) {
            JLabel none = new JLabel("  Chưa có phim nào.");
            none.setFont(UITheme.FONT_BODY);
            none.setForeground(UITheme.TEXT_MUTED);
            none.setBorder(new EmptyBorder(12, 16, 12, 16));
            popupPanel.add(none);
        } else {
            for (int i = 0; i < count; i++) {
                cinema.model.Movie m = newest.get(i);
                final cinema.model.Movie sel = m;

                JPanel[] rowRef = new JPanel[1];
                JPanel row = new JPanel(new BorderLayout(16, 0)) {
                    private boolean hov = false;
                    {
                        setOpaque(true);
                        rowRef[0] = this;
                        addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent e) {
                                hov = true;
                                repaint();
                            }

                            public void mouseExited(java.awt.event.MouseEvent e) {
                                hov = false;
                                repaint();
                            }

                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                SwingUtilities.getWindowAncestor(rowRef[0]).setVisible(false);
                                frame.showMovieDetail(sel);
                            }
                        });
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(hov ? UITheme.BG_HOVER : new Color(18, 18, 38));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.setBorder(new EmptyBorder(10, 18, 10, 18));

                JLabel numLbl = new JLabel(String.valueOf(i + 1) + ".");
                numLbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                numLbl.setForeground(UITheme.withAlpha(UITheme.ACCENT_GOLD, 180));
                numLbl.setPreferredSize(new Dimension(24, 20));

                JLabel nameLbl = new JLabel(m.getTitle());
                nameLbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, 14f));
                nameLbl.setForeground(UITheme.TEXT_PRIMARY);

                JLabel genreLbl = new JLabel(m.getGenre() != null ? m.getGenre() : "");
                genreLbl.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
                genreLbl.setForeground(UITheme.ACCENT_CYAN);
                genreLbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.withAlpha(UITheme.ACCENT_CYAN, 80), 1, true),
                        new EmptyBorder(1, 6, 1, 6)));

                JPanel textCol = new JPanel();
                textCol.setOpaque(false);
                textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
                nameLbl.setAlignmentX(LEFT_ALIGNMENT);
                genreLbl.setAlignmentX(LEFT_ALIGNMENT);
                textCol.add(nameLbl);
                textCol.add(Box.createVerticalStrut(3));
                textCol.add(genreLbl);

                row.add(numLbl, BorderLayout.WEST);
                row.add(textCol, BorderLayout.CENTER);
                popupPanel.add(row);

                if (i < count - 1) {
                    JSeparator rowSep = new JSeparator();
                    rowSep.setForeground(UITheme.withAlpha(UITheme.TEXT_MUTED, 40));
                    rowSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    popupPanel.add(rowSep);
                }
            }
        }

        // Show as lightweight popup window
        JWindow win = new JWindow(SwingUtilities.getWindowAncestor(anchor));
        win.setContentPane(popupPanel);
        win.pack();
        win.setMinimumSize(new Dimension(380, 50));
        win.pack();

        Point loc = anchor.getLocationOnScreen();
        int x = loc.x + anchor.getWidth() - win.getWidth();
        int y = loc.y + anchor.getHeight() + 6;
        win.setLocation(x, y);
        win.setVisible(true);

        // Close when clicking outside
        win.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
            }

            public void windowLostFocus(java.awt.event.WindowEvent e) {
                lastPopupCloseTime = System.currentTimeMillis();
                win.setVisible(false);
                win.dispose();
                if (notificationPopup == win) {
                    notificationPopup = null;
                }
            }
        });
        notificationPopup = win;
    }

    private void showAddMovieDialog() {
        JDialog dlg = new JDialog(frame, "Nhập Phim Mới", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(720, 680);
        dlg.setLocationRelativeTo(frame);

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), 4));
                g2.fillRoundRect(0, 0, getWidth(), 4, 24, 24);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setLayout(new BorderLayout(0, 24));
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JLabel lblTitle = new JLabel("Nhập Phim Mới", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE.deriveFont(26f));
        lblTitle.setForeground(UITheme.ACCENT_GOLD);
        root.add(lblTitle, BorderLayout.NORTH);

        // Fields
        CinemaField tfTitle = new CinemaField("");
        CinemaField tfDirector = new CinemaField("");
        CinemaField tfCast = new CinemaField("");
        CinemaField tfDuration = new CinemaField("");
        CinemaField tfReleaseDate = new CinemaField(java.time.LocalDate.now().toString());

        CinemaField tfGenre = new CinemaField("");
        CinemaField tfLanguage = new CinemaField("");
        CinemaField tfAgeRating = new CinemaField("");

        JTextArea taSynopsis = new JTextArea(5, 20);
        taSynopsis.setFont(UITheme.FONT_BODY);
        taSynopsis.setBackground(UITheme.BG_INPUT);
        taSynopsis.setForeground(UITheme.TEXT_PRIMARY);
        taSynopsis.setCaretColor(UITheme.ACCENT_GOLD);
        taSynopsis.setLineWrap(true);
        taSynopsis.setWrapStyleWord(true);
        taSynopsis.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane scrollSyn = new JScrollPane(taSynopsis);
        scrollSyn.setBorder(BorderFactory.createLineBorder(UITheme.withAlpha(UITheme.TEXT_MUTED, 100), 1, true));
        scrollSyn.setOpaque(false);
        scrollSyn.getViewport().setOpaque(false);

        // Form Layout
        JPanel topForm = new JPanel(new GridLayout(4, 2, 24, 16));
        topForm.setOpaque(false);
        topForm.add(createFieldWrap("Tên phim:", tfTitle));
        topForm.add(createFieldWrap("Ngày phát hành (YYYY-MM-DD):", tfReleaseDate));
        topForm.add(createFieldWrap("Đạo diễn:", tfDirector));
        topForm.add(createFieldWrap("Thời lượng (phút):", tfDuration));
        topForm.add(createFieldWrap("Thể loại (VD: Hành động, Viễn tưởng):", tfGenre));
        topForm.add(createFieldWrap("Ngôn ngữ (VD: Phụ đề, Lồng tiếng):", tfLanguage));
        topForm.add(createFieldWrap("Diễn viên (cách nhau bởi dấu phẩy):", tfCast));
        topForm.add(createFieldWrap("Độ tuổi (VD: P, K, T13, T16, C18):", tfAgeRating));

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(topForm, BorderLayout.NORTH);
        center.add(createFieldWrap("Tóm tắt nội dung:", scrollSyn), BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        pnlBtns.setOpaque(false);
        CinemaButton btnSave = new CinemaButton("Lưu Phim", CinemaButton.Style.GOLD);
        CinemaButton btnCancel = new CinemaButton("Hủy", CinemaButton.Style.GHOST);
        btnSave.setPreferredSize(new Dimension(140, 42));
        btnCancel.setPreferredSize(new Dimension(100, 42));

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        root.add(pnlBtns, BorderLayout.SOUTH);

        // Actions
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            try {
                cinema.model.Movie m = new cinema.model.Movie();
                m.setTitle(tfTitle.getText().trim());
                m.setDirector(tfDirector.getText().trim());
                String[] cast = tfCast.getText().split(",");
                java.util.List<String> castList = new java.util.ArrayList<>();
                for (String c : cast)
                    if (!c.trim().isEmpty())
                        castList.add(c.trim());
                m.setCastMembers(castList);
                m.setGenre(tfGenre.getText().trim());
                m.setDuration(Integer.parseInt(tfDuration.getText().trim()));
                m.setLanguage(tfLanguage.getText().trim());
                m.setAgeRating(tfAgeRating.getText().trim());
                m.setReleaseDate(java.time.LocalDate.parse(tfReleaseDate.getText().trim()));
                m.setSynopsis(taSynopsis.getText().trim());

                if (movieDAO.addMovie(m)) {
                    frame.showMessage("Thêm phim thành công!", false);
                    dlg.dispose();
                    loadMovies(activeGenre);
                } else {
                    frame.showMessage("Có lỗi xảy ra khi thêm phim.", true);
                }
            } catch (Exception ex) {
                frame.showMessage("Vui lòng kiểm tra lại dữ liệu nhập. " + ex.getMessage(), true);
            }
        });

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel createFieldWrap(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        l.setForeground(UITheme.withAlpha(UITheme.TEXT_SECONDARY, 200));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(40, 10, 80),
                        getWidth(), getHeight(), new Color(10, 30, 80)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Stars
                g2.setColor(new Color(255, 255, 255, 40));
                for (int i = 0; i < 60; i++) {
                    int x = (i * 113 + 7) % getWidth();
                    int y = (i * 97 + 11) % getHeight();
                    g2.fillOval(x, y, i % 3 == 0 ? 2 : 1, i % 3 == 0 ? 2 : 1);
                }
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(0, 140));
        hero.setLayout(new GridBagLayout());

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("Đặt Vé Xem Phim Online");
        h1.setFont(UITheme.FONT_TITLE);
        h1.setForeground(UITheme.ACCENT_GOLD);
        h1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel h2 = new JLabel("Chọn phim, suất chiếu và ghế ngồi yêu thích của bạn!");
        h2.setFont(UITheme.FONT_BODY);
        h2.setForeground(UITheme.TEXT_SECONDARY);
        h2.setAlignmentX(LEFT_ALIGNMENT);

        txt.add(h1);
        txt.add(Box.createVerticalStrut(8));
        txt.add(h2);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 30, 0, 0);
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        hero.add(txt, gc);

        return hero;
    }

    private JPanel buildGenreBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        bar.setOpaque(false);

        JLabel lbl = new JLabel("Thể loại:");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        bar.add(lbl);

        for (String g : GENRES) {
            JButton btn = buildChip(g);
            bar.add(btn);
        }
        return bar;
    }

    private JButton buildChip(String genre) {
        JButton btn = new JButton(genre) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = genre.equals(activeGenre);
                if (active) {
                    g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), getHeight()));
                } else {
                    g2.setColor(UITheme.BG_HOVER);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
                g2.setColor(active ? Color.BLACK : UITheme.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 30));
        btn.addActionListener(e -> {
            activeGenre = genre;
            genreBar.repaint();
            loadMovies(genre);
        });
        return btn;
    }

    public void reloadMovies() {
        loadMovies(activeGenre);
    }

    public void loadMovies(String genre) {
        String keyword = tfSearch != null ? tfSearch.getText().trim() : "";
        List<Movie> movies;
        if (!keyword.isEmpty()) {
            movies = movieDAO.searchByTitle(keyword);
        } else {
            movies = movieDAO.getByGenre(genre);
        }

        movieGrid.removeAll();
        if (movies.isEmpty()) {
            JLabel empty = new JLabel("Không tìm thấy phim nào.");
            empty.setFont(UITheme.FONT_SUBTITLE);
            empty.setForeground(UITheme.TEXT_MUTED);
            movieGrid.add(empty);
        } else {
            for (Movie m : movies) {
                MovieCard card = new MovieCard(m, this::onMovieSelected);
                movieGrid.add(card);
            }
        }
        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private void onMovieSelected(Movie movie) {
        frame.showMovieDetail(movie);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    public void refreshUser() {
        if (lblUser != null) {
            MovieAccount acc = frame.getCurrentAccount();
            if (acc != null) {
                lblUser.setText("Account: " + acc.getUsername() + " | Points: " + acc.getRewardPoints() + " | Rank: "
                        + acc.getMembershipLevel());
                if (btnAddMovie != null) {
                    btnAddMovie.setVisible(acc.isManager());
                }
            } else {
                lblUser.setText("");
                if (btnAddMovie != null) {
                    btnAddMovie.setVisible(false);
                }
            }
        }
    }

    // ── Wrap Layout helper ───────────────────────────────────
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension min = layoutSize(target, false);
            min.width -= (getHgap() + 1);
            return min;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth > 0)
                            rowWidth += hgap;
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }

    // ── Custom dark scrollbar ─────────────────────────────────
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = UITheme.withAlpha(UITheme.ACCENT_GOLD, 100);
            trackColor = UITheme.BG_SECONDARY;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroBtn();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return zeroBtn();
        }

        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
