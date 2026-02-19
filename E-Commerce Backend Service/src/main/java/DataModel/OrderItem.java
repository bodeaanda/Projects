package DataModel;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;

    // Constructor that initializes all the fields
    public OrderItem() {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Override toString method to return a string representation of the Client object
    @Override
    public String toString() {
        return " Order item [orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity + "]";
    }
}
