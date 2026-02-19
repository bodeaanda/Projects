package DataAccess;

import DataModel.Bill;
import Connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for interacting with the "Log" table in the database.
 * Provides methods for inserting new bills and retrieving all bills from the log.
 * <p>
 * The {@link LogDAO} class handles the database operations related to the Bill entities, including
 * inserting a new bill and fetching all bills from the "Log" table.
 */

public class LogDAO {

    /**
     * Establishes a connection to the database.
     *
     * @return a {@link Connection} object representing the database connection.
     * @throws SQLException if a database access error occurs.
     */
    private Connection getConnection() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    /**
     * Inserts a new bill into the "Log" table.
     *
     * @param bill the {@link Bill} object to be inserted.
     * @return the inserted {@link Bill} object with the generated ID, or null if insertion fails.
     * @throws SQLException if an error occurs during the insert operation.
     */
    public Bill insertBill(Bill bill) {
        String query = "INSERT INTO Log (orderId, clientId, clientName, productId, productName, quantity, pricePerUnit, totalPrice, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, bill.orderId());
            preparedStatement.setInt(2, bill.clientId());
            preparedStatement.setString(3, bill.clientName());
            preparedStatement.setInt(4, bill.productId());
            preparedStatement.setString(5, bill.productName());
            preparedStatement.setInt(6, bill.quantity());
            preparedStatement.setDouble(7, bill.pricePerUnit());
            preparedStatement.setDouble(8, bill.totalPrice());
            preparedStatement.setDate(9, java.sql.Date.valueOf(bill.timestamp()));

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed creating bill.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    return new Bill(
                            generatedId,
                            bill.orderId(),
                            bill.clientId(),
                            bill.clientName(),
                            bill.productId(),
                            bill.productName(),
                            bill.quantity(),
                            bill.pricePerUnit(),
                            bill.totalPrice(),
                            bill.timestamp()
                    );
                } else {
                    throw new SQLException("Failed creating bill, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all bills from the "Log" table.
     *
     * @return a {@link List} of all {@link Bill} objects from the log.
     */
    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT * FROM Log";

        try (
                Connection connection = getConnection();
                Statement preparedStatement = connection.createStatement();
                ResultSet resultSet = preparedStatement.executeQuery(query)) {
            while (resultSet.next()) {
                Bill bill = new Bill(
                        resultSet.getInt("id"),
                        resultSet.getInt("orderId"),
                        resultSet.getInt("clientId"),
                        resultSet.getString("clientName"),
                        resultSet.getInt("productId"),
                        resultSet.getString("productName"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("pricePerUnit"),
                        resultSet.getDouble("totalPrice"),
                        resultSet.getTimestamp("timestamp").toLocalDateTime().toLocalDate()
                );
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bills;
    }
}
