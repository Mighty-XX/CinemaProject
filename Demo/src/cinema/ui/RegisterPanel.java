package cinema.ui;

import cinema.dao.CustomerDAO;
import cinema.ui.components.CinemaButton;
import cinema.ui.components.CinemaField;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RegisterPanel extends JPanel {

    private final CinemaFrame frame;
    private final CustomerDAO dao = new CustomerDAO();

    private CinemaField tfFullName, tfDob, tfPhone, tfUser;
    private JPasswordField tfPass, tfConfirm;
    private JLabel lblMsg;

    public RegisterPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Tạo Tài Khoản", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Đăng ký để đặt vé dễ dàng hơn", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        tfFullName = makeField("Họ và tên *");
        tfDob = makeField("Ngày sinh (yyyy-MM-dd)");
        tfPhone = makeField("Số điện thoại");
        tfUser = makeField("Tên đăng nhập *");
        tfPass = makePassField("Mật khẩu *");
        tfConfirm = makePassField("Xác nhận mật khẩu *");

        lblMsg = new JLabel(" ", SwingConstants.CENTER);
        lblMsg.setFont(UITheme.FONT_SMALL);
        lblMsg.setForeground(UITheme.ACCENT_PINK);
        lblMsg.setAlignmentX(CENTER_ALIGNMENT);

        CinemaButton btnReg = new CinemaButton("Đăng ký", CinemaButton.Style.GOLD);
        btnReg.setPreferredSize(new Dimension(320, 44));
        btnReg.setMaximumSize(new Dimension(320, 44));
        btnReg.setAlignmentX(CENTER_ALIGNMENT);
        btnReg.addActionListener(e -> doRegister());

        CinemaButton btnBack = new CinemaButton("Quay lại đăng nhập", CinemaButton.Style.GHOST);
        btnBack.setPreferredSize(new Dimension(320, 44));
        btnBack.setMaximumSize(new Dimension(320, 44));
        btnBack.setAlignmentX(CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> frame.showPanel("LOGIN"));

        for (JComponent c : new JComponent[] {
                title, sub,
                label("Họ và tên"), tfFullName,
                label("Ngày sinh"), tfDob,
                label("Điện thoại"), tfPhone,
                label("Tên đăng nhập"), tfUser,
                label("Mật khẩu"), tfPass,
                label("Xác nhận"), tfConfirm,
                lblMsg, btnReg, btnBack }) {
            card.add(c instanceof JLabel && ((JLabel) c).getText().equals(" ") ? c : c);
            card.add(Box.createVerticalStrut(6));
        }

        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.withAlpha(UITheme.BG_CARD, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_PURPLE, 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
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

    private CinemaField makeField(String ph) {
        CinemaField f = new CinemaField(ph);
        f.setMaximumSize(new Dimension(320, 42));
        f.setAlignmentX(CENTER_ALIGNMENT);
        return f;
    }

    private JPasswordField makePassField(String ph) {
        JPasswordField f = new JPasswordField();
        f.setFont(UITheme.FONT_BODY);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setCaretColor(UITheme.ACCENT_GOLD);
        f.setBackground(UITheme.BG_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.withAlpha(UITheme.TEXT_MUTED, 120)),
                new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(320, 42));
        f.setAlignmentX(CENTER_ALIGNMENT);
        return f;
    }

    private void doRegister() {
        String name = tfFullName.getText().trim();
        String user = tfUser.getText().trim();
        String phone = tfPhone.getText().trim();
        String dobS = tfDob.getText().trim();
        String pass = new String(tfPass.getPassword());
        String conf = new String(tfConfirm.getPassword());

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            lblMsg.setText("Vui lòng điền các trường bắt buộc (*).");
            return;
        }
        if (!pass.equals(conf)) {
            lblMsg.setText("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (pass.length() < 4) {
            lblMsg.setText("Mật khẩu phải có ít nhất 4 ký tự.");
            return;
        }
        if (dao.isUsernameTaken(user)) {
            lblMsg.setText("Tên đăng nhập đã tồn tại.");
            return;
        }

        LocalDate dob = null;
        if (!dobS.isEmpty()) {
            try {
                dob = LocalDate.parse(dobS);
            } catch (DateTimeParseException ex) {
                lblMsg.setText("Định dạng ngày sinh không hợp lệ (yyyy-MM-dd).");
                return;
            }
        }

        boolean ok = dao.register(name, dob, phone, user, pass);
        if (ok) {
            frame.showMessage("Đăng ký thành công! Hãy đăng nhập.", false);
            frame.showPanel("LOGIN");
        } else {
            lblMsg.setText("Đăng ký thất bại. Thử lại.");
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

        g2.setColor(UITheme.withAlpha(UITheme.ACCENT_PURPLE, 25));
        g2.fillOval(getWidth() - 300, -100, 500, 500);
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
