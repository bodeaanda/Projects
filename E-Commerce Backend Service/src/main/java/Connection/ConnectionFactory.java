package Connection;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton factory class responsible for creating and managing database connections.
 */
public class ConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(ConnectionFactory.class.getName());
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DBURL = "jdbc:mysql://localhost:3306/orders_management";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private static ConnectionFactory singleInstance = new ConnectionFactory();

    /**
     * Private constructor that loads the JDBC driver.
     */
    private ConnectionFactory() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error occured!", e);
        }
    }

    /**
     * Creates a new database connection.
     *
     * @return a new Connection object
     */
    private Connection createConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DBURL, USER, PASSWORD);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occured!", e);
        }
        return connection;
    }

    /**
     * Returns a new database connection instance.
     *
     * @return a Connection instance
     */
    public static Connection getConnection() {
        return singleInstance.createConnection();
    }

    /**
     * Closes the given Connection object.
     *
     * @param connection the Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error occured!", e);
            }
        }
    }

    /**
     * Closes the given Statement object.
     *
     * @param statement the Statement to close
     */
    public static void closeConnection(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error occured!", e);
            }
        }
    }

    /**
     * Closes the given ResultSet object.
     *
     * @param resultSet the ResultSet to close
     */
    public static void closeConnection(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error occured!", e);
            }
        }
    }
}
