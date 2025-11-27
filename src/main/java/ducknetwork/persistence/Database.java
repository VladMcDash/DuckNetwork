package ducknetwork.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage a single, persistent database connection.
 */
public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/duckdb";
    private static final String USER = "duckuser";
    private static final String PASSWORD = "Vlady123";

    private static Database INSTANCE;
    private Connection connection;

    /**
     * Stabilește conexiunea inițială.
     */
    private Database() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found! (nu merge!)", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish initial database connection!", e);
        }
    }

    /**
     * Provides the Singleton instance of the Database class.
     */
    public static synchronized Database getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    /**
     * Returns the persistent database Connection object.
     * Dacă conexiunea s-a închis (e.g., timeout de la server), o re-stabilește.
     * * @return The database connection.
     * @throws SQLException If re-establishment fails.
     */
    public Connection getConnection() throws SQLException {

        if (this.connection == null || this.connection.isClosed()) {
            try {
                this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                throw new SQLException("Re-establishment of the database connection failed: " + e.getMessage(), e);
            }
        }
        return this.connection;
    }

    /**
     * Utility method to close the connection gracefully (should be called on app shutdown).
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}