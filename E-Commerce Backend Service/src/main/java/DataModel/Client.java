package DataModel;

public class Client {
    private int id;
    private String name;
    private String address;
    private String phone;
    private String email;

    // Constructor that initializes all the fields
    public Client() {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Override toString method to return a string representation of the Client object
    @Override
    public String toString() {
        return "Client [id=" + id + ", name=" + name + ", address=" + address + ", phone=" + phone + ", email=" + email + "]";
    }
}
