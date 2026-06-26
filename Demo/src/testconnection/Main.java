package testconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static String url = "jdbc:postgresql://localhost:5432/g09_cinema";
    private static String username = "postgres";
    private static String password = "admin";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection(url, username, password);

        if (con == null)
            System.out.println("failed");
        else
            System.out.println("connect success");

    }

}
