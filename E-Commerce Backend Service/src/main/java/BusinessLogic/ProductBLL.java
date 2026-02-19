package BusinessLogic;

import BusinessLogic.Validators.ProductValidator;
import DataAccess.GenericDAO;
import DataModel.Product;

import java.util.NoSuchElementException;

/**
 * Business Logic Layer for managing products.
 */
public class ProductBLL {
    private final GenericDAO<Product> productDAO;
    private final ProductValidator productValidator;

    /**
     * Constructs a ProductBLL and initializes DAO and Validator components.
     */
    public ProductBLL() {
        productDAO = new GenericDAO<>(Product.class);
        productValidator = new ProductValidator();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the Product object
     * @throws NoSuchElementException if the product is not found
     */
    public Product getProductById(int id) {
        Product product = productDAO.findById(id);
        if (product == null) {
            throw new NoSuchElementException("Product " + id + " not found!");
        }
        return product;
    }

    /**
     * Inserts a new product into the database after validation.
     *
     * @param product the Product to be inserted
     * @return the inserted Product
     */
    public Product insertProduct(Product product) {
        productValidator.validate(product);
        return productDAO.insert(product);
    }

    /**
     * Updates an existing product in the database after validation.
     *
     * @param product the Product to be updated
     * @return the updated Product
     */
    public Product updateProduct(Product product) {
        productValidator.validate(product);
        return productDAO.update(product);
    }

    /**
     * Deletes a product from the database by ID.
     *
     * @param id the ID of the product to delete
     */
    public void deleteProduct(int id) {
        productDAO.delete(id);
    }
}
