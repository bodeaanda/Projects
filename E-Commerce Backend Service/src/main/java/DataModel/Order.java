package DataModel;

import java.time.LocalDate;

public class Order {
    private int id;
    private int clientId;
    private LocalDate date;
    private double total;

    // Constructor that initializes all the fields
    public Order() {
        this.id = id;
        this.clientId = clientId;
        this.date = date;
        this.total = 0;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public LocalDate getDate() {
        return date;
    }
    public double getTotal() {
        return total;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Override toString method to return a string representation of the Client object
    @Override
    public String toString() {
        return "Order [id=" + id + ", clientId=" + clientId + ", date=" + date + ", total=" + total + "]";
    }
}
