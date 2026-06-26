package cinema.dao;

import cinema.model.Customer;
import cinema.model.MovieAccount;
import cinema.util.DBConnection;
import cinema.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    /* ── DEMO in-memory store (used when DB is unavailable) ── */
    private static final List<Customer> DEMO_CUSTOMERS = new ArrayList<>();
    private static final List<MovieAccount> DEMO_ACCOUNTS = new ArrayList<>();
    private static int nextCustId = 100;
    private static int nextAccId = 100;

    static {
        Customer c = new Customer(1, "Nguyễn Văn A", LocalDate.of(2000, 1, 15));
        DEMO_CUSTOMERS.add(c);
        MovieAccount acc = new MovieAccount(1, 1, "demo", PasswordUtil.hash("1234"),
                "SILVER", 120, "CUSTOMER");
        DEMO_ACCOUNTS.add(acc);

        Customer manager = new Customer(2, "Manager", LocalDate.of(1990, 1, 1));
        DEMO_CUSTOMERS.add(manager);
        MovieAccount adminAcc = new MovieAccount(2, 2, "admin", PasswordUtil.hash("admin"),
                "DIAMOND", 0, "MANAGER");
        DEMO_ACCOUNTS.add(adminAcc);
    }

    // ── Register ──────────────────────────────────────────────
    public boolean register(String fullName, LocalDate dob,
            String phone, String username, String plainPassword) {
        Connection conn = DBConnection.get();
        if (conn == null)
            return registerDemo(fullName, dob, username, plainPassword);

        try {
            conn.setAutoCommit(false);
            // 1. Insert Customer
            int custId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Customer(fullname,dob) VALUES(?,?) RETURNING customerid")) {
                ps.setString(1, fullName);
                ps.setDate(2, dob != null ? Date.valueOf(dob) : null);
                ResultSet rs = ps.executeQuery();
                rs.next();
                custId = rs.getInt(1);
            }

            // 3. Insert account
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Movie_Account(customerid,username,password_hash,membershiplevel,rewardpoints)" +
                            " VALUES(?,?,?,'BRONZE',0)")) {
                ps.setInt(1, custId);
                ps.setString(2, username);
                ps.setString(3, PasswordUtil.hash(plainPassword));
                ps.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            System.err.println("[CustomerDAO] register error: " + e.getMessage());
            return false;
        }
    }

    private boolean registerDemo(String fullName, LocalDate dob, String username, String pw) {
        for (MovieAccount a : DEMO_ACCOUNTS)
            if (a.getUsername().equals(username))
                return false;
        int cid = ++nextCustId;
        DEMO_CUSTOMERS.add(new Customer(cid, fullName, dob));
        DEMO_ACCOUNTS.add(new MovieAccount(++nextAccId, cid, username,
                PasswordUtil.hash(pw), "BRONZE", 0));
        return true;
    }

    // ── Login ─────────────────────────────────────────────────
    public MovieAccount login(String username, String plainPassword) {
        Connection conn = DBConnection.get();
        if (conn == null)
            return loginDemo(username, plainPassword);

        String sql = "SELECT * FROM Movie_Account WHERE username=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (PasswordUtil.verify(plainPassword, hash)) {
                    MovieAccount acc = new MovieAccount();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setCustomerId(rs.getInt("customerid"));
                    acc.setUsername(rs.getString("username"));
                    acc.setPasswordHash(hash);
                    acc.setMembershipLevel(rs.getString("membershiplevel"));
                    acc.setRewardPoints(rs.getInt("rewardpoints"));

                    try {
                        String role = rs.getString("role");
                        if (role != null)
                            acc.setRole(role);
                    } catch (SQLException ex) {
                        // DB may not have role column yet
                        acc.setRole("CUSTOMER");
                        if ("admin".equals(acc.getUsername())) {
                            acc.setRole("MANAGER"); // Fallback for admin user if column is missing
                        }
                    }

                    return acc;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] login error: " + e.getMessage());
        }

        // Fallback for demo admin account if not found in DB
        if ("admin".equals(username) || "demo".equals(username)) {
            return loginDemo(username, plainPassword);
        }

        return null;
    }

    private MovieAccount loginDemo(String username, String pw) {
        for (MovieAccount a : DEMO_ACCOUNTS)
            if (a.getUsername().equals(username) && PasswordUtil.verify(pw, a.getPasswordHash()))
                return a;
        return null;
    }

    // ── Get customer by id ─────────────────────────────────────
    public Customer getById(int customerId) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            for (Customer c : DEMO_CUSTOMERS)
                if (c.getCustomerId() == customerId)
                    return c;
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Customer WHERE customerid=?")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getInt("customerid"));
                c.setFullName(rs.getString("fullname"));
                Date d = rs.getDate("dob");
                if (d != null)
                    c.setDob(d.toLocalDate());
                return c;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            for (MovieAccount a : DEMO_ACCOUNTS)
                if (a.getUsername().equalsIgnoreCase(username))
                    return true;
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM Movie_Account WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ── Reward Points ─────────────────────────────────────────
    public static String determineLevel(int points) {
        if (points >= 10000)
            return "DIAMOND";
        if (points >= 5000)
            return "GOLD";
        if (points >= 2000)
            return "SILVER";
        return "BRONZE";
    }

    public void updateRewardPoints(int accountId, int pointsToAdd) {
        Connection conn = DBConnection.get();
        if (conn == null) {
            for (MovieAccount a : DEMO_ACCOUNTS) {
                if (a.getAccountId() == accountId) {
                    int newPoints = a.getRewardPoints() + pointsToAdd;
                    a.setRewardPoints(newPoints);
                    a.setMembershipLevel(determineLevel(newPoints));
                }
            }
            return;
        }
        try {
            int currentPoints = 0;
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT rewardpoints FROM Movie_Account WHERE account_id=?")) {
                ps.setInt(1, accountId);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    currentPoints = rs.getInt(1);
            }
            int newPoints = currentPoints + pointsToAdd;
            String newLevel = determineLevel(newPoints);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Movie_Account SET rewardpoints=?, membershiplevel=? WHERE account_id=?")) {
                ps.setInt(1, newPoints);
                ps.setString(2, newLevel);
                ps.setInt(3, accountId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] update points error: " + e.getMessage());
        }
    }
}
