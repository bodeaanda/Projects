package Presentation;

import DataModel.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * The {@code View} class is the graphical user interface (GUI) for the Order Management System.
 * It interacts with the {@link Controller} class to display and manage clients, products, and orders.
 */
public class View extends JFrame {

    private Controller controller;
    private DefaultTableModel billTableModel;
    private DefaultTableModel productTableModel;

    /**
     * Constructs a new {@code View} for the Order Management System.
     * Initializes the window with tabs for Clients, Products, and Orders.
     */
    public View() {
        super("Order Management System");
        controller = new Controller();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(102, 153, 204));
        tabs.setForeground(new Color(240, 248, 255));
        tabs.setFont(new Font("Serif", Font.BOLD, 20));

        tabs.addTab("Clients", buildClientPanel());
        tabs.addTab("Products", buildProductPanel());
        tabs.addTab("Orders", buildOrderPanel());
        tabs.addTab("Bills", buildBillPanel());

        add(tabs);
        setVisible(true);
    }

    /**
     * Generic method to create a styled JTable with given column names
     *
     * @param columnNames The names of the columns for the table
     * @return A styled JTable
     */
    private JTable createCustomTable(String[] columnNames) {
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        table.setBackground(new Color(254, 254, 250));
        table.setForeground(new Color(102, 153, 204));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 20));
        table.getTableHeader().setBackground(new Color(102, 153, 204));
        table.getTableHeader().setForeground(new Color(240, 248, 255));
        table.setGridColor(new Color(102, 153, 204));
        table.setFont(new Font("Serif", Font.BOLD, 14));
        table.setSelectionBackground(new Color(123, 175, 212));

        return table;
    }

    /**
     * Generic method to refresh a table using reflection to extract data from objects
     *
     * @param <T> The type of data objects
     * @param tableModel The table model to refresh
     * @param data The list of data objects
     * @param propertyNames Array of property names to display (method names without "get" prefix)
     */
    private <T> void refreshTableGeneric(DefaultTableModel tableModel, List<T> data, String[] propertyNames) {
        tableModel.setRowCount(0);

        for (T item : data) {
            Object[] rowData = new Object[propertyNames.length];

            for (int i = 0; i < propertyNames.length; i++) {
                try {
                    String methodName = "get" + propertyNames[i].substring(0, 1).toUpperCase() +
                            propertyNames[i].substring(1);
                    Method method = item.getClass().getMethod(methodName);
                    rowData[i] = method.invoke(item);
                } catch (Exception e) {
                    rowData[i] = "N/A";
                }
            }

            tableModel.addRow(rowData);
        }
    }

    /**
     * Builds and returns the panel for managing clients.
     *
     * @return The JPanel containing client management components.
     */
    private JPanel buildClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(237, 234, 224));

        String[] columnNames = {"Client ID", "Name", "Address", "Email", "Phone"};
        JTable table = createCustomTable(columnNames);
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(new Color(237, 234, 224));

        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        JLabel nameLabel = new JLabel("Name");
        nameLabel.setForeground(new Color(102, 153, 204));
        nameLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel addressLabel = new JLabel("Address");
        addressLabel.setForeground(new Color(102, 153, 204));
        addressLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setForeground(new Color(102, 153, 204));
        emailLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel phoneLabel = new JLabel("Phone");
        phoneLabel.setForeground(new Color(102, 153, 204));
        phoneLabel.setFont(new Font("Serif", Font.BOLD, 20));

        inputPanel.add(nameLabel); inputPanel.add(nameField);
        inputPanel.add(addressLabel); inputPanel.add(addressField);
        inputPanel.add(emailLabel); inputPanel.add(emailField);
        inputPanel.add(phoneLabel); inputPanel.add(phoneField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(237, 234, 224));

        JButton addButton = new JButton("Add");
        addButton.setForeground(new Color(5, 74, 41));
        addButton.setFont(new Font("Serif", Font.BOLD, 18));
        addButton.setBackground(new Color(91, 186, 111));
        JButton updateButton = new JButton("Edit");
        updateButton.setForeground(new Color(0, 50, 98));
        updateButton.setFont(new Font("Serif", Font.BOLD, 18));
        updateButton.setBackground(new Color(137, 207, 240));
        JButton deleteButton = new JButton("Delete");
        deleteButton.setForeground(new Color(124, 9, 2));
        deleteButton.setFont(new Font("Serif", Font.BOLD, 18));
        deleteButton.setBackground(new Color(255, 127, 127));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        addButton.addActionListener(e -> {
            controller.addClient(nameField.getText(), addressField.getText(), emailField.getText(), phoneField.getText());
            refreshTableGeneric(tableModel, controller.getAllClients(), new String[]{"id", "name", "address", "email", "phone"});
        });

        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                controller.updateClient(id, nameField.getText(), addressField.getText(), emailField.getText(), phoneField.getText());
                refreshTableGeneric(tableModel, controller.getAllClients(), new String[]{"id", "name", "address", "email", "phone"});
            } else {
                JOptionPane.showMessageDialog(this, "Select a client to edit.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                controller.deleteClient(id);
                refreshTableGeneric(tableModel, controller.getAllClients(), new String[]{"id", "name", "address", "email", "phone"});
            } else {
                JOptionPane.showMessageDialog(this, "Select a client to delete.");
            }
        });

        table.getSelectionModel().addListSelectionListener(event -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                addressField.setText(tableModel.getValueAt(row, 2).toString());
                emailField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(237, 234, 224));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshTableGeneric(tableModel, controller.getAllClients(), new String[]{"id", "name", "address", "email", "phone"});

        return panel;
    }

    /**
     * Builds and returns the panel for managing products.
     *
     * @return The JPanel containing product management components.
     */
    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(237, 234, 224));

        String[] columnNames = {"Product ID", "Name", "Price", "Quantity"};
        JTable table = createCustomTable(columnNames);
        productTableModel = (DefaultTableModel) table.getModel();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(new Color(237, 234, 224));

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(new Color(102, 153, 204));
        nameLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setForeground(new Color(102, 153, 204));
        priceLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setForeground(new Color(102, 153, 204));
        quantityLabel.setFont(new Font("Serif", Font.BOLD, 20));

        inputPanel.add(nameLabel); inputPanel.add(nameField);
        inputPanel.add(priceLabel); inputPanel.add(priceField);
        inputPanel.add(quantityLabel); inputPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(237, 234, 224));

        JButton addButton = new JButton("Add");
        addButton.setForeground(new Color(5, 74, 41));
        addButton.setBackground(new Color(91, 186, 111));
        addButton.setFont(new Font("Serif", Font.BOLD, 18));
        JButton updateButton = new JButton("Edit");
        updateButton.setForeground(new Color(0, 50, 98));
        updateButton.setBackground(new Color(137, 207, 240));
        updateButton.setFont(new Font("Serif", Font.BOLD, 18));
        JButton deleteButton = new JButton("Delete");
        deleteButton.setForeground(new Color(124, 9, 2));
        deleteButton.setBackground(new Color(255, 127, 127));
        deleteButton.setFont(new Font("Serif", Font.BOLD, 18));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        addButton.addActionListener(e -> {
            try {
                double price = Double.parseDouble(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                controller.addProduct(nameField.getText(), price, quantity);
                refreshTableGeneric(productTableModel, controller.getAllProducts(), new String[]{"id", "name", "price", "quantity"});
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price and quantity must be numbers.");
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                try {
                    int id = Integer.parseInt(productTableModel.getValueAt(selectedRow, 0).toString());
                    double price = Double.parseDouble(priceField.getText());
                    int quantity = Integer.parseInt(quantityField.getText());
                    controller.updateProduct(id, nameField.getText(), price, quantity);
                    refreshTableGeneric(productTableModel, controller.getAllProducts(), new String[]{"id", "name", "price", "quantity"});
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Price and Quantity must be numbers.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a product to edit.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = Integer.parseInt(productTableModel.getValueAt(selectedRow, 0).toString());
                controller.deleteProduct(id);
                refreshTableGeneric(productTableModel, controller.getAllProducts(), new String[]{"id", "name", "price", "quantity"});
            } else {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
            }
        });

        table.getSelectionModel().addListSelectionListener(event -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                nameField.setText(productTableModel.getValueAt(row, 1).toString());
                priceField.setText(productTableModel.getValueAt(row, 2).toString());
                quantityField.setText(productTableModel.getValueAt(row, 3).toString());
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(237, 234, 224));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshTableGeneric(productTableModel, controller.getAllProducts(), new String[]{"id", "name", "price", "quantity"});

        return panel;
    }

    /**
     * Builds and returns the panel for managing orders.
     *
     * @return The JPanel containing order management components.
     */
    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(237, 234, 224));

        String[] columnNames = {" Order ID", "Client ID", "Date", "Total"};
        JTable table = createCustomTable(columnNames);
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(new Color(237, 234, 224));

        JComboBox<String> productComboBox = new JComboBox<>();
        JComboBox<String> clientComboBox = new JComboBox<>();
        JTextField quantityField = new JTextField();

        List<Product> products = controller.getAllProducts();
        List<Client> clients = controller.getAllClients();

        for (Product product : products) {
            productComboBox.addItem(product.getName() + " (Stock: " + product.getQuantity() + ")");
            productComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        }

        for (Client client : clients) {
            clientComboBox.addItem(client.getName());
            clientComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        }

        JLabel productLabel = new JLabel("Product:");
        productLabel.setForeground(new Color(102, 153, 204));
        productLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel clientLabel = new JLabel("Client:");
        clientLabel.setForeground(new Color(102, 153, 204));
        clientLabel.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setForeground(new Color(102, 153, 204));
        quantityLabel.setFont(new Font("Serif", Font.BOLD, 20));

        inputPanel.add(productLabel); inputPanel.add(productComboBox);
        inputPanel.add(clientLabel); inputPanel.add(clientComboBox);
        inputPanel.add(quantityLabel); inputPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(237, 234, 224));

        JButton addButton = new JButton("Place Order");
        addButton.setForeground(new Color(5, 74, 41));
        addButton.setBackground(new Color(91, 186, 111));
        addButton.setFont(new Font("Serif", Font.BOLD, 18));

        buttonPanel.add(addButton);

        addButton.addActionListener(e -> {
            try {
                String selectedProductName = (String) productComboBox.getSelectedItem();
                String selectedClientName = (String) clientComboBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText());

                Product selectedProduct = controller.getAllProducts().stream()
                        .filter(product -> (product.getName() + " (Stock: " + product.getQuantity() + ")").equals(selectedProductName))
                        .findFirst().orElse(null);

                Client selectedClient = controller.getAllClients().stream()
                        .filter(client -> client.getName().equals(selectedClientName))
                        .findFirst().orElse(null);

                if (selectedProduct != null && selectedClient != null) {
                    controller.placeOrder(selectedClient.getId(), selectedProduct.getId(), quantity);

                    refreshTableGeneric(tableModel, controller.getAllOrders(), new String[]{"id", "clientId", "date", "total"});
                    refreshBillTable(billTableModel);
                    refreshTableGeneric(productTableModel, controller.getAllProducts(), new String[]{"id", "name", "price", "quantity"});

                } else {
                    JOptionPane.showMessageDialog(this, "Please select a valid product and client.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or error while placing the order.");
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(237, 234, 224));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshTableGeneric(tableModel, controller.getAllOrders(), new String[]{"id", "clientId", "date", "total"});

        return panel;
    }

    /**
     * Constructs the GUI panel responsible for displaying all bills in a table format.
     * <p>
     * It sets up a JTable with columns such as Bill ID, Order ID, Client Name, etc., and styles it.
     * The table is populated using the {@link #refreshBillTable(DefaultTableModel)} method.
     *
     * @return A JPanel containing the scrollable table with bill data.
     */
    private JPanel buildBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(237, 234, 224));

        String[] columnNames = {"Bill ID", "Order ID", "Client ID", "Client Name", "Product ID", "Product Name", "Quantity", "Price/Unit", "Total", "Date"};
        billTableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(billTableModel);
        table.setBackground(new Color(254, 254, 250));
        table.setForeground(new Color(102, 153, 204));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(102, 153, 204));
        table.getTableHeader().setForeground(new Color(240, 248, 255));
        table.setFont(new Font("Serif", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(123, 175, 212));
        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBillTable(billTableModel);

        return panel;
    }

    /**
     * Populates the bill table with data retrieved from the controller.
     * <p>
     * It uses a Java Stream to transform a list of {@link DataModel.Bill} objects
     * into rows and adds them to the table model.
     *
     * @param tableModel The {@link DefaultTableModel} associated with the bill table to refresh.
     */
    private void refreshBillTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        controller.getAllBills().stream()
                .map(b -> new Object[]{
                        b.id(),
                        b.orderId(),
                        b.clientId(),
                        b.clientName(),
                        b.productId(),
                        b.productName(),
                        b.quantity(),
                        b.pricePerUnit(),
                        b.totalPrice(),
                        b.timestamp()
                })
                .forEach(tableModel::addRow);
    }

    /**
     * Main method to start the application.
     *
     * @param args Command-line arguments (not used in this case).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(View::new);
    }
}