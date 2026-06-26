package cinema.dao;

import cinema.model.Seat;
import cinema.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatDAO {

    /**
     * Returns all seats for a room with 'booked' flag set based on
     * existing tickets for the given movieshow.
     */
    public List<Seat> getSeatsForShow(int roomId, int movieShowId) {
        Connection conn = DBConnection.get();
        if (conn == null) return buildDemoSeats(roomId, movieShowId);

        // Get booked seat IDs
        Set<Integer> bookedIds = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT seatid FROM Ticket WHERE movieshowid=? AND status='BOOKED'")) {
            ps.setInt(1, movieShowId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookedIds.add(rs.getInt(1));
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        List<Seat> seats = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Seat WHERE roomid=? ORDER BY seatrow, seatnumber")) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String row = rs.getString("seatrow").trim();
                String type = "Standard";
                if (row.equals("F") || row.equals("H") || row.equals("I") || row.equals("J")) type = "Couple";
                else if (row.compareTo("C") > 0 && row.compareTo("H") < 0) type = "VIP";
                
                Seat s = new Seat(rs.getInt("seatid"), roomId,
                        row, rs.getInt("seatnumber"),
                        type);
                s.setBooked(bookedIds.contains(s.getSeatId()));
                seats.add(s);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return seats;
    }

    // ── Demo generator ────────────────────────────────────────
    private List<Seat> buildDemoSeats(int roomId, int movieShowId) {
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A","B","C","D","E","F","G","H"};
        int cols = 10;
        int id = roomId * 1000;
        // Mark some seats as randomly booked based on showId
        Set<String> prebooked = new HashSet<>();
        int seed = movieShowId * 7 + roomId * 3;
        for (int i = 0; i < 12; i++) {
            int r = (seed * (i + 1) * 31) % rows.length;
            int c = (seed * (i + 1) * 17 + i) % cols + 1;
            prebooked.add(rows[r] + c);
        }

        for (String row : rows) {
            for (int col = 1; col <= cols; col++) {
                String type = "Standard";
                if (row.equals("F") || row.equals("H") || row.equals("I") || row.equals("J")) type = "Couple";
                else if (row.compareTo("C") > 0 && row.compareTo("H") < 0) type = "VIP";

                Seat s = new Seat(id++, roomId, row, col, type);
                s.setBooked(prebooked.contains(row + col));
                seats.add(s);
            }
        }
        return seats;
    }
}
