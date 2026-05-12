package db;

import java.sql.*;

/**
 * Singleton JDBC connection manager for placement_records_db.
 */
public class DBConnection {

    private static Connection conn;

    /** Connect to MySQL and return the connection. */
    public static Connection connect(String host, String port, String user, String pass)
            throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/placement_records_db"
                   + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        conn = DriverManager.getConnection(url, user, pass);
        return conn;
    }

    public static Connection getConnection() {
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
