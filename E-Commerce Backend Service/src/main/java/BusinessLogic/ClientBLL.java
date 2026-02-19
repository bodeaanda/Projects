package BusinessLogic;

import BusinessLogic.Validators.ClientValidator;
import DataAccess.GenericDAO;
import DataModel.Client;

import java.util.NoSuchElementException;

public class ClientBLL {
    private final GenericDAO<Client> clientDAO;
    private final ClientValidator clientValidator;

    /**
     * Constructs a ClientBLL and initializes DAO and Validator.
     */
    public ClientBLL() {
        clientDAO = new GenericDAO<>(Client.class);
        clientValidator = new ClientValidator();
    }

    /**
     * Retrieves a client by its ID.
     *
     * @param id the client's ID
     * @return the client with the specified ID
     * @throws NoSuchElementException if the client is not found
     */
    public Client getClientByID(int id) {
        Client client = clientDAO.findById(id);
        if (client == null) {
            throw new NoSuchElementException("Client " + id + " not found!");
        }
        return client;
    }

    /**
     * Inserts a new client after validation.
     *
     * @param client the client to insert
     * @return the inserted client
     */
    public Client insertClient(Client client) {
        clientValidator.validate(client);
        return clientDAO.insert(client);
    }

    /**
     * Updates an existing client after validation.
     *
     * @param client the client to update
     * @return the updated client
     */
    public Client updateClient(Client client) {
        clientValidator.validate(client);
        return clientDAO.update(client);
    }

    /**
     * Deletes a client by its ID.
     *
     * @param id the client's ID to delete
     */
    public void deleteClient(int id) {
        clientDAO.delete(id);
    }
}
