package cinema.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection to PostgreSQL G09_Cinema.
 * Requires postgresql-XX.jar in classpath.
 */
public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/g09_cinema";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    private static Connection instance;

    private DBConnection() {
    }

    public static Connection get() {
        try {
            if (instance == null || instance.isClosed()) {
                Class.forName("org.postgresql.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to G09_Cinema");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] PostgreSQL Driver not found. Add postgresql-XX.jar to lib/");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            System.err.println("[DB] App will use demo data instead.");
        }
        return instance;
    }

    public static void close() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException ignored) {
        }
    }
}
