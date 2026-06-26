package cinema.ui;

import cinema.dao.CustomerDAO;
import cinema.model.MovieAccount;
import cinema.ui.components.CinemaButton;
import cinema.ui.components.CinemaField;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    private final CinemaFrame frame;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private CinemaField  tfUser;
    private JPasswordField tfPass;
    private JLabel       lblMsg;

    public LoginPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setBackground(UITheme.withAlpha(UITheme.BG_CARD, 220));
        card.setPreferredSize(new Dimension(420, 500));

        // ── Logo / Title ──────────────────────────────────────
        JLabel logo = new JLabel(" ", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("G09 Cinema", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Đăng nhập để tiếp tục", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        // ── Fields ────────────────────────────────────────────
        tfUser = new CinemaField("");
        tfUser.setMaximumSize(new Dimension(320, 42));
        tfUser.setAlignmentX(CENTER_ALIGNMENT);

        tfPass = new JPasswordField() {
            {
                UITheme.addThemeListener(() -> {
                    setForeground(UITheme.TEXT_PRIMARY);
                    setCaretColor(UITheme.ACCENT_GOLD);
                    repaint();
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.ARC, UITheme.ARC);
                g2.setColor(hasFocus()
                        ? UITheme.accentGold()
                        : UITheme.withAlpha(UITheme.TEXT_MUTED, 120));
                g2.setStroke(new BasicStroke(hasFocus() ? 1.8f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, UITheme.ARC, UITheme.ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tfPass.setFont(UITheme.FONT_BODY);
        tfPass.setForeground(UITheme.TEXT_PRIMARY);
        tfPass.setCaretColor(UITheme.ACCENT_GOLD);
        tfPass.setOpaque(false);
        tfPass.setBorder(new EmptyBorder(10, 14, 10, 14));
        tfPass.setMaximumSize(new Dimension(320, 42));
        tfPass.setAlignmentX(CENTER_ALIGNMENT);

        lblMsg = new JLabel(" ", SwingConstants.CENTER);
        lblMsg.setFont(UITheme.FONT_SMALL);
        lblMsg.setForeground(UITheme.ACCENT_PINK);
        lblMsg.setAlignmentX(CENTER_ALIGNMENT);

        // ── Buttons ───────────────────────────────────────────
        CinemaButton btnLogin = new CinemaButton("Đăng nhập", CinemaButton.Style.GOLD);
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.setMaximumSize(new Dimension(320, 44));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());

        CinemaButton btnReg = new CinemaButton("Tạo tài khoản mới", CinemaButton.Style.GHOST);
        btnReg.setPreferredSize(new Dimension(320, 44));
        btnReg.setMaximumSize(new Dimension(320, 44));
        btnReg.setAlignmentX(CENTER_ALIGNMENT);
        btnReg.addActionListener(e -> frame.showPanel("REGISTER"));

        // Guest
        JLabel guest = new JLabel("<html><u>Tiếp tục không đăng nhập</u></html>", SwingConstants.CENTER);
        guest.setFont(UITheme.FONT_SMALL);
        guest.setForeground(UITheme.TEXT_MUTED);
        guest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        guest.setAlignmentX(CENTER_ALIGNMENT);
        guest.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { frame.loginAsGuest(); }
        });

        // Enter key on password
        tfPass.addActionListener(e -> doLogin());

        // ── Assemble ──────────────────────────────────────────
        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(label("Tên đăng nhập"));
        card.add(Box.createVerticalStrut(6));
        card.add(tfUser);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Mật khẩu"));
        card.add(Box.createVerticalStrut(6));
        card.add(tfPass);
        card.add(Box.createVerticalStrut(6));
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(12));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnReg);
        card.add(Box.createVerticalStrut(16));
        card.add(guest);

        // Rounded card background
        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.withAlpha(UITheme.BG_CARD, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(card);

        add(wrapper, new GridBagConstraints());
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
        l.setForeground(UITheme.TEXT_SECONDARY);
        l.setMaximumSize(new Dimension(320, 20));
        l.setPreferredSize(new Dimension(320, 20));
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private void doLogin() {
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblMsg.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        MovieAccount acc = customerDAO.login(user, pass);
        if (acc != null) {
            frame.setCurrentAccount(acc);
            frame.showPanel("HOME");
        } else {
            lblMsg.setText("Sai tên đăng nhập hoặc mật khẩu!");
            tfPass.setText("");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Stars
        g2.setColor(UITheme.withAlpha(UITheme.TEXT_PRIMARY, 15));
        for (int i = 0; i < 80; i++) {
            int x = (i * 113 + 7) % getWidth();
            int y = (i * 97 + 11) % getHeight();
            int s = i % 3 == 0 ? 3 : 2;
            g2.fillOval(x, y, s, s);
        }

        // Film strips
        drawFilmStrip(g2, getWidth() / 4, getHeight() / 4, -25);
        drawFilmStrip(g2, getWidth() * 3 / 4, getHeight() * 3 / 4, 15);

        // Decorative circles
        g2.setColor(UITheme.withAlpha(UITheme.ACCENT_PURPLE, 30));
        g2.fillOval(-100, -100, 400, 400);
        g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 20));
        g2.fillOval(getWidth() - 200, getHeight() - 200, 400, 400);
        g2.dispose();
    }

    private void drawFilmStrip(Graphics2D g2, int cx, int cy, double angle) {
        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);
        g2.rotate(Math.toRadians(angle));
        
        int w = 800;
        int h = 140;
        int x = -w / 2;
        int y = -h / 2;
        
        g2.setColor(UITheme.withAlpha(UITheme.TEXT_PRIMARY, 6));
        g2.fillRoundRect(x, y, w, h, 16, 16);
        
        g2.setColor(UITheme.withAlpha(UITheme.TEXT_PRIMARY, 12));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, w, h, 16, 16);
        
        for(int i = 15; i < w - 25; i += 40) {
            g2.drawRoundRect(x + i, y + 12, 20, 16, 4, 4);
            g2.drawRoundRect(x + i, y + h - 28, 20, 16, 4, 4);
        }
        g2.setTransform(old);
    }
}
