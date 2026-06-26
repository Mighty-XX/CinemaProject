package cinema.ui;

import cinema.model.*;
import cinema.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Main application frame using CardLayout for panel navigation.
 * All panels register here and navigate via showPanel().
 */
public class CinemaFrame extends JFrame {

    private final CardLayout   cardLayout = new CardLayout();
    private final JPanel       cardPanel  = new JPanel(cardLayout);

    // Panels
    private final LoginPanel        loginPanel;
    private final RegisterPanel     registerPanel;
    private final HomePanel         homePanel;
    private final MovieDetailPanel  detailPanel;
    private final SeatSelectionPanel seatPanel;
    private final CheckoutPanel     checkoutPanel;
    private final ConfirmationPanel confirmPanel;

    // Session
    private MovieAccount currentAccount;

    public CinemaFrame() {
        setTitle("G09 Cinema – Đặt Vé Xem Phim");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));

        // ── Build panels ────────────────────────────────────────
        loginPanel    = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        homePanel     = new HomePanel(this);
        detailPanel   = new MovieDetailPanel(this);
        seatPanel     = new SeatSelectionPanel(this);
        checkoutPanel = new CheckoutPanel(this);
        confirmPanel  = new ConfirmationPanel(this);

        cardPanel.setOpaque(false);
        cardPanel.add(loginPanel,    "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");
        cardPanel.add(homePanel,     "HOME");
        cardPanel.add(detailPanel,   "DETAIL");
        cardPanel.add(seatPanel,     "SEAT");
        cardPanel.add(checkoutPanel, "CHECKOUT");
        cardPanel.add(confirmPanel,  "CONFIRM");

        // Background paint
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(cardPanel, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        // Full synchronized theme update on every component in the frame
        UITheme.addThemeListener(() -> SwingUtilities.invokeLater(() -> {
            cinema.util.ThemeApplicator.apply(getContentPane());
            getContentPane().repaint();
        }));

        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        if ("HOME".equals(name)) {
            homePanel.refreshUser();
            homePanel.reloadMovies();
        }
        cardLayout.show(cardPanel, name);
    }

    public void showMovieDetail(Movie movie) {
        detailPanel.loadMovie(movie);
        showPanel("DETAIL");
    }

    public void showSeatSelection(MovieShow show) {
        seatPanel.loadShow(show);
        showPanel("SEAT");
    }

    public void showCheckout(MovieShow show, List<Seat> seats) {
        checkoutPanel.load(show, seats);
        showPanel("CHECKOUT");
    }

    public void showConfirmation(Bill bill, List<Ticket> tickets,
                                 MovieShow show, Map<Product, Integer> cart) {
        confirmPanel.load(bill, tickets, show, cart);
        showPanel("CONFIRM");
    }

    public void loginAsGuest() {
        currentAccount = null;
        showPanel("HOME");
    }

    // ── Session ─────────────────────────────────────────────────
    public MovieAccount getCurrentAccount()          { return currentAccount; }
    public void setCurrentAccount(MovieAccount acc)  { this.currentAccount = acc; }

    // ── Dialogs ─────────────────────────────────────────────────
    public void showMessage(String message, boolean isError) {
        JDialog dlg = new JDialog(this, "Thông báo", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(420, 240);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(isError ? UITheme.withAlpha(UITheme.ACCENT_PINK, 180) : UITheme.goldGradient(0, 0, getWidth(), 3));
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.setColor(UITheme.withAlpha(isError ? UITheme.ACCENT_PINK : UITheme.ACCENT_GOLD, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new javax.swing.border.EmptyBorder(28, 36, 28, 36));

        String iconStr = isError ? "⚠️" : "ℹ️";
        JLabel iconLbl = new JLabel(iconStr, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setForeground(isError ? UITheme.ACCENT_PINK : UITheme.ACCENT_GOLD);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel(message, SwingConstants.CENTER);
        msgLbl.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 18f));
        msgLbl.setForeground(UITheme.TEXT_PRIMARY);
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        cinema.ui.components.CinemaButton btnOk = new cinema.ui.components.CinemaButton("OK", cinema.ui.components.CinemaButton.Style.GOLD);
        btnOk.setPreferredSize(new Dimension(140, 42));
        btnOk.setMaximumSize(new Dimension(140, 42));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dlg.dispose());

        root.add(iconLbl);
        root.add(Box.createVerticalStrut(16));
        root.add(msgLbl);
        root.add(Box.createVerticalStrut(24));
        root.add(btnOk);

        dlg.setContentPane(root);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setVisible(true);
    }

    public boolean showConfirmDialog(String message) {
        JDialog dlg = new JDialog(this, "Xác nhận", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(420, 240);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), 3));
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new javax.swing.border.EmptyBorder(28, 36, 28, 36));

        JLabel iconLbl = new JLabel("❓", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setForeground(UITheme.ACCENT_GOLD);
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLbl = new JLabel(message, SwingConstants.CENTER);
        msgLbl.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 18f));
        msgLbl.setForeground(UITheme.TEXT_PRIMARY);
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        pnlBtns.setOpaque(false);
        
        cinema.ui.components.CinemaButton btnYes = new cinema.ui.components.CinemaButton("Có", cinema.ui.components.CinemaButton.Style.GOLD);
        cinema.ui.components.CinemaButton btnNo = new cinema.ui.components.CinemaButton("Không", cinema.ui.components.CinemaButton.Style.GHOST);
        btnYes.setPreferredSize(new Dimension(100, 42));
        btnNo.setPreferredSize(new Dimension(100, 42));
        
        boolean[] result = new boolean[]{false};
        
        btnYes.addActionListener(e -> {
            result[0] = true;
            dlg.dispose();
        });
        btnNo.addActionListener(e -> {
            result[0] = false;
            dlg.dispose();
        });
        
        pnlBtns.add(btnNo);
        pnlBtns.add(btnYes);

        root.add(iconLbl);
        root.add(Box.createVerticalStrut(16));
        root.add(msgLbl);
        root.add(Box.createVerticalStrut(24));
        root.add(pnlBtns);

        dlg.setContentPane(root);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setVisible(true);
        
        return result[0];
    }
}
