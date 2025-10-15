package verification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager for MySQL (Railway).
 * Ensures a single persistent connection across the entire application.
 */
public class Database {

    private static final String URL = "jdbc:mysql://shortline.proxy.rlwy.net:36648/railway";
    private static final String USER = "root";
    private static final String PASSWORD = "wHwviYYfzHbeerUnyxIyccXUrYgAhzsL";

    private static Connection connection = null;

    /**
     * Initializes the connection (kept for backward compatibility).
     */
    public static void initialize() {
        getConnection(); // Simply calls getConnection to establish connection if not connected
    }

    /**
     * Returns a single shared MySQL connection.
     * Automatically reconnects if the connection is closed.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to MySQL database as user '" + USER + "'.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the database connection when the application exits.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("🔒 Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
