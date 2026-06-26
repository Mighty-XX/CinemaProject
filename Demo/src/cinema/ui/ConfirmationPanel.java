package cinema.ui;

import cinema.model.*;
import cinema.ui.components.CinemaButton;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Booking confirmation screen shown after successful payment.
 */
public class ConfirmationPanel extends JPanel {

    private final CinemaFrame frame;

    private Bill            bill;
    private List<Ticket>    tickets;
    private MovieShow       show;
    private Map<Product, Integer> cart;

    public ConfirmationPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void load(Bill bill, List<Ticket> tickets, MovieShow show, Map<Product, Integer> cart) {
        this.bill    = bill;
        this.tickets = tickets;
        this.show    = show;
        this.cart    = cart;
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // Centre the card
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        centre.add(buildCard());
        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // top accent bar
                g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), 4));
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                // border
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 60));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 44, 32, 44));
        card.setPreferredSize(new Dimension(560, 640));

        // ── Success icon ─────────────────────────────────────────
        JLabel icon = new JLabel("✅", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        icon.setForeground(new Color(72, 220, 120));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Đặt Vé Thành Công!", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 26f));
        title.setForeground(new Color(72, 220, 120));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel billId = new JLabel("Mã đơn: #" + (bill != null ? bill.getBillId() : "—"), SwingConstants.CENTER);
        billId.setFont(UITheme.FONT_BODY.deriveFont(Font.ITALIC));
        billId.setForeground(UITheme.TEXT_MUTED);
        billId.setAlignmentX(CENTER_ALIGNMENT);

        // ── Divider ──────────────────────────────────────────────
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(UITheme.withAlpha(UITheme.ACCENT_GOLD, 50));
        sep1.setMaximumSize(new Dimension(480, 1));

        // ── Info rows ────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentX(CENTER_ALIGNMENT);

        String movieTitle = show != null && show.getMovie() != null ? show.getMovie().getTitle() : "—";
        addRow(info, "Phim",           movieTitle,                               UITheme.ACCENT_GOLD);

        if (show != null && show.getShowtime() != null) {
            String showtime = show.getShowtime().getShowDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + "  |  " + show.getShowtime().getStartTime()
                    + " → " + show.getShowtime().getEndTime();
            addRow(info, "Suất chiếu", showtime, UITheme.TEXT_PRIMARY);
        }

        if (show != null && show.getRoom() != null)
            addRow(info, "Phòng chiếu", show.getRoom().getRoomName(), UITheme.TEXT_PRIMARY);

        if (tickets != null && !tickets.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tickets.size(); i++) {
                sb.append(tickets.get(i).getSeatLabel());
                if (i < tickets.size() - 1) sb.append(", ");
            }
            addRow(info, "Ghế", sb.toString(), UITheme.ACCENT_CYAN);
        }

        if (bill != null)
            addRow(info, "Thanh toán", bill.getPaymentType(), UITheme.TEXT_PRIMARY);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy"));
        addRow(info, "Thời gian đặt", ts, UITheme.TEXT_MUTED);

        // ── Divider ──────────────────────────────────────────────
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(UITheme.withAlpha(UITheme.ACCENT_GOLD, 50));
        sep2.setMaximumSize(new Dimension(480, 1));

        // ── Total ────────────────────────────────────────────────
        JPanel totalRow = new JPanel(new BorderLayout(0, 0));
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(CENTER_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(480, 50));

        JLabel totalLbl = new JLabel("TỔNG TIỀN");
        totalLbl.setFont(UITheme.FONT_HEADING.deriveFont(Font.BOLD, 15f));
        totalLbl.setForeground(UITheme.TEXT_SECONDARY);

        BigDecimal amt = bill != null ? bill.getTotalAmount() : BigDecimal.ZERO;
        JLabel totalVal = new JLabel(String.format("%,.0f đ", amt));
        totalVal.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 30f));
        totalVal.setForeground(UITheme.ACCENT_GOLD);
        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalVal, BorderLayout.EAST);

        // ── Barcode ──────────────────────────────────────────────
        JPanel barcode = buildBarcode(bill != null ? bill.getBillId() : 0);
        barcode.setAlignmentX(CENTER_ALIGNMENT);

        // ── Buttons ──────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        CinemaButton btnHome = new CinemaButton("Về trang chủ", CinemaButton.Style.GOLD);
        btnHome.setPreferredSize(new Dimension(170, 44));
        btnHome.addActionListener(e -> frame.showPanel("HOME"));

        CinemaButton btnNew = new CinemaButton("Đặt vé mới", CinemaButton.Style.GHOST);
        btnNew.setPreferredSize(new Dimension(150, 44));
        btnNew.addActionListener(e -> frame.showPanel("HOME"));

        btnRow.add(btnHome);
        btnRow.add(btnNew);

        // ── Assemble ─────────────────────────────────────────────
        card.add(icon);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(billId);
        card.add(Box.createVerticalStrut(20));
        card.add(sep1);
        card.add(Box.createVerticalStrut(16));
        card.add(info);
        card.add(Box.createVerticalStrut(16));
        card.add(sep2);
        card.add(Box.createVerticalStrut(16));
        card.add(totalRow);
        card.add(Box.createVerticalStrut(20));
        card.add(barcode);
        card.add(Box.createVerticalStrut(24));
        card.add(btnRow);

        return card;
    }

    // ── Info row ──────────────────────────────────────────────────
    private void addRow(JPanel p, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(480, 32));

        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY.deriveFont(14f));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setPreferredSize(new Dimension(120, 24));

        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, 14f));
        v.setForeground(valueColor);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        p.add(row);
        p.add(Box.createVerticalStrut(8));
    }

    // ── Fake barcode ──────────────────────────────────────────────
    private JPanel buildBarcode(int id) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int x = 10;
                boolean[] pattern = generatePattern(id);
                for (boolean wide : pattern) {
                    int w = wide ? 4 : 2;
                    g2.setColor(UITheme.TEXT_PRIMARY);
                    g2.fillRect(x, 4, w, 42);
                    x += w + 1;
                }
                g2.setFont(new Font("Courier New", Font.PLAIN, 11));
                g2.setColor(UITheme.TEXT_MUTED);
                String txt = String.format("%010d", id);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, 56);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(320, 64); }
        };
    }

    private boolean[] generatePattern(int id) {
        boolean[] p = new boolean[65];
        int v = id;
        for (int i = 0; i < p.length; i++) {
            p[i] = (v & 1) == 1;
            v = (v >> 1) ^ (id * 31 + i * 17);
        }
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(UITheme.withAlpha(new Color(72, 220, 120), 12));
        g2.fillOval(-80, -80, 400, 400);
        g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 10));
        g2.fillOval(getWidth() - 280, getHeight() - 280, 500, 500);
        g2.dispose();
    }
}
