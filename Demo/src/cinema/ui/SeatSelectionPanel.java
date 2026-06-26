package cinema.ui;

import cinema.dao.SeatDAO;
import cinema.model.*;
import cinema.ui.components.CinemaButton;
import cinema.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionPanel extends JPanel {

    private final CinemaFrame frame;
    private final SeatDAO     seatDAO = new SeatDAO();

    private MovieShow     currentShow;
    private List<Seat>    allSeats;
    private final List<Seat> selectedSeats = new ArrayList<>();

    private JPanel  seatMapPanel;
    private JLabel  lblSelected;
    private JLabel  lblTotal;

    private static final BigDecimal PRICE_STANDARD = new BigDecimal("85000");
    private static final BigDecimal PRICE_VIP       = new BigDecimal("130000");
    private static final BigDecimal PRICE_COUPLE    = new BigDecimal("200000");

    public SeatSelectionPanel(CinemaFrame frame) {
        this.frame = frame;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    public void loadShow(MovieShow show) {
        this.currentShow = show;
        this.selectedSeats.clear();
        allSeats = seatDAO.getSeatsForShow(show.getRoomId(), show.getMovieShowId());
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // ── Header ─────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Main content ───────────────────────────────────────
        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Seat map (center)
        seatMapPanel = buildSeatMap();
        JScrollPane mapScroll = new JScrollPane(seatMapPanel);
        mapScroll.setOpaque(false);
        mapScroll.getViewport().setOpaque(false);
        mapScroll.setBorder(null);
        main.add(mapScroll, BorderLayout.CENTER);

        // Right panel: legend + summary
        JPanel right = buildRightPanel();
        main.add(right, BorderLayout.EAST);

        add(main, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.withAlpha(UITheme.BG_SECONDARY, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 60));
                g2.fillRect(0, getHeight()-2, getWidth(), 2);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(12, 20, 12, 20));

        CinemaButton btnBack = new CinemaButton("← Quay lại", CinemaButton.Style.GHOST);
        btnBack.setPreferredSize(new Dimension(130, 36));
        btnBack.addActionListener(e -> frame.showMovieDetail(currentShow.getMovie()));
        h.add(btnBack, BorderLayout.WEST);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;

        JLabel title = new JLabel(currentShow.getMovie() != null ?
                currentShow.getMovie().getTitle() : "Chọn Ghế");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.ACCENT_GOLD);
        info.add(title, gc);

        gc.gridy = 1;
        String showInfo = currentShow.getShowtime() != null ?
                currentShow.getShowtime().toString() : "";
        String roomInfo = currentShow.getRoom() != null ?
                " | " + currentShow.getRoom().getRoomName() : "";
        JLabel sub = new JLabel(showInfo + roomInfo);
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        info.add(sub, gc);

        h.add(info, BorderLayout.CENTER);
        return h;
    }

    private JPanel buildSeatMap() {
        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Screen
        JPanel screen = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(200, 200, 255, 80),
                        getWidth(), getHeight(), new Color(100, 100, 200, 20)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
                g2.setColor(UITheme.withAlpha(UITheme.TEXT_SECONDARY, 180));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "— MÀN HÌNH —";
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                        (getHeight()+fm.getAscent())/2 - 2);
                g2.dispose();
            }
        };
        screen.setPreferredSize(new Dimension(580, 32));
        screen.setMaximumSize(new Dimension(2000, 32));
        screen.setOpaque(false);
        screen.setAlignmentX(CENTER_ALIGNMENT);

        outer.add(screen);
        outer.add(Box.createVerticalStrut(24));

        // Group seats by row
        String currentRow = null;
        JPanel rowPanel = null;

        for (Seat seat : allSeats) {
            if (!seat.getSeatRow().equals(currentRow)) {
                currentRow = seat.getSeatRow();
                rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
                rowPanel.setOpaque(false);
                rowPanel.setAlignmentX(CENTER_ALIGNMENT);

                // Row label
                JLabel rowLbl = new JLabel(currentRow);
                rowLbl.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));
                rowLbl.setForeground(UITheme.TEXT_MUTED);
                rowLbl.setPreferredSize(new Dimension(20, 36));
                rowPanel.add(rowLbl);

                outer.add(rowPanel);
                outer.add(Box.createVerticalStrut(4));
            }
            if (rowPanel != null) rowPanel.add(buildSeatButton(seat));
        }

        return outer;
    }

    private JButton buildSeatButton(Seat seat) {
        JButton btn = new JButton(String.valueOf(seat.getSeatNumber())) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg;
                if (seat.isBooked()) {
                    bg = UITheme.SEAT_BOOKED;
                } else if (selectedSeats.contains(seat)) {
                    bg = UITheme.SEAT_SELECTED;
                } else if ("VIP".equals(seat.getSeatType())) {
                    bg = UITheme.withAlpha(UITheme.SEAT_VIP, 200);
                } else if ("Couple".equals(seat.getSeatType())) {
                    bg = UITheme.withAlpha(UITheme.SEAT_COUPLE, 200);
                } else {
                    bg = UITheme.withAlpha(UITheme.SEAT_AVAILABLE, 190);
                }

                // Chair shape
                g2.setColor(bg);
                g2.fillRoundRect(2, 6, getWidth()-4, getHeight()-10, 6, 6);
                // Headrest
                g2.fillRoundRect(4, 2, getWidth()-8, 8, 4, 4);

                // Number
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(seat.isBooked() ? new Color(255,255,255,120) : Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                        getHeight()-fm.getDescent()-4);

                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(34, 34));

        if (!seat.isBooked()) {
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> toggleSeat(seat, btn));
        } else {
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            btn.setToolTipText("Ghế đã đặt");
        }
        return btn;
    }

    private void toggleSeat(Seat seat, JButton btn) {
        List<Seat> targetSeats = new ArrayList<>();
        targetSeats.add(seat);

        if ("Couple".equals(seat.getSeatType())) {
            int col = seat.getSeatNumber();
            int partnerCol = (col % 2 != 0) ? col + 1 : col - 1;
            for (Seat s : allSeats) {
                if (s.getSeatRow().equals(seat.getSeatRow()) && s.getSeatNumber() == partnerCol) {
                    targetSeats.add(s);
                    break;
                }
            }
        }

        boolean isSelecting = !selectedSeats.contains(seat);

        if (isSelecting) {
            if (selectedSeats.size() + targetSeats.size() > 8) {
                frame.showMessage("Chỉ được chọn tối đa 8 ghế.", true);
                return;
            }
            for (Seat s : targetSeats) {
                if (s.isBooked()) {
                    frame.showMessage("Ghế đôi đi kèm đã bị đặt.", true);
                    return;
                }
            }
            for (Seat s : targetSeats) {
                if (!selectedSeats.contains(s)) selectedSeats.add(s);
            }
        } else {
            selectedSeats.removeAll(targetSeats);
        }

        if (seatMapPanel != null) seatMapPanel.repaint();
        updateSummary();
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(240, 0));

        // Legend
        JPanel legend = buildLegend();
        p.add(legend);
        p.add(Box.createVerticalStrut(20));

        // Summary card
        JPanel summaryCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(UITheme.withAlpha(UITheme.ACCENT_GOLD, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        summaryCard.setOpaque(false);
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sumTitle = new JLabel("Ghế đã chọn");
        sumTitle.setFont(UITheme.FONT_HEADING);
        sumTitle.setForeground(UITheme.ACCENT_GOLD);
        sumTitle.setAlignmentX(LEFT_ALIGNMENT);

        lblSelected = new JLabel("Chưa chọn ghế");
        lblSelected.setFont(UITheme.FONT_BODY);
        lblSelected.setForeground(UITheme.TEXT_SECONDARY);
        lblSelected.setAlignmentX(LEFT_ALIGNMENT);

        lblTotal = new JLabel("Tổng: 0 đ");
        lblTotal.setFont(UITheme.FONT_SUBTITLE);
        lblTotal.setForeground(UITheme.ACCENT_GOLD);
        lblTotal.setAlignmentX(LEFT_ALIGNMENT);

        CinemaButton btnNext = new CinemaButton("Tiếp tục →", CinemaButton.Style.GOLD);
        btnNext.setPreferredSize(new Dimension(200, 44));
        btnNext.setMaximumSize(new Dimension(200, 44));
        btnNext.setAlignmentX(LEFT_ALIGNMENT);
        btnNext.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                frame.showMessage("Vui lòng chọn ít nhất 1 ghế.", true);
            } else {
                frame.showCheckout(currentShow, new ArrayList<>(selectedSeats));
            }
        });

        summaryCard.add(sumTitle);
        summaryCard.add(Box.createVerticalStrut(10));
        summaryCard.add(lblSelected);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(new JSeparator() {{
            setMaximumSize(new Dimension(200, 1));
            setForeground(UITheme.withAlpha(UITheme.TEXT_MUTED, 80));
        }});
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(lblTotal);
        summaryCard.add(Box.createVerticalStrut(14));
        summaryCard.add(btnNext);

        p.add(summaryCard);
        return p;
    }

    private JPanel buildLegend() {
        JPanel lp = new JPanel();
        lp.setOpaque(false);
        lp.setLayout(new BoxLayout(lp, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Chú thích");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(UITheme.TEXT_SECONDARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        lp.add(title);
        lp.add(Box.createVerticalStrut(8));

        addLegendRow(lp, UITheme.SEAT_AVAILABLE, "Ghế trống (Standard)  85.000đ");
        addLegendRow(lp, UITheme.SEAT_VIP,       "VIP                  130.000đ");
        addLegendRow(lp, UITheme.SEAT_COUPLE,    "Sweet Box            200.000đ");
        addLegendRow(lp, UITheme.SEAT_SELECTED,  "Đang chọn");
        addLegendRow(lp, UITheme.SEAT_BOOKED,    "Đã đặt");

        return lp;
    }

    private void addLegendRow(JPanel p, Color c, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, 14, 14, 4, 4);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        row.add(dot);
        row.add(lbl);
        p.add(row);
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            lblSelected.setText("Chưa chọn ghế");
            lblTotal.setText("Tổng: 0 đ");
            return;
        }
        StringBuilder sb = new StringBuilder("<html>");
        BigDecimal total = BigDecimal.ZERO;
        for (Seat s : selectedSeats) {
            BigDecimal price = getPrice(s);
            total = total.add(price);
            sb.append(s.getLabel()).append(" (").append(s.getSeatType()).append(")<br>");
        }
        sb.append("</html>");
        lblSelected.setText(sb.toString());
        lblTotal.setText("Tổng: " + formatVnd(total));
        lblSelected.repaint();
        lblTotal.repaint();
    }

    private BigDecimal getPrice(Seat s) {
        return switch (s.getSeatType()) {
            case "VIP"    -> PRICE_VIP;
            case "Couple" -> PRICE_COUPLE;
            default       -> PRICE_STANDARD;
        };
    }

    public static BigDecimal computeTotal(List<Seat> seats) {
        BigDecimal total = BigDecimal.ZERO;
        for (Seat s : seats) {
            total = total.add(switch (s.getSeatType()) {
                case "VIP"    -> new BigDecimal("130000");
                case "Couple" -> new BigDecimal("200000");
                default       -> new BigDecimal("85000");
            });
        }
        return total;
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
