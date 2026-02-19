package BusinessLogic.Validators;

import DataModel.Product;

/**
 * Validator class for Product objects.
 * Ensures that the product fields meet required conditions.
 */
public class ProductValidator implements Validator<Product> {

    /**
     * Validates the given Product object.
     * @param product the product to validate
     * @throws IllegalArgumentException if any product field is invalid
     */
    public void validate(Product product) {
        if(product.getId() != 0){
            validateId(product.getId());
        }
        validateName(product.getName());
        validatePrice(product.getPrice());
        validateQuantity(product.getQuantity());
    }

    /**
     * Validates the product ID.
     * @param id the product ID
     */
    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number!");
        }
    }

    /**
     * Validates the product name.
     * @param name the product name
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
    }

    /**
     * Validates the product price.
     * @param price the product price
     */
    private void validatePrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must be greater than zero!");
        }
    }

    /**
     * Validates the product quantity.
     * @param quantity the product quantity
     */
    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero!");
        }
    }
}
