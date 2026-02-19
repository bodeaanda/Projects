package DataModel;

import java.time.LocalDate;

/**
 * Represents a bill for an order.
 * Contains details about the order, client, product, and transaction.
 * <p>
 * The bill includes the following fields: ID, Order ID, Client ID, Client name,
 * Product ID, Product name, Quantity, Price per unit, Total price, and Timestamp.
 */

public record Bill (
        int id,
        int orderId,
        int clientId,
        String clientName,
        int productId,
        String productName,
        int quantity,
        double pricePerUnit,
        double totalPrice,
        LocalDate timestamp

) {}
