package BusinessLogic.Validators;

import DataModel.Client;

/**
 * Validator class for validating Client objects.
 * Makes sure that client fields: ID, name, email, and phone are in valid format.
 */
public class ClientValidator implements Validator<Client> {

    /**
     * Validates the given Client object.
     * @param client the Client object to validate
     * @throws IllegalArgumentException if any field is invalid
     */
    public void validate(Client client) {

        if(client.getId() != 0){
            validateId(client.getId());
        }
        validateName(client.getName());
        validateEmail(client.getEmail());
        validatePhone(client.getPhone());
    }

    /**
     * Validates the client ID.
     * @param id the client ID
     */
    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Client ID must be a positive number!");
        }

    }

    /**
     * Validates the client name.
     * @param name the client name
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty!");
        }
    }

    /**
     * Validates the client email.
     * @param email the client email
     */
    private void validateEmail(String email) {
        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format!");
        }
    }

    /**
     * Validates the client phone number.
     * @param phone the client phone number
     */
    private void validatePhone(String phone) {
        if (!phone.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Phone number must have exactly 10 digits!");
        }
    }
}
