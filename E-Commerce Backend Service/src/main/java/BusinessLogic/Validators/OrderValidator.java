package BusinessLogic.Validators;

import DataModel.Order;
import java.time.LocalDate;

/**
 * Validator class for validating Order objects.
 * Checks the validity of order fields such as ID, client ID, date, total price.
 */
public class OrderValidator implements Validator<Order> {

    /**
     * Validates the given Order object.
     * @param order the Order object to validate
     * @throws IllegalArgumentException if any field is invalid
     */
    public void validate(Order order) {
        if(order.getId() != 0){
            validateId(order.getId());
        }
        validateClientId(order.getClientId());
        validateDate(order.getDate());
        validateTotalPrice(order.getTotal());
    }

    /**
     * Validates the order ID.
     * @param id the order ID
     */
    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number!");
        }
    }

    /**
     * Validates the client ID associated with the order.
     * @param clientId the client ID
     */
    private void validateClientId(int clientId) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID must be a positive number!");
        }
    }

    /**
     * Validates the order date.
     * @param date the order date
     */
    private void validateDate(LocalDate date) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be null or in the future.");
        }
    }

    /**
     * Validates the total price of the order.
     * @param totalPrice the total price
     */
    private void validateTotalPrice(double totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("Total price must be greater than zero!");
        }
    }
}
