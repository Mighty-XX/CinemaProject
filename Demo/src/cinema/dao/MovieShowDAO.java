package cinema.dao;

import cinema.model.*;
import cinema.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MovieShowDAO {

    /* ── Demo data ── */
    private static final List<Showtime>  DEMO_TIMES  = new ArrayList<>();
    private static final List<Room>      DEMO_ROOMS  = new ArrayList<>();
    private static final List<MovieShow> DEMO_SHOWS  = new ArrayList<>();

    static {
        // Rooms
        DEMO_ROOMS.add(new Room(1, "Phòng 1 - Standard", "Standard"));
        DEMO_ROOMS.add(new Room(2, "Phòng 2 - IMAX",     "IMAX"));
        DEMO_ROOMS.add(new Room(3, "Phòng 3 - 4DX",      "4DX"));
        DEMO_ROOMS.add(new Room(4, "Phòng 4 - VIP",      "VIP"));

        // Showtimes for next 3 days
        int sid = 1;
        String[] slots = {"09:00","11:30","14:00","16:30","19:00","21:30"};
        for (int day = 0; day < 3; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            for (String s : slots) {
                LocalTime start = LocalTime.parse(s);
                DEMO_TIMES.add(new Showtime(sid++, start, start.plusHours(2), date));
            }
        }

        // Shows: each movie x first 4 showtimes in room 1/2
        int showId = 1;
        for (int movieId = 1; movieId <= 6; movieId++) {
            for (int i = 0; i < 4; i++) {
                Showtime st = DEMO_TIMES.get(movieId - 1 + i * 2 % DEMO_TIMES.size());
                Room    r  = DEMO_ROOMS.get(i % DEMO_ROOMS.size());
                MovieShow ms = new MovieShow(showId++, movieId, r.getRoomId(),
                                             st.getShowtimeId(), "UPCOMING");
                ms.setRoom(r);
                ms.setShowtime(st);
                DEMO_SHOWS.add(ms);
            }
        }
    }

    public List<MovieShow> getByMovie(int movieId) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            List<MovieShow> r = new ArrayList<>();
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (MovieShow ms : DEMO_SHOWS) {
                if (ms.getMovieId() == movieId) {
                    String sig = ms.getRoomId() + "_" + ms.getShowtimeId();
                    if (seen.add(sig)) r.add(ms);
                }
            }
            return r;
        }
        List<MovieShow> list = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        String sql = "SELECT ms.*, r.roomname, r.roomtype, " +
                     "s.starttime, s.endtime, s.showdate " +
                     "FROM MovieShow ms " +
                     "JOIN Room r ON ms.roomid=r.roomid " +
                     "JOIN Showtime s ON ms.showtimeid=s.showtimeid " +
                     "WHERE ms.movieid=? AND (ms.status='Sắp chiếu' OR ms.status='Đang chiếu') " +
                     "ORDER BY s.showdate, s.starttime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int roomId = rs.getInt("roomid");
                int showtimeId = rs.getInt("showtimeid");
                String sig = roomId + "_" + showtimeId;
                if (!seen.add(sig)) continue;

                MovieShow ms = new MovieShow(rs.getInt("movieshowid"), movieId,
                        roomId, showtimeId,
                        rs.getString("status"));
                Room r = new Room(roomId, rs.getString("roomname"),
                                  rs.getString("roomtype"));
                ms.setRoom(r);
                Showtime st = new Showtime(showtimeId,
                        rs.getTime("starttime").toLocalTime(),
                        rs.getTime("endtime").toLocalTime(),
                        rs.getDate("showdate").toLocalDate());
                ms.setShowtime(st);
                list.add(ms);
            }
        } catch (SQLException e) { System.err.println("[ShowDAO] " + e.getMessage()); }
        return list;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(DEMO_ROOMS);
    }
}
