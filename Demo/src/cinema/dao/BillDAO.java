package cinema.dao;

import cinema.model.Bill;
import cinema.model.Ticket;
import cinema.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class BillDAO {

    private static int nextBillId   = 1000;
    private static int nextTicketId = 5000;

    /**
     * Persists bill + tickets in a single transaction.
     * Falls back to in-memory IDs when no DB.
     */
    public Bill saveBillWithTickets(Bill bill, List<Ticket> tickets) {
        Connection conn = DBConnection.get();
        if (conn == null) return saveDemo(bill, tickets);

        try {
            conn.setAutoCommit(false);

            // 1. Bill
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Bill(customerid,staffid,discountamount,totalamount)" +
                    " VALUES(?,NULL,?,?) RETURNING billid")) {
                ps.setInt(1, bill.getCustomerId());
                ps.setBigDecimal(2, bill.getDiscountAmount());
                ps.setBigDecimal(3, bill.getTotalAmount());
                ResultSet rs = ps.executeQuery();
                rs.next();
                bill.setBillId(rs.getInt(1));
            }

            // 2. Payment
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Payment(billid,paymenttype) VALUES(?,?)")) {
                ps.setInt(1, bill.getBillId());
                ps.setString(2, bill.getPaymentType());
                ps.executeUpdate();
            }

            // 3. Tickets
            for (Ticket t : tickets) {
                t.setBillId(bill.getBillId());
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Ticket(movieshowid,seatid,billid,ticketprice,status,bookingchannel)" +
                        " VALUES(?,?,?,?,'BOOKED','ONLINE') RETURNING ticketid")) {
                    ps.setInt(1, t.getMovieShowId());
                    ps.setInt(2, t.getSeatId());
                    ps.setInt(3, t.getBillId());
                    ps.setBigDecimal(4, t.getTicketPrice());
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    t.setTicketId(rs.getInt(1));
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.err.println("[BillDAO] " + e.getMessage());
            return saveDemo(bill, tickets);
        }
        return bill;
    }

    private Bill saveDemo(Bill bill, List<Ticket> tickets) {
        bill.setBillId(++nextBillId);
        for (Ticket t : tickets) {
            t.setBillId(bill.getBillId());
            t.setTicketId(++nextTicketId);
        }
        System.out.println("[BillDAO] Saved demo bill #" + bill.getBillId());
        return bill;
    }
}
