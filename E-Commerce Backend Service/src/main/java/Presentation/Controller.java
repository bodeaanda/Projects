package Presentation;

import BusinessLogic.*;
import DataAccess.*;
import DataModel.*;
import BusinessLogic.Validators.*;

import javax.swing.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller class responsible for managing the business logic operations related to Clients, Orders, and Products.
 * This class acts as the interface between the UI (Presentation Layer) and the business logic (BLL) and data access (DAO) layers.
 * It performs CRUD operations for Clients, Orders, and Products, and handles interactions with the respective BLL classes.
 */
public class Controller {

    private ClientBLL clientBLL;
    private OrderBLL orderBLL;
    private ProductBLL productBLL;

    private GenericDAO<Client> clientDAO;
    private GenericDAO<Order> orderDAO;
    private GenericDAO<Product> productDAO;
    private GenericDAO<OrderItem> orderItemDAO;

    private ClientValidator clientValidator;
    private OrderValidator orderValidator;
    private ProductValidator productValidator;

    /**
     * Constructs a new Controller object that initializes the BLL, DAO, and Validator components.
     */
    public Controller() {
        clientBLL = new ClientBLL();
        orderBLL = new OrderBLL();
        productBLL = new ProductBLL();

        clientDAO = new GenericDAO<>(Client.class);
        orderDAO = new GenericDAO<>(Order.class);
        productDAO = new GenericDAO<>(Product.class);
        orderItemDAO = new GenericDAO<>(OrderItem.class);

        clientValidator = new ClientValidator();
        orderValidator = new OrderValidator();
        productValidator = new ProductValidator();
    }

    public List<Client> getAllClients() {
        return clientDAO.findAll();
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public List<Bill> getAllBills() {
        return new LogDAO().getAllBills();
    }

    public void addClient(String name, String address, String email, String phone) {
        try {
            Client newClient = new Client();
            newClient.setName(name);
            newClient.setAddress(address);
            newClient.setEmail(email);
            newClient.setPhone(phone);

            clientValidator.validate(newClient);

            clientBLL.insertClient(newClient);
            JOptionPane.showMessageDialog(null, "Client added!");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public void updateClient(int id, String name, String address, String email, String phone) {
        try {
            Client existingClient = clientBLL.getClientByID(id);
            existingClient.setName(name);
            existingClient.setAddress(address);
            existingClient.setEmail(email);
            existingClient.setPhone(phone);

            clientValidator.validate(existingClient);

            clientBLL.updateClient(existingClient);
            JOptionPane.showMessageDialog(null, "Client updated!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public void deleteClient(int id) {
        try {
            clientBLL.deleteClient(id);
            JOptionPane.showMessageDialog(null, "Client deleted!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public void addProduct(String name, double price, int quantity) {
        try {
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setPrice(price);
            newProduct.setQuantity(quantity);

            productValidator.validate(newProduct);

            productBLL.insertProduct(newProduct);
            JOptionPane.showMessageDialog(null, "Product added!");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public void updateProduct(int id, String name, double price, int quantity) {
        try {
            Product existingProduct = productBLL.getProductById(id);
            existingProduct.setName(name);
            existingProduct.setPrice(price);
            existingProduct.setQuantity(quantity);

            productValidator.validate(existingProduct);

            productBLL.updateProduct(existingProduct);
            JOptionPane.showMessageDialog(null, "Product updated!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public void deleteProduct(int id) {
        try {
            productBLL.deleteProduct(id);
            JOptionPane.showMessageDialog(null, "Product deleted!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public Bill placeOrder(int clientId, int productId, int quantity) {
        try {
            Product product = productBLL.getProductById(productId);
            if (product.getQuantity() < quantity) {
                JOptionPane.showMessageDialog(null, "Insufficient stock!");
                return null;
            }

            double total = product.getPrice() * quantity;

            Order order = new Order();
            order.setClientId(clientId);
            order.setDate(LocalDate.now());
            order.setTotal(total);

            Order insertedOrder = orderBLL.insertOrder(order);
            if (insertedOrder == null) {
                JOptionPane.showMessageDialog(null, "Error saving data!");
                return null;
            }

            OrderItem item = new OrderItem();
            item.setOrderId(insertedOrder.getId());
            item.setProductId(productId);
            item.setQuantity(quantity);

            orderItemDAO.insert(item);

            product.setQuantity(product.getQuantity() - quantity);
            productBLL.updateProduct(product);

            Client client = clientBLL.getClientByID(clientId);

            Bill bill = new Bill(
                    0,
                    insertedOrder.getId(),
                    clientId,
                    client.getName(),
                    productId,
                    product.getName(),
                    quantity,
                    product.getPrice(),
                    total,
                    LocalDate.now()
            );

            Bill insertedBill = new LogDAO().insertBill(bill);

            JOptionPane.showMessageDialog(null, "Order has been placed!");

            return insertedBill;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return null;
        }
    }
}
