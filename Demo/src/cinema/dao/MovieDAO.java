package cinema.dao;

import cinema.model.Movie;
import cinema.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    /* ── Demo data ── */
    private static final List<Movie> DEMO = new ArrayList<>();

    static {
        // index: 0=title 1=director 2=genre 3=duration 4=language 5=ageRating
        // 6=releaseDate 7=posterColor 8=synopsis
        Object[][] data = {
                { "Dune: Part Two", "Denis Villeneuve",
                        List.of("Timothée Chalamet", "Zendaya", "Austin Butler"),
                        "Viễn tưởng", 155, "Phụ đề", "T13", "2024-03-01", "#2a1040",
                        "Paul Atreides dẫn dắt người Fremen trong cuộc chiến tranh thiêng liêng chống lại kẻ thù." },
                { "Avengers: Endgame", "Anthony & Joe Russo",
                        List.of("Robert Downey Jr", "Chris Evans", "Scarlett Johansson"),
                        "Hành động", 181, "Lồng tiếng", "T13", "2019-04-26", "#1a1a40",
                        "Sau Infinity War, nhóm Avengers tập hợp lần cuối để cứu vũ trụ." },
                { "Inside Out 2", "Kelsey Mann",
                        List.of("Amy Poehler", "Maya Hawke", "Kensington Tallman"),
                        "Hài hước", 100, "Lồng tiếng", "K", "2024-06-14", "#0d3040",
                        "Riley bước vào tuổi teen, những cảm xúc mới xuất hiện trong đầu cô bé." },
                { "Joker: Folie à Deux", "Todd Phillips",
                        List.of("Joaquin Phoenix", "Lady Gaga"),
                        "Tình cảm", 138, "Phụ đề", "C18", "2024-10-04", "#400d0d",
                        "Arthur Fleck gặp lại Harley Quinn trong bệnh viện Arkham." },
                { "The Batman", "Matt Reeves",
                        List.of("Robert Pattinson", "Zoë Kravitz", "Paul Dano"),
                        "Hành động", 176, "Lồng tiếng", "T16", "2022-03-04", "#0a0a20",
                        "Bruce Wayne trẻ tuổi đối mặt với loạt vụ giết người bí ẩn ở Gotham." },
                { "Interstellar", "Christopher Nolan",
                        List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                        "Viễn tưởng", 169, "Phụ đề", "T13", "2014-11-07", "#001030",
                        "Đoàn phi hành gia du hành qua lỗ sâu đục để tìm ngôi nhà mới cho nhân loại." },
        };
        int id = 1;
        for (Object[] d : data) {
            @SuppressWarnings("unchecked")
            Movie m = new Movie(id++, (String) d[0], (String) d[1], (List<String>) d[2],
                    (String) d[3], (int) d[4], (String) d[5], (String) d[6],
                    LocalDate.parse((String) d[7]), (String) d[9]);
            m.setPosterColor((String) d[8]);
            DEMO.add(m);
        }
    }

    public List<Movie> getAll() {
        Connection conn = DBConnection.get();
        if (conn == null)
            return new ArrayList<>(DEMO);

        List<Movie> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM Movie ORDER BY releasedate DESC")) {
            while (rs.next()) {
                Movie m = map(rs);
                m.setCastMembers(loadCastMembers(conn, m.getMovieId()));
                list.add(m);
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] " + e.getMessage());
            return new ArrayList<>(DEMO);
        }
        return list;
    }

    public boolean addMovie(Movie m) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            int nextId = DEMO.stream().mapToInt(Movie::getMovieId).max().orElse(0) + 1;
            m.setMovieId(nextId);
            if (m.getPosterColor() == null) m.setPosterColor("#112233");
            DEMO.add(0, m);
            return true;
        }
        try {
            conn.setAutoCommit(false);
            int movieId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Movie(title, director, genre, duration, language, agerating, releasedate, synopsis) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING movieid")) {
                ps.setString(1, m.getTitle());
                ps.setString(2, m.getDirector());
                ps.setString(3, m.getGenre());
                ps.setInt(4, m.getDuration());
                ps.setString(5, m.getLanguage());
                ps.setString(6, m.getAgeRating());
                ps.setDate(7, m.getReleaseDate() != null ? Date.valueOf(m.getReleaseDate()) : null);
                ps.setString(8, m.getSynopsis());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    movieId = rs.getInt(1);
                    m.setMovieId(movieId);
                } else {
                    conn.rollback();
                    return false;
                }
            }
            if (m.getCastMembers() != null && !m.getCastMembers().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Cast_Member(movieid, \"cast\") VALUES (?, ?)")) {
                    for (String c : m.getCastMembers()) {
                        ps.setInt(1, movieId);
                        ps.setString(2, c.trim());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ignored) {}
            System.err.println("[MovieDAO] addMovie error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMovie(int movieId) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            return DEMO.removeIf(m -> m.getMovieId() == movieId);
        }
        try {
            conn.setAutoCommit(false);
            
            // 1. Kiểm tra xem đã có vé nào được đặt cho phim này chưa
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Ticket WHERE movieshowid IN (SELECT movieshowid FROM MovieShow WHERE movieid=?)")) {
                check.setInt(1, movieId);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Phim đã có người đặt vé, không cho phép xóa
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }
            
            // 2. Xóa các dữ liệu phụ thuộc không quan trọng (Cast_Member)
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Cast_Member WHERE movieid=?")) {
                ps1.setInt(1, movieId);
                ps1.executeUpdate();
            }
            
            // 3. Xóa các suất chiếu (MovieShow) của phim này
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM MovieShow WHERE movieid=?")) {
                ps2.setInt(1, movieId);
                ps2.executeUpdate();
            }
            
            // 4. Cuối cùng, xóa phim
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM Movie WHERE movieid=?")) {
                ps3.setInt(1, movieId);
                int rows = ps3.executeUpdate();
                conn.commit();
                conn.setAutoCommit(true);
                return rows > 0;
            }
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ignored) {}
            System.err.println("[MovieDAO] deleteMovie error: " + e.getMessage());
            return false;
        }
    }

    public List<Movie> searchByTitle(String keyword) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            String kw = keyword.toLowerCase();
            List<Movie> r = new ArrayList<>();
            for (Movie m : DEMO)
                if (m.getTitle().toLowerCase().contains(kw))
                    r.add(m);
            return r;
        }
        List<Movie> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Movie WHERE LOWER(title) LIKE ? ORDER BY releasedate DESC")) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Movie m = map(rs);
                m.setCastMembers(loadCastMembers(conn, m.getMovieId()));
                list.add(m);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public List<Movie> getByGenre(String genre) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            List<Movie> r = new ArrayList<>();
            for (Movie m : DEMO)
                if (genre.equals("Tất cả") || m.getGenre().equals(genre))
                    r.add(m);
            return r;
        }
        List<Movie> list = new ArrayList<>();
        String sql = genre.equals("Tất cả")
                ? "SELECT * FROM Movie ORDER BY releasedate DESC"
                : "SELECT * FROM Movie WHERE genre=? ORDER BY releasedate DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!genre.equals("Tất cả"))
                ps.setString(1, genre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Movie m = map(rs);
                m.setCastMembers(loadCastMembers(conn, m.getMovieId()));
                list.add(m);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    // ── DB helpers ────────────────────────────────────────────

    /**
     * Maps a ResultSet row from the Movie table.
     * Does NOT populate castMembers (handled separately via Cast_Member table).
     */
    private Movie map(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("movieid"));
        m.setTitle(rs.getString("title"));
        m.setDirector(rs.getString("director"));
        // castMembers is loaded separately from Cast_Member table
        m.setGenre(rs.getString("genre"));
        m.setDuration(rs.getInt("duration"));
        m.setLanguage(rs.getString("language"));
        m.setAgeRating(rs.getString("agerating"));
        Date d = rs.getDate("releasedate");
        if (d != null)
            m.setReleaseDate(d.toLocalDate());
        m.setSynopsis(rs.getString("synopsis"));
        return m;
    }

    /**
     * Queries Cast_Member table and returns a List of cast name strings for the
     * given movie.
     */
    private List<String> loadCastMembers(Connection conn, int movieId) {
        List<String> cast = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT \"cast\" FROM Cast_Member WHERE movieid = ? ORDER BY \"cast\"")) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cast.add(rs.getString("cast"));
        } catch (SQLException e) {
            System.err.println("[MovieDAO] loadCastMembers error: " + e.getMessage());
        }
        return cast;
    }
}