package cinema.ui;

import cinema.dao.BillDAO;
import cinema.dao.ProductDAO;
import cinema.model.*;
import cinema.ui.components.CinemaButton;
import cinema.ui.components.RoundedPanel;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class CheckoutPanel extends JPanel {

    private final CinemaFrame frame;
    private final ProductDAO productDAO = new ProductDAO();
    private final BillDAO billDAO = new BillDAO();

    private MovieShow show;
    private List<Seat> seats;
    private final Map<Product, Integer> cart = new LinkedHashMap<>();

    private JLabel lblTicketTotal;
    private JLabel lblFoodTotal;
    private JLabel lblGrandTotal;

    private String selectedPayment = "Tiền mặt";
    private final String[] PAYMENT_OPTIONS = { "Tiền mặt", "Thẻ tín dụng", "Ví MoMo", "ZaloPay", "VNPay" };

    public CheckoutPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void load(MovieShow show, List<Seat> seats) {
        this.show = show;
        this.seats = seats;
        this.cart.clear();
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // Header
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(buildBookingSummary());
        left.add(Box.createVerticalStrut(20));
        left.add(buildFoodSection());
        left.add(Box.createVerticalStrut(20));
        left.add(buildPaymentSection());

        JScrollPane scroll = new JScrollPane(left);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new HomePanel.DarkScrollBarUI());
        content.add(scroll, BorderLayout.CENTER);
        content.add(buildOrderPanel(), BorderLayout.EAST);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.withAlpha(UITheme.BG_SECONDARY, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 60));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(12, 20, 12, 20));

        CinemaButton btnBack = new CinemaButton("← Chọn lại ghế", CinemaButton.Style.GHOST);
        btnBack.setPreferredSize(new Dimension(160, 36));
        btnBack.addActionListener(e -> frame.showSeatSelection(show));
        h.add(btnBack, BorderLayout.WEST);

        JLabel title = new JLabel("Thanh Toán");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.ACCENT_GOLD);
        title.setBorder(new EmptyBorder(0, 20, 0, 0));
        h.add(title, BorderLayout.CENTER);
        return h;
    }

    private JPanel buildBookingSummary() {
        JPanel card = makeCard("Thông Tin Đặt Vé");

        String movieTitle = show.getMovie() != null ? show.getMovie().getTitle() : "—";
        String showInfo = show.getShowtime() != null ? show.getShowtime().toString() : "—";
        String roomInfo = show.getRoom() != null ? show.getRoom().getRoomName() : "—";

        addInfoRow(card, "Phim:", movieTitle);
        addInfoRow(card, "Suất chiếu:", showInfo);
        addInfoRow(card, "Phòng:", roomInfo);

        StringBuilder seatList = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            seatList.append(seats.get(i).getLabel());
            if (i < seats.size() - 1)
                seatList.append(", ");
        }
        addInfoRow(card, "Ghế đã chọn:", seatList.toString());

        BigDecimal ticketTotal = SeatSelectionPanel.computeTotal(seats);
        lblTicketTotal = new JLabel(formatVnd(ticketTotal));
        lblTicketTotal.setFont(UITheme.FONT_HEADING);
        lblTicketTotal.setForeground(UITheme.ACCENT_GOLD);
        addLabelRow(card, "Tiền vé:", lblTicketTotal);

        return card;
    }

    private JPanel buildFoodSection() {
        JPanel card = makeCard("Thêm Đồ Ăn & Nước Uống");
        List<Product> products = productDAO.getAll();

        for (Product p : products) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(6, 0, 6, 0));

            JLabel nameLbl = new JLabel(p.getProductName());
            nameLbl.setFont(UITheme.FONT_BODY);
            nameLbl.setForeground(UITheme.TEXT_PRIMARY);

            JLabel priceLbl = new JLabel(formatVnd(p.getUnitPrice()));
            priceLbl.setFont(UITheme.FONT_BODY);
            priceLbl.setForeground(UITheme.ACCENT_CYAN);

            JPanel left2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            left2.setOpaque(false);
            left2.add(nameLbl);
            left2.add(priceLbl);

            // Quantity spinner
            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            qtyPanel.setOpaque(false);
            JButton minus = miniBtn("−");
            JLabel qtyLbl = new JLabel("0");
            qtyLbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
            qtyLbl.setForeground(UITheme.TEXT_PRIMARY);
            qtyLbl.setPreferredSize(new Dimension(24, 20));
            qtyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            JButton plus = miniBtn("+");

            plus.addActionListener(e -> {
                int q = cart.getOrDefault(p, 0) + 1;
                if (q > 9)
                    return;
                cart.put(p, q);
                qtyLbl.setText(String.valueOf(q));
                updateTotals();
            });
            minus.addActionListener(e -> {
                int q = cart.getOrDefault(p, 0);
                if (q <= 0)
                    return;
                q--;
                if (q == 0)
                    cart.remove(p);
                else
                    cart.put(p, q);
                qtyLbl.setText(String.valueOf(q));
                updateTotals();
            });

            qtyPanel.add(minus);
            qtyPanel.add(qtyLbl);
            qtyPanel.add(plus);

            row.add(left2, BorderLayout.CENTER);
            row.add(qtyPanel, BorderLayout.EAST);
            card.add(row);
        }

        lblFoodTotal = new JLabel("0 đ");
        lblFoodTotal.setFont(UITheme.FONT_HEADING);
        lblFoodTotal.setForeground(UITheme.ACCENT_CYAN);
        addLabelRow(card, "Đồ ăn/uống:", lblFoodTotal);

        return card;
    }

    private JPanel buildPaymentSection() {
        JPanel card = makeCard("Phương Thức Thanh Toán");
        ButtonGroup bg = new ButtonGroup();

        for (String opt : PAYMENT_OPTIONS) {
            JRadioButton rb = new JRadioButton(opt);
            rb.setFont(UITheme.FONT_BODY);
            rb.setForeground(UITheme.TEXT_PRIMARY);
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            rb.setSelected(opt.equals(selectedPayment));
            rb.addActionListener(e -> {
                selectedPayment = opt;
            });
            bg.add(rb);
            card.add(rb);
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    private JPanel buildOrderPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(260, 0));

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), 3));
                g2.fillRoundRect(0, 0, getWidth(), 3, 2, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Đơn Hàng");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        lblGrandTotal = new JLabel(formatVnd(SeatSelectionPanel.computeTotal(seats)));
        lblGrandTotal.setFont(UITheme.FONT_TITLE);
        lblGrandTotal.setForeground(UITheme.ACCENT_GOLD);
        lblGrandTotal.setAlignmentX(LEFT_ALIGNMENT);

        JLabel vatNote = new JLabel("* Đã bao gồm VAT");
        vatNote.setFont(UITheme.FONT_SMALL);
        vatNote.setForeground(UITheme.TEXT_MUTED);
        vatNote.setAlignmentX(LEFT_ALIGNMENT);

        CinemaButton btnPay = new CinemaButton("✓  Xác nhận đặt vé", CinemaButton.Style.GOLD);
        btnPay.setPreferredSize(new Dimension(220, 48));
        btnPay.setMaximumSize(new Dimension(220, 48));
        btnPay.setAlignmentX(LEFT_ALIGNMENT);
        btnPay.addActionListener(e -> confirmBooking());

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(new JSeparator() {
            {
                setMaximumSize(new Dimension(220, 1));
                setForeground(UITheme.withAlpha(UITheme.TEXT_MUTED, 60));
            }
        });
        card.add(Box.createVerticalStrut(10));
        card.add(lblGrandTotal);
        card.add(Box.createVerticalStrut(4));
        card.add(vatNote);
        card.add(Box.createVerticalStrut(20));
        card.add(btnPay);

        p.add(card);
        return p;
    }

    // ── Helpers ────────────────────────────────────────────────

    private JPanel makeCard(String sectionTitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hdr = new JLabel(sectionTitle);
        hdr.setFont(UITheme.FONT_HEADING);
        hdr.setForeground(UITheme.ACCENT_GOLD);
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hdr);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    private void addInfoRow(JPanel card, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        l.setForeground(UITheme.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BODY);
        v.setForeground(UITheme.TEXT_PRIMARY);
        row.add(l);
        row.add(v);
        card.add(row);
    }

    private void addLabelRow(JPanel card, String label, JLabel value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        l.setForeground(UITheme.TEXT_SECONDARY);
        row.add(l);
        row.add(value);
        card.add(Box.createVerticalStrut(6));
        card.add(row);
    }

    private JButton miniBtn(String txt) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setFont(UITheme.FONT_BUTTON);
                g2.setColor(UITheme.ACCENT_GOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(28, 28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void updateTotals() {
        BigDecimal food = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> e : cart.entrySet())
            food = food.add(e.getKey().getUnitPrice().multiply(BigDecimal.valueOf(e.getValue())));
        BigDecimal ticket = SeatSelectionPanel.computeTotal(seats);
        lblFoodTotal.setText(formatVnd(food));
        lblGrandTotal.setText(formatVnd(ticket.add(food)));
    }

    private void confirmBooking() {
        BigDecimal ticketTotal = SeatSelectionPanel.computeTotal(seats);
        BigDecimal foodTotal = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> e : cart.entrySet())
            foodTotal = foodTotal.add(e.getKey().getUnitPrice().multiply(BigDecimal.valueOf(e.getValue())));
        final BigDecimal grand = ticketTotal.add(foodTotal);

        // ── Payment simulation dialog ──────────────────────────────
        showPaymentDialog(grand, () -> {
            MovieAccount acc = frame.getCurrentAccount();
            int custId = acc != null ? acc.getCustomerId() : 0;

            Bill bill = new Bill();
            bill.setCustomerId(custId);
            bill.setTotalAmount(grand);
            bill.setDiscountAmount(BigDecimal.ZERO);
            bill.setPaymentType(selectedPayment);

            List<Ticket> tickets = new ArrayList<>();
            for (Seat s : seats) {
                Ticket t = new Ticket(show.getMovieShowId(), s.getSeatId(), 0,
                        SeatSelectionPanel.computeTotal(List.of(s)), "ONLINE");
                t.setSeatLabel(s.getLabel());
                t.setMovieTitle(show.getMovie() != null ? show.getMovie().getTitle() : "");
                tickets.add(t);
            }

            Bill saved = billDAO.saveBillWithTickets(bill, tickets);

            if (acc != null) {
                int pointsEarned = grand.divide(new BigDecimal(10000)).intValue();
                cinema.dao.CustomerDAO dao = new cinema.dao.CustomerDAO();
                dao.updateRewardPoints(acc.getAccountId(), pointsEarned);
                int total = acc.getRewardPoints() + pointsEarned;
                acc.setRewardPoints(total);
                acc.setMembershipLevel(cinema.dao.CustomerDAO.determineLevel(total));
            }

            frame.showConfirmation(saved, tickets, show, cart);
        });
    }

    /**
     * Simulated payment dialog: shows method info → processing animation → done
     * callback.
     */
    private void showPaymentDialog(BigDecimal amount, Runnable onSuccess) {
        // icons mapped per method
        java.util.Map<String, String> icons = new java.util.LinkedHashMap<>();
        icons.put("Tiền mặt", "💵");
        icons.put("Thẻ tín dụng", "💳");
        icons.put("Ví MoMo", "💜");
        icons.put("ZaloPay", "🔵");
        icons.put("VNPay", "🔴");

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Xác nhận thanh toán",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(460, 340);
        dlg.setLocationRelativeTo(this);

        // ── Root panel ───────────────────────────────────────────
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(UITheme.goldGradient(0, 0, getWidth(), 3));
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 36, 28, 36));

        // ── PHASE 1: Confirm ─────────────────────────────────────
        JPanel confirmPhase = new JPanel();
        confirmPhase.setOpaque(false);
        confirmPhase.setLayout(new BoxLayout(confirmPhase, BoxLayout.Y_AXIS));

        String ico = icons.getOrDefault(selectedPayment, "💳");
        JLabel methodIcon = new JLabel(ico, SwingConstants.CENTER);
        methodIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        methodIcon.setForeground(UITheme.ACCENT_GOLD);
        methodIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel methodName = new JLabel(selectedPayment, SwingConstants.CENTER);
        methodName.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 22f));
        methodName.setForeground(UITheme.TEXT_PRIMARY);
        methodName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel amountLbl = new JLabel(String.format("%,.0f đ", amount), SwingConstants.CENTER);
        amountLbl.setFont(UITheme.FONT_TITLE.deriveFont(Font.BOLD, 28f));
        amountLbl.setForeground(UITheme.ACCENT_GOLD);
        amountLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Xác nhận để tiến hành thanh toán", SwingConstants.CENTER);
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(CENTER_ALIGNMENT);

        CinemaButton btnCancel = new CinemaButton("Hủy", CinemaButton.Style.GHOST);
        btnCancel.setPreferredSize(new Dimension(120, 42));
        btnCancel.addActionListener(e -> dlg.dispose());

        CinemaButton btnPay = new CinemaButton("Thanh toán ngay", CinemaButton.Style.GOLD);
        btnPay.setPreferredSize(new Dimension(180, 42));

        btnRow.add(btnCancel);
        btnRow.add(btnPay);

        confirmPhase.add(methodIcon);
        confirmPhase.add(Box.createVerticalStrut(10));
        confirmPhase.add(methodName);
        confirmPhase.add(Box.createVerticalStrut(6));
        confirmPhase.add(amountLbl);
        confirmPhase.add(Box.createVerticalStrut(12));
        confirmPhase.add(hint);
        confirmPhase.add(Box.createVerticalStrut(20));
        confirmPhase.add(btnRow);
        root.add(confirmPhase);

        // ── PHASE 2: Processing ───────────────────────────────────
        JPanel processingPhase = new JPanel();
        processingPhase.setOpaque(false);
        processingPhase.setLayout(new BoxLayout(processingPhase, BoxLayout.Y_AXIS));

        JLabel spinnerLbl = new JLabel("⏳", SwingConstants.CENTER);
        spinnerLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        spinnerLbl.setForeground(UITheme.ACCENT_GOLD);
        spinnerLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel procLbl = new JLabel("Đang xử lý giao dịch...", SwingConstants.CENTER);
        procLbl.setFont(UITheme.FONT_HEADING.deriveFont(Font.BOLD, 18f));
        procLbl.setForeground(UITheme.ACCENT_GOLD);
        procLbl.setAlignmentX(CENTER_ALIGNMENT);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setForeground(UITheme.ACCENT_GOLD);
        bar.setBackground(UITheme.BG_HOVER);
        bar.setBorderPainted(false);
        bar.setMaximumSize(new Dimension(320, 8));
        bar.setAlignmentX(CENTER_ALIGNMENT);

        processingPhase.add(Box.createVerticalStrut(30));
        processingPhase.add(spinnerLbl);
        processingPhase.add(Box.createVerticalStrut(14));
        processingPhase.add(procLbl);
        processingPhase.add(Box.createVerticalStrut(20));
        processingPhase.add(bar);

        // ── Switch phases on pay click ────────────────────────────
        btnPay.addActionListener(e -> {
            root.remove(confirmPhase);
            root.add(processingPhase);
            root.revalidate();
            root.repaint();

            javax.swing.Timer timer = new javax.swing.Timer(30, null);
            int[] progress = { 0 };
            timer.addActionListener(ae -> {
                progress[0] += 2;
                bar.setValue(progress[0]);
                if (progress[0] >= 100) {
                    timer.stop();
                    dlg.dispose();
                    onSuccess.run();
                }
            });
            timer.start();
        });

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private String formatVnd(BigDecimal v) {
        return String.format("%,.0f đ", v);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(UITheme.bgGradient(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
