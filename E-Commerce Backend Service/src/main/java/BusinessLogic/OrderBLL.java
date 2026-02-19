package BusinessLogic;

import BusinessLogic.Validators.OrderValidator;
import DataAccess.GenericDAO;
import DataModel.Order;

/**
 * Business Logic Layer for managing orders.
 */
public class OrderBLL {
    private final GenericDAO<Order> orderDAO;
    private final OrderValidator orderValidator;

    /**
     * Constructs an OrderBLL and initializes DAO, BLL, and Validator components.
     */
    public OrderBLL() {
        orderDAO = new GenericDAO<>(Order.class);
        orderValidator = new OrderValidator();
    }

    /**
     * Inserts a new order into the database after validation.
     *
     * @param order the Order to be inserted
     * @return the inserted Order with generated ID
     */
    public Order insertOrder(Order order) {
        orderValidator.validate(order);
        return orderDAO.insert(order);
    }
}
